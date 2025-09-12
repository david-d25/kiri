package space.davids_digital.kiri.agent.engine

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.agent.app.AppManager
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.agent.frame.FrameRenderer
import space.davids_digital.kiri.agent.frame.addCreatedAtNow
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.memory.MemoryManager
import space.davids_digital.kiri.agent.tool.*
import space.davids_digital.kiri.integration.anthropic.AnthropicMessagesService
import space.davids_digital.kiri.llm.LlmMessageRequest
import space.davids_digital.kiri.llm.LlmMessageRequest.Tools.ToolChoice.REQUIRED
import space.davids_digital.kiri.llm.LlmMessageResponse
import space.davids_digital.kiri.llm.LlmToolUseResult
import space.davids_digital.kiri.llm.dsl.llmMessageRequest
import space.davids_digital.kiri.llm.dsl.llmToolUseResult
import space.davids_digital.kiri.model.EngineState
import space.davids_digital.kiri.service.exception.ServiceException
import java.util.concurrent.atomic.AtomicBoolean

@Service
class AgentEngine(
    private val appManager: AppManager,
    private val memoryManager: MemoryManager,
    private val toolRegistry: AgentToolRegistry,
    private val toolScanner: AgentToolScanner,
    private val toolParameterMapper: AgentToolParameterMapper,
    private val toolCallExecutor: ToolCallExecutor,
    private val frameRenderer: FrameRenderer,
    private val frames: FrameBuffer,
    private val eventBus: EngineEventBus,
    private val anthropicMessagesService: AnthropicMessagesService
) : AgentToolProvider {
    companion object {
        private const val RECOVERY_TIMEOUT_MS = 10000L
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    private val run = AtomicBoolean(false)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var sleepJob: Job? = null
    private val tickMutex = Mutex()

    private val mutableState = MutableStateFlow(EngineState.PAUSED)

    val state: StateFlow<EngineState> = mutableState.asStateFlow()

    @PostConstruct
    private fun init() {
        resetFrames()
        scope.launch {
            eventBus.events.collect(::handleEvent)
        }
    }

    suspend fun start() {
        if (state.value == EngineState.RUNNING || state.value == EngineState.STOPPING) {
            log.warn("Agent engine is already running or stopping, start request ignored")
            return
        }
        if (sleepJob != null) {
            log.debug("Interrupting agent sleep to start")
            sleepJob?.cancelAndJoin()
        }
        if (run.compareAndSet(false, true)) {
            scope.launch {
                try {
                    log.info("Starting agent engine")
                    while (run.get()) {
                        tickInternal()
                    }
                } catch (e: Exception) {
                    handleError(e)
                }
            }
        }
    }

    suspend fun tick() {
        scope.launch {
            tickInternal()
        }
    }

    private suspend fun tickInternal(): Boolean {
        if (!tickMutex.tryLock()) {
            return false
        }
        try {
            mutableState.emit(EngineState.RUNNING)
            updateToolRegistry()
            memoryManager.tick()
            val request = buildRequest()
            val response = anthropicMessagesService.request(request)
            handleResponse(response)
            eventBus.events.emit(TickEvent())
            return true
        } catch (e: Exception) {
            log.error("Tick error", e)
            return false
        } finally {
            tickMutex.unlock()
            mutableState.emit(EngineState.PAUSED)
        }
    }

    suspend fun softStop() {
        log.info("Soft stopping agent engine")
        run.set(false)
        if (mutableState.value == EngineState.RUNNING) {
            mutableState.emit(EngineState.STOPPING)
        } else {
            mutableState.emit(EngineState.PAUSED)
        }
    }

    suspend fun hardStop() {
        log.info("Hard stopping agent engine")
        run.set(false)
        scope.coroutineContext[Job]?.cancelChildren()
        sleepJob?.cancel()
        sleepJob = null
        mutableState.emit(EngineState.PAUSED)
        log.info("Agent engine stopped")
    }

    @PreDestroy
    private fun shutdown() {
        runBlocking {
            hardStop()
        }
    }

    private fun resetFrames() {
        frames.clearOnlyRolling()
        addSimpleText("system", "System started.")
    }

    private suspend fun buildRequest(): LlmMessageRequest {
        val systemText = this::class.java.getResource("/prompts/main.txt")?.readText()
        return llmMessageRequest {
            model = "claude-sonnet-4-20250514"
            system = systemText ?: ""
            maxOutputTokens = 2048
            temperature = 1.0
            tools {
                choice = REQUIRED
                allowParallelUse = true
                toolRegistry.iterate().forEach {
                    function {
                        name = it.fullName
                        description = it.description
                        parameters = toolParameterMapper.map(it.callable)
                    }
                }
            }
            frameRenderer.render(frames, this)
        }
    }

    private suspend fun handleResponse(response: LlmMessageResponse) {
        for (item in response.content) {
            if (item !is LlmMessageResponse.ContentItem.ToolUse) {
                log.warn("Unexpected agent response part '${item::class}', skipping")
                continue
            }

            // Very tricky shit happening here, will probably need to refactor

            val toolUse = item.toolUse

            val proxy = object {
                lateinit var provider: () -> LlmToolUseResult
            }
            val toolResult: (String) -> Unit = {
                proxy.provider = {
                    llmToolUseResult {
                        id = toolUse.id
                        name = toolUse.name
                        output {
                            text(it)
                        }
                    }
                }
            }
            toolResult("<Tool is still running, result is not ready...>")

            frames.addToolCall {
                this.toolUse = item.toolUse
                resultProvider = {
                    proxy.provider()
                }
            }

            log.info("Agent called tool '${toolUse.name}'")

            val entry = toolRegistry.find(toolUse.name)
            if (entry == null) {
                log.warn("Tool '${toolUse.name}' not found in registry, which is weird")
                toolResult("Unexpected error: tool '${toolUse.name}' was not found. This is certainly a system bug.")
                continue
            }
            val toolResponse = try {
                toolCallExecutor.execute(entry.callable, toolUse.input, entry.receiver)
            } catch (e: ServiceException) {
                log.error("Tool '${toolUse.name}' threw a service exception", e)
                toolResult("Tool threw an exception with message '${e.message}'")
                continue
            } catch (e: Exception) {
                log.error("Tool '${toolUse.name}' threw an exception", e)
                toolResult(
                    """
                    Tool threw an unexpected exception ${e::class}: ${e.message}.
                    Here are top 3 stack trace items:
                    ```
                    ${e.stackTrace.take(3).joinToString("\n")}
                    ```
                    """.trimIndent()
                )
                continue
            }
            if (toolResponse === Unit) {
                toolResult("ok")
            } else {
                toolResult(toolResponse.toString())
            }
        }
    }

    private suspend fun handleError(e: Exception) {
        log.error("Engine error", e)
        addSimpleText("system", "Engine error: ${e.message}")
        if (run.get()) {
            log.info("Will try to recover in $RECOVERY_TIMEOUT_MS ms")
        }
        mutableState.emit(EngineState.PAUSED)
        // Try to recover
        delay(RECOVERY_TIMEOUT_MS)
        if (run.get()) {
            run.set(false)
            log.info("Recovering engine")
            addSimpleText("system", "Attempting to recover...")
            start()
        }
    }

    private fun updateToolRegistry() {
        toolRegistry.clear()
        toolScanner.scan(listOf(this, appManager, memoryManager), toolRegistry)
    }

    private suspend fun wakeUp() {
        if (sleepJob != null) {
            log.debug("Interrupting agent sleep due to event")
            sleepJob?.cancelAndJoin()
        }
        if (state.value == EngineState.PAUSED) {
            start()
        }
    }

    /**
     * Handle incoming engine events (e.g., wake-up signals).
     */
    private suspend fun handleEvent(event: EngineEvent) {
        if (event is WakeUpRequestEvent) {
            wakeUp()
        }
    }

    private fun addSimpleText(tagName: String, text: String) {
        frames.addStatic {
            addCreatedAtNow()
            tag = tagName
            content = dataFrameContent {
                text(text)
            }
        }
    }

    override fun getAvailableAgentToolMethods() = listOf(::think, ::sleep)

    @AgentToolMethod(description = "Think to yourself.")
    fun think(thoughts: String) {
        addSimpleText("thought", thoughts)
    }

    @AgentToolMethod(
        description = "Pause for a specified amount of time. Recommended max is 8 hours, but may be higher. "
    )
    suspend fun sleep(seconds: Long) {
        if (seconds == 0L) {
            log.debug("Agent is going to sleep")
        } else {
            log.debug("Agent is going to sleep for $seconds seconds")
        }
        val sleptAt = System.currentTimeMillis()

        if (sleepJob != null) {
            log.debug("Agent is already sleeping, cancelling current sleep")
            sleepJob?.cancelAndJoin()
        }

        val newSleepJob = scope.launch {
            try {
                mutableState.emit(EngineState.PAUSED)
                if (seconds == 0L) {
                    delay(Long.MAX_VALUE)
                } else {
                    delay(seconds * 1000)
                }
                log.debug("Agent woke up after sleeping for $seconds seconds")
                addSimpleText("system", "Slept for $seconds seconds.")
            } catch (_: CancellationException) {
                log.debug("Agent was woken up from sleep")
                val sleptFor = (System.currentTimeMillis() - sleptAt) / 1000
                addSimpleText("system", "Slept for $sleptFor seconds")
            }
        }

        sleepJob = newSleepJob

        try {
            newSleepJob.join()
        } finally {
            sleepJob = null
        }
    }
}
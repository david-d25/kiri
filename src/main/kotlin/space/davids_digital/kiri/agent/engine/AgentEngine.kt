package space.davids_digital.kiri.agent.engine

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.*
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
        private const val RECOVERY_TIMEOUT_MS = 5000L
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    private val running = AtomicBoolean(false)
    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var currentSleepJob: Job? = null

    @PostConstruct
    private fun init() {
        resetFrames()
        engineScope.launch {
            eventBus.subscribe().collect(::handleEvent)
        }
        start()
    }

    fun start() {
        if (running.compareAndSet(false, true)) {
            engineScope.launch {
                try {
                    log.info("Starting Kiri engine")
                    while (running.get()) {
                        tick()
                    }
                } catch (e: Exception) {
                    handleError(e)
                }
            }
        }
    }

    fun softStop() {
        log.info("Soft stopping agent engine")
        running.set(false)
    }

    fun hardStop() {
        log.info("Hard stopping agent engine")
        running.set(false)
        engineScope.cancel() // TODO don't cancel the whole scope
        log.info("Agent engine stopped")
    }

    private fun resetFrames() {
        frames.clearOnlyRolling()
        frames.addSimpleText("system", "System started.")
    }

    private suspend fun tick() {
        updateToolRegistry()
        memoryManager.tick()
        val request = buildRequest()
        val response = anthropicMessagesService.request(request)
        handleResponse(response)
    }

    private fun buildRequest(): LlmMessageRequest {
        val systemText = this::class.java.getResource("/prompts/main.txt")?.readText()
        return llmMessageRequest {
            model = "claude-3-7-sonnet-latest"
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
        frames.addSimpleText("system", "Engine error: ${e.message}")
        if (running.get()) {
            log.info("Will try to recover in $RECOVERY_TIMEOUT_MS ms")
        }
        // Try to recover
        delay(RECOVERY_TIMEOUT_MS)
        if (running.get()) {
            running.set(false)
            log.info("Recovering engine")
            frames.addSimpleText("system", "Attempting to recover...")
            start()
        }
    }

    private fun updateToolRegistry() {
        toolRegistry.clear()
        toolScanner.scan(listOf(this, appManager, memoryManager), toolRegistry)
    }

    private suspend fun handleEvent(event: EngineEvent) {
        currentSleepJob?.cancelAndJoin() // TODO separate event for interruption?
    }

    private fun FrameBuffer.addSimpleText(tagName: String, text: String) {
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
        frames.addSimpleText("thought", thoughts)
    }

    @AgentToolMethod(
        description = "Sleep for a specified amount of time. Pass 0 to sleep infinitely until something happens."
    )
    suspend fun sleep(seconds: Long) {
        if (seconds == 0L) {
            log.debug("Agent is going to sleep")
        } else {
            log.debug("Agent is going to sleep for $seconds seconds")
        }
        val sleptAt = System.currentTimeMillis()

        if (currentSleepJob != null) {
            log.debug("Agent is already sleeping, cancelling current sleep")
            currentSleepJob?.cancelAndJoin()
        }

        val newSleepJob = engineScope.launch(context = Job()) {
            try {
                if (seconds == 0L) {
                    delay(Long.MAX_VALUE)
                } else {
                    delay(seconds * 1000)
                }
                log.debug("Agent woke up after sleeping for $seconds seconds")
                frames.addSimpleText("system", "Slept for $seconds seconds.")
            } catch (_: CancellationException) {
                log.debug("Agent was woken up from sleep")
                val sleptFor = (System.currentTimeMillis() - sleptAt) / 1000
                frames.addSimpleText("system", "Slept for $sleptFor seconds.")
            }
        }

        currentSleepJob = newSleepJob

        try {
            newSleepJob.join()
        } finally {
            currentSleepJob = null
        }
    }
}
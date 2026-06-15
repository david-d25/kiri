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
import space.davids_digital.kiri.agent.engine.event.EngineEvent
import space.davids_digital.kiri.agent.engine.event.SleepEvent
import space.davids_digital.kiri.agent.engine.event.TickEvent
import space.davids_digital.kiri.agent.engine.event.WakeUpRequestEvent
import space.davids_digital.kiri.agent.engine.lifecycle.EngineLifecycleHookExecutor
import space.davids_digital.kiri.agent.engine.lifecycle.LifecycleHookProvider
import space.davids_digital.kiri.agent.frame.*
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.memory.MemoryManager
import space.davids_digital.kiri.agent.tool.*
import space.davids_digital.kiri.llm.ChatCompletionRequest.Reasoning.Effort
import space.davids_digital.kiri.llm.ChatCompletionRequest.Tools.ToolChoice.AUTO
import space.davids_digital.kiri.llm.ChatCompletionRequest.Tools.ToolChoice.REQUIRED
import space.davids_digital.kiri.llm.ChatCompletionResponse
import space.davids_digital.kiri.llm.ChatCompletionToolUseResult
import space.davids_digital.kiri.llm.ChatCompletionWebSearch
import space.davids_digital.kiri.llm.dsl.chatCompletionRequest
import space.davids_digital.kiri.llm.dsl.chatCompletionToolUseResult
import space.davids_digital.kiri.model.EngineState
import space.davids_digital.kiri.orm.service.SettingOrmService
import space.davids_digital.kiri.service.ChatCompletionService
import space.davids_digital.kiri.service.ChatCompletionServiceRegistry
import space.davids_digital.kiri.service.exception.ServiceException
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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
    private val chatCompletionServiceRegistry: ChatCompletionServiceRegistry,
    private val lifecycleHookExecutor: EngineLifecycleHookExecutor,
    private val lifecycleHookProviders: List<LifecycleHookProvider>,
    settings: SettingOrmService
) : AgentToolProvider {
    companion object {
        private const val RECOVERY_TIMEOUT_MS = 10000L
        private const val TEXT_ONLY_RESPONSE_STOP_THRESHOLD = 5
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    private val instructions by settings.declareString("agent.engine.content.instructions", "")
    private val modelHandle by settings.declareStringNullable("agent.engine.modelHandle", null)
    private val maxOutputTokens by settings.declareLong("agent.engine.llm.maxOutputTokens", 1024)
    private val reasoningEnabled by settings.declareBoolean("agent.engine.llm.reasoning.enabled", false)
    private val reasoningMaxTokens by settings.declareLong("agent.engine.llm.reasoning.maxTokens", 2048)
    private val reasoningEffort by settings.declareStringNullable("agent.engine.llm.reasoning.effort", null)
    private val webSearchEnabled by settings.declareBoolean("agent.engine.llm.tool.external.webSearch.enabled", false)

    private val run = AtomicBoolean(false)
    private val eventHandlingScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val mainScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    @Volatile
    private var sleepJob: Job? = null
    private val tickMutex = Mutex()

    /**
     * Level-triggered wake-up flag. Set when a wake-up is requested (e.g. an incoming notification)
     * and cleared at the start of each tick when the agent re-reads the frame buffer. Guards against
     * the race where a wake-up arrives after the agent decides to sleep but before [sleepJob] exists:
     * in that window [wakeUp] has nothing to cancel, so [pause] consults this flag and skips sleeping.
     */
    private val wakePending = AtomicBoolean(false)

    private var consecutiveTextOnlyResponses = 0

    private val mutableState = MutableStateFlow(EngineState.PAUSED)

    val state: StateFlow<EngineState> = mutableState.asStateFlow()

    @PostConstruct
    private fun init() {
        frames.clear()
        addSimpleText("system", "System started.")
        eventHandlingScope.launch {
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
            consecutiveTextOnlyResponses = 0
            mainScope.launch {
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
        mainScope.launch {
            tickInternal()
        }
    }

    private suspend fun tickInternal() {
        if (!tickMutex.tryLock()) {
            return
        }
        try {
            mutableState.emit(EngineState.RUNNING)
            wakePending.set(false)
            val modelHandle = modelHandle
            if (modelHandle.isNullOrBlank()) {
                log.error("No model handle configured")
                softStop()
                return
            }
            val chatCompletionService = chatCompletionServiceRegistry.findByModelHandle(modelHandle)
            if (chatCompletionService == null) {
                log.error("No chat completion service found for model handle '$modelHandle'")
                softStop()
                return
            }
            updateToolRegistry()
            memoryManager.tick()
            val response = generateResponse(modelHandle, chatCompletionService)
            handleResponse(response)
            eventBus.events.emit(TickEvent())
            return
        } catch (e: Exception) {
            log.error("Tick error", e)
            delay(10.seconds)
            return
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
        mainScope.coroutineContext[Job]?.cancelChildren()
        eventHandlingScope.coroutineContext[Job]?.cancelChildren()
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

    private suspend fun generateResponse(
        modelHandle: String,
        chatCompletionService: ChatCompletionService
    ): ChatCompletionResponse {
        val maxOutputTokensWithReasoning = if (reasoningEnabled) {
            maxOutputTokens + reasoningMaxTokens
        } else {
            maxOutputTokens
        }
        val request = chatCompletionRequest {
            this.modelHandle = modelHandle
            this@chatCompletionRequest.instructions = this@AgentEngine.instructions
            maxOutputTokens = maxOutputTokensWithReasoning
            temperature = 1.0
            tools {
                choice = if (reasoningEnabled) AUTO else REQUIRED
                allowParallelUse = true
                toolRegistry.iterate().forEach {
                    function {
                        name = it.fullName
                        description = it.description
                        parameters = toolParameterMapper.map(it.callable)
                    }
                }
                external {
                    webSearch {
                        enabled = webSearchEnabled
                    }
                }
            }
            reasoning {
                enabled = reasoningEnabled
                effort = when (reasoningEffort) {
                    "low" -> Effort.LOW
                    "medium" -> Effort.MEDIUM
                    "high" -> Effort.HIGH
                    else -> Effort.AUTO
                }
                maxTokens = reasoningMaxTokens
            }
            with (frameRenderer) {
                render(frames)
            }
        }
        log.info("${request.tools.functions.size} function tools available for this request")
        return chatCompletionService.request(request)
    }

    private suspend fun handleResponse(response: ChatCompletionResponse) {
        for (item in response.content) {
            handleResponseItem(item)
        }
        val hasToolAction = response.content.any {
            it is ChatCompletionResponse.ContentItem.ToolUse || it is ChatCompletionWebSearch
        }
        if (hasToolAction) {
            consecutiveTextOnlyResponses = 0
        } else {
            consecutiveTextOnlyResponses++
            addSimpleText("system", "Warning: you MUST call tools. Text-only responses are not allowed in agent mode.")
            if (consecutiveTextOnlyResponses >= TEXT_ONLY_RESPONSE_STOP_THRESHOLD) {
                log.error("Model produced $consecutiveTextOnlyResponses consecutive text-only responses, stopping to prevent credit drain")
                addSimpleText("system", "Engine stopped: model is not calling tools (possible credit drain).")
                consecutiveTextOnlyResponses = 0
                softStop()
            }
        }
    }

    private suspend fun handleResponseItem(item: ChatCompletionResponse.ContentItem) {
        when (item) {
            is ChatCompletionResponse.ContentItem.ToolUse -> handleResponseItem(item)
            is ChatCompletionWebSearch -> handleResponseItem(item)
            is ChatCompletionResponse.ContentItem.Reasoning -> handleResponseItem(item)
            is ChatCompletionResponse.ContentItem.RedactedReasoning -> {} // No displayable content
            is ChatCompletionResponse.ContentItem.Text -> {
                log.warn("Model produced text response in agent mode: '${item.text.take(100)}'")
                addSimpleText("assistant", item.text)
            }
        }
    }

    private suspend fun handleResponseItem(item: ChatCompletionResponse.ContentItem.Reasoning) {
        val content = item.content
        if (!content.isNullOrBlank()) {
            frames.add(ReasoningFrame(content, item.signature, item.id))
        }
    }

    private suspend fun handleResponseItem(item: ChatCompletionResponse.ContentItem.ToolUse) {
        // Very tricky shit happening here, will probably need to refactor

        val toolUse = item.toolUse

        val proxy = object {
            lateinit var provider: () -> ChatCompletionToolUseResult
        }
        val toolResult: (String) -> Unit = {
            proxy.provider = {
                chatCompletionToolUseResult {
                    id = toolUse.id
                    name = toolUse.name
                    output {
                        text(it)
                    }
                }
            }
        }
        toolResult("<Tool is still running, result is not ready...>")

        log.info("Agent called tool '${toolUse.name}'")

        val entry = toolRegistry.find(toolUse.name)
        if (entry == null || entry.createFrame) {
            frames.addToolCall {
                this.toolUse = item.toolUse
                resultProvider = {
                    proxy.provider()
                }
            }
        }
        if (entry == null) {
            log.warn("Tool '${toolUse.name}' not found in registry, which is weird")
            toolResult("Unexpected error: tool '${toolUse.name}' was not found")
            return
        }

        val toolResponse = try {
            toolCallExecutor.execute(entry.callable, toolUse.input, entry.receiver)
        } catch (e: ServiceException) {
            log.error("Tool '${toolUse.name}' threw a service exception", e)
            toolResult("Tool failed with message '${e.message}'")
            return
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
            return
        }
        proxy.provider = { ToolCallFrame.formatResult(toolUse.id, toolUse.name, toolResponse) }
    }

    private suspend fun handleResponseItem(item: ChatCompletionWebSearch) {
        frames.add(NativeWebSearchFrame(webSearch = item))
    }

    private suspend fun handleError(e: Exception) {
        log.error("Engine error", e)
        addSimpleText("system", "Engine error: ${e.message}")
        if (run.get()) {
            log.info("Will try to recover in $RECOVERY_TIMEOUT_MS ms")
        }
        mutableState.emit(EngineState.PAUSED)
        // Try to recover
        delay(RECOVERY_TIMEOUT_MS.milliseconds)
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

    /**
     * Rebuilds the tool registry from current providers.
     * Safe to call from outside the tick loop (e.g. from admin endpoints).
     */
    fun refreshToolRegistry() {
        updateToolRegistry()
    }

    private suspend fun wakeUp() {
        wakePending.set(true)
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
        mainScope.launch {
            if (event is WakeUpRequestEvent) {
                wakeUp()
            }
        }
    }

    private fun addSimpleText(tagName: String, text: String) {
        frames.addStatic {
            tag = tagName
            content = dataFrameContent {
                text(text)
            }
        }
    }

    override fun getAvailableAgentToolMethods() = buildList {
        if (!reasoningEnabled) add(::think)
        add(::pause)
        add(::compact)
    }

    @AgentToolMethod(description = "Think to yourself and plan next moves.")
    fun think(
        @Suppress("unused") // Value stays in framebuffer
        thoughts: String,
    ) {}

    @AgentToolMethod(description = "Free up short-term memory by summarizing older content")
    fun compact(
        @AgentToolParameter(description = "Information to retain")
        @Suppress("unused") // Value stays in framebuffer
        summary: String,
        @AgentToolParameter(description = "Number of most recent messages to keep unaltered, better to keep 6-12 to not lose context")
        keepLastN: Int = 10
    ): String {
        frames.trim(keepLastN.coerceIn(3..frames.hardLimit))
        return "Memory compacted, kept last $keepLastN items."
    }

    @AgentToolMethod(
        description = "Wait for a specified amount of time. " +
                "Notifications (i.e. chat mentions) will wake you up. "
    )
    suspend fun pause(hours: Long, minutes: Long, seconds: Long) {
        val effectiveSeconds = hours * 3600 + minutes * 60 + seconds
        log.debug("Agent is going to sleep for $effectiveSeconds seconds")
        val sleptAt = System.currentTimeMillis()

        if (sleepJob != null) {
            log.debug("Agent is already sleeping, cancelling current sleep")
            sleepJob?.cancelAndJoin()
        }

        val newSleepJob = mainScope.launch {
            try {
                mutableState.emit(EngineState.PAUSED)
                eventBus.events.emit(SleepEvent(effectiveSeconds))

                // A wake-up requested after this tick started (e.g. a notification that arrived
                // while the agent was deciding to sleep) means there's something new to look at,
                // so skip sleeping entirely instead of dropping the wake-up.
                if (wakePending.get()) {
                    log.debug("Agent skipped sleep due to pending wake-up")
                    addSimpleText("sleep", "Something prevents you from sleeping")
                    return@launch
                }

                delay(effectiveSeconds.seconds)
                log.debug("Agent woke up after sleeping for $effectiveSeconds seconds")
                addSimpleText("sleep", "Slept for $effectiveSeconds seconds.")
            } catch (_: CancellationException) {
                val sleptFor = (System.currentTimeMillis() - sleptAt) / 1000
                log.debug("Agent was woken up from sleep (job cancelled)")
                addSimpleText("sleep", "Slept for $sleptFor seconds")
            }
        }

        sleepJob = newSleepJob

        try {
            newSleepJob.join()
        } finally {
            sleepJob = null
        }

        val hookProviders = listOf(this, appManager) +
            appManager.getSubProviders() +
            lifecycleHookProviders
        lifecycleHookExecutor.executeOnWake(hookProviders, frames)
    }
}
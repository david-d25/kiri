package space.davids_digital.kiri.agent.engine

import com.anthropic.models.Model
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.agent.app.AppSystem
import space.davids_digital.kiri.agent.frame.Frame
import space.davids_digital.kiri.agent.frame.FrameRenderer
import space.davids_digital.kiri.agent.memory.MemorySystem
import space.davids_digital.kiri.agent.tool.AgentToolParameterMapper
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolProvider
import space.davids_digital.kiri.agent.tool.AgentToolRegistry
import space.davids_digital.kiri.agent.tool.AgentToolScanner
import space.davids_digital.kiri.integration.anthropic.AnthropicMessagesService
import space.davids_digital.kiri.llm.LlmMessageRequest
import space.davids_digital.kiri.llm.llmMessageRequest
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean

@Service
class AgentEngine(
    private val appSystem: AppSystem,
    private val memorySystem: MemorySystem,
    private val frameRenderer: FrameRenderer,
    private val anthropicMessagesService: AnthropicMessagesService,
    private val toolRegistry: AgentToolRegistry,
    private val toolScanner: AgentToolScanner,
    private val agentToolParameterMapper: AgentToolParameterMapper,
) : AgentToolProvider {
    companion object {
        private const val RECOVERY_TIMEOUT_MS = 5000L
    }

    private val log = LoggerFactory.getLogger(this::class.java)

    private val frames = LinkedList<Frame>()
    private val running = AtomicBoolean(false)
    private val engineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    @PostConstruct
    private fun init() {
        resetFrames()
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
        log.info("Soft stopping Kiri engine")
        running.set(false)
    }

    fun hardStop() {
        log.info("Hard stopping Kiri engine")
        running.set(false)
        engineScope.cancel()
        log.info("Kiri engine stopped")
    }

    private fun addFrame(block: Frame.Builder.() -> Unit) {
        val builder = Frame.Builder()
        block(builder)
        frames.add(builder.build())
    }

    private fun resetFrames() {
        frames.clear()
        addFrame {
            type = "system"
            content = "System started."
        }
        addFrame {
            type = "system"
            content = "System is cold-started. Please explore the environment to warm up your memory."
        }
    }

    private suspend fun tick() {
        updateToolRegistry()
        val input = frameRenderer.render(frames)
        val request = buildRequest(input)
        val response = anthropicMessagesService.request(request)
        // TODO: Handle response
        println(response)
        softStop()
        delay(5000)
    }

    private fun buildRequest(input: String): LlmMessageRequest<Model> {
        val systemText = this::class.java.getResource("/prompts/main.txt")?.readText()
        return llmMessageRequest {
            model = Model.CLAUDE_3_7_SONNET_LATEST
            system = systemText ?: ""
            userMessage {
                text(input)
            }
            maxOutputTokens = 1024
            temperature = 0.5
            tools {
                choice = LlmMessageRequest.Tools.ToolChoice.REQUIRED
                allowParallelUse = false
                toolRegistry.iterate().forEach {
                    function {
                        name = it.fullName
                        description = it.description
                        parameters = agentToolParameterMapper.map(it.callable)
                    }
                }
            }
        }
    }

    private suspend fun handleError(e: Exception) {
        log.error("Engine error", e)
        addFrame {
            type = "system"
            content = "Engine error: ${e.message}"
        }
        if (running.get()) {
            log.info("Will try to recover in $RECOVERY_TIMEOUT_MS ms")
        }
        // Try to recover
        delay(RECOVERY_TIMEOUT_MS)
        if (running.get()) {
            addFrame {
                type = "system"
                content = "Attempting to recover..."
            }
            start()
        }
    }

    private fun updateToolRegistry() {
        toolRegistry.clear()
        toolScanner.scan(listOf(this, appSystem, memorySystem), toolRegistry)
    }

    override fun getAvailableAgentToolMethods() = listOf(::think)

    @AgentToolMethod(name = "think", description = "Think to yourself")
    private fun think(thoughts: String) {
        addFrame { type = "thought"; content = thoughts }
    }
}
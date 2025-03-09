package space.davids_digital.kiri.agent.engine

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
import space.davids_digital.kiri.integration.anthropic.AnthropicMessagesService
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean

@Service
class AgentEngine(
    private val appSystem: AppSystem,
    private val frameRenderer: FrameRenderer,
    private val anthropicMessagesService: AnthropicMessagesService,
) {
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
            }
        }
    }

    fun stop() {
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
            type = "tips"
            content = "System is cold-started. Please explore the environment to warm up your memory."
        }
    }

    private suspend fun tick() {
        val input = frameRenderer.render(frames)
        val response = anthropicMessagesService.createTemp(input)
        delay(5000)
    }
}
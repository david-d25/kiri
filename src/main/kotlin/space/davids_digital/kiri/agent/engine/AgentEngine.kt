package space.davids_digital.kiri.agent.engine

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.agent.app.AppSystem
import space.davids_digital.kiri.agent.frame.Frame
import space.davids_digital.kiri.agent.frame.FrameRenderer
import space.davids_digital.kiri.integration.anthropic.AnthropicMessagesService
import java.util.LinkedList

@Service
class AgentEngine(
    private val appSystem: AppSystem,
    private val frameRenderer: FrameRenderer,
    private val anthropicMessagesService: AnthropicMessagesService,
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val frames = LinkedList<Frame>()
    private var running = false

    @PostConstruct
    private fun init() {
        resetFrames()
        start()
    }

    private fun start() {
        running = true
        runBlocking {
            launch {
                try {
                    while (running) {
                        tick()
                    }
                } catch (e: Exception) {
                    log.error("Engine error", e)
                }
            }
        }
    }

    private fun resetFrames() {
        frames.clear()
        frames.add(Frame(
            "system",
            emptyMap(),
            "System started.",
        ))
        frames.add(Frame(
            "tips",
            emptyMap(),
            "System is cold-started. Please explore the environment to warm up your memory.",
        ))
    }

    private suspend fun tick() {
        val input = frameRenderer.render(frames)
        val response = anthropicMessagesService.createTemp(input)
        frames.add(Frame("think", emptyMap(), response))
        log.info("LLM: $response")
        delay(3000)
    }
}
package space.davids_digital.kiri.agent.memory

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.Frame
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.agent.frame.FrameRenderer
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolParameterMapper
import space.davids_digital.kiri.agent.tool.AgentToolProvider
import space.davids_digital.kiri.agent.tool.AgentToolParameter
import space.davids_digital.kiri.integration.anthropic.AnthropicMessagesService
import space.davids_digital.kiri.llm.LlmMessageRequest
import space.davids_digital.kiri.llm.LlmMessageRequest.Tools.ToolChoice.REQUIRED
import space.davids_digital.kiri.llm.LlmMessageResponse
import space.davids_digital.kiri.llm.dsl.llmMessageRequest
import space.davids_digital.kiri.service.MemoryService
import java.time.ZonedDateTime

/**
 * This component manages the agent's memory.
 */
@Component
@AgentToolNamespace("memory")
class MemoryManager(
    private val frames: FrameBuffer,
    private val frameRenderer: FrameRenderer,
    private val anthropicMessagesService: AnthropicMessagesService,
    private val toolParameterMapper: AgentToolParameterMapper,
    private val memoryService: MemoryService,
) : AgentToolProvider {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val skipFirstNTicks = 3
    private var tick = 0

    private val processedFixedFrames = mutableMapOf<DataFrame, Int>()
    private val processedRollingFrames = mutableSetOf<Frame>()

    suspend fun tick() {
        if (tick < skipFirstNTicks) {
            tick++
            return
        }
//        memorizeFromFrames()
        tick++
    }

    private suspend fun memorizeFromFrames() {
        val fixedFrames = getUnprocessedFixedFrames()
        val rollingFrames = getUnprocessedRollingFrames()
        log.debug("Memorizing ${fixedFrames.size} fixed and ${rollingFrames.size} rolling frames")
        val request = buildMemorizingRequest(fixedFrames, rollingFrames)
        val response = anthropicMessagesService.request(request)
        handleMemorizingResponse(response)
        // TODO processedFixedFrames
        processedRollingFrames.addAll(rollingFrames)
        cleanUp()
    }

    private suspend fun buildMemorizingRequest(
        fixedFrames: List<DataFrame>,
        rollingFrames: List<Frame>
    ): LlmMessageRequest {
        val prompt = this::class.java.getResource("/prompts/memory/memory.txt")?.readText()
        val examples = this::class.java.getResource("/prompts/memory/examples.txt")?.readText()
        return llmMessageRequest {
            model = "claude-3-7-sonnet-latest"
            maxOutputTokens = 1024
            temperature = 0.0
            tools {
                choice = REQUIRED
                allowParallelUse = false
                function {
                    name = "memorize"
                    parameters = toolParameterMapper.map(::memorize)
                }
            }
            examples?.let {
                userMessage {
                    text(it)
                }
            }
            prompt?.let {
                userMessage {
                    text(it)
                }
            }
            frameRenderer.render(fixedFrames, rollingFrames, this)
        }
    }

    private fun handleMemorizingResponse(response: LlmMessageResponse) {
        println(response) // TODO
    }

    private fun cleanUp() {
        val universe = HashSet<Frame>()
        universe.addAll(frames)
        processedFixedFrames.filterKeys { !universe.contains(it) }
            .forEach { processedFixedFrames.remove(it.key) }
        processedRollingFrames.removeAll(processedRollingFrames.filter { !universe.contains(it) })
    }

    private fun getUnprocessedFixedFrames(): List<DataFrame> {
        return emptyList() // TODO
    }

    private fun getUnprocessedRollingFrames(): List<Frame> {
        val result = ArrayList<Frame>(frames.size)
        for (frame in frames.onlyRolling) {
            if (!processedRollingFrames.contains(frame)) {
                result.add(frame)
            }
        }
        return result
    }

    override fun getAvailableAgentToolMethods(): Collection<Function<*>> = listOf(::memorize, ::query)

    @AgentToolMethod(description = "Remember facts, associations, behavior, etc. to general purpose memory.")
    suspend fun memorize(
        @AgentToolParameter(
            name = "keys",
            description = "What the memory should be associated with (AND logic)." +
                    "It's recommended to include names, dates (incl. current), nicknames, " +
                    "aspects, circumstances, etc.). More keys = more specific memory."
        )
        keyStrings: List<String>,

        @AgentToolParameter(description = "What to remember")
        value: String
    ): String {
        if (keyStrings.isEmpty()) {
            return "No keys provided"
        }
        if (value.isBlank()) {
            return "Value is empty"
        }
        val keys = memoryService.getOrCreateKeys(keyStrings.filter { it.isNotBlank() })
        val point = memoryService.getOrCreatePoint(value)
        memoryService.remember(keys, point, ZonedDateTime.now())
        return "ok"
    }

    @AgentToolMethod(description = "Search in memory")
    suspend fun query(
        @AgentToolParameter(
            name = "keys",
            description = "What the desired memory piece is associated with (names, dates, aspects, circumstances, " +
                    "etc.). More keys = more specific memory."
        )
        keyStrings: List<String>,
    ): String {
        val keys = memoryService.getOrCreateKeys(keyStrings)
        val memories = memoryService.retrieve(keys, 5)
        return buildString {
            appendLine("Most related memories:")
            for (memory in memories) {
                append("- ")
                append("(score:")
                append(String.format("%.3f", memory.score))
                append(") ")
                appendLine(memory.point.value)
            }
        }
    }
}
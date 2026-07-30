package space.davids_digital.kiri.agent.memory

import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolParameter
import space.davids_digital.kiri.agent.tool.AgentToolProvider
import space.davids_digital.kiri.service.MemoryService
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Exposes the agent's long-term memory as tools: nothing is memorized automatically,
 * the agent decides for itself what to store, recall and drop.
 */
@Component
@AgentToolNamespace("memory")
class MemoryManager(
    private val memoryService: MemoryService,
) : AgentToolProvider {
    override fun getAvailableAgentToolMethods() = listOf(::memorize, ::query, ::forget)

    @AgentToolMethod(description = "Remember something to general purpose memory")
    suspend fun memorize(
        @AgentToolParameter(
            name = "keys",
            description = "Keys to associate with memory (AND semantic): " +
                    "names, dates, terms, locations, conditions, current date, etc. Longer is better."
        )
        keyStrings: List<String>,

        @AgentToolParameter(description = "Text to remember")
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

    @AgentToolMethod(description = "Returns memories closest to the provided keys")
    suspend fun query(
        @AgentToolParameter(
            name = "keys",
            description = "Keys with AND semantic. Provide minimal set of keys needed for better results"
        )
        keyStrings: List<String>,
    ): String {
        val keys = memoryService.getOrCreateKeys(keyStrings)
        val memories = memoryService.retrieve(keys, 6)
        if (memories.isEmpty()) {
            return "No memories found for the provided keys."
        }
        val shortIds = memoryService.resolveShortIds(memories.map { it.point.id })
        val now = ZonedDateTime.now()
        return buildString {
            appendLine("Most relevant memories (top to bottom):")
            memories.forEach { memory ->
                appendLine()
                append("[id=")
                append(shortIds[memory.point.id] ?: memory.point.id.toString().replace("-", ""))
                append(", relevance=")
                append(String.format("%.2f", memory.score))
                append(", created=")
                append(formatCreatedAt(memory.point.createdAt, now))
                appendLine("]")
                appendLine(memory.point.value)
            }
        }
    }

    @AgentToolMethod(
        description = "Forget a memory by its id (as shown in memory_query output). " +
                "Accepts either the short prefix or the full hex id."
    )
    suspend fun forget(
        @AgentToolParameter(description = "Memory id from memory_query output (short prefix or full hex)")
        id: String,
    ): String {
        val candidates = memoryService.resolveMemoryPointIdFromHex(id)
        return when (candidates.size) {
            0 -> "No memory found for id '$id'."
            1 -> {
                memoryService.forget(candidates[0])
                "Forgotten memory $id."
            }
            else -> "Ambiguous id '$id' — matches ${candidates.size} memories. " +
                    "Provide more characters. Candidates: " +
                    candidates.joinToString(", ") { it.toString().replace("-", "") }
        }
    }

    private fun formatCreatedAt(createdAt: ZonedDateTime, now: ZonedDateTime): String {
        val elapsed = Duration.between(createdAt, now)
        val seconds = elapsed.seconds
        return when {
            seconds < 60 -> "just now"
            seconds < 3600 -> "${elapsed.toMinutes()}m ago"
            seconds < 86400 -> "${elapsed.toHours()}h ago"
            seconds < 30 * 86400 -> "${elapsed.toDays()}d ago"
            else -> createdAt.withZoneSameInstant(ZoneOffset.UTC).format(ABSOLUTE_DATE_FORMAT)
        }
    }

    companion object {
        private val ABSOLUTE_DATE_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm 'UTC'")
    }
}
package space.davids_digital.kiri.agent.app.files

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.app.AgentApp
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.service.TemporaryFilesService
import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import kotlin.reflect.KFunction

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@AgentToolNamespace("files")
class FilesApp(
    private val temporaryFiles: TemporaryFilesService,
) : AgentApp("files") {

    companion object {
        private const val DEFAULT_LIMIT_LINES = 2000
        private const val MAX_LINE_CHARS = 2000
        private const val MAX_RESPONSE_CHARS = 50_000
    }

    @AgentToolMethod
    suspend fun list(): String {
        val files = temporaryFiles.list()
        if (files.isEmpty()) return "No files."
        return files.joinToString("\n") { "- ${it.name} (${it.size} bytes)" }
    }

    @AgentToolMethod(
        description = "Read a text file by lines [offset, offset+limit). " +
                "Long lines truncated to $MAX_LINE_CHARS chars, total response capped at $MAX_RESPONSE_CHARS chars. " +
                "Fails for binary files."
    )
    suspend fun read(
        name: String,
        offset: Int = 0,
        limit: Int = DEFAULT_LIMIT_LINES,
    ): String {
        val bytes = temporaryFiles.getContent(name) ?: return "File '$name' not found."
        val text = decodeUtf8(bytes) ?: return "Binary file '$name', ${bytes.size} bytes. Cannot read as text."
        val lines = text.lines()
        if (offset >= lines.size) {
            return "Empty range. File has ${lines.size} lines."
        }
        val end = (offset + limit).coerceAtMost(lines.size)
        val selected = lines.subList(offset, end)

        val out = StringBuilder()
        var truncated = false
        for ((i, line) in selected.withIndex()) {
            val capped = if (line.length > MAX_LINE_CHARS) {
                "${line.take(MAX_LINE_CHARS)}…<truncated, ${line.length - MAX_LINE_CHARS} more chars>"
            } else line
            if (out.length + capped.length + 1 > MAX_RESPONSE_CHARS) {
                out.append("…<response capped at $MAX_RESPONSE_CHARS chars; stopped at line ${offset + i}>")
                truncated = true
                break
            }
            if (i > 0) out.append('\n')
            out.append(capped)
        }
        if (!truncated && end < lines.size) {
            out.append("\n…<${lines.size - end} more lines; call again with offset=$end>")
        }
        return out.toString()
    }

    @AgentToolMethod(description = "Create or overwrite a text file")
    suspend fun write(name: String, content: String): String {
        temporaryFiles.create(name, content.toByteArray(Charsets.UTF_8))
        return "Wrote '$name' (${content.length} chars)."
    }

    @AgentToolMethod(
        description = "Replace exact occurrence of 'old' with 'new'. " +
                "Fails if 'old' is missing or non-unique unless replaceAll=true."
    )
    suspend fun edit(
        name: String,
        old: String,
        new: String,
        replaceAll: Boolean = false,
    ): String {
        val bytes = temporaryFiles.getContent(name) ?: return "File '$name' not found."
        val text = decodeUtf8(bytes) ?: return "Binary file '$name', cannot edit as text."
        if (old.isEmpty()) return "Argument 'old' must not be empty."
        val count = countOccurrences(text, old)
        if (count == 0) return "'old' string not found in '$name'."
        if (count > 1 && !replaceAll) {
            return "'old' string occurs $count times in '$name'. Provide more context or pass replaceAll=true."
        }
        val updated = if (replaceAll) text.replace(old, new) else text.replaceFirst(old, new)
        temporaryFiles.create(name, updated.toByteArray(Charsets.UTF_8))
        return if (replaceAll) "Replaced $count occurrence(s) in '$name'." else "Replaced 1 occurrence in '$name'."
    }

    @AgentToolMethod
    suspend fun delete(name: String): String {
        return if (temporaryFiles.delete(name)) "Deleted '$name'." else "File '$name' not found."
    }

    override fun getAvailableAgentToolMethods(): Collection<KFunction<*>> =
        listOf(::list, ::read, ::write, ::edit, ::delete)

    private fun decodeUtf8(bytes: ByteArray): String? {
        val decoder = Charsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        return try {
            decoder.decode(ByteBuffer.wrap(bytes)).toString()
        } catch (_: CharacterCodingException) {
            null
        }
    }

    private fun countOccurrences(haystack: String, needle: String): Int {
        var count = 0
        var idx = 0
        while (true) {
            val found = haystack.indexOf(needle, idx)
            if (found < 0) break
            count++
            idx = found + needle.length
        }
        return count
    }
}

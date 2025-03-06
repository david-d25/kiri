package space.davids_digital.kiri.agent.frame

import io.ktor.util.escapeHTML
import org.springframework.stereotype.Component

@Component
class FrameRenderer {
    /**
     * Renders an agent frame to a string.
     * Output has the following format:
     * ```
     * <type attribute1="value1" attribute2="value2" ...>
     *     content
     * </type>
     * ```
     *
     * @param frame The frame to render, content is HTML-escaped.
     * @param output The output string builder.
     */
    fun render(frame: Frame, output: StringBuilder) {
        with(output) {
            val attrString = frame.attributes.entries
                .map { entry -> entry.key to entry.value.escapeHTML() }
                .joinToString(" ") { (key, value) -> "$key=\"$value\"" }
                .let { if (it.isNotEmpty()) " $it" else "" }
            appendLine("<${frame.type}$attrString>")
            appendLine(frame.content.escapeHTML())
            appendLine("</${frame.type}>")
        }
    }

    fun render(frames: List<Frame>, output: StringBuilder) {
        frames.forEach { render(it, output) }
    }

    fun render(frames: List<Frame>): String {
        val output = StringBuilder()
        render(frames, output)
        return output.toString()
    }
}
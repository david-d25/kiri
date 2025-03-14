package space.davids_digital.kiri.agent.frame

import io.ktor.util.escapeHTML
import org.springframework.stereotype.Component

@Component
class FrameRenderer {
    /**
     * Renders a static frame to a string.
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
    fun render(frame: StaticDataFrame, output: StringBuilder) {
        with(output) {
            val attrString = frame.attributes.entries
                .map { entry -> entry.key to entry.value.escapeHTML() }
                .joinToString(" ") { (key, value) -> "$key=\"$value\"" }
                .let { if (it.isNotEmpty()) " $it" else "" }
            appendLine("<${frame.tag}$attrString>")
            appendLine(frame.content.escapeHTML())
            appendLine("</${frame.tag}>")
        }
    }

    fun render(frames: List<StaticDataFrame>, output: StringBuilder) {
        frames.forEach { render(it, output) }
    }

    fun render(frames: List<StaticDataFrame>): String {
        val output = StringBuilder()
        render(frames, output)
        return output.toString()
    }

    fun render(frame: StaticDataFrame): String {
        val output = StringBuilder()
        render(frame, output)
        return output.toString()
    }
}
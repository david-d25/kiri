package space.davids_digital.kiri.agent.frame

import io.ktor.util.escapeHTML
import org.springframework.stereotype.Component
import space.davids_digital.kiri.llm.dsl.LlmMessageRequestBuilder

@Component
class FrameRenderer {
    /**
     * Renders a frame to a LLM message request builder.
     * Output has the following format:
     * ```
     * <type attribute1="value1" attribute2="value2" ...>
     *     content
     * </type>
     * ```
     * @param frame The frame to render.
     * @param target The target LLM message request builder.
     */
    fun render(frame: Frame, target: LlmMessageRequestBuilder<*>) {
        when (frame) {
            is DataFrame -> target.renderDataFrame(frame)
            is ToolCallFrame -> target.renderToolCallFrame(frame)
        }
    }

    /**
     * Renders a data frame to a LLM message request builder.
     * Consecutive text parts are concatenated into a single text message part.
     */
    private fun LlmMessageRequestBuilder<*>.renderDataFrame(frame: DataFrame) {
        val builder = StringBuilder()
        val content = frame.renderContent()
        val attrString = frame.attributes.entries
            .map { entry -> entry.key to entry.value.escapeHTML() }
            .joinToString(" ") { (key, value) -> "$key=\"$value\"" }
            .let { if (it.isNotEmpty()) " $it" else "" }
        builder.append("<").append(frame.tag).append(attrString).appendLine(">")
        userMessage {
            for (contentPart in content) {
                if (contentPart is DataFrame.Text) {
                    builder.append(contentPart.text.escapeHTML())
                } else if (contentPart is DataFrame.Image) {
                    if (builder.isNotEmpty()) {
                        text(builder.toString())
                        builder.clear()
                    }
                    image(contentPart.data, contentPart.type)
                }
            }
            builder.appendLine("</${frame.tag}>")
            text(builder.toString())
        }
    }

    private fun LlmMessageRequestBuilder<*>.renderToolCallFrame(frame: ToolCallFrame) {
        assistantMessage {
            toolUse {
                id = frame.toolUse.id
                name = frame.toolUse.name
                input = frame.toolUse.input
            }
        }
        userMessage {
            toolResult {
                val result = frame.resultProvider()
                id = result.toolUseId
                output = result.output
            }
        }
    }
}
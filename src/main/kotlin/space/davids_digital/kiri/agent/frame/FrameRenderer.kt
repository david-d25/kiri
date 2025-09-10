package space.davids_digital.kiri.agent.frame

import io.ktor.util.escapeHTML
import org.springframework.stereotype.Component
import space.davids_digital.kiri.llm.dsl.LlmMessageRequestBuilder
import space.davids_digital.kiri.llm.dsl.LlmMessageRequestUserMessageBuilder

@Component
class FrameRenderer {
    suspend fun render(frames: FrameBuffer, target: LlmMessageRequestBuilder) {
        render(frames.onlyFixed, frames.onlyRolling, target)
    }

    suspend fun render(fixedFrames: Iterable<DataFrame>, rollingFrames: Iterable<Frame>, target: LlmMessageRequestBuilder) {
        render(fixedFrames.iterator(), rollingFrames.iterator(), target)
    }

    suspend fun render(fixedFrames: Iterator<DataFrame>, rollingFrames: Iterator<Frame>, target: LlmMessageRequestBuilder) {
        target.userMessage {
            text("<fixed>\n")
            for (frame in fixedFrames) {
                renderDataFrame(frame)
            }
            text("</fixed>\n")
        }
        for (frame in rollingFrames) {
            when (frame) {
                is DataFrame -> target.userMessage { renderDataFrame(frame) }
                is ToolCallFrame -> target.renderToolCallFrame(frame)
            }
        }
    }

    /**
     * Renders a data frame to a LLM message request builder.
     * Consecutive text parts are concatenated into a single text message part.
     */
    private suspend fun LlmMessageRequestUserMessageBuilder.renderDataFrame(frame: DataFrame) {
        val builder = StringBuilder()
        val content = frame.renderContent()
        val attrString = frame.attributes.entries
            .map { entry -> entry.key to entry.value.escapeHTML() }
            .joinToString(" ") { (key, value) -> "$key=\"$value\"" }
            .let { if (it.isNotEmpty()) " $it" else "" }
        builder.append("<").append(frame.tag).append(attrString).appendLine(">")
        for (contentPart in content) {
            if (contentPart is DataFrame.Text) {
                builder.append(contentPart.text)
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

    private suspend fun LlmMessageRequestBuilder.renderToolCallFrame(frame: ToolCallFrame) {
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
                name = result.name
                output = result.output
            }
        }
    }
}
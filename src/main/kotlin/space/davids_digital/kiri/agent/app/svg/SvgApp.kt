package space.davids_digital.kiri.agent.app.svg

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.app.AgentApp
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.llm.ChatCompletionImageType
import space.davids_digital.kiri.service.TemporaryFilesService
import kotlin.reflect.KFunction

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@AgentToolNamespace("svg")
class SvgApp(
    private val svgRenderService: SvgRenderService,
    private val temporaryFiles: TemporaryFilesService,
) : AgentApp("svg") {

    @AgentToolMethod(
        description = "Render an existing SVG file to PNG. " +
                "Reads <sourceName>, writes <pngName>, returns preview. " +
                "Create the SVG with files_write first."
    )
    suspend fun render(
        sourceName: String,
        pngName: String,
    ): List<DataFrame.ContentPart> {
        val svgBytes = temporaryFiles.getContent(sourceName)
            ?: return dataFrameContent { line("File '$sourceName' not found.") }
        val svgContent = svgBytes.toString(Charsets.UTF_8)
        return try {
            val pngBytes = svgRenderService.renderToPng(svgContent)
            temporaryFiles.create(pngName, pngBytes)
            dataFrameContent {
                line("Rendered '$sourceName' → '$pngName'. Preview:")
                image(pngBytes, ChatCompletionImageType.PNG)
            }
        } catch (e: Exception) {
            dataFrameContent { line("Render failed: ${e.message}") }
        }
    }

    override fun getAvailableAgentToolMethods(): Collection<KFunction<*>> = listOf(::render)
}

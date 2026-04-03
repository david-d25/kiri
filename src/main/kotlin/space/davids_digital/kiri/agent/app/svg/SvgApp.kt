package space.davids_digital.kiri.agent.app.svg

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.app.AgentApp
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolParameter
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

    private val names = mutableSetOf<String>()

    @AgentToolMethod(
        description = "Write SVG, render it to PNG, save both as temporary files, and return a preview image. " +
                "Files are saved as {name}.svg and {name}.png. " +
                "Use different names to work on multiple SVGs in parallel."
    )
    suspend fun write(
        @AgentToolParameter(description = "Name for this SVG, e.g. 'diagram', 'logo'. Also used as filename prefix.")
        name: String,
        @AgentToolParameter(description = "Full SVG content as XML string")
        svgContent: String
    ): List<DataFrame.ContentPart> {
        temporaryFiles.create("$name.svg", svgContent.toByteArray(Charsets.UTF_8))
        names.add(name)
        return try {
            val pngBytes = svgRenderService.renderToPng(svgContent)
            temporaryFiles.create("$name.png", pngBytes)
            dataFrameContent {
                line("Saved $name.svg and $name.png. Preview:")
                image(pngBytes, ChatCompletionImageType.PNG)
            }
        } catch (e: Exception) {
            dataFrameContent {
                line("Saved $name.svg but render to PNG failed: ${e.message}")
                line("SVG source (first 500 chars):")
                text(svgContent.take(500))
            }
        }
    }

    @AgentToolMethod(description = "Show the SVG source code for a given name")
    suspend fun showSource(
        @AgentToolParameter(description = "SVG name")
        name: String
    ): String {
        val content = temporaryFiles.getContent("$name.svg")
            ?: return "SVG '$name' not found."
        return String(content, Charsets.UTF_8)
    }

    @AgentToolMethod(description = "List all SVG names created in this session")
    fun list(): String {
        if (names.isEmpty()) return "No SVGs. Use svg_write to create one."
        return names.joinToString("\n") { "- $it ($it.svg, $it.png)" }
    }

    override fun render(): List<DataFrame.ContentPart> = dataFrameContent {
        if (names.isEmpty()) {
            text("SVG app is open. No SVGs yet.")
        } else {
            line("SVG app is open. Files:")
            for (name in names) {
                line("  - $name.svg, $name.png")
            }
        }
    }

    override fun getAvailableAgentToolMethods(): Collection<KFunction<*>> =
        listOf(::write, ::showSource, ::list)
}

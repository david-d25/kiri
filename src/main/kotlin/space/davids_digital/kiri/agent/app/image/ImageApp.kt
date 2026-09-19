package space.davids_digital.kiri.agent.app.image

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.app.AgentApp
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.DataFrameUtils.getImageType
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolParameter
import space.davids_digital.kiri.integration.google.GoogleGenAiImageService
import space.davids_digital.kiri.integration.openai.OpenaiImageService
import space.davids_digital.kiri.service.TemporaryFilesService

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@AgentToolNamespace("image")
class ImageApp(
    private val openaiImageService: OpenaiImageService,
    private val googleGenAiImageService: GoogleGenAiImageService,
    private val temporaryFiles: TemporaryFilesService,
) : AgentApp("image") {
    @AgentToolMethod(description = "Generate or edit an image using gpt-image-2.5 model. " +
            "Cannot reference images from the internet. " +
            "Generation may take several minutes.")
    suspend fun openaiGenerate(
        @AgentToolParameter(description = "Input prompt, max 32000 characters.")
        prompt: String,

        @AgentToolParameter(
            description = "Optional visual references; up to 16 images, each must be png/webp/jpg, less than 50 MB"
        )
        referenceImages: List<String>,

        @AgentToolParameter(description = "Model variant: SUNBURST (default) for best quality, " +
                "FLARE for faster and cheaper generation at roughly gpt-image-2 quality")
        model: OpenaiImageService.Model = OpenaiImageService.Model.SUNBURST,

        @AgentToolParameter(description = "Rendering quality, AUTO by default. XHIGH and MAX take noticeably longer.")
        quality: OpenaiImageService.Quality = OpenaiImageService.Quality.AUTO,

        @AgentToolParameter(description = "'auto' (default) or an explicit 'WIDTHxHEIGHT', e.g. '1536x1024'. " +
                "Both sides must be multiples of 16 and at most 3840, the total must be between 0.65 and 8.3 " +
                "megapixels, and the aspect ratio must stay within 1:3..3:1.")
        size: String = OpenaiImageService.SIZE_AUTO,

        @AgentToolParameter(description = "Background handling: AUTO (default) lets the model decide, " +
                "OPAQUE forces a filled background, TRANSPARENT leaves an alpha channel around the subject " +
                "(requires PNG or WEBP output).")
        background: OpenaiImageService.Background = OpenaiImageService.Background.AUTO,

        @AgentToolParameter(description = "Output encoding: PNG (default) and WEBP keep the alpha channel, " +
                "JPEG drops it.")
        outputFormat: OpenaiImageService.OutputFormat = OpenaiImageService.OutputFormat.PNG,
    ): List<DataFrame.ContentPart> {
        val bytes = if (referenceImages.isEmpty()) {
            openaiImageService.generate(prompt, model, quality, size, background, outputFormat)
        } else {
            val imageContents = referenceImages.map {
                temporaryFiles.getContent(it) ?: error("file '$it' not found")
            }
            openaiImageService.edit(imageContents, prompt, model, quality, size, background, outputFormat).first()
        }
        val imageType = bytes.getImageType()
        val fileName = "img${System.currentTimeMillis()}.${(imageType ?: outputFormat.imageType).extension}"
        temporaryFiles.create(fileName, bytes)

        return dataFrameContent {
            line("Image saved as temporary file: $fileName")
            if (background == OpenaiImageService.Background.TRANSPARENT) {
                line("Transparency survives only if the file is sent as a document; photos are re-encoded as JPEG.")
            }
            if (imageType == null) {
                line("Error: Unable to determine image type")
            } else {
                image(bytes, imageType)
            }
        }
    }

    @AgentToolMethod(description = "Generate an image using Gemini model. Can reference images from the internet. " +
            "Generation may take about a minute.")
    suspend fun geminiGenerate(
        @AgentToolParameter(description = "Input prompt to feed into model, " +
                "prefer to describe the whole scene rather than individual tags")
        prompt: String,
        @AgentToolParameter(description = "Optional visual references")
        referenceImages: List<String>
    ): List<DataFrame.ContentPart> {
        val imageContents = referenceImages.map { temporaryFiles.getContent(it) ?: error("file '$it' not found") }
        val results = googleGenAiImageService.generate(prompt, imageContents)
        val names = List(results.size) { index -> "img${System.currentTimeMillis()}${index}.png" }
        val createdFiles = results.mapIndexed { index, bytes ->
            val fileName = names[index]
            temporaryFiles.create(fileName, bytes)
        }
        return dataFrameContent {
            line("Images saved as temporary files: ${names.joinToString(", ")}")
            if (createdFiles.size <= 2) {
                for (bytes in results) {
                    val imageType = bytes.getImageType()
                    if (imageType == null) {
                        line("Error: Unable to determine image type")
                    } else {
                        image(bytes, imageType)
                    }
                }
            }
        }
    }

    override fun getAvailableAgentToolMethods() = listOf(::openaiGenerate, ::geminiGenerate)
}

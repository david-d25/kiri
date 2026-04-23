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
    @AgentToolMethod(description = "Generate or edit an image using gpt-image-2 model (current SOTA, best quality). " +
            "If referenceImages is empty, generates from scratch; otherwise edits/combines the given reference " +
            "images according to the prompt (up to 16 images, each must be png/webp/jpg, less than 50 MB). " +
            "Cannot reference images from the internet (use geminiGenerate if you need that). " +
            "Generation may take several minutes.")
    suspend fun openaiGenerate(
        @AgentToolParameter(description = "Input prompt, max 32000 characters.")
        prompt: String,
        @AgentToolParameter(description = "List of image filenames to edit/combine as reference. " +
                "Leave empty for pure text-to-image generation.")
        referenceImages: List<String>
    ): List<DataFrame.ContentPart> {
        val bytes = if (referenceImages.isEmpty()) {
            openaiImageService.generate(prompt)
        } else {
            val imageContents = referenceImages.map {
                temporaryFiles.getContent(it) ?: error("file '$it' not found")
            }
            openaiImageService.edit(imageContents, prompt).first()
        }
        val fileName = "img${System.currentTimeMillis()}.png"
        temporaryFiles.create(fileName, bytes)

        return dataFrameContent {
            line("Image saved as temporary file: $fileName")
            val imageType = bytes.getImageType()
            if (imageType == null) {
                line("Error: Unable to determine image type")
            } else {
                image(bytes, imageType)
            }
        }
    }

    @AgentToolMethod(description = "Generate an image using Gemini model. Can google and reference images from " +
            "the internet (unlike openaiGenerate), but overall quality is lower than gpt-image-2 (current SOTA). " +
            "Generation may take several minutes.")
    suspend fun geminiGenerate(
        @AgentToolParameter(description = "Input prompt to feed into model, " +
                "prefer to describe the whole scene rather than individual tags")
        prompt: String,
        @AgentToolParameter(description = "List of image filenames to feed into model as reference, if needed")
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

    override fun render() = dataFrameContent {
        line("Image toolbox is open")
    }

    override fun getAvailableAgentToolMethods() = listOf(::openaiGenerate, ::geminiGenerate)
}

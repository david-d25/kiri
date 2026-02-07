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
import kotlin.collections.map

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@AgentToolNamespace("image")
class ImageApp(
    private val openaiImageService: OpenaiImageService,
    private val googleGenAiImageService: GoogleGenAiImageService,
    private val temporaryFiles: TemporaryFilesService,
) : AgentApp("image") {
    @AgentToolMethod(description = "Generate an image using gpt-image-1.5 model. Generation may take several minutes.")
    suspend fun openaiGenerate(
        @AgentToolParameter(description = "Input prompt, max 32000 characters.")
        prompt: String
    ): List<DataFrame.ContentPart> {
        val bytes = openaiImageService.generate(prompt)
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

    @AgentToolMethod(description = "Generate an image using Gemini model. Generation may take several minutes.")
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

//    @AgentToolMethod(
//        description = "Creates an edited or extended image given one or more source images and a prompt, " +
//                "uses gpt-image-1.5 model"
//    )
//    suspend fun openaiEdit(
//        @AgentToolParameter(description = "The image to edit as filename, less than 50 MB")
//        image: String,

//        @AgentToolParameter(description = "A text description of the desired image, max 32000 characters. " +
//                "For example: 'Create a lovely gift basket with these four items in it'")
//
//        prompt: String,
//
//        @AgentToolParameter(description = "The number of images to generate, between 1 and 10, recommended to not exceed 4")
//        n: Long = 1
//    ): List<DataFrame.ContentPart> {
//        val file = temporaryFiles.getContent(image)
//            ?: throw IllegalArgumentException("File '$image' not found, maybe it has been expired")
//        val editedImages = service.edit(file, prompt, n)
//        val createdFiles = editedImages.map { bytes ->
//            val fileName = "img${System.currentTimeMillis()}.png"
//            temporaryFiles.create(fileName, bytes)
//        }
//        return dataFrameContent {
//            line("Edited images saved as temporary files: ${createdFiles.joinToString(", ")}")
//            for (bytes in editedImages) {
//                val imageType = bytes.getImageType()
//                if (imageType == null) {
//                    line("Error: Unable to determine image type")
//                } else {
//                    image(bytes, imageType)
//                }
//            }
//        }
//    }

    override fun render() = dataFrameContent {
        line("Image toolbox is open")
    }

    override fun getAvailableAgentToolMethods() = listOf(::openaiGenerate, ::geminiGenerate)
}

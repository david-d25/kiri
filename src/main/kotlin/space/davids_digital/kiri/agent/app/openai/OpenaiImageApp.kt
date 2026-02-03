package space.davids_digital.kiri.agent.app.openai

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
import space.davids_digital.kiri.integration.openai.OpenaiImageService
import space.davids_digital.kiri.service.TemporaryFilesService

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@AgentToolNamespace("openaiImage")
class OpenaiImageApp(
    private val service: OpenaiImageService,
    private val temporaryFiles: TemporaryFilesService,
) : AgentApp("openaiImage") {
    @AgentToolMethod(description = "Generate an image using gpt-image-1.5 model. Generation may take several minutes.")
    suspend fun generate(
        @AgentToolParameter(description = "A text description of the desired image, max 32000 characters")
        prompt: String
    ): List<DataFrame.ContentPart> {
        val bytes = service.generate(prompt)
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

//    @AgentToolMethod(
//        description = "Creates an edited or extended image given one or more source images and a prompt, " +
//                "uses gpt-image-1.5 model"
//    )
//    suspend fun edit(
//        @AgentToolParameter(description = "The image to edit as filename, less than 50 MB")
//        image: String,
//
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
        line("OpenAI Image toolbox is open")
    }

    override fun getAvailableAgentToolMethods() = listOf(::generate)

}
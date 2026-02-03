package space.davids_digital.kiri.integration.openai

import com.openai.models.images.ImageEditParams
import com.openai.models.images.ImageGenerateParams
import com.openai.models.images.ImageModel
import com.openai.models.images.ImagesResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Base64
import kotlin.jvm.optionals.getOrNull

@Service
class OpenaiImageService (
    private val clientHolder: OpenaiClientHolder,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Generate an image from a text prompt using `gpt-image-1.5` model.
     * Returns the image as a byte array in PNG format.
     *
     * @param prompt The text prompt to generate the image from, max 32000 characters.
     * @return The generated image as a byte array in PNG format.
     */
    fun generate(prompt: String): ByteArray {
        log.info("Generating image with gpt-image-1.5, prompt: $prompt")
        val client = clientHolder.requireClient()
        val response = client.images().generate(
            ImageGenerateParams.builder()
                .model(ImageModel.GPT_IMAGE_1_5)
                .prompt(prompt)
                .moderation(ImageGenerateParams.Moderation.LOW)
                .build()
        )
        val images = getImages(response)
        require(images.isNotEmpty()) { "No images returned" }
        if (images.size > 1) {
            log.warn("Multiple images returned, using the first one")
        }
        return images.first()
    }

    /**
     * Creates an edited or extended image given one or more source images and a prompt using `gpt-image-1.5` model.
     *
     * @param image The image to edit, should be `png`, `webp`, or `jpg` file less than 50 MB
     * images.
     * @param prompt A text description of the desired image(s), max 32000 characters.
     * @param n The number of images to generate. Must be between 1 and 10.
     */
    fun edit(image: ByteArray, prompt: String, n: Long = 1): List<ByteArray> {
        require(image.isNotEmpty()) { "images array must not be empty" }
        require(n in 1..10) { "n must be between 1 and 10" }
        log.info("Editing image(s) with gpt-image-1.5, ${image.size} images, prompt: '$prompt', n: $n")
        val client = clientHolder.requireClient()
        val response = client.images().edit(
            ImageEditParams.builder()
                .model(ImageModel.GPT_IMAGE_1_5)
                .prompt(prompt)
                .image(image)
                .n(n)
                .build()
        )
        val editedImages = getImages(response)
        log.info("Received ${editedImages.size} images")
        return editedImages
    }

    private fun getImages(response: ImagesResponse): List<ByteArray> {
        val images = response.data().getOrNull() ?: error("No image data in image response")
        require(images.isNotEmpty()) { "No images returned" }
        return images.map { imageData ->
            val b64 = imageData.b64Json().getOrNull() ?: error("No image data in image response")
            Base64.getDecoder().decode(b64)
        }
    }
}
package space.davids_digital.kiri.integration.openai

import com.openai.core.MultipartField
import com.openai.models.images.ImageEditParams
import com.openai.models.images.ImageGenerateParams
import com.openai.models.images.ImageModel
import com.openai.models.images.ImagesResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.agent.frame.DataFrameUtils.getImageType
import space.davids_digital.kiri.llm.ChatCompletionImageType
import java.util.Base64
import kotlin.jvm.optionals.getOrNull

@Service
class OpenaiImageService (
    private val clientHolder: OpenaiClientHolder,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Generate an image from a text prompt using `gpt-image-2` model.
     * Returns the image as a byte array in PNG format.
     *
     * @param prompt The text prompt to generate the image from, max 32000 characters.
     * @return The generated image as a byte array in PNG format.
     */
    fun generate(prompt: String): ByteArray {
        log.info("Generating image with gpt-image-2, prompt: $prompt")
        val client = clientHolder.requireClient()
        val response = client.images().generate(
            ImageGenerateParams.builder()
                .model(ImageModel.of("gpt-image-2"))
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
     * Creates an edited or extended image given one or more source images and a prompt using `gpt-image-2` model.
     *
     * @param images The source images as byte arrays. Each image must be a `png`, `webp`, or `jpg` file less than 50 MB.
     * Up to 16 images are allowed.
     * @param prompt A text description of the desired image(s), max 32000 characters.
     * @param n The number of images to generate. Must be between 1 and 10.
     */
    fun edit(images: List<ByteArray>, prompt: String, n: Long = 1): List<ByteArray> {
        require(images.isNotEmpty()) { "images list must not be empty" }
        require(images.size <= 16) { "up to 16 images allowed for GPT image models" }
        require(n in 1..10) { "n must be between 1 and 10" }
        log.info("Editing image(s) with gpt-image-2, count: ${images.size}, prompt: '$prompt', n: $n")
        val client = clientHolder.requireClient()

        val imageField = buildImageField(images)

        val response = client.images().edit(
            ImageEditParams.builder()
                .model(ImageModel.of("gpt-image-2"))
                .prompt(prompt)
                .image(imageField)
                .n(n)
                .build()
        )
        val editedImages = getImages(response)
        log.info("Received ${editedImages.size} images")
        return editedImages
    }

    /**
     * Builds a multipart field for the `image` parameter of `/v1/images/edits` with proper per-image filenames
     * and content types. OpenAI's multipart endpoint requires a valid filename with a recognized extension;
     * passing raw bytes without a filename causes the request to fail.
     */
    private fun buildImageField(images: List<ByteArray>): MultipartField<ImageEditParams.Image> {
        val streams = images.map { it.inputStream() }
        val value = if (images.size == 1) {
            ImageEditParams.Image.ofInputStream(streams.single())
        } else {
            ImageEditParams.Image.ofInputStreams(streams)
        }
        // Use the first image's detected type for content-type/filename. OpenAI only needs a valid extension;
        // when multiple images are sent, each is serialized as its own part under this shared filename.
        val type = images.first().getImageType() ?: ChatCompletionImageType.PNG
        return MultipartField.builder<ImageEditParams.Image>()
            .value(value)
            .contentType(type.mimeType())
            .filename("image.${type.extension()}")
            .build()
    }

    private fun ChatCompletionImageType.mimeType(): String = when (this) {
        ChatCompletionImageType.JPEG -> "image/jpeg"
        ChatCompletionImageType.PNG -> "image/png"
        ChatCompletionImageType.GIF -> "image/gif"
        ChatCompletionImageType.WEBP -> "image/webp"
    }

    private fun ChatCompletionImageType.extension(): String = when (this) {
        ChatCompletionImageType.JPEG -> "jpg"
        ChatCompletionImageType.PNG -> "png"
        ChatCompletionImageType.GIF -> "gif"
        ChatCompletionImageType.WEBP -> "webp"
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

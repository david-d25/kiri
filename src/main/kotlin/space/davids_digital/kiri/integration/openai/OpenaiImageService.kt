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
import kotlin.math.max
import kotlin.math.min

@Service
class OpenaiImageService (
    private val clientHolder: OpenaiClientHolder,
) {
    companion object {
        const val SIZE_AUTO = "auto"

        private const val MAX_EDGE = 3840
        private const val EDGE_MULTIPLE = 16
        private const val MIN_TOTAL_PIXELS = 655_360L
        private const val MAX_TOTAL_PIXELS = 8_294_400L
        private const val MAX_ASPECT_RATIO = 3.0

        private val SIZE_PATTERN = Regex("(\\d+)x(\\d+)")

        /**
         * Checks a resolution against the GPT Image 2.5 limits locally, so a bad value fails right away instead of
         * costing a round trip that ends in a 400.
         */
        internal fun validateSize(size: String) {
            if (size == SIZE_AUTO) {
                return
            }
            val match = SIZE_PATTERN.matchEntire(size)
            requireNotNull(match) { "size must be '$SIZE_AUTO' or 'WIDTHxHEIGHT', got '$size'" }
            val width = match.groupValues[1].toInt()
            val height = match.groupValues[2].toInt()
            require(width % EDGE_MULTIPLE == 0 && height % EDGE_MULTIPLE == 0) {
                "both sides must be multiples of $EDGE_MULTIPLE, got ${width}x$height"
            }
            require(width <= MAX_EDGE && height <= MAX_EDGE) {
                "neither side may exceed $MAX_EDGE, got ${width}x$height"
            }
            val totalPixels = width.toLong() * height.toLong()
            require(totalPixels in MIN_TOTAL_PIXELS..MAX_TOTAL_PIXELS) {
                "total pixel count must be between $MIN_TOTAL_PIXELS and $MAX_TOTAL_PIXELS, got $totalPixels"
            }
            require(max(width, height).toDouble() / min(width, height) <= MAX_ASPECT_RATIO) {
                "aspect ratio must be within 1:3 and 3:1, got ${width}x$height"
            }
        }

        /**
         * `output_compression` is deliberately never sent: it only applies to the lossy encodings and smears exactly
         * the alpha edges a transparent background is asked for.
         */
        internal fun validateAlpha(background: Background, outputFormat: OutputFormat) {
            require(background != Background.TRANSPARENT || outputFormat.supportsAlpha) {
                "transparent background requires an output format with an alpha channel, " +
                        "${outputFormat.apiValue} has none"
            }
        }
    }

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * GPT Image 2.5 variants. They take the same parameters and differ in the quality/latency trade-off.
     */
    enum class Model(val apiName: String) {
        FLARE("gpt-image-2.5-flare"),
        SUNBURST("gpt-image-2.5-sunburst"),
    }

    enum class Quality(val apiValue: String) {
        AUTO("auto"),
        LOW("low"),
        MEDIUM("medium"),
        HIGH("high"),
        XHIGH("xhigh"),
        MAX("max"),
    }

    enum class Background(val apiValue: String) {
        AUTO("auto"),
        OPAQUE("opaque"),
        TRANSPARENT("transparent"),
    }

    enum class OutputFormat(val apiValue: String, val imageType: ChatCompletionImageType) {
        PNG("png", ChatCompletionImageType.PNG),
        WEBP("webp", ChatCompletionImageType.WEBP),
        JPEG("jpeg", ChatCompletionImageType.JPEG);

        val supportsAlpha get() = this != JPEG
    }

    /**
     * Generate an image from a text prompt.
     * Returns the image as a byte array encoded according to [outputFormat].
     *
     * @param prompt The text prompt to generate the image from, max 32000 characters.
     * @param model The GPT Image 2.5 variant to use.
     * @param quality Rendering quality; higher levels cost more and take longer.
     * @param size [SIZE_AUTO] or an explicit `WIDTHxHEIGHT` resolution, see [validateSize].
     * @param background Whether the background is filled or left transparent.
     * @param outputFormat Encoding of the returned image; only PNG and WEBP carry an alpha channel.
     */
    fun generate(
        prompt: String,
        model: Model = Model.SUNBURST,
        quality: Quality = Quality.AUTO,
        size: String = SIZE_AUTO,
        background: Background = Background.AUTO,
        outputFormat: OutputFormat = OutputFormat.PNG,
    ): ByteArray {
        validateSize(size)
        validateAlpha(background, outputFormat)
        log.info(
            "Generating image with ${model.apiName}, quality: ${quality.apiValue}, size: $size, " +
                    "background: ${background.apiValue}, format: ${outputFormat.apiValue}, prompt: $prompt"
        )
        val client = clientHolder.requireClient()
        val response = client.images().generate(
            ImageGenerateParams.builder()
                .model(ImageModel.of(model.apiName))
                .prompt(prompt)
                .quality(ImageGenerateParams.Quality.of(quality.apiValue))
                .size(ImageGenerateParams.Size.of(size))
                .background(ImageGenerateParams.Background.of(background.apiValue))
                .outputFormat(ImageGenerateParams.OutputFormat.of(outputFormat.apiValue))
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
     * Creates an edited or extended image given one or more source images and a prompt.
     *
     * GPT Image 2.5 always reads the source images at high fidelity, so `input_fidelity` is intentionally left unset.
     *
     * @param images The source images as byte arrays. Each image must be a `png`, `webp`, or `jpg` file less than
     * 50 MB. Up to 16 images are allowed; alpha channels of the sources are passed through untouched.
     * @param prompt A text description of the desired image(s), max 32000 characters.
     * @param model The GPT Image 2.5 variant to use.
     * @param quality Rendering quality; higher levels cost more and take longer.
     * @param size [SIZE_AUTO] or an explicit `WIDTHxHEIGHT` resolution, see [validateSize].
     * @param background Whether the background is filled or left transparent.
     * @param outputFormat Encoding of the returned images; only PNG and WEBP carry an alpha channel.
     * @param n The number of images to generate. Must be between 1 and 10.
     */
    fun edit(
        images: List<ByteArray>,
        prompt: String,
        model: Model = Model.SUNBURST,
        quality: Quality = Quality.AUTO,
        size: String = SIZE_AUTO,
        background: Background = Background.AUTO,
        outputFormat: OutputFormat = OutputFormat.PNG,
        n: Long = 1,
    ): List<ByteArray> {
        require(images.isNotEmpty()) { "images list must not be empty" }
        require(images.size <= 16) { "up to 16 images allowed for GPT image models" }
        require(n in 1..10) { "n must be between 1 and 10" }
        validateSize(size)
        validateAlpha(background, outputFormat)
        log.info(
            "Editing image(s) with ${model.apiName}, count: ${images.size}, quality: ${quality.apiValue}, " +
                    "size: $size, background: ${background.apiValue}, format: ${outputFormat.apiValue}, " +
                    "prompt: '$prompt', n: $n"
        )
        val client = clientHolder.requireClient()

        val imageField = buildImageField(images)

        val response = client.images().edit(
            ImageEditParams.builder()
                .model(ImageModel.of(model.apiName))
                .prompt(prompt)
                .image(imageField)
                .quality(ImageEditParams.Quality.of(quality.apiValue))
                .size(ImageEditParams.Size.of(size))
                .background(ImageEditParams.Background.of(background.apiValue))
                .outputFormat(ImageEditParams.OutputFormat.of(outputFormat.apiValue))
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
            .contentType(type.mimeType)
            .filename("image.${type.extension}")
            .build()
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

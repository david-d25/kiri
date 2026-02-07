package space.davids_digital.kiri.integration.google

import com.google.genai.types.*
import org.springframework.stereotype.Service

@Service
class GoogleGenAiImageService (
    private val clientHolder: GoogleGenAiClientHolder
) {
    fun generate(
        prompt: String,
        referenceImages: List<ByteArray>
    ): List<ByteArray> {
        require(referenceImages.size <= 14) { "A maximum of 14 reference images can be provided" }

        val client = clientHolder.requireClient()
        val config = GenerateContentConfig.builder()
            .responseModalities("IMAGE")
            .imageConfig(
                ImageConfig.builder()
                    .imageSize("2K")
                    .build()
            )
            .tools(Tool.builder()
                .googleSearch(GoogleSearch.builder().build())
                .build())
            .build()

        val response: GenerateContentResponse = client.models.generateContent(
            "gemini-3-pro-image-preview",
            Content.fromParts(
                *buildList {
                    add(Part.fromText(prompt))
                    addAll(referenceImages.map { Part.fromBytes(it, "image/png") })
                }.toTypedArray()
            ),
            config
        )

        return buildList {
            for (part in response.parts()!!) {
                if (part.inlineData().isPresent) {
                    val blob = part.inlineData().get()
                    if (blob.data().isPresent) {
                        add(blob.data().get())
                    }
                }
            }
        }
    }
}
package space.davids_digital.kiri.agent.frame

import space.davids_digital.kiri.llm.ChatCompletionImageType

abstract class DataFrame : Frame() {
    abstract val tag: String
    abstract val attributes: Map<String, String>
    abstract fun renderContent(): List<ContentPart>

    sealed class ContentPart

    data class Text(val text: String) : ContentPart()
    data class Image(val data: ByteArray, val type: ChatCompletionImageType) : ContentPart()
}
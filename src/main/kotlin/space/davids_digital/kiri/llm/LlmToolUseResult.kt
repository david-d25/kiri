package space.davids_digital.kiri.llm

data class LlmToolUseResult(val toolUseId: String, val output: Output) {
    sealed class Output {
        data class Text(val text: String) : Output()
        data class Image(val data: ByteArray, val mediaType: LlmImageType) : Output()
    }
}
package space.davids_digital.kiri.llm

enum class ChatCompletionImageType(val mimeType: String, val extension: String) {
    JPEG("image/jpeg", "jpg"),
    PNG("image/png", "png"),
    GIF("image/gif", "gif"),
    WEBP("image/webp", "webp"),
}

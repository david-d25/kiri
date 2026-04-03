package space.davids_digital.kiri.integration.telegram

sealed class TelegramOutgoingAttachment {
    data class Photo(val data: ByteArray) : TelegramOutgoingAttachment()
    data class Document(val data: ByteArray, val name: String) : TelegramOutgoingAttachment()
}

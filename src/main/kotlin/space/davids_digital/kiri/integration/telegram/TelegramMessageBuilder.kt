package space.davids_digital.kiri.integration.telegram

import space.davids_digital.kiri.model.telegram.TelegramInlineKeyboardMarkup
import space.davids_digital.kiri.model.telegram.TelegramMessageEntity

class TelegramMessageBuilder {
    var text: String = ""
    var entities: List<TelegramMessageEntity> = emptyList()
    val attachments: MutableList<TelegramOutgoingAttachment> = mutableListOf()
    var replyMarkup: TelegramInlineKeyboardMarkup? = null
    var disableNotification: Boolean = false
    var messageThreadId: Int? = null
    var replyToMessageId: Int? = null

    fun html(html: String) {
        val parsed = TelegramHtmlMapper.fromHtml(html)
        text = parsed.text
        entities = parsed.entities
    }

    fun photo(data: ByteArray) {
        attachments.add(TelegramOutgoingAttachment.Photo(data))
    }

    fun document(data: ByteArray, name: String) {
        attachments.add(TelegramOutgoingAttachment.Document(data, name))
    }
}

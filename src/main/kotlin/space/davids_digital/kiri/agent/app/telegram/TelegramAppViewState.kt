package space.davids_digital.kiri.agent.app.telegram

import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.model.telegram.TelegramMessage

data class TelegramAppViewState (
    val chatsView: List<TelegramChat> = emptyList(),
    val chatsPage: Int = 0,
    val chatsTotalPages: Int = 0,
    val openedChat: TelegramChat? = null,
    val messagesView: List<TelegramMessage> = emptyList(),
    val newerMessagesRemaining: Long = 0,
    val oldLastReadMessageId: Int? = null
)
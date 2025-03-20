package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_switch_inline_query_chosen_chat")
class TelegramSwitchInlineQueryChosenChatEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "query")
    var query: String? = null

    @Column(name = "allow_user_chats")
    var allowUserChats: Boolean? = null

    @Column(name = "allow_bot_chats")
    var allowBotChats: Boolean? = null

    @Column(name = "allow_group_chats")
    var allowGroupChats: Boolean? = null

    @Column(name = "allow_channel_chats")
    var allowChannelChats: Boolean? = null
}

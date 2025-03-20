package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_inline_keyboard_buttons")
class TelegramInlineKeyboardButtonEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "text", nullable = false)
    var text: String = ""

    @Column(name = "url")
    var url: String? = null

    @Column(name = "callback_data")
    var callbackData: String? = null

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "web_app_id", referencedColumnName = "internal_id")
    var webApp: TelegramWebAppInfoEntity? = null

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "login_url_id", referencedColumnName = "internal_id")
    var loginUrl: TelegramLoginUrlEntity? = null

    @Column(name = "switch_inline_query")
    var switchInlineQuery: String? = null

    @Column(name = "switch_inline_query_current_chat")
    var switchInlineQueryCurrentChat: String? = null

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "switch_inline_query_chosen_chat_id", referencedColumnName = "internal_id")
    var switchInlineQueryChosenChat: TelegramSwitchInlineQueryChosenChatEntity? = null

    @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
    @JoinColumn(name = "copy_text_button_id", referencedColumnName = "internal_id")
    var copyTextButton: TelegramCopyTextButtonEntity? = null

    @Column(name = "pay", nullable = false)
    var pay: Boolean = false
}

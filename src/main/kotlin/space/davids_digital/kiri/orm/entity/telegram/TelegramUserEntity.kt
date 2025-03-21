package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_users")
class TelegramUserEntity {
    @Id
    @Column(name = "id")
    var id: Long = 0

    @Column(name = "is_bot")
    var bot: Boolean = false

    @Column(name = "first_name")
    var firstName: String = ""

    @Column(name = "last_name")
    var lastName: String? = null

    @Column(name = "username")
    var username: String? = null

    @Column(name = "language_code")
    var languageCode: String? = null

    @Column(name = "is_premium")
    var premium: Boolean = false

    @Column(name = "added_to_attachment_menu")
    var addedToAttachmentMenu: Boolean = false

    @Column(name = "can_join_groups")
    var canJoinGroups: Boolean = false

    @Column(name = "can_read_all_group_messages")
    var canReadAllGroupMessages: Boolean = false

    @Column(name = "supports_inline_queries")
    var supportsInlineQueries: Boolean = false

    @Column(name = "can_connect_to_business")
    var canConnectToBusiness: Boolean = false

    @Column(name = "has_main_web_app")
    var hasMainWebApp: Boolean = false
}

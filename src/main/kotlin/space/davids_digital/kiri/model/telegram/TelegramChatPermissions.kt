package space.davids_digital.kiri.model.telegram

/**
 * Describes actions that a non-administrator user is allowed to take in a chat.
 */
data class TelegramChatPermissions (
    /**
     * True, if the user is allowed to send text messages, contacts, giveaways, giveaway winners, invoices, locations
     * and venues.
     */
    val canSendMessages: Boolean,
    /**
     * True, if the user is allowed to send audios.
     */
    val canSendAudios: Boolean,
    /**
     * True, if the user is allowed to send documents.
     */
    val canSendDocuments: Boolean,
    /**
     * True, if the user is allowed to send photos.
     */
    val canSendPhotos: Boolean,
    /**
     * True, if the user is allowed to send videos.
     */
    val canSendVideos: Boolean,
    /**
     * True, if the user is allowed to send video notes.
     */
    val canSendVideoNotes: Boolean,
    /**
     * True, if the user is allowed to send voice notes.
     */
    val canSendVoiceNotes: Boolean,
    /**
     * True, if the user is allowed to send polls.
     */
    val canSendPolls: Boolean,
    /**
     * True, if the user is allowed to send animations, games, stickers and use inline bots.
     */
    val canSendOtherMessages: Boolean,
    /**
     * True, if the user is allowed to add web page previews to their messages.
     */
    val canAddWebPagePreviews: Boolean,
    /**
     * True, if the user is allowed to change the chat title, photo and other settings. Ignored in public supergroups.
     */
    val canChangeInfo: Boolean,
    /**
     * True, if the user is allowed to invite new users to the chat.
     */
    val canInviteUsers: Boolean,
    /**
     * True, if the user is allowed to pin messages. Ignored in public supergroups.
     */
    val canPinMessages: Boolean,
    /**
     * True, if the user is allowed to create forum topics. If omitted defaults to the value of [canPinMessages].
     */
    val canManageTopics: Boolean,
)
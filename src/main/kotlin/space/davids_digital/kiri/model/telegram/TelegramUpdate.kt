package space.davids_digital.kiri.model.telegram

/**
 * Represents an incoming update.
 * At most one of the optional parameters can be present in any given update.
 */
data class TelegramUpdate (
    /**
     * The update's unique identifier.
     * Update identifiers start from a certain positive number and increase sequentially.
     * This identifier becomes especially handy if you're using
     * [webhooks](https://core.telegram.org/bots/api#setwebhook), since it allows you to ignore repeated
     * updates or to restore the correct update sequence, should they get out of order.
     * If there are no new updates for at least a week, then identifier of the next update will be chosen randomly
     * instead of sequentially.
     */
    val updateId: Int,

    /**
     * New incoming message of any kind - text, photo, sticker, etc.
     */
    val message: TelegramMessage? = null,

    /**
     * New version of a message that is known to the bot and was edited.
     * This update may at times be triggered by changes to message fields that are either unavailable or not actively
     * used by your bot.
     */
    val editedMessage: TelegramMessage? = null,

    /**
     * New incoming channel post of any kind - text, photo, sticker, etc.
     */
    val channelPost: TelegramMessage? = null,

    /**
     * New version of a channel post that is known to the bot and was edited.
     * This update may at times be triggered by changes to message fields that are either unavailable or not actively
     * used by your bot.
     */
    val editedChannelPost: TelegramMessage? = null,

    /**
     * The bot was connected to or disconnected from a business account,
     * or a user edited an existing connection with the bot
     */
    val businessConnection: TelegramBusinessConnection? = null,

    /**
     * New message from a connected business account
     */
    val businessMessage: TelegramMessage? = null,

    /**
     * New version of a message from a connected business account
     */
    val editedBusinessMessage: TelegramMessage? = null,

    /**
     * Messages were deleted from a connected business account
     */
    val deletedBusinessMessages: TelegramBusinessMessagesDeleted? = null,

    /**
     * A reaction to a message was changed by a user.
     * The bot must be an administrator in the chat and must explicitly specify `"message_reaction"` in the list of
     * _allowed_updates_ to receive these updates. The update isn't received for reactions set by bots.
     */
    val messageReaction: TelegramMessageReactionUpdated? = null,

    /**
     * Reactions to a message with anonymous reactions were changed.
     * The bot must be an administrator in the chat and must explicitly specify `"message_reaction_count"`
     * in the list of _allowed_updates_ to receive these updates.
     * The updates are grouped and can be sent with delay up to a few minutes.
     */
    val messageReactionCount: TelegramMessageReactionCountUpdated? = null,

    /**
     * New incoming [inline](https://core.telegram.org/bots/api#inline-mode) query
     */
    val inlineQuery: TelegramInlineQuery? = null,

    /**
     * The result of an [inline](https://core.telegram.org/bots/api#inline-mode) query that was chosen by a user and
     * sent to their chat partner.
     * Please see our documentation on the
     * [feedback collecting](https://core.telegram.org/bots/inline#collecting-feedback) for details on how to enable
     * these updates for your bot.
     */
    val chosenInlineResult: TelegramChosenInlineResult? = null,

    /**
     * New incoming callback query
     */
    val callbackQuery: TelegramCallbackQuery? = null,

    /**
     * New incoming shipping query. Only for invoices with flexible price
     */
    val shippingQuery: TelegramShippingQuery? = null,

    /**
     * New incoming pre-checkout query. Contains full information about checkout
     */
    val preCheckoutQuery: TelegramPreCheckoutQuery? = null,

    /**
     * A user purchased paid media with a non-empty payload sent by the bot in a non-channel chat
     */
    val purchasedPaidMedia: TelegramPaidMediaPurchased? = null,

    /**
     * New poll state. Bots receive only updates about manually stopped polls and polls, which are sent by the bot
     */
    val poll: TelegramPoll? = null,

    /**
     * A user changed their answer in a non-anonymous poll. Bots receive new votes only in polls that were sent by the
     * bot itself.
     */
    val pollAnswer: TelegramPollAnswer? = null,

    /**
     * The bot's chat member status was updated in a chat. For private chats, this update is received only when the
     * bot is blocked or unblocked by the user.
     */
    val myChatMember: TelegramChatMemberUpdated? = null,

    /**
     * A chat member's status was updated in a chat. The bot must be an administrator in the chat and must explicitly
     * specify `"chat_member"` in the list of _allowed_updates_ to receive these updates.
     */
    val chatMember: TelegramChatMemberUpdated? = null,

    /**
     * A request to join the chat has been sent. The bot must have the _can_invite_users_ administrator right in
     * the chat to receive these updates.
     */
    val chatJoinRequest: TelegramChatJoinRequest? = null,

    /**
     * A chat boost was added or changed. The bot must be an administrator in the chat to receive these updates.
     */
    val chatBoost: TelegramChatBoostUpdated? = null,

    /**
     * A boost was removed from a chat. The bot must be an administrator in the chat to receive these updates.
     */
    val removedChatBoost: TelegramChatBoostRemoved? = null,
)
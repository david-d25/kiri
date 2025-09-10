package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

data class TelegramMessage (
    override val chatId: Long,
    /**
     * Unique message identifier inside this chat.
     * In specific instances (e.g., message containing a video sent to a big chat), the server might automatically
     * schedule a message instead of sending it immediately. In such cases, this field will be 0 and the relevant
     * message will be unusable until it is actually sent.
     */
    override val messageId: Int,
    /**
     * Unique identifier of a message thread to which the message belongs; for supergroups only.
     */
    val messageThreadId: Int? = null,
    /**
     * Sender of the message; may be empty for messages sent to channels.
     * For backward compatibility, if the message was sent on behalf of a chat,
     * the field contains a fake sender user in non-channel chats.
     */
    val fromId: Long? = null,
    /**
     * Sender of the message when sent on behalf of a chat.
     * For example, the supergroup itself for messages sent by its anonymous administrators or a linked channel for
     * messages automatically forwarded to the channel's discussion group. For backward compatibility, if the message
     * was sent on behalf of a chat, the field [fromId] contains a fake sender user in non-channel chats.
     */
    val senderChatId: Long? = null,
    /**
     * If the sender of the message boosted the chat, the number of boosts added by the user.
     */
    val senderBoostCount: Int? = null,
    /**
     * The bot that actually sent the message on behalf of the business account.
     * Available only for outgoing messages sent on behalf of the connected business account.
     */
    val senderBusinessBotId: Long? = null,
    /**
     * Date the message was sent.
     */
    val date: ZonedDateTime,
    /**
     * Unique identifier of the business connection from which the message was received.
     * If non-empty, the message belongs to a chat of the corresponding business account that is independent of
     * any potential bot chat which might share the same identifier.
     */
    val businessConnectionId: String? = null,
    /**
     * Information about the original message for forwarded messages.
     */
    val forwardOrigin: TelegramMessageOrigin? = null,
    /**
     * True, if the message is sent to a forum topic.
     */
    val isTopicMessage: Boolean = false,
    /**
     * True, if the message is a channel post that was automatically forwarded to the connected discussion group.
     */
    val isAutomaticForward: Boolean = false,
    /**
     * For replies in the same chat and message thread, the original message. Note that the Message object in this field
     * will not contain further reply_to_message fields even if it itself is a reply.
     */
    val replyToMessage: TelegramMessage? = null,
    /**
     * Information about the message that is being replied to, which may come from another chat or forum topic.
     */
    val externalReplyInfo: TelegramExternalReplyInfo? = null,
    /**
     * For replies that quote part of the original message, the quoted part of the message.
     */
    val quote: TelegramTextQuote? = null,
    /**
     * For replies to a story, the original story.
     */
    val replyToStory: TelegramStory? = null,
    /**
     * Bot through which the message was sent.
     */
    val viaBot: TelegramUser? = null,
    /**
     * Date the message was last edited.
     */
    val editDate: ZonedDateTime? = null,
    /**
     * True, if the message can't be forwarded.
     */
    val hasProtectedContent: Boolean = false,
    /**
     * True, if the message was sent by an implicit action, for example, as an away or a greeting business message, or
     * as a scheduled message.
     */
    val isFromOffline: Boolean = false,
    /**
     *  The unique identifier of a media message group this message belongs to.
     */
    val mediaGroupId: String? = null,
    /**
     * Signature of the post author for messages in channels, or the custom title of an anonymous group administrator.
     */
    val authorSignature: String? = null,
    /**
     * For text messages, the actual UTF-8 text of the message.
     */
    val text: String? = null,
    /**
     * For text messages, special entities like usernames, URLs, bot commands, etc. that appear in the text.
     */
    val entities: List<TelegramMessageEntity> = emptyList(),
    /**
     * Options used for link preview generation for the message, if it is a text message and link preview options were
     * changed.
     */
    val linkPreviewOptions: TelegramLinkPreviewOptions? = null,
    /**
     * Unique identifier of the message effect added to the message.
     */
    val effectId: String? = null,
    /**
     * Message is an animation, information about the animation.
     * For backward compatibility, when this field is set, the [document] field will also be set.
     */
    val animation: TelegramAnimation? = null,
    /**
     * Message is an audio file, information about the file.
     */
    val audio: TelegramAudio? = null,
    /**
     * Message is a general file, information about the file.
     */
    val document: TelegramDocument? = null,
    /**
     * Message contains paid media; information about the paid media.
     */
    val paidMediaInfo: TelegramPaidMediaInfo? = null,
    /**
     * Message is a photo, available sizes of the photo.
     */
    val photo: List<TelegramPhotoSize> = emptyList(),
    /**
     * Message is a sticker, information about the sticker.
     */
    val sticker: TelegramSticker? = null,
    /**
     * Message is a forwarded story.
     */
    val story: TelegramStory? = null,
    /**
     * Message is a video, information about the video.
     */
    val video: TelegramVideo? = null,
    /**
     * Message is a [video note](https://telegram.org/blog/video-messages-and-telescope), information about the video
     * message.
     */
    val videoNote: TelegramVideoNote? = null,
    /**
     * Message is a voice message, information about the file.
     */
    val voice: TelegramVoice? = null,
    /**
     * Caption for the animation, audio, document, paid media, photo, video or voice.
     */
    val caption: String? = null,
    /**
     * For messages with a caption, special entities like usernames, URLs, bot commands, etc. that appear in the
     * caption.
     */
    val captionEntities: List<TelegramMessageEntity> = emptyList(),
    /**
     * True, if the caption must be shown above the message media.
     */
    val showCaptionAboveMedia: Boolean = false,
    /**
     * True, if the message media is covered by a spoiler animation.
     */
    val hasMediaSpoiler: Boolean = false,
    /**
     * Message is a shared contact, information about the contact.
     */
    val contact: TelegramContact? = null,
    /**
     * Message is a die with random value.
     */
    val dice: TelegramDice? = null,
    /**
     * Message is a game, information about the game.
     */
    val game: TelegramGame? = null,
    /**
     * Message is a native poll, information about the poll.
     */
    val poll: TelegramPoll? = null,
    /**
     * Message is a venue, information about the venue. For backward compatibility, when this field is set, the
     * [location] field will also be set.
     */
    val venue: TelegramVenue? = null,
    /**
     * Message is a shared location, information about the location.
     */
    val location: TelegramLocation? = null,
    /**
     * New members that were added to the group or supergroup and information about them (the bot itself may be one of
     * these members).
     */
    val newChatMembers: List<Long> = emptyList(),
    /**
     * A member was removed from the group, information about them (this member may be the bot itself).
     */
    val leftChatMemberId: Long? = null,
    /**
     * A chat title was changed to this value.
     */
    val newChatTitle: String? = null,
    /**
     * A chat photo was change to this value.
     */
    val newChatPhoto: List<TelegramPhotoSize> = emptyList(),
    /**
     * Service message: the chat photo was deleted.
     */
    val deleteChatPhoto: Boolean = false,
    /**
     * Service message: the group has been created.
     */
    val groupChatCreated: Boolean = false,
    /**
     * Service message: the supergroup has been created. This field can't be received in a message coming through
     * updates, because bot can't be a member of a supergroup when it is created. It can only be found in
     * reply_to_message if someone replies to a very first message in a directly created supergroup.
     */
    val supergroupChatCreated: Boolean = false,
    /**
     * Service message: the channel has been created. This field can't be received in a message coming through updates,
     * because bot can't be a member of a channel when it is created. It can only be found in reply_to_message if
     * someone replies to a very first message in a channel.
     */
    val channelChatCreated: Boolean = false,
    /**
     * Service message: auto-delete timer settings changed in the chat.
     */
    val messageAutoDeleteTimerChanged: TelegramMessageAutoDeleteTimerChanged? = null,
    /**
     * The group has been migrated to a supergroup with the specified identifier.
     */
    val migrateToChatId: Long? = null,
    /**
     * The supergroup has been migrated from a group with the specified identifier.
     */
    val migrateFromChatId: Long? = null,
    /**
     * Specified message was pinned. Note that the Message object in this field will not contain further
     * reply_to_message fields even if it itself is a reply.
     */
    val pinnedMessage: TelegramMaybeInaccessibleMessage? = null,
    /**
     * Message is an invoice for a payment, information about the invoice.
     */
    val invoice: TelegramInvoice? = null,
    /**
     * Message is a service message about a successful payment, information about the payment.
     */
    val successfulPayment: TelegramSuccessfulPayment? = null,
    /**
     * Message is a service message about a refunded payment, information about the payment.
     */
    val refundedPayment: TelegramRefundedPayment? = null,
    /**
     * Service message: users were shared with the bot.
     */
    val usersShared: TelegramUsersShared? = null,
    /**
     * Service message: a chat was shared with the bot.
     */
    val chatShared: TelegramChatShared? = null,
    /**
     * The domain name of the website on which the user has logged in.
     */
    val connectedWebsite: String? = null,
    /**
     * Service message: the user allowed the bot to write messages after adding it to the attachment or side menu,
     * launching a Web App from a link, or accepting an explicit request from a Web App sent by the method
     * [requestWriteAccess](https://core.telegram.org/bots/webapps#initializing-mini-apps).
     */
    val writeAccessAllowed: TelegramWriteAccessAllowed? = null,
    /**
     * Telegram Passport data.
     */
    val passportData: TelegramPassportData? = null,
    /**
     * Service message. A user in the chat triggered another user's proximity alert while sharing Live Location.
     */
    val proximityAlertTriggered: TelegramProximityAlertTriggered? = null,
    /**
     * Service message: user boosted the chat.
     */
    val chatBoostAdded: TelegramChatBoostAdded? = null,
    /**
     * Service message: chat background set.
     */
    val chatBackgroundSet: TelegramChatBackground? = null,
    /**
     * Service message: forum topic created.
     */
    val forumTopicCreated: TelegramForumTopicCreated? = null,
    /**
     * Service message: forum topic edited.
     */
    val forumTopicEdited: TelegramForumTopicEdited? = null,
    /**
     * Service message: forum topic closed.
     */
    val forumTopicClosed: TelegramForumTopicClosed? = null,
    /**
     * Service message: forum topic reopened.
     */
    val forumTopicReopened: TelegramForumTopicReopened? = null,
    /**
     * Service message: the 'General' forum topic hidden.
     */
    val generalForumTopicHidden: TelegramGeneralForumTopicHidden? = null,
    /**
     * Service message: the 'General' forum topic unhidden.
     */
    val generalForumTopicUnhidden: TelegramGeneralForumTopicUnhidden? = null,
    /**
     * Service message: a scheduled giveaway was created.
     */
    val giveawayCreated: TelegramGiveawayCreated? = null,
    /**
     * The message is a scheduled giveaway message.
     */
    val giveaway: TelegramGiveaway? = null,
    /**
     * A giveaway with public winners was completed.
     */
    val giveawayWinners: TelegramGiveawayWinners? = null,
    /**
     * Service message: a giveaway without public winners was completed.
     */
    val giveawayCompleted: TelegramGiveawayCompleted? = null,
    /**
     * Service message: video chat started.
     */
    val videoChatScheduled: TelegramVideoChatScheduled? = null,
    /**
     * Service message: video chat ended.
     */
    val videoChatEnded: TelegramVideoChatEnded? = null,
    /**
     * Service message: new participants invited to a video chat.
     */
    val videoChatParticipantsInvited: TelegramVideoChatParticipantsInvited? = null,
    /**
     * Service message: data sent by a Web App.
     */
    val webAppData: TelegramWebAppData? = null,
    /**
     * Inline keyboard attached to the message. `login_url` buttons are represented as ordinary `url` buttons.
     */
    val replyMarkup: TelegramInlineKeyboardMarkup? = null,
): TelegramMaybeInaccessibleMessage(chatId, messageId)
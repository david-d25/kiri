package space.davids_digital.kiri.integration.telegram

import com.pengrad.telegrambot.model.Animation
import com.pengrad.telegrambot.model.Audio
import com.pengrad.telegrambot.model.CallbackQuery
import com.pengrad.telegrambot.model.Chat
import com.pengrad.telegrambot.model.ChatFullInfo
import com.pengrad.telegrambot.model.ChatInviteLink
import com.pengrad.telegrambot.model.ChatJoinRequest
import com.pengrad.telegrambot.model.ChatLocation
import com.pengrad.telegrambot.model.ChatMember
import com.pengrad.telegrambot.model.ChatMemberUpdated
import com.pengrad.telegrambot.model.ChatPermissions
import com.pengrad.telegrambot.model.ChatPhoto
import com.pengrad.telegrambot.model.ChatShared
import com.pengrad.telegrambot.model.ChosenInlineResult
import com.pengrad.telegrambot.model.Contact
import com.pengrad.telegrambot.model.Dice
import com.pengrad.telegrambot.model.Document
import com.pengrad.telegrambot.model.File
import com.pengrad.telegrambot.model.ForumTopicClosed
import com.pengrad.telegrambot.model.ForumTopicCreated
import com.pengrad.telegrambot.model.ForumTopicEdited
import com.pengrad.telegrambot.model.ForumTopicReopened
import com.pengrad.telegrambot.model.Game
import com.pengrad.telegrambot.model.GeneralForumTopicHidden
import com.pengrad.telegrambot.model.GeneralForumTopicUnhidden
import com.pengrad.telegrambot.model.InlineQuery
import com.pengrad.telegrambot.model.Invoice
import com.pengrad.telegrambot.model.LinkPreviewOptions
import com.pengrad.telegrambot.model.Location
import com.pengrad.telegrambot.model.MaskPosition
import com.pengrad.telegrambot.model.Message
import com.pengrad.telegrambot.model.MessageAutoDeleteTimerChanged
import com.pengrad.telegrambot.model.MessageEntity
import com.pengrad.telegrambot.model.MessageReactionCountUpdated
import com.pengrad.telegrambot.model.MessageReactionUpdated
import com.pengrad.telegrambot.model.OrderInfo
import com.pengrad.telegrambot.model.PhotoSize
import com.pengrad.telegrambot.model.Poll
import com.pengrad.telegrambot.model.PollAnswer
import com.pengrad.telegrambot.model.PollOption
import com.pengrad.telegrambot.model.PreCheckoutQuery
import com.pengrad.telegrambot.model.ProximityAlertTriggered
import com.pengrad.telegrambot.model.RefundedPayment
import com.pengrad.telegrambot.model.ShippingAddress
import com.pengrad.telegrambot.model.ShippingQuery
import com.pengrad.telegrambot.model.Sticker
import com.pengrad.telegrambot.model.Story
import com.pengrad.telegrambot.model.SuccessfulPayment
import com.pengrad.telegrambot.model.TextQuote
import com.pengrad.telegrambot.model.Update
import com.pengrad.telegrambot.model.User
import com.pengrad.telegrambot.model.UsersShared
import com.pengrad.telegrambot.model.Venue
import com.pengrad.telegrambot.model.Video
import com.pengrad.telegrambot.model.VideoChatEnded
import com.pengrad.telegrambot.model.VideoChatParticipantsInvited
import com.pengrad.telegrambot.model.VideoChatScheduled
import com.pengrad.telegrambot.model.VideoNote
import com.pengrad.telegrambot.model.Voice
import com.pengrad.telegrambot.model.WebAppData
import com.pengrad.telegrambot.model.WebAppInfo
import com.pengrad.telegrambot.model.WriteAccessAllowed
import com.pengrad.telegrambot.model.business.BusinessConnection
import com.pengrad.telegrambot.model.business.BusinessMessageDeleted
import com.pengrad.telegrambot.model.chatbackground.BackgroundFill
import com.pengrad.telegrambot.model.chatbackground.BackgroundFillFreeformGradient
import com.pengrad.telegrambot.model.chatbackground.BackgroundFillGradient
import com.pengrad.telegrambot.model.chatbackground.BackgroundFillSolid
import com.pengrad.telegrambot.model.chatbackground.BackgroundType
import com.pengrad.telegrambot.model.chatbackground.BackgroundTypeChatTheme
import com.pengrad.telegrambot.model.chatbackground.BackgroundTypeFill
import com.pengrad.telegrambot.model.chatbackground.BackgroundTypePattern
import com.pengrad.telegrambot.model.chatbackground.BackgroundTypeWallpaper
import com.pengrad.telegrambot.model.chatbackground.ChatBackground
import com.pengrad.telegrambot.model.chatboost.ChatBoost
import com.pengrad.telegrambot.model.chatboost.ChatBoostAdded
import com.pengrad.telegrambot.model.chatboost.ChatBoostRemoved
import com.pengrad.telegrambot.model.chatboost.ChatBoostUpdated
import com.pengrad.telegrambot.model.chatboost.source.ChatBoostSource
import com.pengrad.telegrambot.model.chatboost.source.ChatBoostSourceGiftCode
import com.pengrad.telegrambot.model.chatboost.source.ChatBoostSourceGiveaway
import com.pengrad.telegrambot.model.chatboost.source.ChatBoostSourcePremium
import com.pengrad.telegrambot.model.giveaway.Giveaway
import com.pengrad.telegrambot.model.giveaway.GiveawayCompleted
import com.pengrad.telegrambot.model.giveaway.GiveawayCreated
import com.pengrad.telegrambot.model.giveaway.GiveawayWinners
import com.pengrad.telegrambot.model.message.InaccessibleMessage
import com.pengrad.telegrambot.model.message.MaybeInaccessibleMessage
import com.pengrad.telegrambot.model.message.origin.MessageOrigin
import com.pengrad.telegrambot.model.message.origin.MessageOriginChannel
import com.pengrad.telegrambot.model.message.origin.MessageOriginChat
import com.pengrad.telegrambot.model.message.origin.MessageOriginHiddenUser
import com.pengrad.telegrambot.model.message.origin.MessageOriginUser
import com.pengrad.telegrambot.model.paidmedia.PaidMedia
import com.pengrad.telegrambot.model.paidmedia.PaidMediaInfo
import com.pengrad.telegrambot.model.paidmedia.PaidMediaPhoto
import com.pengrad.telegrambot.model.paidmedia.PaidMediaPreview
import com.pengrad.telegrambot.model.paidmedia.PaidMediaPurchased
import com.pengrad.telegrambot.model.paidmedia.PaidMediaVideo
import com.pengrad.telegrambot.model.reaction.ReactionCount
import com.pengrad.telegrambot.model.reaction.ReactionType
import com.pengrad.telegrambot.model.reaction.ReactionTypeCustomEmoji
import com.pengrad.telegrambot.model.reaction.ReactionTypeEmoji
import com.pengrad.telegrambot.model.reaction.ReactionTypePaid
import com.pengrad.telegrambot.model.request.CallbackGame
import com.pengrad.telegrambot.model.request.CopyTextButton
import com.pengrad.telegrambot.model.request.InlineKeyboardButton
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup
import com.pengrad.telegrambot.model.request.LoginUrl
import com.pengrad.telegrambot.model.request.SwitchInlineQueryChosenChat
import com.pengrad.telegrambot.model.shared.SharedUser
import com.pengrad.telegrambot.passport.EncryptedCredentials
import com.pengrad.telegrambot.passport.EncryptedPassportElement
import com.pengrad.telegrambot.passport.PassportData
import com.pengrad.telegrambot.passport.PassportFile
import space.davids_digital.kiri.model.telegram.TelegramAnimation
import space.davids_digital.kiri.model.telegram.TelegramAudio
import space.davids_digital.kiri.model.telegram.TelegramBackgroundFill
import space.davids_digital.kiri.model.telegram.TelegramBackgroundType
import space.davids_digital.kiri.model.telegram.TelegramBusinessBotRights
import space.davids_digital.kiri.model.telegram.TelegramBusinessConnection
import space.davids_digital.kiri.model.telegram.TelegramBusinessMessagesDeleted
import space.davids_digital.kiri.model.telegram.TelegramCallbackGame
import space.davids_digital.kiri.model.telegram.TelegramCallbackQuery
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.model.telegram.TelegramChatBoost
import space.davids_digital.kiri.model.telegram.TelegramChatBoostAdded
import space.davids_digital.kiri.model.telegram.TelegramChatBoostRemoved
import space.davids_digital.kiri.model.telegram.TelegramChatBoostSource
import space.davids_digital.kiri.model.telegram.TelegramChatBoostUpdated
import space.davids_digital.kiri.model.telegram.TelegramChatInviteLink
import space.davids_digital.kiri.model.telegram.TelegramChatJoinRequest
import space.davids_digital.kiri.model.telegram.TelegramChatLocation
import space.davids_digital.kiri.model.telegram.TelegramChatMember
import space.davids_digital.kiri.model.telegram.TelegramChatMemberUpdated
import space.davids_digital.kiri.model.telegram.TelegramChatPermissions
import space.davids_digital.kiri.model.telegram.TelegramChatPhoto
import space.davids_digital.kiri.model.telegram.TelegramChatShared
import space.davids_digital.kiri.model.telegram.TelegramChosenInlineResult
import space.davids_digital.kiri.model.telegram.TelegramContact
import space.davids_digital.kiri.model.telegram.TelegramCopyTextButton
import space.davids_digital.kiri.model.telegram.TelegramDice
import space.davids_digital.kiri.model.telegram.TelegramDocument
import space.davids_digital.kiri.model.telegram.TelegramEncryptedCredentials
import space.davids_digital.kiri.model.telegram.TelegramEncryptedPassportElement
import space.davids_digital.kiri.model.telegram.TelegramFile
import space.davids_digital.kiri.model.telegram.TelegramForumTopicClosed
import space.davids_digital.kiri.model.telegram.TelegramForumTopicCreated
import space.davids_digital.kiri.model.telegram.TelegramForumTopicEdited
import space.davids_digital.kiri.model.telegram.TelegramForumTopicReopened
import space.davids_digital.kiri.model.telegram.TelegramGame
import space.davids_digital.kiri.model.telegram.TelegramGeneralForumTopicHidden
import space.davids_digital.kiri.model.telegram.TelegramGeneralForumTopicUnhidden
import space.davids_digital.kiri.model.telegram.TelegramGiveaway
import space.davids_digital.kiri.model.telegram.TelegramGiveawayCompleted
import space.davids_digital.kiri.model.telegram.TelegramGiveawayCreated
import space.davids_digital.kiri.model.telegram.TelegramGiveawayWinners
import space.davids_digital.kiri.model.telegram.TelegramInaccessibleMessage
import space.davids_digital.kiri.model.telegram.TelegramInlineKeyboardButton
import space.davids_digital.kiri.model.telegram.TelegramInlineKeyboardMarkup
import space.davids_digital.kiri.model.telegram.TelegramInlineQuery
import space.davids_digital.kiri.model.telegram.TelegramInvoice
import space.davids_digital.kiri.model.telegram.TelegramLinkPreviewOptions
import space.davids_digital.kiri.model.telegram.TelegramLocation
import space.davids_digital.kiri.model.telegram.TelegramLoginUrl
import space.davids_digital.kiri.model.telegram.TelegramMaskPosition
import space.davids_digital.kiri.model.telegram.TelegramMaybeInaccessibleMessage
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.model.telegram.TelegramMessageAutoDeleteTimerChanged
import space.davids_digital.kiri.model.telegram.TelegramMessageEntity
import space.davids_digital.kiri.model.telegram.TelegramMessageOrigin
import space.davids_digital.kiri.model.telegram.TelegramMessageReactionCountUpdated
import space.davids_digital.kiri.model.telegram.TelegramMessageReactionUpdated
import space.davids_digital.kiri.model.telegram.TelegramOrderInfo
import space.davids_digital.kiri.model.telegram.TelegramPaidMedia
import space.davids_digital.kiri.model.telegram.TelegramPaidMediaInfo
import space.davids_digital.kiri.model.telegram.TelegramPaidMediaPurchased
import space.davids_digital.kiri.model.telegram.TelegramPassportData
import space.davids_digital.kiri.model.telegram.TelegramPassportFile
import space.davids_digital.kiri.model.telegram.TelegramPhotoSize
import space.davids_digital.kiri.model.telegram.TelegramPoll
import space.davids_digital.kiri.model.telegram.TelegramPollAnswer
import space.davids_digital.kiri.model.telegram.TelegramPollOption
import space.davids_digital.kiri.model.telegram.TelegramPreCheckoutQuery
import space.davids_digital.kiri.model.telegram.TelegramProximityAlertTriggered
import space.davids_digital.kiri.model.telegram.TelegramReactionCount
import space.davids_digital.kiri.model.telegram.TelegramReactionType
import space.davids_digital.kiri.model.telegram.TelegramRefundedPayment
import space.davids_digital.kiri.model.telegram.TelegramSharedUser
import space.davids_digital.kiri.model.telegram.TelegramShippingAddress
import space.davids_digital.kiri.model.telegram.TelegramShippingQuery
import space.davids_digital.kiri.model.telegram.TelegramSticker
import space.davids_digital.kiri.model.telegram.TelegramStory
import space.davids_digital.kiri.model.telegram.TelegramSuccessfulPayment
import space.davids_digital.kiri.model.telegram.TelegramSwitchInlineQueryChosenChat
import space.davids_digital.kiri.model.telegram.TelegramTextQuote
import space.davids_digital.kiri.model.telegram.TelegramUpdate
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.model.telegram.TelegramUsersShared
import space.davids_digital.kiri.model.telegram.TelegramVenue
import space.davids_digital.kiri.model.telegram.TelegramVideo
import space.davids_digital.kiri.model.telegram.TelegramVideoChatEnded
import space.davids_digital.kiri.model.telegram.TelegramVideoChatParticipantsInvited
import space.davids_digital.kiri.model.telegram.TelegramVideoChatScheduled
import space.davids_digital.kiri.model.telegram.TelegramVideoNote
import space.davids_digital.kiri.model.telegram.TelegramVoice
import space.davids_digital.kiri.model.telegram.TelegramWebAppData
import space.davids_digital.kiri.model.telegram.TelegramWebAppInfo
import space.davids_digital.kiri.model.telegram.TelegramWriteAccessAllowed
import java.awt.Color
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

// TODO use MapStruct

fun Update.toModel() = TelegramUpdate(
    updateId = updateId(),
    message = message()?.toModel(),
    editedMessage = editedMessage()?.toModel(),
    channelPost = channelPost()?.toModel(),
    editedChannelPost = editedChannelPost()?.toModel(),
    businessConnection = businessConnection()?.toModel(),
    businessMessage = businessMessage()?.toModel(),
    editedBusinessMessage = editedBusinessMessage()?.toModel(),
    deletedBusinessMessages = deletedBusinessMessages()?.toModel(),
    messageReaction = messageReaction()?.toModel(),
    messageReactionCount = messageReactionCount()?.toModel(),
    inlineQuery = inlineQuery()?.toModel(),
    chosenInlineResult = chosenInlineResult()?.toModel(),
    callbackQuery = callbackQuery()?.toModel(),
    shippingQuery = shippingQuery()?.toModel(),
    preCheckoutQuery = preCheckoutQuery()?.toModel(),
    purchasedPaidMedia = purchasedPaidMedia()?.toModel(),
    poll = poll()?.toModel(),
    pollAnswer = pollAnswer()?.toModel(),
    myChatMember = myChatMember()?.toModel(),
    chatMember = chatMember()?.toModel(),
    chatJoinRequest = chatJoinRequest()?.toModel(),
    chatBoost = chatBoost()?.toModel(),
    removedChatBoost = removedChatBoost()?.toModel(),
)

fun ChatBoostRemoved.toModel() = TelegramChatBoostRemoved(
    chat = chat().toModel(),
    boostId = boostId(),
    removeDate = removeDate().toZonedDateTime(),
    source = source().toModel()
)

fun ChatBoostSource.toModel() = when (this) {
    // "user" is protected, so we can't access it directly
    is ChatBoostSourcePremium -> TelegramChatBoostSource.Premium(user = TelegramUser(0, false, "unknown"))
    // "user" is protected, so we can't access it directly
    is ChatBoostSourceGiftCode -> TelegramChatBoostSource.GiftCode(user = TelegramUser(0, false, "unknown"))
    is ChatBoostSourceGiveaway -> TelegramChatBoostSource.Giveaway(
        giveawayMessageId = giveawayMessageId(),
        user = TelegramUser(0, false, "unknown"), // "user" is protected, so we can't access it directly
        prizeStarCount = prizeStarCount(),
        isUnclaimed = isUnclaimed == true,
    )
    else -> throw IllegalArgumentException("Unknown ChatBoostSource type: ${this::class.java.name}")
}

fun ChatBoostUpdated.toModel() = TelegramChatBoostUpdated(
    chat = chat().toModel(),
    boost = boost().toModel(),
)

fun ChatBoost.toModel() = TelegramChatBoost(
    boostId = boostId(),
    addDate = addDate().toZonedDateTime(),
    expirationDate = expirationDate().toZonedDateTime(),
    source = source().toModel()
)

fun ChatJoinRequest.toModel() = TelegramChatJoinRequest(
    chat = chat().toModel(),
    from = from().toModel(),
    userChatId = userChatId(),
    date = date().toZonedDateTime(),
    bio = bio(),
    inviteLink = inviteLink()?.toModel(),
)

fun ChatInviteLink.toModel() = TelegramChatInviteLink(
    inviteLink = inviteLink(),
    creator = creator().toModel(),
    createsJoinRequest = createsJoinRequest() == true,
    isPrimary = isPrimary == true,
    isRevoked = isRevoked == true,
    name = name(),
    expireDate = expireDate()?.toZonedDateTime(),
    memberLimit = memberLimit(),
    pendingJoinRequestCount = pendingJoinRequestCount()
)

fun ChatMemberUpdated.toModel() = TelegramChatMemberUpdated(
    chat = chat().toModel(),
    from = from().toModel(),
    date = date().toZonedDateTime(),
    oldChatMember = oldChatMember().toModel(),
    newChatMember = newChatMember().toModel(),
    inviteLink = inviteLink()?.toModel(),
    viaJoinRequest = viaJoinRequest() == true,
    viaChatFolderInviteLink = viaChatFolderInviteLink() == true,
)

fun ChatMember.toModel() = when (status()) {
    ChatMember.Status.administrator -> TelegramChatMember.Administrator(
        user = user().toModel(),
        canBeEdited = canBeEdited() == true,
        isAnonymous = isAnonymous == true,
        customTitle = customTitle(),
        canManageChat = canManageChat() == true,
        canDeleteMessages = canDeleteMessages() == true,
        canManageVideoChats = canManageVideoChats() == true,
        canRestrictMembers = canRestrictMembers() == true,
        canPromoteMembers = canPromoteMembers() == true,
        canChangeInfo = canChangeInfo() == true,
        canInviteUsers = canInviteUsers() == true,
        canPostStories = canPostStories() == true,
        canEditStories = canEditStories() == true,
        canDeleteStories = canDeleteStories() == true,
        canPostMessages = canPostMessages() == true,
        canEditMessages = canEditMessages() == true,
        canPinMessages = canPinMessages() == true,
        canManageTopics = canManageTopics() == true
    )
    ChatMember.Status.member -> TelegramChatMember.Member(user().toModel(), untilDate()?.toZonedDateTime())
    ChatMember.Status.restricted -> TelegramChatMember.Restricted(
        user = user().toModel(),
        isMember = isMember == true,
        untilDate = untilDate()?.toZonedDateTime(),
        canSendMessages = canSendMessages() == true,
        canSendAudios = canSendAudios() == true,
        canSendDocuments = canSendDocuments() == true,
        canSendPhotos = canSendPhotos() == true,
        canSendVideos = canSendVideos() == true,
        canSendVideoNotes = canSendVideoNotes() == true,
        canSendVoiceNotes = canSendVoiceNotes() == true,
        canSendPolls = canSendPolls() == true,
        canSendOtherMessages = canSendOtherMessages() == true,
        canAddWebPagePreviews = canAddWebPagePreviews() == true,
        canChangeInfo = canChangeInfo() == true,
        canInviteUsers = canInviteUsers() == true,
        canPinMessages = canPinMessages() == true,
        canManageTopics = canManageTopics() == true
    )
    ChatMember.Status.creator -> TelegramChatMember.Owner(
        user = user().toModel(),
        isAnonymous = isAnonymous == true,
        customTitle = customTitle()
    )
    ChatMember.Status.left -> TelegramChatMember.Left(user().toModel())
    ChatMember.Status.kicked -> TelegramChatMember.Banned(user().toModel(), untilDate()?.toZonedDateTime())
}

fun PollAnswer.toModel() = TelegramPollAnswer(
    pollId = pollId(),
    voterChat = voterChat()?.toModel(),
    user = user()?.toModel(),
    optionIds = optionIds()?.toList() ?: emptyList()
)

fun PaidMediaPurchased.toModel() = TelegramPaidMediaPurchased(
    user = from.toModel(),
    paidMediaPayload = paidMediaPayload
)

fun PreCheckoutQuery.toModel() = TelegramPreCheckoutQuery(
    id = id(),
    from = from().toModel(),
    currency = currency(),
    totalAmount = totalAmount(),
    invoicePayload = invoicePayload(),
    shippingOptionId = shippingOptionId(),
    orderInfo = orderInfo()?.toModel(),
)

fun ShippingQuery.toModel() = TelegramShippingQuery(
    id = id(),
    from = from().toModel(),
    invoicePayload = invoicePayload(),
    shippingAddress = shippingAddress()?.toModel(),
)

fun ChosenInlineResult.toModel() = TelegramChosenInlineResult(
    resultId = resultId(),
    from = from().toModel(),
    location = location()?.toModel(),
    inlineMessageId = inlineMessageId(),
    query = query(),
)

fun InlineQuery.toModel() = TelegramInlineQuery(
    id = id(),
    from = from().toModel(),
    query = query(),
    offset = offset(),
    chatType = chatType(),
    location = location()?.toModel(),
)

fun MessageReactionCountUpdated.toModel() = TelegramMessageReactionCountUpdated(
    chat = chat().toModel(),
    messageId = messageId(),
    date = date().toZonedDateTime(),
    reactions = reactions()?.map { it.toModel() } ?: emptyList()
)

fun ReactionCount.toModel() = TelegramReactionCount(
    type = type().toModel(),
    totalCount = totalCount()
)

fun MessageReactionUpdated.toModel() = TelegramMessageReactionUpdated(
    chat = chat().toModel(),
    messageId = messageId(),
    user = user().toModel(),
    actorChat = actorChat()?.toModel(),
    date = date().toZonedDateTime(),
    oldReaction = oldReaction()?.map { it.toModel() } ?: emptyList(),
    newReaction = newReaction()?.map { it.toModel() } ?: emptyList(),
)

fun ReactionType.toModel() = when (this) {
    is ReactionTypeCustomEmoji -> TelegramReactionType.CustomEmoji(customEmojiId())
    is ReactionTypeEmoji -> TelegramReactionType.Emoji(emoji())
    is ReactionTypePaid -> TelegramReactionType.Paid()
    else -> throw IllegalArgumentException("Unknown ReactionType: ${this::class.java.name}")
}

fun BusinessMessageDeleted.toModel() = TelegramBusinessMessagesDeleted(
    businessConnectionId = businessConnectionId(),
    chat = chat().toModel(),
    messageIds = messageIds().toList()
)

fun Chat.toModel() = TelegramChat(
    id = id(),
    type = type().toModel(),
    title = title(),
    username = username(),
    firstName = firstName(),
    lastName = lastName(),
)

fun Chat.Type.toModel() = when (this) {
    Chat.Type.Private -> TelegramChat.Type.PRIVATE
    Chat.Type.supergroup -> TelegramChat.Type.SUPERGROUP
    Chat.Type.group -> TelegramChat.Type.GROUP
    Chat.Type.channel -> TelegramChat.Type.CHANNEL
}

fun BusinessConnection.toModel() = TelegramBusinessConnection(
    id = id(),
    user = user().toModel(),
    userChatId = userChatId(),
    date = date().toZonedDateTime(),
    rights = TelegramBusinessBotRights(
        canReply = canReply() == true,
    ),
    enabled = isEnabled == true,
)

fun CallbackQuery.toModel() = TelegramCallbackQuery(
    id = id(),
    from = from().toModel(),
    message = maybeInaccessibleMessage()?.toModel(),
    inlineMessageId = inlineMessageId(),
    chatInstance = chatInstance(),
    data = data(),
    gameShortName = gameShortName()
)

fun ChatFullInfo.toModel() = TelegramChat(
    id = id(),
    type = type().toModel(),
    title = title(),
    username = username(),
    firstName = firstName(),
    lastName = lastName(),
    photo = photo()?.toModel(),
    bio = bio(),
    description = description(),
    inviteLink = inviteLink(),
    pinnedMessage = pinnedMessage()?.toModel(),
    permissions = permissions()?.toModel(),
    slowModeDelay = slowModeDelay(),
    stickerSetName = stickerSetName(),
    canSetStickerSet = canSetStickerSet(),
    linkedChatId = linkedChatId(),
    location = location()?.toModel(),
)

fun ChatFullInfo.Type.toModel() = when (this) {
    ChatFullInfo.Type.Private -> TelegramChat.Type.PRIVATE
    ChatFullInfo.Type.group -> TelegramChat.Type.GROUP
    ChatFullInfo.Type.supergroup -> TelegramChat.Type.SUPERGROUP
    ChatFullInfo.Type.channel -> TelegramChat.Type.CHANNEL
}

fun ChatPhoto.toModel() = TelegramChatPhoto(
    smallFileId = this.smallFileId(),
    smallFileUniqueId = this.smallFileUniqueId(),
    bigFileId = this.bigFileId(),
    bigFileUniqueId = this.bigFileUniqueId(),
)

fun Message.toModel(): TelegramMessage {
    return TelegramMessage(
        messageId = messageId(),
        messageThreadId = messageThreadId(),
        fromId = from()?.id(),
        senderBoostCount = senderBoostCount()?.toLong(),
        senderBusinessBotId = senderBusinessBot()?.id(),
        date = date().toZonedDateTime(),
        businessConnectionId = businessConnectionId(),
        chatId = chat().id(),
        forwardOrigin = forwardOrigin()?.toModel(),
        isTopicMessage = isTopicMessage == true,
        isAutomaticForward = isAutomaticForward == true,
        quote = quote()?.toModel(),
        replyToMessage = replyToMessage()?.toModel(),
        replyToStory = replyToStory()?.toModel(),
        viaBot = viaBot()?.toModel(),
        editDate = editDate()?.toZonedDateTime(),
        hasProtectedContent = hasProtectedContent() == true,
        isFromOffline = isFromOffline == true,
        mediaGroupId = mediaGroupId(),
        authorSignature = authorSignature(),
        text = text(),
        entities = entities()?.map { it.toModel() } ?: emptyList(),
        linkPreviewOptions = linkPreviewOptions()?.toModel(),
        effectId = effectId(),
        animation = animation()?.toModel(),
        audio = audio()?.toModel(),
        document = document()?.toModel(),
        paidMediaInfo = paidMedia()?.toModel(),
        photo = photo()?.map { it.toModel() } ?: emptyList(),
        sticker = sticker()?.toModel(),
        story = story()?.toModel(),
        video = video()?.toModel(),
        videoNote = videoNote()?.toModel(),
        voice = voice()?.toModel(),
        caption = caption(),
        captionEntities = captionEntities()?.map { it.toModel() } ?: emptyList(),
        showCaptionAboveMedia = showCaptionAboveMedia() == true,
        hasMediaSpoiler = hasMediaSpoiler() == true,
        contact = contact()?.toModel(),
        dice = dice()?.toModel(),
        game = game()?.toModel(),
        poll = poll()?.toModel(),
        venue = venue()?.toModel(),
        location = location()?.toModel(),
        newChatMembers = newChatMembers()?.map { it.id() } ?: emptyList(),
        leftChatMemberId = leftChatMember()?.id(),
        newChatTitle = newChatTitle(),
        newChatPhoto = newChatPhoto()?.map { it.toModel() } ?: emptyList(),
        deleteChatPhoto = deleteChatPhoto() == true,
        groupChatCreated = groupChatCreated() == true,
        supergroupChatCreated = supergroupChatCreated() == true,
        channelChatCreated = channelChatCreated() == true,
        messageAutoDeleteTimerChanged = messageAutoDeleteTimerChanged()?.toModel(),
        migrateToChatId = migrateToChatId(),
        migrateFromChatId = migrateFromChatId(),
        pinnedMessage = pinnedMessage()?.toModel(),
        invoice = invoice()?.toModel(),
        successfulPayment = successfulPayment()?.toModel(),
        refundedPayment = refundedPayment()?.toModel(),
        usersShared = usersShared()?.toModel(),
        chatShared = chatShared()?.toModel(),
        connectedWebsite = connectedWebsite(),
        writeAccessAllowed = writeAccessAllowed()?.toModel(),
        passportData = passportData()?.toModel(),
        proximityAlertTriggered = proximityAlertTriggered()?.toModel(),
        chatBoostAdded = boostAdded()?.toModel(),
        chatBackgroundSet = chatBackgroundSet()?.toModel(),
        forumTopicCreated = forumTopicCreated()?.toModel(),
        forumTopicEdited = forumTopicEdited()?.toModel(),
        forumTopicClosed = forumTopicClosed()?.toModel(),
        forumTopicReopened = forumTopicReopened()?.toModel(),
        generalForumTopicHidden = generalForumTopicHidden()?.toModel(),
        generalForumTopicUnhidden = generalForumTopicUnhidden()?.toModel(),
        giveawayCreated = giveawayCreated()?.toModel(),
        giveaway = giveaway()?.toModel(),
        giveawayWinners = giveawayWinners()?.toModel(),
        giveawayCompleted = this.giveawayCompleted()?.toModel(),
        videoChatScheduled = videoChatScheduled()?.toModel(),
        videoChatEnded = videoChatEnded()?.toModel(),
        videoChatParticipantsInvited = videoChatParticipantsInvited()?.toModel(),
        webAppData = webAppData()?.toModel(),
        replyMarkup = replyMarkup()?.toModel(),
    )
}

fun ChatPermissions.toModel() = TelegramChatPermissions (
    canSendMessages = canSendMessages() == true,
    canSendAudios = canSendAudios() == true,
    canSendDocuments = canSendDocuments() == true,
    canSendPhotos = canSendPhotos() == true,
    canSendVideos = canSendVideos() == true,
    canSendVideoNotes = canSendVideoNotes() == true,
    canSendVoiceNotes = canSendVoiceNotes() == true,
    canSendPolls = canSendPolls() == true,
    canSendOtherMessages = canSendOtherMessages() == true,
    canAddWebPagePreviews = canAddWebPagePreviews() == true,
    canChangeInfo = canChangeInfo() == true,
    canInviteUsers = canInviteUsers() == true,
    canPinMessages = canPinMessages() == true,
    canManageTopics = canManageTopics() == true,
)

fun ChatLocation.toModel() = TelegramChatLocation (
    location = location().toModel(),
    address = address(),
)

fun MessageOrigin.toModel(): TelegramMessageOrigin {
    when (type()) {
        "user" -> {
            this as MessageOriginUser
            return TelegramMessageOrigin.User(date().toZonedDateTime(), senderUser().id())
        }
        "hidden_user" -> {
            this as MessageOriginHiddenUser
            return TelegramMessageOrigin.HiddenUser(date().toZonedDateTime(), senderUserName())
        }
        "chat" -> {
            this as MessageOriginChat
            return TelegramMessageOrigin.Chat(date().toZonedDateTime(), senderChat().id(), authorSignature())
        }
        "channel" -> {
            this as MessageOriginChannel
            return TelegramMessageOrigin.Channel(
                date().toZonedDateTime(),
                chat().id(),
                messageId(),
                authorSignature()
            )
        }
        else -> {
            return TelegramMessageOrigin.Unknown(date().toZonedDateTime())
        }
    }
}

fun TextQuote.toModel() = TelegramTextQuote (
    text = text(),
    entities = entities()?.map { it.toModel() } ?: emptyList(),
    position = position(),
    isManual = isManual == true,
)

fun Story.toModel() = TelegramStory (chatId = chat().id(), id = id().toLong())

fun User.toModel() = TelegramUser (
    id = id(),
    isBot = isBot == true,
    firstName = firstName(),
    lastName = lastName(),
    username = username(),
    languageCode = languageCode(),
    isPremium = isPremium == true,
    addedToAttachmentMenu = addedToAttachmentMenu() == true,
    canJoinGroups = canJoinGroups() == true,
    canReadAllGroupMessages = canReadAllGroupMessages() == true,
    supportsInlineQueries = supportsInlineQueries() == true,
    canConnectToBusiness = canConnectToBusiness() == true,
    hasMainWebApp = hasMainWebApp() == true,
)

fun TelegramUser.toDto(): User {
    return User(id) // Other setters are not available
}

fun MessageEntity.toModel() = TelegramMessageEntity (
    type = type().toModel(),
    offset = offset(),
    length = length(),
    url = url(),
    userId = user()?.id(),
    language = language(),
    customEmojiId = customEmojiId(),
)

fun TelegramMessageEntity.toDto(): MessageEntity {
    val result = MessageEntity(type.toDto(), offset, length)
    result.url(url)
    result.user(User(userId))
    result.language(language)
    result.customEmojiId(customEmojiId)
    return result
}

fun MessageEntity.Type.toModel() = when (this) {
    MessageEntity.Type.mention -> TelegramMessageEntity.Type.MENTION
    MessageEntity.Type.hashtag -> TelegramMessageEntity.Type.HASHTAG
    MessageEntity.Type.cashtag -> TelegramMessageEntity.Type.CASHTAG
    MessageEntity.Type.bot_command -> TelegramMessageEntity.Type.BOT_COMMAND
    MessageEntity.Type.url -> TelegramMessageEntity.Type.URL
    MessageEntity.Type.email -> TelegramMessageEntity.Type.EMAIL
    MessageEntity.Type.phone_number -> TelegramMessageEntity.Type.PHONE_NUMBER
    MessageEntity.Type.bold -> TelegramMessageEntity.Type.BOLD
    MessageEntity.Type.italic -> TelegramMessageEntity.Type.ITALIC
    MessageEntity.Type.code -> TelegramMessageEntity.Type.CODE
    MessageEntity.Type.pre -> TelegramMessageEntity.Type.PRE
    MessageEntity.Type.text_link -> TelegramMessageEntity.Type.TEXT_LINK
    MessageEntity.Type.text_mention -> TelegramMessageEntity.Type.TEXT_MENTION
    MessageEntity.Type.underline -> TelegramMessageEntity.Type.UNDERLINE
    MessageEntity.Type.strikethrough -> TelegramMessageEntity.Type.STRIKETHROUGH
    MessageEntity.Type.spoiler -> TelegramMessageEntity.Type.SPOILER
    MessageEntity.Type.custom_emoji -> TelegramMessageEntity.Type.CUSTOM_EMOJI
    MessageEntity.Type.blockquote -> TelegramMessageEntity.Type.BLOCKQUOTE
    MessageEntity.Type.expandable_blockquote -> TelegramMessageEntity.Type.EXPANDABLE_BLOCKQUOTE
}

fun TelegramMessageEntity.Type.toDto() = when (this) {
    TelegramMessageEntity.Type.MENTION -> MessageEntity.Type.mention
    TelegramMessageEntity.Type.HASHTAG -> MessageEntity.Type.hashtag
    TelegramMessageEntity.Type.CASHTAG -> MessageEntity.Type.cashtag
    TelegramMessageEntity.Type.BOT_COMMAND -> MessageEntity.Type.bot_command
    TelegramMessageEntity.Type.URL -> MessageEntity.Type.url
    TelegramMessageEntity.Type.EMAIL -> MessageEntity.Type.email
    TelegramMessageEntity.Type.PHONE_NUMBER -> MessageEntity.Type.phone_number
    TelegramMessageEntity.Type.BOLD -> MessageEntity.Type.bold
    TelegramMessageEntity.Type.ITALIC -> MessageEntity.Type.italic
    TelegramMessageEntity.Type.CODE -> MessageEntity.Type.code
    TelegramMessageEntity.Type.PRE -> MessageEntity.Type.pre
    TelegramMessageEntity.Type.TEXT_LINK -> MessageEntity.Type.text_link
    TelegramMessageEntity.Type.TEXT_MENTION -> MessageEntity.Type.text_mention
    TelegramMessageEntity.Type.UNDERLINE -> MessageEntity.Type.underline
    TelegramMessageEntity.Type.STRIKETHROUGH -> MessageEntity.Type.strikethrough
    TelegramMessageEntity.Type.SPOILER -> MessageEntity.Type.spoiler
    TelegramMessageEntity.Type.CUSTOM_EMOJI -> MessageEntity.Type.custom_emoji
    TelegramMessageEntity.Type.BLOCKQUOTE -> MessageEntity.Type.blockquote
    TelegramMessageEntity.Type.EXPANDABLE_BLOCKQUOTE -> MessageEntity.Type.expandable_blockquote
}

fun LinkPreviewOptions.toModel() = TelegramLinkPreviewOptions (
    isDisabled = isDisabled == true,
    url = url(),
    preferSmallMedia = preferSmallMedia() == true,
    preferLargeMedia = preferLargeMedia() == true,
    showAboveText = showAboveText() == true,
)

fun Animation.toModel() = TelegramAnimation (
    fileId = fileId(),
    fileUniqueId = fileUniqueId(),
    width = width(),
    height = height(),
    duration = duration(),
    thumbnail = thumbnail()?.toModel(),
    fileName = fileName(),
    mimeType = mimeType(),
    fileSize = fileSize(),
)

fun Audio.toModel() = TelegramAudio (
    fileId = fileId ?: throw IllegalArgumentException("Audio.fileId is required"),
    fileUniqueId = fileUniqueId ?: throw IllegalArgumentException("audio.fileUniqueId ID is required"),
    duration = duration ?: throw IllegalArgumentException("Audio.duration is required"),
    performer = performer,
    title = title,
    fileName = fileName,
    mimeType = mimeType,
    fileSize = fileSize,
    thumbnail = thumbnail?.toModel(),
)

fun Document.toModel() = TelegramDocument(
    fileId(), fileUniqueId(), thumbnail()?.toModel(), fileName(), mimeType(), fileSize()
)

fun PaidMediaInfo.toModel() = TelegramPaidMediaInfo(starCount(), paidMedia().map { it.toModel() })

fun PaidMedia.toModel(): TelegramPaidMedia {
    when (type()) {
        "preview" -> {
            this as PaidMediaPreview
            return TelegramPaidMedia.Preview(width, height, duration)
        }
        "photo" -> {
            this as PaidMediaPhoto
            return TelegramPaidMedia.Photo(photo.map { it.toModel() })
        }
        "video" -> {
            this as PaidMediaVideo
            return TelegramPaidMedia.Video(video.toModel())
        }
        else -> {
            return TelegramPaidMedia.Unknown()
        }
    }
}

fun PhotoSize.toModel() = TelegramPhotoSize(fileId(), fileUniqueId(), width(), height(), fileSize())

fun Sticker.toModel() = TelegramSticker(
    fileId(),
    fileUniqueId(),
    when (type()) {
        Sticker.Type.regular -> TelegramSticker.Type.REGULAR
        Sticker.Type.mask -> TelegramSticker.Type.MASK
        Sticker.Type.custom_emoji -> TelegramSticker.Type.CUSTOM_EMOJI
        else -> TelegramSticker.Type.UNKNOWN
    },
    width(),
    height(),
    isAnimated == true,
    isVideo == true,
    thumbnail()?.toModel(),
    emoji(),
    setName(),
    premiumAnimation()?.toModel(),
    maskPosition()?.toModel(),
    customEmojiId(),
    needsRepainting() == true,
    fileSize(),
)

fun File.toModel() = TelegramFile(fileId(), fileUniqueId(), fileSize(), filePath())

fun MaskPosition.toModel() = TelegramMaskPosition(point(), xShift(), yShift(), scale(),)

fun Video.toModel() = TelegramVideo(
    fileId,
    fileUniqueId,
    width,
    height,
    duration,
    thumbnail?.toModel(),
    cover?.map { it.toModel() } ?: emptyList(),
    startTimestamp,
    fileName,
    mimeType,
    fileSize,
)

fun VideoNote.toModel() = TelegramVideoNote(
    fileId(), fileUniqueId(), length(), duration(), thumbnail()?.toModel(), fileSize()
)

fun Voice.toModel() = TelegramVoice(fileId(), fileUniqueId(), duration(), mimeType(), fileSize())

fun Contact.toModel() = TelegramContact(phoneNumber(), firstName(), lastName(), userId(), vcard())

fun Dice.toModel() = TelegramDice(emoji(), value())

fun Game.toModel() = TelegramGame(
    title(),
    description(),
    photo().map { it.toModel() },
    text(),
    textEntities().map { it.toModel() },
    animation().toModel()
)

fun Poll.toModel() = TelegramPoll (
    id(),
    question(),
    questionEntities()?.map { it.toModel() } ?: emptyList(),
    options()?.map { it.toModel() } ?: emptyList(),
    totalVoterCount(),
    isClosed,
    isAnonymous,
    when (type()) {
        Poll.Type.quiz -> TelegramPoll.Type.QUIZ
        Poll.Type.regular -> TelegramPoll.Type.REGULAR
    },
    allowsMultipleAnswers(),
    correctOptionId(),
    explanation(),
    explanationEntities()?.map { it.toModel() } ?: emptyList(),
    openPeriod(),
    closeDate()?.toZonedDateTime(),
)

fun PollOption.toModel() = TelegramPollOption(text(), voterCount(), textEntities()?.map { it.toModel() } ?: emptyList())

fun Venue.toModel() = TelegramVenue(
    location().toModel(), title(), address(), foursquareId(), foursquareType(), googlePlaceId(), googlePlaceType()
)

fun Location.toModel() = TelegramLocation(
    latitude(), longitude(), horizontalAccuracy(), livePeriod(), heading(), proximityAlertRadius()
)

fun MessageAutoDeleteTimerChanged.toModel() = TelegramMessageAutoDeleteTimerChanged(messageAutoDeleteTime())

fun MaybeInaccessibleMessage.toModel(): TelegramMaybeInaccessibleMessage{
    return if (date() == 0) {
        (this as InaccessibleMessage).toModel()
    } else {
        (this as Message).toModel()
    }
}

fun InaccessibleMessage.toModel() = TelegramInaccessibleMessage(chat().id(), messageId())

fun Invoice.toModel() = TelegramInvoice(title(), description(), startParameter(), currency(), totalAmount().toLong())

fun SuccessfulPayment.toModel() = TelegramSuccessfulPayment(
    currency,
    totalAmount,
    invoicePayload,
    subscriptionExpirationDate?.toZonedDateTime(),
    isRecurring == true,
    isFirstRecurring = true,
    shippingOptionId,
    orderInfo?.toModel(),
    telegramPaymentChargeId,
    providerPaymentChargeId
)

fun OrderInfo.toModel() = TelegramOrderInfo(name(), phoneNumber(), email(), shippingAddress()?.toModel())

fun ShippingAddress.toModel() = TelegramShippingAddress(
    countryCode(), state(), city(), streetLine1(), streetLine2(), postCode()
)

fun RefundedPayment.toModel() = TelegramRefundedPayment(
    currency(), totalAmount(), invoicePayload(), telegramPaymentChargeId(), providerPaymentChargeId()
)

fun UsersShared.toModel() =  TelegramUsersShared(requestId().toLong(), users().map { it.toModel() })

fun SharedUser.toModel() = TelegramSharedUser(
    userId().toLong(),
    firstName(),
    lastName(),
    username(),
    photo()?.map { it.toModel() }
)

fun ChatShared.toModel() = TelegramChatShared(
    requestId().toLong(), chatId(), title(), username(), photo()?.map { it.toModel() }
)

fun WriteAccessAllowed.toModel() = TelegramWriteAccessAllowed(
    fromRequest() == true, webAppName(), fromAttachmentMenu() == true
)

fun PassportData.toModel() = TelegramPassportData(data().map { it.toModel() }, credentials().toModel())

fun EncryptedPassportElement.toModel() = TelegramEncryptedPassportElement(
    type().toModel(),
    data(),
    phoneNumber(),
    email(),
    files()?.map { it.toModel() },
    frontSide()?.toModel(),
    reverseSide()?.toModel(),
    selfie()?.toModel(),
    translation()?.map { it.toModel() },
    hash()
)

fun EncryptedPassportElement.Type.toModel() = when (this) {
    EncryptedPassportElement.Type.personal_details -> TelegramEncryptedPassportElement.Type.PERSONAL_DETAILS
    EncryptedPassportElement.Type.passport -> TelegramEncryptedPassportElement.Type.PASSPORT
    EncryptedPassportElement.Type.driver_license -> TelegramEncryptedPassportElement.Type.DRIVER_LICENSE
    EncryptedPassportElement.Type.identity_card -> TelegramEncryptedPassportElement.Type.IDENTITY_CARD
    EncryptedPassportElement.Type.internal_passport -> TelegramEncryptedPassportElement.Type.INTERNAL_PASSPORT
    EncryptedPassportElement.Type.address -> TelegramEncryptedPassportElement.Type.ADDRESS
    EncryptedPassportElement.Type.utility_bill -> TelegramEncryptedPassportElement.Type.UTILITY_BILL
    EncryptedPassportElement.Type.bank_statement -> TelegramEncryptedPassportElement.Type.BANK_STATEMENT
    EncryptedPassportElement.Type.rental_agreement -> TelegramEncryptedPassportElement.Type.RENTAL_AGREEMENT
    EncryptedPassportElement.Type.passport_registration -> TelegramEncryptedPassportElement.Type.PASSPORT_REGISTRATION
    EncryptedPassportElement.Type.temporary_registration -> TelegramEncryptedPassportElement.Type.TEMPORARY_REGISTRATION
    EncryptedPassportElement.Type.phone_number -> TelegramEncryptedPassportElement.Type.PHONE_NUMBER
    EncryptedPassportElement.Type.email -> TelegramEncryptedPassportElement.Type.EMAIL
}

fun PassportFile.toModel() = TelegramPassportFile(
    fileId(), fileUniqueId(), fileSize(), fileDate().toZonedDateTime()
)

fun EncryptedCredentials.toModel() = TelegramEncryptedCredentials(data(), hash(), secret())

fun ProximityAlertTriggered.toModel() = TelegramProximityAlertTriggered(traveler().id(), watcher().id(), distance())

fun ChatBoostAdded.toModel() = TelegramChatBoostAdded(boostCount())

fun ChatBackground.toModel(): Nothing = TODO("Not yet implemented")

fun BackgroundType.toModel(): TelegramBackgroundType {
    return when (this) {
        is BackgroundTypeChatTheme -> TelegramBackgroundType.ChatTheme(themeName())
        is BackgroundTypeFill -> TelegramBackgroundType.Fill(fill().toModel(), darkThemeDimming())
        is BackgroundTypePattern -> TelegramBackgroundType.Pattern(
            document().toModel(),
            fill().toModel(),
            intensity(),
            isInverted,
            isMoving
        )
        is BackgroundTypeWallpaper -> TelegramBackgroundType.Wallpaper(
            document().toModel(),
            darkThemeDimming(),
            isBlurred,
            isMoving
        )
        else -> TelegramBackgroundType.Unknown()
    }
}

fun BackgroundFill.toModel(): TelegramBackgroundFill {
    return when (this) {
        is BackgroundFillSolid -> TelegramBackgroundFill.Solid(Color(color()))
        is BackgroundFillGradient -> TelegramBackgroundFill.Gradient(
            Color(topColor()),
            Color(bottomColor()),
            rotationAngle()
        )
        is BackgroundFillFreeformGradient -> TelegramBackgroundFill.FreeformGradient(colors().map { Color(it) })
        else -> TelegramBackgroundFill.Unknown()
    }
}

fun ForumTopicCreated.toModel() = TelegramForumTopicCreated(name(), Color(iconColor()), iconCustomEmojiId())

fun ForumTopicEdited.toModel() = TelegramForumTopicEdited(name(), iconCustomEmojiId())

fun ForumTopicClosed.toModel() = TelegramForumTopicClosed()

fun ForumTopicReopened.toModel() = TelegramForumTopicReopened()

fun GeneralForumTopicHidden.toModel() = TelegramGeneralForumTopicHidden()

fun GeneralForumTopicUnhidden.toModel() = TelegramGeneralForumTopicUnhidden()

fun GiveawayCreated.toModel() = TelegramGiveawayCreated(prizeStarCount)

fun Giveaway.toModel() = TelegramGiveaway(
    chats().map { it.id() },
    winnersSelectionDate().toZonedDateTime(),
    winnerCount(),
    onlyNewMembers() == true,
    hasPublicWinners() == true,
    prizeDescription(),
    countryCodes().toList(),
    prizeStarCount(),
    premiumSubscriptionMonthCount(),
)

fun GiveawayWinners.toModel() = TelegramGiveawayWinners(
    chat().id(),
    giveawayMessageId().toLong(),
    winnersSelectionDate().toZonedDateTime(),
    winnerCount(),
    winners().map { it.id() },
    additionalChatCount(),
    prizeStarCount(),
    premiumSubscriptionMonthCount(),
    unclaimedPrizeCount(),
    onlyNewMembers() == true,
    wasRefunded() == true,
    prizeDescription(),
)

fun GiveawayCompleted.toModel() = TelegramGiveawayCompleted(
    winnerCount(),
    unclaimedPrizeCount(),
    message = this.giveawayMessage().toModel(),
    isStarGiveaway == true
)

fun VideoChatScheduled.toModel() = TelegramVideoChatScheduled(startDate().toZonedDateTime())

fun VideoChatEnded.toModel() = TelegramVideoChatEnded(duration().toLong())

fun VideoChatParticipantsInvited.toModel() = TelegramVideoChatParticipantsInvited(users().map { it.id() })

fun WebAppData.toModel() = TelegramWebAppData(data(), buttonText())

fun InlineKeyboardMarkup.toModel() = TelegramInlineKeyboardMarkup(inlineKeyboard().map { it.map { it.toModel() } })
fun TelegramInlineKeyboardMarkup.toDto() = InlineKeyboardMarkup(
    *inlineKeyboard.map {
        it.map { it.toDto() }.toTypedArray()
    }.toTypedArray()
)

fun InlineKeyboardButton.toModel() = TelegramInlineKeyboardButton(
    text ?: throw IllegalArgumentException("InlineKeyboardButton.text is required"),
    url,
    callbackData,
    webApp?.toModel(),
    loginUrl?.toModel(),
    switchInlineQuery,
    switchInlineQueryCurrentChat,
    switchInlineQueryChosenChat?.toModel(),
    copyText?.toModel(),
    callbackGame?.toModel(),
    pay == true,
)

fun TelegramInlineKeyboardButton.toDto() = InlineKeyboardButton(
    text,
    url,
    loginUrl?.toDto(),
    callbackData,
    switchInlineQuery,
    switchInlineQueryCurrentChat,
    switchInlineQueryChosenChat?.toDto(),
    callbackGame?.toDto(),
    pay,
    webApp?.toDto(),
    copyText?.toDto()
)

fun WebAppInfo.toModel() = TelegramWebAppInfo(url())
fun TelegramWebAppInfo.toDto() = WebAppInfo(url)

fun LoginUrl.toModel() = TelegramLoginUrl("<LIBRARY SETTER NOT AVAILABLE>") // Setters are not available
fun TelegramLoginUrl.toDto(): LoginUrl = LoginUrl(url)
    .forwardText(forwardText)
    .botUsername(botUsername)
    .requestWriteAccess(requestWriteAccess)


fun SwitchInlineQueryChosenChat.toModel() = TelegramSwitchInlineQueryChosenChat() // Setters are not available
fun TelegramSwitchInlineQueryChosenChat.toDto(): SwitchInlineQueryChosenChat = SwitchInlineQueryChosenChat()
    .query(query)
    .allowUserChats(allowUserChats)
    .allowBotChats(allowBotChats)
    .allowGroupChats(allowGroupChats)
    .allowChannelChats(allowChannelChats)

fun CopyTextButton.toModel() = TelegramCopyTextButton(text)
fun TelegramCopyTextButton.toDto() = CopyTextButton(text)

fun CallbackGame.toModel() = TelegramCallbackGame()
fun TelegramCallbackGame.toDto() = CallbackGame

private fun Int.toZonedDateTime() = ZonedDateTime.ofInstant(Instant.ofEpochSecond(this.toLong()), UTC)
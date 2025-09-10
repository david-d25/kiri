package space.davids_digital.kiri.integration.telegram

import com.pengrad.telegrambot.model.*
import com.pengrad.telegrambot.model.business.BusinessBotRights
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
import com.pengrad.telegrambot.model.message.origin.*
import com.pengrad.telegrambot.model.paidmedia.PaidMedia
import com.pengrad.telegrambot.model.paidmedia.PaidMediaInfo
import com.pengrad.telegrambot.model.paidmedia.PaidMediaPhoto
import com.pengrad.telegrambot.model.paidmedia.PaidMediaPreview
import com.pengrad.telegrambot.model.paidmedia.PaidMediaPurchased
import com.pengrad.telegrambot.model.paidmedia.PaidMediaVideo
import com.pengrad.telegrambot.model.reaction.*
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
import org.mapstruct.*
import org.springframework.beans.factory.annotation.Autowired
import space.davids_digital.kiri.model.telegram.*
import space.davids_digital.kiri.service.TelegramChatMetadataService
import space.davids_digital.kiri.service.TelegramUserMetadataService
import java.awt.Color
import java.time.Instant
import java.time.ZoneOffset.UTC
import java.time.ZonedDateTime

@Mapper
abstract class TelegramIntegrationMapper {

    @Autowired
    lateinit var telegramUserMetadataService: TelegramUserMetadataService

    @Autowired
    lateinit var telegramChatMetadataService: TelegramChatMetadataService

    abstract fun toModel(dto: BusinessBotRights?): TelegramBusinessBotRights

    @Mapping(target = "copy", ignore = true)
    abstract fun toModel(dto: GiveawayCreated?): TelegramGiveawayCreated?

    @Mapping(target = "copy", ignore = true)
    abstract fun toModel(dto: CopyTextButton?): TelegramCopyTextButton?

    fun toModel(dto: Update?): TelegramUpdate? = dto?.let {
        TelegramUpdate(
            it.updateId(),
            it.message()?.let(::toModel),
            it.editedMessage()?.let(::toModel),
            it.channelPost()?.let(::toModel),
            it.editedChannelPost()?.let(::toModel),
            it.businessConnection()?.let(::toModel),
            it.businessMessage()?.let(::toModel),
            it.editedBusinessMessage()?.let(::toModel),
            it.deletedBusinessMessages()?.let(::toModel),
            it.messageReaction()?.let(::toModel),
            it.messageReactionCount()?.let(::toModel),
            it.inlineQuery()?.let(::toModel),
            it.chosenInlineResult()?.let(::toModel),
            it.callbackQuery()?.let(::toModel),
            it.shippingQuery()?.let(::toModel),
            it.preCheckoutQuery()?.let(::toModel),
            it.purchasedPaidMedia()?.let(::toModel),
            it.poll()?.let(::toModel),
            it.pollAnswer()?.let(::toModel),
            it.myChatMember()?.let(::toModel),
            it.chatMember()?.let(::toModel),
            it.chatJoinRequest()?.let(::toModel),
            it.chatBoost()?.let(::toModel),
            it.removedChatBoost()?.let(::toModel)
        )
    }

    fun toModel(dto: Audio?): TelegramAudio? = dto?.let {
        TelegramAudio(
            it.fileId ?: throw IllegalArgumentException("fileId is null"),
            it.fileUniqueId ?: throw IllegalArgumentException("fileUniqueId is null"),
            it.duration ?: throw IllegalArgumentException("duration is null"),
            it.performer,
            it.title,
            it.fileName,
            it.mimeType,
            it.fileSize,
            toModel(it.thumbnail)
        )
    }

    fun toModel(dto: InlineKeyboardButton?): TelegramInlineKeyboardButton? = dto?.let {
        TelegramInlineKeyboardButton(
            it.text ?: "",
            it.url,
            it.callbackData,
            toModel(it.webApp),
            toModel(it.loginUrl),
            it.switchInlineQuery,
            it.switchInlineQueryCurrentChat,
            toModel(it.switchInlineQueryChosenChat),
            toModel(it.copyText),
            toModel(it.callbackGame),
            booleanOrFalse(it.pay)
        )
    }

    fun toModel(dto: SuccessfulPayment?): TelegramSuccessfulPayment? = dto?.let {
        TelegramSuccessfulPayment(
            it.currency,
            it.totalAmount,
            it.invoicePayload,
            integerToZonedDateTime(it.subscriptionExpirationDate)!!,
            booleanOrFalse(it.isRecurring),
            booleanOrFalse(it.isFirstRecurring),
            it.shippingOptionId,
            toModel(it.orderInfo),
            it.telegramPaymentChargeId,
            it.providerPaymentChargeId
        )
    }

    fun toModel(dto: ChatBoostRemoved?): TelegramChatBoostRemoved? = dto?.let { dto ->
        TelegramChatBoostRemoved(
            dto.chat().let { toModel(it)!! },
            dto.boostId(),
            dto.removeDate().let { integerToZonedDateTime(it)!! },
            dto.source().let { toModel(it)!! }
        )
    }

    fun toModel(dto: ChatBoostUpdated?): TelegramChatBoostUpdated? = dto?.let {
        TelegramChatBoostUpdated(
            toModel(it.chat())!!,
            toModel(it.boost())!!
        )
    }

    fun toModel(dto: ChatBoost?): TelegramChatBoost? = dto?.let {
        TelegramChatBoost(
            it.boostId(),
            integerToZonedDateTime(it.addDate())!!,
            integerToZonedDateTime(it.expirationDate())!!,
            toModel(it.source())!!
        )
    }

    fun toModel(dto: ChatJoinRequest?): TelegramChatJoinRequest? = dto?.let {
        TelegramChatJoinRequest(
            toModel(it.chat())!!,
            toModel(it.from()),
            it.userChatId(),
            integerToZonedDateTime(it.date())!!,
            it.bio(),
            toModel(it.inviteLink())
        )
    }

    fun toModel(dto: PollAnswer?): TelegramPollAnswer? = dto?.let {
        TelegramPollAnswer(
            it.pollId(),
            toModel(it.voterChat()),
            toModel(it.user()),
            it.optionIds().toList()
        )
    }

    fun toModel(dto: PaidMediaPurchased?): TelegramPaidMediaPurchased? = dto?.let {
        TelegramPaidMediaPurchased(
            toModel(it.from),
            it.paidMediaPayload
        )
    }

    fun toModel(dto: PreCheckoutQuery?): TelegramPreCheckoutQuery? = dto?.let {
        TelegramPreCheckoutQuery(
            it.id(),
            toModel(it.from()),
            it.currency(),
            it.totalAmount(),
            it.invoicePayload(),
            it.shippingOptionId(),
            toModel(it.orderInfo())
        )
    }

    fun toModel(dto: ShippingQuery?): TelegramShippingQuery? = dto?.let {
        TelegramShippingQuery(
            it.id(),
            toModel(it.from()),
            it.invoicePayload(),
            toModel(it.shippingAddress())
        )
    }

    fun toModel(dto: ChosenInlineResult?): TelegramChosenInlineResult? = dto?.let {
        TelegramChosenInlineResult(
            it.resultId(),
            toModel(it.from()),
            toModel(it.location()),
            it.inlineMessageId(),
            it.query()
        )
    }

    fun toModel(dto: InlineQuery?): TelegramInlineQuery? = dto?.let {
        TelegramInlineQuery(
            it.id(),
            toModel(it.from()),
            it.query(),
            it.offset(),
            it.chatType(),
            toModel(it.location())
        )
    }

    fun toModel(dto: ReactionCount?): TelegramReactionCount? = dto?.let {
        TelegramReactionCount(
            toModel(it.type())!!,
            it.totalCount()
        )
    }

    fun toModel(dto: BusinessMessageDeleted?): TelegramBusinessMessagesDeleted? = dto?.let {
        TelegramBusinessMessagesDeleted(
            it.businessConnectionId(),
            toModel(it.chat())!!,
            it.messageIds().toList()
        )
    }

    fun toModel(dto: Chat?): TelegramChat? = dto?.let {
        TelegramChat(
            it.id(),
            toModel(it.type())!!,
            it.title(),
            it.username(),
            it.firstName(),
            it.lastName(),
            metadata = telegramChatMetadataService.getOrCreateDefault(it.id(), toModel(it.type())!!)
        )
    }

    fun toModel(dto: ChatFullInfo?): TelegramChat? = dto?.let {
        TelegramChat(
            it.id(),
            toModel(it.type())!!,
            it.title(),
            it.username(),
            it.firstName(),
            it.lastName(),
            toModel(it.photo()),
            it.bio(),
            it.description(),
            it.inviteLink(),
            toModel(it.pinnedMessage()),
            toModel(it.permissions()),
            it.slowModeDelay(),
            it.stickerSetName(),
            it.canSetStickerSet(),
            it.linkedChatId(),
            toModel(it.location()),
            telegramChatMetadataService.getOrCreateDefault(it.id(), toModel(it.type())!!)
        )
    }

    fun toModel(dto: CallbackQuery?): TelegramCallbackQuery? = dto?.let {
        TelegramCallbackQuery(
            it.id(),
            toModel(it.from()),
            toModel(it.maybeInaccessibleMessage()),
            it.inlineMessageId(),
            it.chatInstance(),
            it.data(),
            it.gameShortName()
        )
    }

    fun toModel(dto: ChatPhoto?): TelegramChatPhoto? = dto?.let {
        TelegramChatPhoto(
            it.smallFileId(),
            it.smallFileUniqueId(),
            it.bigFileId(),
            it.bigFileUniqueId()
        )
    }

    fun toModel(dto: ChatLocation?): TelegramChatLocation? = dto?.let {
        TelegramChatLocation(
            toModel(it.location())!!,
            it.address()
        )
    }

    fun toModel(dto: MessageEntity?): TelegramMessageEntity? = dto?.let {
        TelegramMessageEntity(
            toModel(it.type())!!,
            it.offset(),
            it.length(),
            it.url(),
            it.user()?.id(),
            it.language(),
            it.customEmojiId()
        )
    }

    fun toModel(dto: Animation?): TelegramAnimation? = dto?.let {
        TelegramAnimation(
            it.fileId(),
            it.fileUniqueId(),
            it.width(),
            it.height(),
            it.duration(),
            toModel(it.thumbnail()),
            it.fileName(),
            it.mimeType(),
            it.fileSize()
        )
    }

    fun toModel(dto: Document?): TelegramDocument? = dto?.let {
        TelegramDocument(
            it.fileId(),
            it.fileUniqueId(),
            toModel(it.thumbnail()),
            it.fileName(),
            it.mimeType(),
            it.fileSize()
        )
    }

    fun toModel(dto: Voice?): TelegramVoice? = dto?.let {
        TelegramVoice(
            it.fileId(),
            it.fileUniqueId(),
            it.duration(),
            it.mimeType(),
            it.fileSize()
        )
    }

    fun toModel(dto: Contact?): TelegramContact? = dto?.let {
        TelegramContact(
            it.phoneNumber(),
            it.firstName(),
            it.lastName(),
            it.userId(),
            it.vcard()
        )
    }

    fun toModel(dto: ForumTopicClosed?): TelegramForumTopicClosed? = dto?.let {
        TelegramForumTopicClosed()
    }

    fun toModel(dto: ForumTopicReopened?): TelegramForumTopicReopened? = dto?.let {
        TelegramForumTopicReopened()
    }

    fun toModel(dto: GeneralForumTopicHidden?): TelegramGeneralForumTopicHidden? = dto?.let {
        TelegramGeneralForumTopicHidden()
    }

    fun toModel(dto: GeneralForumTopicUnhidden?): TelegramGeneralForumTopicUnhidden? = dto?.let {
        TelegramGeneralForumTopicUnhidden()
    }

    fun toModel(dto: ForumTopicEdited?): TelegramForumTopicEdited? = dto?.let {
        TelegramForumTopicEdited(
            it.name(),
            it.iconCustomEmojiId()
        )
    }

    fun toModel(dto: ChatInviteLink?): TelegramChatInviteLink? = dto?.let {
        TelegramChatInviteLink(
            it.inviteLink(),
            toModel(it.creator()),
            booleanOrFalse(it.createsJoinRequest()),
            booleanOrFalse(it.isPrimary),
            booleanOrFalse(it.isRevoked),
            it.name(),
            integerToZonedDateTime(it.expireDate()),
            it.memberLimit(),
            it.pendingJoinRequestCount(),
        )
    }

    fun toModel(dto: ChatMemberUpdated?): TelegramChatMemberUpdated? = dto?.let {
        TelegramChatMemberUpdated(
            toModel(it.chat())!!,
            toModel(it.from()),
            integerToZonedDateTime(it.date())!!,
            toModel(it.oldChatMember())!!,
            toModel(it.newChatMember())!!,
            toModel(it.inviteLink()),
            booleanOrFalse(it.viaJoinRequest()),
            booleanOrFalse(it.viaChatFolderInviteLink())
        )
    }

    fun toModel(dto: BusinessConnection?): TelegramBusinessConnection? = dto?.let {
        TelegramBusinessConnection(
            it.id(),
            toModel(it.user()),
            it.userChatId(),
            integerToZonedDateTime(it.date())!!,
            toModel(it.rights()),
            booleanOrFalse(it.isEnabled)
        )
    }

    fun toModel(dto: ChatPermissions?): TelegramChatPermissions? = dto?.let {
        TelegramChatPermissions(
            booleanOrFalse(it.canSendMessages()),
            booleanOrFalse(it.canSendAudios()),
            booleanOrFalse(it.canSendDocuments()),
            booleanOrFalse(it.canSendPhotos()),
            booleanOrFalse(it.canSendVideos()),
            booleanOrFalse(it.canSendVideoNotes()),
            booleanOrFalse(it.canSendVoiceNotes()),
            booleanOrFalse(it.canSendPolls()),
            booleanOrFalse(it.canSendOtherMessages()),
            booleanOrFalse(it.canAddWebPagePreviews()),
            booleanOrFalse(it.canChangeInfo()),
            booleanOrFalse(it.canInviteUsers()),
            booleanOrFalse(it.canPinMessages()),
            booleanOrFalse(it.canManageTopics())
        )
    }

    fun toModel(dto: LinkPreviewOptions?): TelegramLinkPreviewOptions? = dto?.let {
        TelegramLinkPreviewOptions(
            it.isDisabled,
            it.url(),
            it.preferSmallMedia(),
            it.preferLargeMedia(),
            it.showAboveText()
        )
    }

    fun toModel(dto: PhotoSize?): TelegramPhotoSize? = dto?.let {
        TelegramPhotoSize(
            it.fileId(),
            it.fileUniqueId(),
            it.width(),
            it.height(),
            it.fileSize()
        )
    }

    fun toModel(dto: Sticker?): TelegramSticker? = dto?.let {
        TelegramSticker(
            it.fileId(),
            it.fileUniqueId(),
            toModel(it.type())!!,
            it.width(),
            it.height(),
            it.isAnimated,
            it.isVideo,
            toModel(it.thumbnail())!!,
            it.emoji(),
            it.setName(),
            toModel(it.premiumAnimation()),
            toModel(it.maskPosition()),
            it.customEmojiId(),
            it.needsRepainting(),
            it.fileSize()
        )
    }


    fun toModel(dto: File?): TelegramFile? {
        return dto?.let {
            TelegramFile(it.fileId(), it.fileUniqueId(), it.fileSize(), it.filePath())
        }
    }



    fun toModel(dto: MaskPosition?): TelegramMaskPosition? = dto?.let {
        TelegramMaskPosition(
            it.point(),
            it.xShift(),
            it.yShift(),
            it.scale()
        )
    }

    fun toModel(dto: VideoNote?): TelegramVideoNote? = dto?.let {
        TelegramVideoNote(
            it.fileId(),
            it.fileUniqueId(),
            it.length(),
            it.duration(),
            toModel(it.thumbnail()),
            it.fileSize()
        )
    }

    fun toModel(dto: Dice?): TelegramDice? = dto?.let {
        TelegramDice(
            it.emoji(),
            it.value()
        )
    }

    fun toModel(dto: Venue?): TelegramVenue? = dto?.let {
        TelegramVenue(
            toModel(it.location())!!,
            it.title(),
            it.address(),
            it.foursquareId(),
            it.foursquareType(),
            it.googlePlaceId(),
            it.googlePlaceType()
        )
    }

    fun toModel(dto: Location?): TelegramLocation? = dto?.let {
        TelegramLocation(
            it.latitude(),
            it.longitude(),
            it.horizontalAccuracy(),
            it.livePeriod(),
            it.heading(),
            it.proximityAlertRadius()
        )
    }

    fun toModel(dto: MessageAutoDeleteTimerChanged?): TelegramMessageAutoDeleteTimerChanged? = dto?.let {
        TelegramMessageAutoDeleteTimerChanged(
            it.messageAutoDeleteTime()
        )
    }

    fun toModel(dto: Invoice?): TelegramInvoice? = dto?.let {
        TelegramInvoice(
            it.title(),
            it.description(),
            it.startParameter(),
            it.currency(),
            it.totalAmount().toLong()
        )
    }

    fun toModel(dto: OrderInfo?): TelegramOrderInfo? = dto?.let {
        TelegramOrderInfo(
            it.name(),
            it.phoneNumber(),
            it.email(),
            toModel(it.shippingAddress())
        )
    }

    fun toModel(dto: ShippingAddress?): TelegramShippingAddress? = dto?.let {
        TelegramShippingAddress(
            it.countryCode(),
            it.state(),
            it.city(),
            it.streetLine1(),
            it.streetLine2(),
            it.postCode()
        )
    }

    fun toModel(dto: RefundedPayment?): TelegramRefundedPayment? = dto?.let {
        TelegramRefundedPayment(
            it.currency(),
            it.totalAmount(),
            it.invoicePayload(),
            it.telegramPaymentChargeId(),
            it.providerPaymentChargeId()
        )
    }

    fun toModel(dto: WriteAccessAllowed?): TelegramWriteAccessAllowed? = dto?.let {
        TelegramWriteAccessAllowed(
            it.fromRequest(),
            it.webAppName(),
            it.fromAttachmentMenu()
        )
    }

    fun toModel(dto: PassportFile?): TelegramPassportFile? = dto?.let {
        TelegramPassportFile(
            it.fileId(),
            it.fileUniqueId(),
            it.fileSize(),
            integerToZonedDateTime(it.fileDate())!!
        )
    }

    fun toModel(dto: EncryptedCredentials?): TelegramEncryptedCredentials? = dto?.let {
        TelegramEncryptedCredentials(
            it.data(),
            it.hash(),
            it.secret()
        )
    }

    fun toModel(dto: ChatBoostAdded?): TelegramChatBoostAdded? = dto?.let {
        TelegramChatBoostAdded(
            it.boostCount()
        )
    }

    fun toModel(dto: ForumTopicCreated?): TelegramForumTopicCreated? = dto?.let {
        TelegramForumTopicCreated(
            it.name(),
            Color(it.iconColor()),
            it.iconCustomEmojiId()
        )
    }

    fun toModel(dto: Giveaway?): TelegramGiveaway? = dto?.let {
        TelegramGiveaway(
            it.chats().map(Chat::id),
            integerToZonedDateTime(it.winnersSelectionDate())!!,
            it.winnerCount(),
            it.onlyNewMembers(),
            it.hasPublicWinners(),
            it.prizeDescription(),
            it.countryCodes()?.toList() ?: emptyList(),
            it.prizeStarCount(),
            it.premiumSubscriptionMonthCount()
        )
    }

    fun toModel(dto: GiveawayWinners?): TelegramGiveawayWinners? = dto?.let {
        TelegramGiveawayWinners(
            it.chat().id(),
            it.giveawayMessageId(),
            integerToZonedDateTime(it.winnersSelectionDate())!!,
            it.winnerCount(),
            it.winners().map(User::id),
            it.additionalChatCount(),
            it.prizeStarCount(),
            it.premiumSubscriptionMonthCount(),
            it.unclaimedPrizeCount(),
            it.onlyNewMembers(),
            it.wasRefunded(),
            it.prizeDescription()
        )
    }

    fun toModel(dto: VideoChatScheduled?): TelegramVideoChatScheduled? = dto?.let {
        TelegramVideoChatScheduled(
            integerToZonedDateTime(it.startDate())!!
        )
    }

    fun toModel(dto: VideoChatEnded?): TelegramVideoChatEnded? = dto?.let {
        TelegramVideoChatEnded(
            it.duration().toLong()
        )
    }

    fun toModel(dto: VideoChatParticipantsInvited?): TelegramVideoChatParticipantsInvited? = dto?.let {
        TelegramVideoChatParticipantsInvited(
            it.users().map(User::id)
        )
    }

    fun toModel(dto: WebAppData?): TelegramWebAppData? = dto?.let {
        TelegramWebAppData(
            it.data(), it.buttonText()
        )
    }

    fun toModel(dto: InlineKeyboardMarkup?): TelegramInlineKeyboardMarkup? = dto?.let {
        TelegramInlineKeyboardMarkup(
            it.inlineKeyboard().map { row -> row.mapNotNull(::toModel) }
        )
    }

    fun toModel(dto: WebAppInfo?): TelegramWebAppInfo? = dto?.let {
        TelegramWebAppInfo(
            it.url()
        )
    }

    fun toDto(model: TelegramInlineKeyboardButton?): InlineKeyboardButton? = model?.let {
        InlineKeyboardButton(
            it.text,
            it.url,
            it.loginUrl?.let(::toDto),
            it.callbackData,
            it.switchInlineQuery,
            it.switchInlineQueryCurrentChat,
            it.switchInlineQueryChosenChat?.let(::toDto),
            it.callbackGame?.let(::toDto),
            booleanOrFalse(it.pay),
            it.webApp?.let(::toDto),
            it.copyText?.let(::toDto)
        )
    }

    abstract fun toDto(model: TelegramWebAppInfo?): WebAppInfo?

    @Mapping(target = "copy", ignore = true)
    abstract fun toDto(model: TelegramCopyTextButton?): CopyTextButton?

    fun toModel(dto: CallbackGame?): TelegramCallbackGame? = dto?.let { TelegramCallbackGame() }
    fun toDto(model: TelegramCallbackGame?): CallbackGame? = model?.let { CallbackGame }

    fun toModel(dto: StickerSet?): TelegramStickerSet? = dto?.let {
        TelegramStickerSet(
            it.name(),
            it.title(),
            it.stickerType().let(::toModel)!!,
            it.stickers().mapNotNull(::toModel),
            it.thumbnail()?.let(::toModel)
        )
    }

    fun toModel(dto: UsersShared?): TelegramUsersShared? = dto?.let {
        TelegramUsersShared(
            it.requestId(),
            it.users().mapNotNull(::toModel)
        )
    }

    fun toModel(dto: SharedUser?): TelegramSharedUser? = dto?.let {
        TelegramSharedUser(
            it.userId(),
            it.firstName(),
            it.lastName(),
            it.username(),
            it.photo()?.mapNotNull(::toModel)
        )
    }

    fun toModel(dto: ChatShared?): TelegramChatShared? = dto?.let {
        TelegramChatShared(
            it.requestId(),
            it.chatId(),
            it.title(),
            it.username(),
            it.photo()?.mapNotNull(::toModel)
        )
    }

    fun toModel(dto: PassportData?): TelegramPassportData? = dto?.let {
        TelegramPassportData(
            it.data().mapNotNull(::toModel),
            it.credentials().let(::toModel)!!
        )
    }

    fun toModel(dto: EncryptedPassportElement?): TelegramEncryptedPassportElement? = dto?.let {
        TelegramEncryptedPassportElement(
            it.type()?.let(::toModel)!!,
            it.data(),
            it.phoneNumber(),
            it.email(),
            it.files()?.mapNotNull(::toModel) ?: emptyList(),
            it.frontSide()?.let(::toModel),
            it.reverseSide()?.let(::toModel),
            it.selfie()?.let(::toModel),
            it.translation()?.mapNotNull(::toModel) ?: emptyList(),
            it.hash()
        )
    }

    fun toModel(dto: Poll?): TelegramPoll? = dto?.let {
        TelegramPoll(
            it.id(),
            it.question(),
            it.questionEntities()?.mapNotNull(::toModel) ?: emptyList(),
            it.options()?.mapNotNull(::toModel) ?: emptyList(),
            it.totalVoterCount(),
            booleanOrFalse(it.isClosed),
            booleanOrFalse(it.isAnonymous),
            it.type().let(::toModel)!!,
            booleanOrFalse(it.allowsMultipleAnswers()),
            it.correctOptionId(),
            it.explanation(),
            it.explanationEntities()?.mapNotNull(::toModel) ?: emptyList(),
            it.openPeriod(),
            integerToZonedDateTime(it.closeDate())
        )
    }

    fun toModel(dto: PollOption?): TelegramPollOption? = dto?.let {
        TelegramPollOption(
            it.text(),
            it.voterCount(),
            it.textEntities()?.mapNotNull(::toModel) ?: emptyList()
        )
    }

    fun toModel(dto: Game?): TelegramGame? = dto?.let {
        TelegramGame(
            it.title(),
            it.description(),
            it.photo()?.mapNotNull(::toModel) ?: emptyList(),
            it.text(),
            it.textEntities()?.mapNotNull(::toModel) ?: emptyList(),
            it.animation().let(::toModel)
        )
    }

    fun toModel(dto: Video?): TelegramVideo? = dto?.let {
        TelegramVideo(
            it.fileId,
            it.fileUniqueId,
            it.width().toInt(),
            it.height().toInt(),
            it.duration,
            it.thumbnail?.let(::toModel),
            it.cover?.mapNotNull(::toModel) ?: emptyList(),
            it.startTimestamp,
            it.fileName,
            it.mimeType,
            it.fileSize
        )
    }

    fun toModel(dto: PaidMediaInfo?): TelegramPaidMediaInfo? = dto?.let {
        TelegramPaidMediaInfo(
            it.starCount(),
            it.paidMedia()?.mapNotNull(::toModel) ?: emptyList()
        )
    }

    fun toModel(dto: MessageReactionCountUpdated?): TelegramMessageReactionCountUpdated? = dto?.let {
        TelegramMessageReactionCountUpdated(
            it.chat().let(::toModel)!!,
            it.messageId(),
            it.date().let(::integerToZonedDateTime)!!,
            it.reactions()?.mapNotNull(::toModel) ?: emptyList()
        )
    }

    fun toModel(dto: MessageReactionUpdated?): TelegramMessageReactionUpdated? = dto?.let {
        TelegramMessageReactionUpdated(
            it.chat().let(::toModel)!!,
            it.messageId(),
            it.user()?.let(::toModel),
            it.actorChat()?.let(::toModel),
            it.date().let(::integerToZonedDateTime)!!,
            it.oldReaction()?.mapNotNull(::toModel) ?: emptyList(),
            it.newReaction()?.mapNotNull(::toModel) ?: emptyList()
        )
    }

    fun toModel(dto: TextQuote?): TelegramTextQuote? = dto?.let {
        TelegramTextQuote(
            it.text(),
            it.entities()?.mapNotNull(::toModel) ?: emptyList(),
            it.position(),
            it.isManual.let(::booleanOrFalse)
        )
    }

    fun toModel(dto: GiveawayCompleted?): TelegramGiveawayCompleted? = dto?.let {
        TelegramGiveawayCompleted(
            it.winnerCount(),
            it.unclaimedPrizeCount(),
            it.giveawayMessage()?.let(::toModel),
            it.isStarGiveaway.let(::booleanOrFalse)
        )
    }

    fun toModel(dto: Story?): TelegramStory? = dto?.let { TelegramStory(it.chat().id(), it.id()) }

    fun toModel(dto: ProximityAlertTriggered?): TelegramProximityAlertTriggered? = dto?.let {
        TelegramProximityAlertTriggered(
            it.traveler().id(),
            it.watcher().id(),
            it.distance()
        )
    }

    fun toModel(dto: InaccessibleMessage?): TelegramInaccessibleMessage? = dto?.let {
        TelegramInaccessibleMessage(
            it.chat().id(),
            it.messageId()
        )
    }

    fun toModel(dto: User): TelegramUser {
        return TelegramUser(
            dto.id(),
            dto.isBot.let(::booleanOrFalse),
            dto.firstName(),
            dto.lastName(),
            dto.username(),
            dto.languageCode(),
            dto.isPremium.let(::booleanOrFalse),
            dto.addedToAttachmentMenu().let(::booleanOrFalse),
            dto.canJoinGroups().let(::booleanOrFalse),
            dto.canReadAllGroupMessages().let(::booleanOrFalse),
            dto.supportsInlineQueries().let(::booleanOrFalse),
            dto.canConnectToBusiness().let(::booleanOrFalse),
            dto.hasMainWebApp().let(::booleanOrFalse),
            telegramUserMetadataService.getOrCreateDefault(dto.id())
        )
    }

    fun toModel(dto: Message?): TelegramMessage? = dto?.let {
        TelegramMessage(
            it.chat().id(),
            it.messageId(),
            it.messageThreadId(),
            it.from()?.id(),
            it.senderChat()?.id(),
            it.senderBoostCount(),
            it.senderBusinessBot()?.id(),
            integerToZonedDateTime(it.date())!!,
            it.businessConnectionId(),
            toModel(it.forwardOrigin()),
            booleanOrFalse(it.isTopicMessage),
            booleanOrFalse(it.isAutomaticForward),
            it.replyToMessage()?.let(::toModel),
            it.externalReply()?.let(::toModel),
            it.quote()?.let(::toModel),
            it.replyToStory()?.let(::toModel),
            it.viaBot()?.let(::toModel),
            integerToZonedDateTime(it.editDate()),
            booleanOrFalse(it.hasProtectedContent()),
            booleanOrFalse(it.isFromOffline),
            it.mediaGroupId(),
            it.authorSignature(),
            it.text(),
            it.entities()?.mapNotNull(::toModel) ?: emptyList(),
            toModel(it.linkPreviewOptions()),
            it.effectId(),
            it.animation()?.let(::toModel),
            it.audio()?.let(::toModel),
            it.document()?.let(::toModel),
            it.paidMedia()?.let(::toModel),
            it.photo()?.mapNotNull(::toModel) ?: emptyList(),
            it.sticker()?.let(::toModel),
            it.story()?.let(::toModel),
            it.video()?.let(::toModel),
            it.videoNote()?.let(::toModel),
            it.voice()?.let(::toModel),
            it.caption(),
            it.captionEntities()?.mapNotNull(::toModel) ?: emptyList(),
            booleanOrFalse(it.showCaptionAboveMedia()),
            booleanOrFalse(it.hasMediaSpoiler()),
            it.contact()?.let(::toModel),
            it.dice()?.let(::toModel),
            it.game()?.let(::toModel),
            it.poll()?.let(::toModel),
            it.venue()?.let(::toModel),
            it.location()?.let(::toModel),
            it.newChatMembers()?.map(User::id) ?: emptyList(),
            it.leftChatMember()?.id(),
            it.newChatTitle(),
            it.newChatPhoto()?.mapNotNull(::toModel) ?: emptyList(),
            booleanOrFalse(it.deleteChatPhoto()),
            booleanOrFalse(it.groupChatCreated()),
            booleanOrFalse(it.supergroupChatCreated()),
            booleanOrFalse(it.channelChatCreated()),
            it.messageAutoDeleteTimerChanged()?.let(::toModel),
            it.migrateToChatId(),
            it.migrateFromChatId(),
            it.pinnedMessage()?.let(::toModel),
            it.invoice()?.let(::toModel),
            it.successfulPayment()?.let(::toModel),
            it.refundedPayment()?.let(::toModel),
            it.usersShared()?.let(::toModel),
            it.chatShared()?.let(::toModel),
            it.connectedWebsite(),
            it.writeAccessAllowed()?.let(::toModel),
            it.passportData()?.let(::toModel),
            it.proximityAlertTriggered()?.let(::toModel),
            it.boostAdded()?.let(::toModel),
            it.chatBackgroundSet()?.let(::toModel),
            it.forumTopicCreated()?.let(::toModel),
            it.forumTopicEdited()?.let(::toModel),
            it.forumTopicClosed()?.let(::toModel),
            it.forumTopicReopened()?.let(::toModel),
            it.generalForumTopicHidden()?.let(::toModel),
            it.generalForumTopicUnhidden()?.let(::toModel),
            it.giveawayCreated()?.let(::toModel),
            it.giveaway()?.let(::toModel),
            it.giveawayWinners()?.let(::toModel),
            it.giveawayCompleted()?.let(::toModel),
            it.videoChatScheduled()?.let(::toModel),
            it.videoChatEnded()?.let(::toModel),
            it.videoChatParticipantsInvited()?.let(::toModel),
            it.webAppData()?.let(::toModel),
            it.replyMarkup()?.let(::toModel)
        )
    }

    @EnumMapping(nameTransformationStrategy = "case", configuration = "upper")
    abstract fun toModel(src: ChatFullInfo.Type?): TelegramChat.Type?

    @EnumMapping(nameTransformationStrategy = "case", configuration = "upper")
    abstract fun toModel(src: Chat.Type?): TelegramChat.Type?

    @EnumMapping(nameTransformationStrategy = "case", configuration = "upper")
    abstract fun toModel(src: MessageEntity.Type?): TelegramMessageEntity.Type?
    @InheritInverseConfiguration
    abstract fun toDto(src: TelegramMessageEntity.Type?): MessageEntity.Type?

    @EnumMapping(nameTransformationStrategy = "case", configuration = "upper")
    abstract fun toModel(src: Sticker.Type?): TelegramSticker.Type?
    @InheritInverseConfiguration
    abstract fun toDto(src: TelegramSticker.Type?): Sticker.Type?

    @EnumMapping(nameTransformationStrategy = "case", configuration = "upper")
    abstract fun toModel(src: Poll.Type?): TelegramPoll.Type?
    @InheritInverseConfiguration
    abstract fun toDto(src: TelegramPoll.Type?): Poll.Type?

    @EnumMapping(nameTransformationStrategy = "case", configuration = "upper")
    abstract fun toModel(src: EncryptedPassportElement.Type?): TelegramEncryptedPassportElement.Type?
    @InheritInverseConfiguration
    abstract fun toDto(src: TelegramEncryptedPassportElement.Type?): EncryptedPassportElement.Type?

    fun toModel(dto: ExternalReplyInfo?): TelegramExternalReplyInfo? = dto?.let {
        TelegramExternalReplyInfo(
            it.origin().let(::toModel)!!,
            it.chat()?.id(),
            it.messageId(),
            it.linkPreviewOptions()?.let(::toModel),
            it.animation()?.let(::toModel),
            it.audio()?.let(::toModel),
            it.document()?.let(::toModel),
            it.paidMedia()?.let(::toModel),
            it.photo()?.mapNotNull(::toModel) ?: emptyList(),
            it.sticker()?.let(::toModel),
            it.story()?.let(::toModel),
            it.video()?.let(::toModel),
            it.videoNote()?.let(::toModel),
            it.voice()?.let(::toModel),
            booleanOrFalse(it.hasMediaSpoiler()),
            it.contact()?.let(::toModel),
            it.dice()?.let(::toModel),
            it.game()?.let(::toModel),
            it.giveaway()?.let(::toModel),
            it.giveawayWinners()?.let(::toModel),
            it.invoice()?.let(::toModel),
            it.location()?.let(::toModel),
            it.poll()?.let(::toModel),
            it.venue()?.let(::toModel)
        )
    }

    fun toModel(dto: ChatBackground?): TelegramChatBackground? = dto?.let {
        TelegramChatBackground(TelegramBackgroundType.Unknown()) // Type is unavailable
    }

    fun toModel(dto: ChatMember?): TelegramChatMember? {
        return when (dto?.status()) {
            null -> null
            ChatMember.Status.administrator -> TelegramChatMember.Administrator(
                user = toModel(dto.user()),
                canBeEdited = dto.canBeEdited() == true,
                isAnonymous = dto.isAnonymous == true,
                customTitle = dto.customTitle(),
                canManageChat = dto.canManageChat() == true,
                canDeleteMessages = dto.canDeleteMessages() == true,
                canManageVideoChats = dto.canManageVideoChats() == true,
                canRestrictMembers = dto.canRestrictMembers() == true,
                canPromoteMembers = dto.canPromoteMembers() == true,
                canChangeInfo = dto.canChangeInfo() == true,
                canInviteUsers = dto.canInviteUsers() == true,
                canPostStories = dto.canPostStories() == true,
                canEditStories = dto.canEditStories() == true,
                canDeleteStories = dto.canDeleteStories() == true,
                canPostMessages = dto.canPostMessages() == true,
                canEditMessages = dto.canEditMessages() == true,
                canPinMessages = dto.canPinMessages() == true,
                canManageTopics = dto.canManageTopics() == true
            )
            ChatMember.Status.member -> TelegramChatMember.Member(
                user = toModel(dto.user()),
                untilDate = dto.untilDate()?.let(::integerToZonedDateTime)
            )
            ChatMember.Status.restricted -> TelegramChatMember.Restricted(
                user = toModel(dto.user()),
                isMember = dto.isMember == true,
                untilDate = dto.untilDate()?.let(::integerToZonedDateTime),
                canSendMessages = dto.canSendMessages() == true,
                canSendAudios = dto.canSendAudios() == true,
                canSendDocuments = dto.canSendDocuments() == true,
                canSendPhotos = dto.canSendPhotos() == true,
                canSendVideos = dto.canSendVideos() == true,
                canSendVideoNotes = dto.canSendVideoNotes() == true,
                canSendVoiceNotes = dto.canSendVoiceNotes() == true,
                canSendPolls = dto.canSendPolls() == true,
                canSendOtherMessages = dto.canSendOtherMessages() == true,
                canAddWebPagePreviews = dto.canAddWebPagePreviews() == true,
                canChangeInfo = dto.canChangeInfo() == true,
                canInviteUsers = dto.canInviteUsers() == true,
                canPinMessages = dto.canPinMessages() == true,
                canManageTopics = dto.canManageTopics() == true
            )
            ChatMember.Status.creator -> TelegramChatMember.Owner(
                user = toModel(dto.user()),
                isAnonymous = dto.isAnonymous == true,
                customTitle = dto.customTitle()
            )
            ChatMember.Status.left -> TelegramChatMember.Left(toModel(dto.user()))
            ChatMember.Status.kicked -> TelegramChatMember.Banned(
                user = toModel(dto.user()),
                untilDate = dto.untilDate()?.let(::integerToZonedDateTime)
            )
        }
    }

    fun toModel(dto: MessageOrigin?): TelegramMessageOrigin? {
        return when (dto) {
            null -> null
            is MessageOriginUser -> TelegramMessageOrigin.User(
                date = integerToZonedDateTime(dto.date())!!,
                senderUserId = dto.senderUser().id()
            )
            is MessageOriginHiddenUser -> TelegramMessageOrigin.HiddenUser(
                date = integerToZonedDateTime(dto.date())!!,
                senderUserName = dto.senderUserName()
            )
            is MessageOriginChat -> TelegramMessageOrigin.Chat(
                date = integerToZonedDateTime(dto.date())!!,
                senderChatId = dto.senderChat().id(),
                authorSignature = dto.authorSignature()
            )
            is MessageOriginChannel -> TelegramMessageOrigin.Channel(
                date = integerToZonedDateTime(dto.date())!!,
                chatId = dto.chat().id(),
                messageId = dto.messageId(),
                authorSignature = dto.authorSignature()
            )
            else -> {
                return TelegramMessageOrigin.Unknown(integerToZonedDateTime(dto.date())!!)
            }
        }
    }

    fun toModel(dto: ReactionType?): TelegramReactionType? {
        return when (dto) {
            null -> null
            is ReactionTypeCustomEmoji -> TelegramReactionType.CustomEmoji(dto.customEmojiId())
            is ReactionTypeEmoji -> TelegramReactionType.Emoji(dto.emoji())
            is ReactionTypePaid -> TelegramReactionType.Paid()
            else -> throw IllegalArgumentException("Unknown ReactionType: ${this::class.java.name}")
        }
    }

    fun toModel(dto: ChatBoostSource?): TelegramChatBoostSource? {
        return when (dto) {
            null -> null
            // "user" is protected, so we can't access it directly
            is ChatBoostSourcePremium -> TelegramChatBoostSource.Premium(
                user = TelegramUser(0, false, "unknown", metadata = TelegramUser.Metadata(false))
            )
            // "user" is protected, so we can't access it directly
            is ChatBoostSourceGiftCode -> TelegramChatBoostSource.GiftCode(
                user = TelegramUser(0, false, "unknown", metadata = TelegramUser.Metadata(false))
            )
            is ChatBoostSourceGiveaway -> TelegramChatBoostSource.Giveaway(
                giveawayMessageId = dto.giveawayMessageId(),
                // "user" is protected, so we can't access it directly
                user = TelegramUser(0, false, "unknown", metadata = TelegramUser.Metadata(false)),
                prizeStarCount = dto.prizeStarCount(),
                isUnclaimed = dto.isUnclaimed == true,
            )
            else -> throw IllegalArgumentException("Unknown ChatBoostSource type: ${this::class.java.name}")
        }
    }

    fun toModel(dto: PaidMedia?): TelegramPaidMedia? {
        return when (dto) {
            null -> null
            is PaidMediaPreview -> TelegramPaidMedia.Preview(dto.width, dto.height, dto.duration)
            is PaidMediaPhoto -> TelegramPaidMedia.Photo(dto.photo.mapNotNull(::toModel))
            is PaidMediaVideo -> TelegramPaidMedia.Video(toModel(dto.video)!!)
            else -> TelegramPaidMedia.Unknown()
        }
    }

    fun toModel(dto: BackgroundType?): TelegramBackgroundType? {
        return when (dto) {
            null -> null
            is BackgroundTypeChatTheme -> TelegramBackgroundType.ChatTheme(dto.themeName())
            is BackgroundTypeFill -> TelegramBackgroundType.Fill(toModel(dto.fill())!!, dto.darkThemeDimming())
            is BackgroundTypePattern -> TelegramBackgroundType.Pattern(
                document = toModel(dto.document())!!,
                fill = toModel(dto.fill())!!,
                intensity = dto.intensity(),
                isInverted = dto.isInverted,
                isMoving = dto.isMoving
            )
            is BackgroundTypeWallpaper -> TelegramBackgroundType.Wallpaper(
                document = toModel(dto.document())!!,
                darkThemeDimming = dto.darkThemeDimming(),
                isBlurred = dto.isBlurred,
                isMoving = dto.isMoving
            )
            else -> TelegramBackgroundType.Unknown()
        }
    }

    fun toModel(dto: BackgroundFill?): TelegramBackgroundFill? {
        return when (dto) {
            null -> null
            is BackgroundFillSolid -> TelegramBackgroundFill.Solid(Color(dto.color()))
            is BackgroundFillGradient -> TelegramBackgroundFill.Gradient(
                topColor = Color(dto.topColor()),
                bottomColor = Color(dto.bottomColor()),
                rotationAngle = dto.rotationAngle()
            )
            is BackgroundFillFreeformGradient -> TelegramBackgroundFill.FreeformGradient(
                colors = dto.colors().map { Color(it) }
            )
            else -> TelegramBackgroundFill.Unknown()
        }
    }

    fun toModel(dto: MaybeInaccessibleMessage?): TelegramMaybeInaccessibleMessage? {
        if (dto == null) {
            return null
        }
        return if (dto.date() == 0) {
            toModel(dto as InaccessibleMessage)
        } else {
            toModel(dto as Message)
        }
    }

    fun toModel(dto: LoginUrl?): TelegramLoginUrl? {
        if (dto == null) {
            return null
        }
        return TelegramLoginUrl("<LIBRARY SETTER NOT AVAILABLE>") // Setters are not available
    }

    fun toModel(dto: SwitchInlineQueryChosenChat?): TelegramSwitchInlineQueryChosenChat? {
        if (dto == null) {
            return null
        }
        return TelegramSwitchInlineQueryChosenChat() // Setters are not available
    }

    fun toDto(model: TelegramMessageEntity?): MessageEntity? {
        if (model == null) {
            return null
        }
        val result = MessageEntity(toDto(model.type), model.offset, model.length)
        result.url(model.url)
        result.user(User(model.userId))
        result.language(model.language)
        result.customEmojiId(model.customEmojiId)
        return result
    }

    fun toDto(model: TelegramInlineKeyboardMarkup?): InlineKeyboardMarkup? {
        if (model == null) {
            return null
        }
        return InlineKeyboardMarkup(
            *model.inlineKeyboard.map { row ->
                row.map { toDto(it) }.toTypedArray()
            }.toTypedArray()
        )
    }

    fun toDto(model: TelegramLoginUrl?): LoginUrl? {
        if (model == null) {
            return null
        }
        return LoginUrl(model.url)
            .forwardText(model.forwardText)
            .botUsername(model.botUsername)
            .requestWriteAccess(model.requestWriteAccess)
    }

    fun toDto(model: TelegramSwitchInlineQueryChosenChat?): SwitchInlineQueryChosenChat? {
        if (model == null) {
            return null
        }
        return SwitchInlineQueryChosenChat()
            .query(model.query)
            .allowUserChats(model.allowUserChats)
            .allowBotChats(model.allowBotChats)
            .allowGroupChats(model.allowGroupChats)
            .allowChannelChats(model.allowChannelChats)
    }


    @Suppress("unused") // Used in this mapper
    fun integerToZonedDateTime(integer: Int?): ZonedDateTime? {
        if (integer == null) {
            return null
        }
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(integer.toLong()), UTC)
    }

    @Suppress("unused") // Used in this mapper
    fun chatArrayAsIdListOrEmpty(chats: Array<Chat>?): List<Long> {
        if (chats == null) {
            return emptyList()
        }
        return chats.map { it.id() }
    }

    @Suppress("unused") // Used in this mapper
    fun userArrayAsIdListOrEmpty(users: Array<User>?): List<Long> {
        if (users == null) {
            return emptyList()
        }
        return users.map { it.id() }
    }

    @Suppress("unused") // Used in this mapper
    fun intArrayToIntListOrEmptyList(integers: IntArray?): List<Int> {
        return integers?.toList() ?: emptyList()
    }

    @Suppress("unused") // Used in this mapper
    fun booleanOrFalse(value: Boolean?): Boolean = value == true
}
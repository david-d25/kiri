package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramInaccessibleMessage
import space.davids_digital.kiri.model.telegram.TelegramMaybeInaccessibleMessage
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.orm.entity.telegram.*
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramMessageEntityId
import space.davids_digital.kiri.orm.mapping.DateTimeMapper

@Mapper(
    componentModel = "spring",
    uses = [
        DateTimeMapper::class,
        TelegramMessageOriginEntityMapper::class,
        TelegramTextQuoteEntityMapper::class,
        TelegramMessageEntityEntityMapper::class,
        TelegramPhotoSizeEntityMapper::class,
        TelegramContactEntityMapper::class,
        TelegramLocationEntityMapper::class,
        TelegramLinkPreviewOptionsEntityMapper::class,
        TelegramDiceEntityMapper::class,
        TelegramGameEntityMapper::class,
        TelegramPollEntityMapper::class,
        TelegramVenueEntityMapper::class,
        TelegramAnimationEntityMapper::class,
        TelegramAudioEntityMapper::class,
        TelegramDocumentEntityMapper::class,
        TelegramStickerEntityMapper::class,
        TelegramVideoEntityMapper::class,
        TelegramVoiceEntityMapper::class,
        TelegramVideoNoteEntityMapper::class,
        TelegramUserEntityMapper::class,
        TelegramInvoiceEntityMapper::class,
        TelegramSuccessfulPaymentEntityMapper::class,
        TelegramRefundedPaymentEntityMapper::class,
        TelegramInlineKeyboardMarkupEntityMapper::class,
        TelegramChatBoostAddedEntityMapper::class,
        TelegramGiveawayWinnersEntityMapper::class,
        TelegramGiveawayCompletedEntityMapper::class,
        TelegramWriteAccessAllowedEntityMapper::class,
        TelegramStoryEntityMapper::class,
        TelegramMessageAutoDeleteTimerChangedEntityMapper::class,
        TelegramPassportDataEntityMapper::class,
        TelegramChatBackgroundEntityMapper::class,
        TelegramForumTopicEditedEntityMapper::class,
        TelegramForumTopicCreatedEntityMapper::class,
        TelegramForumTopicClosedEntityMapper::class,
        TelegramForumTopicReopenedEntityMapper::class,
        TelegramGeneralForumTopicHiddenEntityMapper::class,
        TelegramGeneralForumTopicUnhiddenEntityMapper::class,
        TelegramGiveawayCreatedEntityMapper::class,
        TelegramGiveawayEntityMapper::class,
        TelegramVideoChatScheduledEntityMapper::class,
        TelegramVideoChatEndedEntityMapper::class,
        TelegramVideoChatParticipantsInvitedEntityMapper::class,
        TelegramUsersSharedEntityMapper::class,
        TelegramChatSharedEntityMapper::class,
        TelegramProximityAlertTriggeredEntityMapper::class,
        TelegramWebAppDataEntityMapper::class,
        TelegramPaidMediaInfoEntityMapper::class,
        TelegramExternalReplyInfoEntityMapper::class,
    ]
)
abstract class TelegramMessageEntityMapper {
    @Mapping(target = "pinnedMessage", source = ".", qualifiedByName = ["mapPinnedMessage"])
    @Mapping(source = "id.messageId", target = "messageId")
    @Mapping(source = "id.chatId", target = "chatId")
    @Mapping(source = "topicMessage", target = "isTopicMessage")
    @Mapping(source = "automaticForward", target = "isAutomaticForward")
    @Mapping(source = "fromOffline", target = "isFromOffline")
    abstract fun toModel(entity: TelegramMessageEntity?): TelegramMessage?

    @Mapping(target = "pinnedMessage", ignore = true)
    @Mapping(target = "pinnedInaccessibleMessage", ignore = true)
    @Mapping(source = ".", target = "id", qualifiedByName = ["toMessageId"])
    abstract fun toEntity(model: TelegramMessage?): TelegramMessageEntity?

    @Mapping(source = "id.messageId", target = "messageId")
    @Mapping(source = "id.chatId", target = "chatId")
    abstract fun toModel(entity: TelegramInaccessibleMessageEntity?): TelegramInaccessibleMessage?

    @Mapping(source = ".", target = "id", qualifiedByName = ["toMessageId"])
    abstract fun toEntity(model: TelegramInaccessibleMessage?): TelegramInaccessibleMessageEntity?

    @Named("toMessageId")
    fun toMessageId(model: TelegramMessage?): TelegramMessageEntityId? =
        if (model == null) null else TelegramMessageEntityId(model.chatId, model.messageId)

    @Named("toMessageId")
    fun toMessageId(model: TelegramInaccessibleMessage?): TelegramMessageEntityId? =
        if (model == null) null else TelegramMessageEntityId(model.chatId, model.messageId)

    @AfterMapping
    protected fun setBackReferences(@MappingTarget entity: TelegramMessageEntity?, model: TelegramMessage?) {
        if (entity == null || model == null) return
        entity.entities.forEach { it.parentMessageText = entity }
        entity.captionEntities.forEach { it.parentMessageCaption = entity }

        // Set correct pinnedMessage field
        when (model.pinnedMessage) {
            is TelegramMessage -> entity.pinnedMessage = toEntity(model.pinnedMessage)
            is TelegramInaccessibleMessage -> entity.pinnedInaccessibleMessage = toEntity(model.pinnedMessage)
            null -> {
                entity.pinnedMessage = null
                entity.pinnedInaccessibleMessage = null
            }
        }
    }

    @Named("mapPinnedMessage")
    fun mapPinnedMessage(entity: TelegramMessageEntity?): TelegramMaybeInaccessibleMessage? {
        return when {
            null == entity -> null
            entity.pinnedMessage != null -> toModel(entity.pinnedMessage!!)
            entity.pinnedInaccessibleMessage != null -> toModel(entity.pinnedInaccessibleMessage!!)
            else -> null
        }
    }
}

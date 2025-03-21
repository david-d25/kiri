package space.davids_digital.kiri.orm.mapping

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramGeneralForumTopicHidden
import space.davids_digital.kiri.model.telegram.TelegramInaccessibleMessage
import space.davids_digital.kiri.model.telegram.TelegramMaybeInaccessibleMessage
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.orm.entity.telegram.*

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
    ]
)
abstract class TelegramMessageEntityMapper {
    @Mapping(target = "pinnedMessage", source = ".", qualifiedByName = ["mapPinnedMessage"])
    abstract fun toModel(entity: TelegramMessageEntity): TelegramMessage

    @Mapping(target = "pinnedMessage", ignore = true)
    @Mapping(target = "pinnedInaccessibleMessage", ignore = true)
    abstract fun toEntity(model: TelegramMessage): TelegramMessageEntity

    @AfterMapping
    protected fun setBackReferences(@MappingTarget entity: TelegramMessageEntity, model: TelegramMessage) {
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
    fun mapPinnedMessage(entity: TelegramMessageEntity): TelegramMaybeInaccessibleMessage? {
        return when {
            entity.pinnedMessage != null -> toModel(entity.pinnedMessage!!)
            entity.pinnedInaccessibleMessage != null -> toModel(entity.pinnedInaccessibleMessage!!)
            else -> null
        }
    }

    abstract fun toModel(entity: TelegramInaccessibleMessageEntity): TelegramInaccessibleMessage
    abstract fun toEntity(model: TelegramInaccessibleMessage): TelegramInaccessibleMessageEntity
}

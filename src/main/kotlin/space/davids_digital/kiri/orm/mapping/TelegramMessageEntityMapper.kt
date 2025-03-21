package space.davids_digital.kiri.orm.mapping

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.orm.entity.telegram.*
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Mapper(
    componentModel = "spring",
    uses = [
        TelegramMessageOriginEntityMapper::class,
        TelegramTextQuoteEntityMapper::class,
        TelegramMessageEntityEntityMapper::class,
        TelegramPhotoSizeEntityMapper::class,
        TelegramContactEntityMapper::class,
        TelegramLocationEntityMapper::class,
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
    ]
)
abstract class TelegramMessageEntityMapper {
    abstract fun toModel(entity: TelegramMessageEntity): TelegramMessage

    abstract fun toEntity(model: TelegramMessage): TelegramMessageEntity

    @AfterMapping
    protected fun setBackReferences(@MappingTarget entity: TelegramMessageEntity) {
        entity.entities.forEach { it.parentMessageText = entity }
        entity.captionEntities.forEach { it.parentMessageCaption = entity }
        entity.replyToMessage?.let { entity.replyToMessage = it }
    }

    @BeforeMapping
    protected fun mapZonedToOffset(model: TelegramMessage): OffsetDateTime = model.date.toOffsetDateTime()

    @AfterMapping
    protected fun mapOffsetToZoned(entity: TelegramMessageEntity): java.time.ZonedDateTime =
        entity.date.atZoneSameInstant(ZoneOffset.UTC)
}

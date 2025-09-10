package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramExternalReplyInfo
import space.davids_digital.kiri.orm.entity.telegram.TelegramExternalReplyInfoEntity

@Mapper(
    componentModel = "spring",
    uses = [
        TelegramMessageOriginEntityMapper::class,
        TelegramLinkPreviewOptionsEntityMapper::class,
        TelegramAnimationEntityMapper::class,
        TelegramAudioEntityMapper::class,
        TelegramDocumentEntityMapper::class,
        TelegramPaidMediaInfoEntityMapper::class,
        TelegramPhotoSizeEntityMapper::class,
        TelegramStickerEntityMapper::class,
        TelegramStoryEntityMapper::class,
        TelegramVideoEntityMapper::class,
        TelegramVideoNoteEntityMapper::class,
        TelegramVoiceEntityMapper::class,
        TelegramContactEntityMapper::class,
        TelegramDiceEntityMapper::class,
        TelegramGameEntityMapper::class,
        TelegramGiveawayEntityMapper::class,
        TelegramGiveawayWinnersEntityMapper::class,
        TelegramInvoiceEntityMapper::class,
        TelegramLocationEntityMapper::class,
        TelegramPollEntityMapper::class,
        TelegramVenueEntityMapper::class
    ]
)
interface TelegramExternalReplyInfoEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramExternalReplyInfo?): TelegramExternalReplyInfoEntity?

    fun toModel(entity: TelegramExternalReplyInfoEntity?): TelegramExternalReplyInfo?
}
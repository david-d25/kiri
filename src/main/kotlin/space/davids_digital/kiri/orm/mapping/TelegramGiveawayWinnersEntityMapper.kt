package space.davids_digital.kiri.orm.mapping

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramGiveawayWinners
import space.davids_digital.kiri.orm.entity.telegram.TelegramGiveawayWinnersEntity
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramGiveawayWinnersId

@Mapper(componentModel = "spring", uses = [DateTimeMapper::class])
abstract class TelegramGiveawayWinnersEntityMapper {
    @Mapping(source = ".", target = "id", qualifiedByName = ["toGiveawayWinnersId"])
    abstract fun toEntity(model: TelegramGiveawayWinners?): TelegramGiveawayWinnersEntity?

    @Mapping(source = "id.giveawayMessageId", target = "giveawayMessageId")
    @Mapping(source = "id.chatId", target = "chatId")
    abstract fun toModel(entity: TelegramGiveawayWinnersEntity?): TelegramGiveawayWinners?

    @Named("toGiveawayWinnersId")
    fun toGiveawayWinnersId(model: TelegramGiveawayWinners?): TelegramGiveawayWinnersId? {
        if (model == null) {
            return null
        }
        return TelegramGiveawayWinnersId(
            chatId = model.chatId,
            giveawayMessageId = model.giveawayMessageId
        )
    }
}

package space.davids_digital.kiri.orm.mapping

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramGiveawayWinners
import space.davids_digital.kiri.orm.entity.telegram.TelegramGiveawayWinnersEntity

@Mapper(componentModel = "spring")
abstract class TelegramGiveawayWinnersEntityMapper {
    @Mapping(target = "id", expression = "java(new TelegramGiveawayWinnersId(model.getChatId(), model.getGiveawayMessageId()))")
    @Mapping(target = "winnersSelectionDate", expression = "java(model.getWinnersSelectionDate().toOffsetDateTime())")
    abstract fun toEntity(model: TelegramGiveawayWinners): TelegramGiveawayWinnersEntity

    @Mapping(target = "chatId", expression = "java(entity.getId().getChatId())")
    @Mapping(target = "giveawayMessageId", expression = "java(entity.getId().getGiveawayMessageId())")
    @Mapping(target = "winnersSelectionDate", expression = "java(entity.getWinnersSelectionDate().atZoneSameInstant(ZoneOffset.UTC))")
    abstract fun toModel(entity: TelegramGiveawayWinnersEntity): TelegramGiveawayWinners
}

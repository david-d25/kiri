package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramGiveaway
import space.davids_digital.kiri.orm.entity.telegram.TelegramGiveawayEntity
import java.time.OffsetDateTime
import java.time.ZonedDateTime

// TODO use DateTimeMapper
@Mapper(componentModel = "spring")
abstract class TelegramGiveawayEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    @Mapping(source = "winnersSelectionDate", target = "winnersSelectionDate", qualifiedByName = ["zonedToOffset"])
    @Mapping(source = "chats", target = "chatIds")
    abstract fun toEntity(model: TelegramGiveaway?): TelegramGiveawayEntity?

    @Mapping(source = "winnersSelectionDate", target = "winnersSelectionDate", qualifiedByName = ["offsetToZoned"])
    @Mapping(source = "chatIds", target = "chats")
    abstract fun toModel(entity: TelegramGiveawayEntity?): TelegramGiveaway?

    @Named("zonedToOffset")
    protected fun zonedToOffset(zonedDateTime: ZonedDateTime?): OffsetDateTime? = zonedDateTime?.toOffsetDateTime()

    @Named("offsetToZoned")
    protected fun offsetToZoned(offsetDateTime: OffsetDateTime?): ZonedDateTime? = offsetDateTime?.toZonedDateTime()
}

package space.davids_digital.kiri.orm.mapping

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramMessageEntity
import space.davids_digital.kiri.orm.entity.telegram.TelegramMessageEntityEntity

@Mapper(componentModel = "spring")
interface TelegramMessageEntityEntityMapper {
    @Mapping(target = "type", source = "type", qualifiedByName = ["enumToString"])
    @Mapping(target = "internalId", ignore = true)
    @Mapping(target = "parentGame", ignore = true)
    @Mapping(target = "parentMessageText", ignore = true)
    @Mapping(target = "parentMessageCaption", ignore = true)
    @Mapping(target = "parentPollQuestion", ignore = true)
    @Mapping(target = "parentPollExplanation", ignore = true)
    @Mapping(target = "parentPollOption", ignore = true)
    @Mapping(target = "parentTextQuote", ignore = true)
    fun toEntity(model: TelegramMessageEntity?): TelegramMessageEntityEntity?

    @Mapping(target = "type", source = "type", qualifiedByName = ["stringToEnum"])
    fun toModel(entity: TelegramMessageEntityEntity?): TelegramMessageEntity?

    companion object {
        @Named("enumToString")
        @JvmStatic
        fun enumToString(type: TelegramMessageEntity.Type): String = type.name

        @Named("stringToEnum")
        @JvmStatic
        fun stringToEnum(type: String): TelegramMessageEntity.Type = TelegramMessageEntity.Type.valueOf(type)
    }
}

package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramMessageOrigin
import space.davids_digital.kiri.model.telegram.TelegramMessageOrigin.*
import space.davids_digital.kiri.orm.entity.telegram.*
import java.time.OffsetDateTime

@Mapper(componentModel = "spring", uses = [DateTimeMapper::class])
abstract class TelegramMessageOriginEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: User): TelegramMessageOriginUserEntity

    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: HiddenUser): TelegramMessageOriginHiddenUserEntity

    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: Chat): TelegramMessageOriginChatEntity

    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: Channel): TelegramMessageOriginChannelEntity

    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: Unknown): TelegramMessageOriginUnknownEntity

    fun toEntity(model: TelegramMessageOrigin?): TelegramMessageOriginEntity? {
        return when (model) {
            is User -> toEntity(model).apply { date = OffsetDateTime.from(model.date) }
            is HiddenUser -> toEntity(model).apply { date = OffsetDateTime.from(model.date) }
            is Chat -> toEntity(model).apply { date = OffsetDateTime.from(model.date) }
            is Channel -> toEntity(model).apply { date = OffsetDateTime.from(model.date) }
            is Unknown -> toEntity(model).apply { date = OffsetDateTime.from(model.date) }
            null -> null
        }
    }

    abstract fun toModel(entity: TelegramMessageOriginUserEntity): User
    abstract fun toModel(entity: TelegramMessageOriginHiddenUserEntity): HiddenUser
    abstract fun toModel(entity: TelegramMessageOriginChatEntity): Chat
    abstract fun toModel(entity: TelegramMessageOriginChannelEntity): Channel

    @Mapping(target = "copy", ignore = true)
    abstract fun toModel(entity: TelegramMessageOriginUnknownEntity): Unknown

    fun toModel(entity: TelegramMessageOriginEntity?): TelegramMessageOrigin? {
        if (entity == null) return null
        val date = entity.date.toZonedDateTime()
        return when (entity) {
            is TelegramMessageOriginUserEntity -> toModel(entity).copy(date = date)
            is TelegramMessageOriginHiddenUserEntity -> toModel(entity).copy(date = date)
            is TelegramMessageOriginChatEntity -> toModel(entity).copy(date = date)
            is TelegramMessageOriginChannelEntity -> toModel(entity).copy(date = date)
            is TelegramMessageOriginUnknownEntity -> toModel(entity).copy(date = date)
            else -> error("Unknown TelegramMessageOriginEntity subtype: ${entity::class.qualifiedName}")
        }
    }
}

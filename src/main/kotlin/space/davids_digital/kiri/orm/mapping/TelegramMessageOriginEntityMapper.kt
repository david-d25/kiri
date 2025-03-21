package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramMessageOrigin
import space.davids_digital.kiri.model.telegram.TelegramMessageOrigin.*
import space.davids_digital.kiri.orm.entity.telegram.*
import java.time.OffsetDateTime

@Mapper(componentModel = "spring")
interface TelegramMessageOriginEntityMapper {
    fun toEntity(model: User): TelegramMessageOriginUserEntity
    fun toEntity(model: HiddenUser): TelegramMessageOriginHiddenUserEntity
    fun toEntity(model: Chat): TelegramMessageOriginChatEntity
    fun toEntity(model: Channel): TelegramMessageOriginChannelEntity
    fun toEntity(model: Unknown): TelegramMessageOriginUnknownEntity
    fun toEntity(model: TelegramMessageOrigin): TelegramMessageOriginEntity {
        return when (model) {
            is User -> toEntity(model).apply { date = OffsetDateTime.from(model.date) }
            is HiddenUser -> toEntity(model).apply { date = OffsetDateTime.from(model.date) }
            is Chat -> toEntity(model).apply { date = OffsetDateTime.from(model.date) }
            is Channel -> toEntity(model).apply { date = OffsetDateTime.from(model.date) }
            is Unknown -> toEntity(model).apply { date = OffsetDateTime.from(model.date) }
        }
    }

    fun toModel(entity: TelegramMessageOriginUserEntity): User
    fun toModel(entity: TelegramMessageOriginHiddenUserEntity): HiddenUser
    fun toModel(entity: TelegramMessageOriginChatEntity): Chat
    fun toModel(entity: TelegramMessageOriginChannelEntity): Channel
    fun toModel(entity: TelegramMessageOriginUnknownEntity): Unknown
    fun toModel(entity: TelegramMessageOriginEntity): TelegramMessageOrigin {
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

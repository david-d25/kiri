package space.davids_digital.kiri.orm.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.orm.entity.TelegramUserMetadataEntity

@Mapper
abstract class TelegramUserMetadataEntityMapper {
    @Mapping(target = "copy", ignore = true)
    @Mapping(target = "isBlocked", source = "blocked")
    abstract fun toModel(entity: TelegramUserMetadataEntity?): TelegramUser.Metadata?
    abstract fun toEntity(model: TelegramUser.Metadata?, userId: Long?): TelegramUserMetadataEntity?
}
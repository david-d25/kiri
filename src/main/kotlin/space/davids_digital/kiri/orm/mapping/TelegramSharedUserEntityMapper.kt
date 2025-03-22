package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramSharedUser
import space.davids_digital.kiri.orm.entity.telegram.TelegramSharedUserEntity

@Mapper(componentModel = "spring", uses = [TelegramPhotoSizeEntityMapper::class])
interface TelegramSharedUserEntityMapper {
    fun toEntity(model: TelegramSharedUser?): TelegramSharedUserEntity?
    fun toModel(entity: TelegramSharedUserEntity?): TelegramSharedUser?
}

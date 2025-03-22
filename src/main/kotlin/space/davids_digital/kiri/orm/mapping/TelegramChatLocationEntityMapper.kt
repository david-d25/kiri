package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramChatLocation
import space.davids_digital.kiri.orm.entity.telegram.TelegramChatLocationEntity

@Mapper(
    componentModel = "spring",
    uses = [TelegramLocationEntityMapper::class]
)
interface TelegramChatLocationEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramChatLocation?): TelegramChatLocationEntity?
    fun toModel(entity: TelegramChatLocationEntity?): TelegramChatLocation?
}

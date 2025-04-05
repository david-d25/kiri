package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramEncryptedPassportElement
import space.davids_digital.kiri.orm.entity.telegram.TelegramEncryptedPassportElementEntity

@Mapper(
    componentModel = "spring",
    uses = [TelegramPassportFileEntityMapper::class]
)
interface TelegramEncryptedPassportElementEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramEncryptedPassportElement?): TelegramEncryptedPassportElementEntity?
    fun toModel(entity: TelegramEncryptedPassportElementEntity?): TelegramEncryptedPassportElement?
}

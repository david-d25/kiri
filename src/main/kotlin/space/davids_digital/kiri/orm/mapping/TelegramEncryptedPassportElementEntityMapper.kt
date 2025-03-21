package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramEncryptedPassportElement
import space.davids_digital.kiri.orm.entity.telegram.TelegramEncryptedPassportElementEntity

@Mapper(
    componentModel = "spring",
    uses = [TelegramPassportFileEntityMapper::class]
)
interface TelegramEncryptedPassportElementEntityMapper {
    fun toEntity(model: TelegramEncryptedPassportElement): TelegramEncryptedPassportElementEntity
    fun toModel(entity: TelegramEncryptedPassportElementEntity): TelegramEncryptedPassportElement
}

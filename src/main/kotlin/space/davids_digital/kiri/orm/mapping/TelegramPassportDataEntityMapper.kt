package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import space.davids_digital.kiri.model.telegram.TelegramPassportData
import space.davids_digital.kiri.orm.entity.telegram.*

@Mapper(
    componentModel = "spring",
    uses = [
        TelegramEncryptedPassportElementEntityMapper::class,
        TelegramEncryptedCredentialsEntityMapper::class
    ]
)
abstract class TelegramPassportDataEntityMapper {
    abstract fun toEntity(model: TelegramPassportData): TelegramPassportDataEntity
    abstract fun toModel(entity: TelegramPassportDataEntity): TelegramPassportData
}

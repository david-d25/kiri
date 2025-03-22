package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import space.davids_digital.kiri.model.telegram.TelegramEncryptedCredentials
import space.davids_digital.kiri.orm.entity.telegram.TelegramEncryptedCredentialsEntity

@Mapper(componentModel = "spring")
interface TelegramEncryptedCredentialsEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    fun toEntity(model: TelegramEncryptedCredentials?): TelegramEncryptedCredentialsEntity?
    fun toModel(entity: TelegramEncryptedCredentialsEntity?): TelegramEncryptedCredentials?
}

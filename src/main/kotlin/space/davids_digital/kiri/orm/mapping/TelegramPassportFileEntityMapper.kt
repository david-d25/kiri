package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import space.davids_digital.kiri.model.telegram.TelegramPassportFile
import space.davids_digital.kiri.orm.entity.telegram.TelegramPassportFileEntity
import java.time.OffsetDateTime
import java.time.ZonedDateTime

@Mapper(componentModel = "spring")
interface TelegramPassportFileEntityMapper {
    @Mapping(source = "fileId", target = "fileDownloadId")
    @Mapping(source = "fileDate", target = "fileDate", qualifiedByName = ["toOffset"])
    fun toEntity(model: TelegramPassportFile): TelegramPassportFileEntity

    @Mapping(source = "fileDownloadId", target = "fileId")
    @Mapping(source = "fileDate", target = "fileDate", qualifiedByName = ["toZoned"])
    fun toModel(entity: TelegramPassportFileEntity): TelegramPassportFile

    @Named("toOffset")
    fun toOffsetDateTime(zonedDateTime: ZonedDateTime): OffsetDateTime = zonedDateTime.toOffsetDateTime()

    @Named("toZoned")
    fun toZonedDateTime(offsetDateTime: OffsetDateTime): ZonedDateTime = offsetDateTime.toZonedDateTime()
}

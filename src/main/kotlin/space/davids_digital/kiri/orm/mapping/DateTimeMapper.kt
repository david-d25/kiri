package space.davids_digital.kiri.orm.mapping

import org.mapstruct.Mapper
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Mapper(componentModel = "spring")
abstract class DateTimeMapper {
    open fun zonedToOffset(zonedDateTime: ZonedDateTime?): OffsetDateTime? =
        zonedDateTime?.toOffsetDateTime()

    open fun offsetToZoned(offsetDateTime: OffsetDateTime?): ZonedDateTime? =
        offsetDateTime?.atZoneSameInstant(ZoneOffset.UTC)
}

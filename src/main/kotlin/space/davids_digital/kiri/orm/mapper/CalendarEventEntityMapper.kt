package space.davids_digital.kiri.orm.mapper

import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.Named
import space.davids_digital.kiri.model.CalendarEvent
import space.davids_digital.kiri.orm.entity.CalendarEventEntity
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Mapper(uses = [DateTimeMapper::class])
abstract class CalendarEventEntityMapper {

    @Mapping(target = "exdates", source = "exdates", qualifiedByName = ["offsetArrayToZonedList"])
    @Mapping(target = "missedPolicy", source = "missedPolicy", qualifiedByName = ["entityMissedPolicyToModel"])
    abstract fun toModel(entity: CalendarEventEntity?): CalendarEvent?

    @Mapping(target = "exdates", source = "exdates", qualifiedByName = ["zonedListToOffsetArray"])
    @Mapping(target = "missedPolicy", source = "missedPolicy", qualifiedByName = ["modelMissedPolicyToEntity"])
    abstract fun toEntity(model: CalendarEvent?): CalendarEventEntity?

    @Named("offsetArrayToZonedList")
    open fun offsetArrayToZonedList(value: Array<OffsetDateTime>?): List<ZonedDateTime> =
        value?.map { it.atZoneSameInstant(ZoneOffset.UTC) } ?: emptyList()

    @Named("zonedListToOffsetArray")
    open fun zonedListToOffsetArray(value: List<ZonedDateTime>?): Array<OffsetDateTime> =
        value?.map { it.toOffsetDateTime() }?.toTypedArray() ?: emptyArray()

    @Named("entityMissedPolicyToModel")
    open fun entityMissedPolicyToModel(value: CalendarEventEntity.MissedPolicy): CalendarEvent.MissedPolicy =
        CalendarEvent.MissedPolicy.valueOf(value.name)

    @Named("modelMissedPolicyToEntity")
    open fun modelMissedPolicyToEntity(value: CalendarEvent.MissedPolicy): CalendarEventEntity.MissedPolicy =
        CalendarEventEntity.MissedPolicy.valueOf(value.name)
}

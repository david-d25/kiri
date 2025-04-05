package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramPaidMediaInfo
import space.davids_digital.kiri.orm.entity.telegram.*

@Mapper(
    componentModel = "spring",
    uses = [TelegramPaidMediaEntityMapper::class]
)
abstract class TelegramPaidMediaInfoEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: TelegramPaidMediaInfo?): TelegramPaidMediaInfoEntity?
    abstract fun toModel(entity: TelegramPaidMediaInfoEntity?): TelegramPaidMediaInfo?

    @AfterMapping
    protected fun setBackReference(@MappingTarget entity: TelegramPaidMediaInfoEntity?) {
        entity?.paidMedia?.forEach { it.parentPaidMediaInfo = entity }
    }
}

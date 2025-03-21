package space.davids_digital.kiri.orm.mapping

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramPaidMediaInfo
import space.davids_digital.kiri.orm.entity.telegram.*

@Mapper(
    componentModel = "spring",
    uses = [TelegramPaidMediaEntityMapper::class]
)
abstract class TelegramPaidMediaInfoEntityMapper {
    abstract fun toModel(entity: TelegramPaidMediaInfoEntity): TelegramPaidMediaInfo
    abstract fun toEntity(model: TelegramPaidMediaInfo): TelegramPaidMediaInfoEntity

    @AfterMapping
    protected fun setBackReference(
        @MappingTarget entity: TelegramPaidMediaInfoEntity
    ) {
        entity.paidMedia.forEach { it.paidMediaInfo = entity }
    }
}

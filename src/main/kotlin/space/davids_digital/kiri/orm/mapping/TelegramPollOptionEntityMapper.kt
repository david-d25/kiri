package space.davids_digital.kiri.orm.mapping

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramPollOption
import space.davids_digital.kiri.orm.entity.telegram.*

@Mapper(
    componentModel = "spring",
    uses = [TelegramMessageEntityEntityMapper::class]
)
abstract class TelegramPollOptionEntityMapper {
    abstract fun toModel(entity: TelegramPollOptionEntity): TelegramPollOption
    abstract fun toEntity(model: TelegramPollOption): TelegramPollOptionEntity

    @AfterMapping
    protected fun setBackReferences(
        @MappingTarget entity: TelegramPollOptionEntity
    ) {
        entity.textEntities.forEach { it.parentPollOption = entity }
    }
}

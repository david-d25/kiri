package space.davids_digital.kiri.orm.mapping.telegram

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramPollOption
import space.davids_digital.kiri.orm.entity.telegram.*

@Mapper(
    componentModel = "spring",
    uses = [TelegramMessageEntityEntityMapper::class]
)
abstract class TelegramPollOptionEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: TelegramPollOption?): TelegramPollOptionEntity?
    abstract fun toModel(entity: TelegramPollOptionEntity?): TelegramPollOption?

    @AfterMapping
    protected fun setBackReferences(@MappingTarget entity: TelegramPollOptionEntity?) {
        entity?.textEntities?.forEach { it.parentPollOption = entity }
    }
}

package space.davids_digital.kiri.orm.mapping

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramGame
import space.davids_digital.kiri.orm.entity.telegram.*

@Mapper(
    componentModel = "spring",
    uses = [
        TelegramAnimationEntityMapper::class,
        TelegramPhotoSizeEntityMapper::class,
        TelegramMessageEntityEntityMapper::class
    ]
)
abstract class TelegramGameEntityMapper {
    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: TelegramGame): TelegramGameEntity

    abstract fun toModel(entity: TelegramGameEntity): TelegramGame

    @AfterMapping
    protected fun setParentInTextEntities(
        @MappingTarget entity: TelegramGameEntity
    ) {
        entity.textEntities.forEach { it.parentGame = entity }
    }
}

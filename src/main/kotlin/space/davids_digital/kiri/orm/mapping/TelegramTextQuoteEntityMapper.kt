package space.davids_digital.kiri.orm.mapping

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramTextQuote
import space.davids_digital.kiri.orm.entity.telegram.TelegramTextQuoteEntity

@Mapper(
    componentModel = "spring",
    uses = [TelegramMessageEntityEntityMapper::class]
)
abstract class TelegramTextQuoteEntityMapper {
    @Mapping(target = "entities", source = "entities")
    @Mapping(target = "internalId", ignore = true)
    abstract fun toEntity(model: TelegramTextQuote?): TelegramTextQuoteEntity?

    @Mapping(source = "manual", target = "isManual")
    abstract fun toModel(entity: TelegramTextQuoteEntity?): TelegramTextQuote?

    @AfterMapping
    protected fun setBackReferences(@MappingTarget entity: TelegramTextQuoteEntity?) {
        entity?.entities?.forEach { it.parentTextQuote = entity }
    }
}

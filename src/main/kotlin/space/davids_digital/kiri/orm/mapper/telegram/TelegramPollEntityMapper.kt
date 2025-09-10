package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.*
import space.davids_digital.kiri.model.telegram.TelegramPoll
import space.davids_digital.kiri.orm.entity.telegram.*
import space.davids_digital.kiri.orm.mapper.DateTimeMapper

@Mapper(
    componentModel = "spring",
    uses = [
        TelegramPollOptionEntityMapper::class,
        TelegramMessageEntityEntityMapper::class,
        DateTimeMapper::class
    ]
)
abstract class TelegramPollEntityMapper {
    abstract fun toEntity(model: TelegramPoll?): TelegramPollEntity?

    @Mapping(source = "anonymous", target = "isAnonymous")
    @Mapping(source = "closed", target = "isClosed")
    abstract fun toModel(entity: TelegramPollEntity?): TelegramPoll?

    @AfterMapping
    protected fun setBackReferences(@MappingTarget entity: TelegramPollEntity?) {
        entity?.questionEntities?.forEach { it.parentPollQuestion = entity }
        entity?.explanationEntities?.forEach { it.parentPollExplanation = entity }
    }
}

package space.davids_digital.kiri.orm.mapper.telegram

import org.mapstruct.Mapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import space.davids_digital.kiri.model.telegram.TelegramGiveawayCompleted
import space.davids_digital.kiri.orm.entity.telegram.TelegramGiveawayCompletedEntity

@Mapper
abstract class TelegramGiveawayCompletedEntityMapper {
    // To avoid circular dependencies, we need to use @Lazy
    @Lazy
    @Autowired
    lateinit var telegramMessageEntityMapper: TelegramMessageEntityMapper

    private fun toEntityWithoutMessage(model: TelegramGiveawayCompleted?): TelegramGiveawayCompletedEntity? {
        if (model == null) {
            return null
        }
        val entity = TelegramGiveawayCompletedEntity()
        entity.winnerCount = model.winnerCount
        entity.unclaimedPrizeCount = model.unclaimedPrizeCount
        entity.isStarGiveaway = model.isStarGiveaway
        return entity
    }

    private fun toModelWithoutMessage(entity: TelegramGiveawayCompletedEntity?): TelegramGiveawayCompleted? {
        if (entity == null) {
            return null
        }
        return TelegramGiveawayCompleted(
            winnerCount = entity.winnerCount,
            unclaimedPrizeCount = entity.unclaimedPrizeCount,
            isStarGiveaway = entity.isStarGiveaway,
        )
    }

    fun toEntity(model: TelegramGiveawayCompleted?): TelegramGiveawayCompletedEntity? {
        if (model == null) {
            return null
        }
        val entity = toEntityWithoutMessage(model)
        if (entity == null) {
            return null
        }
        entity.message = model.message?.let { telegramMessageEntityMapper.toEntity(it) }
        return entity
    }

    fun toModel(entity: TelegramGiveawayCompletedEntity?): TelegramGiveawayCompleted? {
        if (entity == null) {
            return null
        }
        val model = toModelWithoutMessage(entity)
        return model?.copy(
            message = entity.message?.let { telegramMessageEntityMapper.toModel(it) }
        )
    }
}

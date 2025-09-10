package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramGame
import space.davids_digital.kiri.orm.mapper.telegram.TelegramGameEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramGameRepository

@Service
class TelegramGameOrmService(
    private val repo: TelegramGameRepository,
    private val mapper: TelegramGameEntityMapper,
    private val animationOrmService: TelegramAnimationOrmService,
) {
    @Transactional
    fun save(model: TelegramGame): TelegramGame {
        model.animation?.let(animationOrmService::save)
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}
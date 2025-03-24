package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.orm.mapping.TelegramUserEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramUserRepository

@Service
class TelegramUserOrmService(
    private val repo: TelegramUserRepository,
    private val mapper: TelegramUserEntityMapper
) {
    @Transactional
    fun save(telegramUser: TelegramUser): TelegramUser {
        return mapper.toModel(repo.save(mapper.toEntity(telegramUser)!!))!!
    }
}
package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramChatBackground
import space.davids_digital.kiri.orm.mapping.TelegramChatBackgroundEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramChatBackgroundRepository

@Service
class TelegramChatBackgroundOrmService(
    private val repo: TelegramChatBackgroundRepository,
    private val mapper: TelegramChatBackgroundEntityMapper,
    private val backgroundTypeOrmService: TelegramBackgroundTypeOrmService,
) {
    @Transactional
    fun save(model: TelegramChatBackground): TelegramChatBackground {
        backgroundTypeOrmService.save(model.type)
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}
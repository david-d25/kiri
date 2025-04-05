package space.davids_digital.kiri.orm.repository.telegram

import org.springframework.data.jpa.repository.JpaRepository
import space.davids_digital.kiri.orm.entity.telegram.TelegramStoryEntity
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramStoryEntityId

interface TelegramStoryRepository: JpaRepository<TelegramStoryEntity, TelegramStoryEntityId>
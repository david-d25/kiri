package space.davids_digital.kiri.orm.repository.telegram

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.telegram.TelegramAnimationEntity

@Repository
interface TelegramAnimationRepository: JpaRepository<TelegramAnimationEntity, String>
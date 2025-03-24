package space.davids_digital.kiri.orm.repository.telegram

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.telegram.TelegramPaidMediaEntity

@Repository
interface TelegramPaidMediaRepository: JpaRepository<TelegramPaidMediaEntity, Long>
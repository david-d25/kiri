package space.davids_digital.kiri.orm.repository.telegram

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.telegram.TelegramExternalReplyInfoEntity

@Repository
interface TelegramExternalReplyInfoRepository: JpaRepository<TelegramExternalReplyInfoEntity, Long>
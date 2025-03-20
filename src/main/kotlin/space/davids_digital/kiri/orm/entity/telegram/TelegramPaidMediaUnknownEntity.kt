package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("unknown")
class TelegramPaidMediaUnknownEntity : TelegramPaidMediaEntity()
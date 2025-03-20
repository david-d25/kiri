package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
@DiscriminatorValue("video")
class TelegramPaidMediaVideoEntity : TelegramPaidMediaEntity() {
    @OneToOne
    @JoinColumn(name = "video_id", referencedColumnName = "internal_id")
    var video: TelegramVideoEntity? = null
}
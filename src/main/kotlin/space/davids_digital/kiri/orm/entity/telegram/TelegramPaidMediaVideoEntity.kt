package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
@DiscriminatorValue("video")
class TelegramPaidMediaVideoEntity : TelegramPaidMediaEntity() {
    @ManyToOne
    @JoinColumn(name = "video_id", referencedColumnName = "file_unique_id")
    var video: TelegramVideoEntity? = null
}
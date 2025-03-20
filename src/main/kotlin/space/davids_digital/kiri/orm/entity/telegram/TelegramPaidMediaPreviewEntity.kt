package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("preview")
class TelegramPaidMediaPreviewEntity : TelegramPaidMediaEntity() {
    @Column(name = "width")
    var width: Int = 0

    @Column(name = "height")
    var height: Int = 0

    @Column(name = "duration")
    var duration: Int = 0
}
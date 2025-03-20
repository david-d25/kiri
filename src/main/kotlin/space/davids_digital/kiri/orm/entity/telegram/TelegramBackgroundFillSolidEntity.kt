package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("solid")
class TelegramBackgroundFillSolidEntity : TelegramBackgroundFillEntity() {
    @Column(name = "color_rgb", nullable = false)
    var colorRgb: Int = 0
}

package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("freeform")
class TelegramBackgroundFillFreeformEntity : TelegramBackgroundFillEntity() {
    @Column(name = "color1_rgb", nullable = false)
    var color1Rgb: Int = 0

    @Column(name = "color2_rgb", nullable = false)
    var color2Rgb: Int = 0

    @Column(name = "color3_rgb", nullable = false)
    var color3Rgb: Int = 0

    @Column(name = "color4_rgb")
    var color4Rgb: Int? = null
}
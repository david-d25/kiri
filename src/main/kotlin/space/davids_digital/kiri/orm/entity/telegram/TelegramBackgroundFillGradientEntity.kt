package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@Entity
@DiscriminatorValue("gradient")
class TelegramBackgroundFillGradientEntity : TelegramBackgroundFillEntity() {
    @Column(name = "top_color_rgb")
    var topColorRgb: Int = 0

    @Column(name = "bottom_color_rgb")
    var bottomColorRgb: Int = 0

    @Column(name = "rotation_angle")
    var rotationAngle: Int = 0
}
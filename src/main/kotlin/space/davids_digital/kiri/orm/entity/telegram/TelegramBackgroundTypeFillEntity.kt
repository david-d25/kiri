package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne

@Entity
@DiscriminatorValue("fill")
class TelegramBackgroundTypeFillEntity : TelegramBackgroundTypeEntity() {
    @OneToOne
    @JoinColumn(name = "fill_id", referencedColumnName = "internal_id")
    var fill: TelegramBackgroundFillEntity? = null

    @Column(name = "dark_theme_dimming")
    var darkThemeDimming: Int = 0
}
package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne

@Entity
@DiscriminatorValue("wallpaper")
class TelegramBackgroundTypeWallpaperEntity : TelegramBackgroundTypeEntity() {
    @ManyToOne
    @JoinColumn(name = "document_id", referencedColumnName = "file_unique_id")
    var document: TelegramDocumentEntity? = null

    @Column(name = "dark_theme_dimming")
    var darkThemeDimming: Int = 0

    @Column(name = "is_blurred")
    var isBlurred: Boolean = false

    @Column(name = "is_moving")
    var isMoving: Boolean = false
}
package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne

@Entity
@DiscriminatorValue("pattern")
class TelegramBackgroundTypePatternEntity : TelegramBackgroundTypeEntity() {
    @ManyToOne
    @JoinColumn(name = "document_id", referencedColumnName = "file_unique_id")
    var document: TelegramDocumentEntity? = null

    @OneToOne(orphanRemoval = true, cascade = [CascadeType.ALL])
    @JoinColumn(name = "fill_id", referencedColumnName = "internal_id")
    var fill: TelegramBackgroundFillEntity? = null

    @Column(name = "intensity")
    var intensity: Int = 0

    @Column(name = "is_inverted")
    var isInverted: Boolean = false

    @Column(name = "is_moving")
    var isMoving: Boolean = false
}
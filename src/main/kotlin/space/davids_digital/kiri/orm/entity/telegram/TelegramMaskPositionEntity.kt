package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "mask_positions")
class TelegramMaskPositionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "point")
    var point: String = ""

    @Column(name = "x_shift")
    var xShift: Float = 0f

    @Column(name = "y_shift")
    var yShift: Float = 0f

    @Column(name = "scale")
    var scale: Float = 1f
}

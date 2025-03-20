package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_paid_media")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
abstract class TelegramPaidMediaEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @ManyToOne
    @JoinColumn(name = "paid_media_info_id", referencedColumnName = "internal_id")
    var paidMediaInfo: TelegramPaidMediaInfoEntity? = null
}
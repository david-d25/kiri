package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_paid_media_info")
class TelegramPaidMediaInfoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "star_count")
    var starCount: Int = 0

    @OneToMany(mappedBy = "parentPaidMediaInfo", cascade = [CascadeType.ALL], orphanRemoval = true)
    var paidMedia: MutableList<TelegramPaidMediaEntity> = mutableListOf()
}

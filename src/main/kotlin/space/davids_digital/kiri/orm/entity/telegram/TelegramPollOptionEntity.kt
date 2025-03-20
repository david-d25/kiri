package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_poll_options")
class TelegramPollOptionEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "text")
    var text: String = ""

    @Column(name = "voter_count")
    var voterCount: Int = 0

    @OneToMany(mappedBy = "parentPollOption", cascade = [CascadeType.ALL], orphanRemoval = true)
    var textEntities: MutableList<TelegramMessageEntityEntity> = mutableListOf()
}

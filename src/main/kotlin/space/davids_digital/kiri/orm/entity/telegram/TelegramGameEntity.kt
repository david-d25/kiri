package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_games")
class TelegramGameEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "title")
    var title: String = ""

    @Column(name = "description")
    var description: String = ""

    @Column(name = "text")
    var text: String? = null

    @OneToOne
    @JoinColumn(name = "animation_id", referencedColumnName = "file_unique_id")
    var animation: TelegramAnimationEntity? = null

    @OneToMany
    @JoinTable(
        name = "telegram_game_photos",
        joinColumns = [JoinColumn(name = "game_id", referencedColumnName = "internal_id")],
        inverseJoinColumns = [JoinColumn(name = "photo_id", referencedColumnName = "internal_id")]
    )
    var photo: MutableList<TelegramPhotoSizeEntity> = mutableListOf()

    @OneToMany(mappedBy = "parentGame", cascade = [CascadeType.ALL], orphanRemoval = true)
    var textEntities: MutableList<TelegramMessageEntityEntity> = mutableListOf()
}

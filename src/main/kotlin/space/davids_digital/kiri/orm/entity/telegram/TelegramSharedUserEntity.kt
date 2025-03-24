package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_shared_users")
class TelegramSharedUserEntity {
    @Id
    @Column(name = "user_id")
    var userId: Long = 0

    @Column(name = "first_name")
    var firstName: String? = null

    @Column(name = "last_name")
    var lastName: String? = null

    @Column(name = "username")
    var username: String? = null

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        name = "telegram_shared_users_photo_cross_links",
        joinColumns = [JoinColumn(name = "user_id", referencedColumnName = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "photo_id", referencedColumnName = "file_unique_id")]
    )
    var photo: MutableList<TelegramPhotoSizeEntity> = mutableListOf()
}

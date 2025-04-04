package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "users_shared")
class TelegramUsersSharedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "request_id", nullable = false)
    var requestId: Long = 0

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        schema = "telegram",
        name = "users_shared_cross_links",
        joinColumns = [JoinColumn(name = "users_shared_id", referencedColumnName = "internal_id")],
        inverseJoinColumns = [JoinColumn(name = "shared_user_id", referencedColumnName = "user_id")]
    )
    var users: MutableList<TelegramSharedUserEntity> = mutableListOf()
}

package space.davids_digital.kiri.orm.entity

import jakarta.persistence.*

@Entity
@Table(schema = "main", name = "users")
class UserEntity {
    @Id
    @Column(name = "id")
    var id: Long = 0

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    var role: Role = Role.ADMIN

    enum class Role {
        OWNER, ADMIN
    }
}
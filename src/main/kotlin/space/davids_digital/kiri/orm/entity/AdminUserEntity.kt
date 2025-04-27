package space.davids_digital.kiri.orm.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

/**
 * Entity representing administrative users.
 */
@Entity
@Table(schema = "kiri", name = "admins")
class AdminUserEntity(
    @Id
    @Column(name = "user_id")
    var userId: Long = 0
)
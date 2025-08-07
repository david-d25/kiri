package space.davids_digital.kiri.orm.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(schema = "main", name = "settings")
class SettingEntity {
    @Id
    @Column(name = "key")
    var key: String = ""

    @Column(name = "value")
    var value: String? = null

    @Column(name = "encrypted")
    var encrypted: Boolean = false

    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
}
package space.davids_digital.kiri.orm.entity.filesystem

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(schema = "main", name = "file_system_spaces")
class FileSystemSpaceEntity {
    @Id
    @Column(name = "id")
    var id: UUID = UUID.randomUUID()

    @Column(name = "slug", nullable = false)
    var slug: String = ""

    @Column(name = "display_name", nullable = false)
    var displayName: String = ""

    @Column(name = "description")
    var description: String? = null

    @Column(name = "owner_user_id")
    var ownerUserId: Long? = null

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
}

package space.davids_digital.kiri.orm.entity.filesystem

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(schema = "filesystem", name = "file_contents")
class FileSystemFileContentEntity {
    @Id
    @Column(name = "node_id")
    var nodeId: UUID = UUID.randomUUID()

    @Lob
    @Column(name = "content", nullable = false)
    var content: ByteArray = ByteArray(0)
}

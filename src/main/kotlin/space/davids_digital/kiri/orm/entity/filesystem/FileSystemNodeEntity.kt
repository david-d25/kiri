package space.davids_digital.kiri.orm.entity.filesystem

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(schema = "main", name = "file_system_nodes")
class FileSystemNodeEntity {
    @Id
    @Column(name = "id")
    var id: UUID = UUID.randomUUID()

    @Column(name = "space_id", nullable = false)
    var spaceId: UUID = UUID.randomUUID()

    @Column(name = "parent_id")
    var parentId: UUID? = null

    @Column(name = "name", nullable = false)
    var name: String = ""

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: NodeType = NodeType.DIRECTORY

    @Column(name = "mime_type")
    var mimeType: String? = null

    @Column(name = "size", nullable = false)
    var size: Long = 0

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
        schema = "main",
        name = "file_system_node_attributes",
        joinColumns = [JoinColumn(name = "node_id")]
    )
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    var attributes: MutableMap<String, String> = mutableMapOf()

    @Column(name = "created_at", nullable = false)
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "updated_at", nullable = false)
    var updatedAt: OffsetDateTime = OffsetDateTime.now()

    enum class NodeType {
        DIRECTORY,
        FILE,
    }
}

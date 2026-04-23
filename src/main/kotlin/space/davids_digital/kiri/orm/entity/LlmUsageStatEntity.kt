package space.davids_digital.kiri.orm.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(schema = "main", name = "llm_usage_stats")
class LlmUsageStatEntity {
    @Id
    @Column(name = "id")
    var id: UUID = UUID.randomUUID()

    @Column(name = "timestamp")
    var timestamp: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "provider")
    var provider: String = ""

    @Column(name = "model")
    var model: String = ""

    @Column(name = "input_tokens")
    var inputTokens: Long = 0

    @Column(name = "output_tokens")
    var outputTokens: Long = 0

    @Column(name = "cache_read_input_tokens")
    var cacheReadInputTokens: Long = 0

    @Column(name = "cache_creation_input_tokens")
    var cacheCreationInputTokens: Long = 0

    @Column(name = "duration_ms")
    var durationMs: Long = 0
}

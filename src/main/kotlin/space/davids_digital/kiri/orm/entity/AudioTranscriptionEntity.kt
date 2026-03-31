package space.davids_digital.kiri.orm.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.OffsetDateTime

@Entity
@Table(schema = "telegram", name = "audio_transcriptions")
class AudioTranscriptionEntity {
    @Id
    @Column(name = "file_unique_id")
    var fileUniqueId: String = ""

    @Column(name = "text")
    var text: String = ""

    @Column(name = "created_at")
    var createdAt: OffsetDateTime = OffsetDateTime.now()
}

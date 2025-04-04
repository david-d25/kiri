package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(schema = "telegram", name = "passport_files")
class TelegramPassportFileEntity {
    @Id
    @Column(name = "file_unique_id")
    var fileUniqueId: String = ""

    @Column(name = "file_download_id", nullable = false)
    var fileDownloadId: String = ""

    @Column(name = "file_size_bytes", nullable = false)
    var fileSizeBytes: Long = 0

    @Column(name = "file_date", nullable = false)
    var fileDate: OffsetDateTime = OffsetDateTime.now()
}

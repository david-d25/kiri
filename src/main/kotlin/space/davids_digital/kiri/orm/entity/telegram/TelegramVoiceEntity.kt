package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_voices")
class TelegramVoiceEntity {
    @Id
    @Column(name = "file_unique_id")
    var fileUniqueId: String = ""

    @Column(name = "file_download_id", nullable = false)
    var fileDownloadId: String = ""

    @Column(name = "duration", nullable = false)
    var duration: Int = 0

    @Column(name = "mime_type")
    var mimeType: String? = null

    @Column(name = "file_size")
    var fileSize: Long? = null
}

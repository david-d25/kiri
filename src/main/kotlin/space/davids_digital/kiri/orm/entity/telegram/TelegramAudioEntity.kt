package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_audios")
class TelegramAudioEntity {
    @Id
    @Column(name = "file_unique_id")
    var fileUniqueId: String = ""

    @Column(name = "file_download_id")
    var fileDownloadId: String = ""

    @Column(name = "duration")
    var duration: Int = 0

    @Column(name = "performer")
    var performer: String? = null

    @Column(name = "title")
    var title: String? = null

    @Column(name = "file_name")
    var fileName: String? = null

    @Column(name = "mime_type")
    var mimeType: String? = null

    @Column(name = "file_size")
    var fileSize: Long? = null

    @ManyToOne
    @JoinColumn(name = "thumbnail_id", referencedColumnName = "file_unique_id")
    var thumbnail: TelegramPhotoSizeEntity? = null
}

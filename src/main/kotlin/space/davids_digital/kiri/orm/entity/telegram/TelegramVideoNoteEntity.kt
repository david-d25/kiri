package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_video_notes")
class TelegramVideoNoteEntity {
    @Id
    @Column(name = "file_unique_id")
    var fileUniqueId: String = ""

    @Column(name = "file_download_id")
    var fileDownloadId: String = ""

    @Column(name = "length")
    var length: Int = 0

    @Column(name = "duration")
    var duration: Int = 0

    @ManyToOne
    @JoinColumn(name = "thumbnail_id", referencedColumnName = "file_unique_id")
    var thumbnail: TelegramPhotoSizeEntity? = null

    @Column(name = "file_size")
    var fileSize: Long? = null
}

package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "documents")
class TelegramDocumentEntity {
    @Id
    @Column(name = "file_unique_id")
    var fileUniqueId: String = ""

    @Column(name = "file_download_id")
    var fileDownloadId: String = ""

    @ManyToOne
    @JoinColumn(name = "thumbnail_id", referencedColumnName = "file_unique_id")
    var thumbnail: TelegramPhotoSizeEntity? = null

    @Column(name = "file_name")
    var fileName: String? = null

    @Column(name = "mime_type")
    var mimeType: String? = null

    @Column(name = "file_size")
    var fileSize: Long? = null
}

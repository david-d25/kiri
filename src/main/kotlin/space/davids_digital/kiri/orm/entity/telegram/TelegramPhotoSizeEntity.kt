package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "telegram_photo_sizes")
class TelegramPhotoSizeEntity {
    @Id
    @Column(name = "file_unique_id")
    var fileUniqueId: String = ""

    @Column(name = "file_download_id")
    var fileDownloadId: String = ""

    @Column(name = "width")
    var width: Int = 0

    @Column(name = "height")
    var height: Int = 0

    @Column(name = "file_size")
    var fileSize: Long = 0
}
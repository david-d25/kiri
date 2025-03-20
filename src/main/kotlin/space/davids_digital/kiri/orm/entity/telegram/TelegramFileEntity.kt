package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_files")
class TelegramFileEntity {
    @Id
    @Column(name = "file_unique_id")
    var fileUniqueId: String = ""

    @Column(name = "file_download_id")
    var fileDownloadId: String = ""

    @Column(name = "file_size")
    var fileSize: Long? = null

    @Column(name = "file_path")
    var filePath: String? = null
}
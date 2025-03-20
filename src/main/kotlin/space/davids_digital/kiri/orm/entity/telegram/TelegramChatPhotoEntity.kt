package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "telegram_chat_photos")
class TelegramChatPhotoEntity {
    @Id
    @Column(name = "internal_id")
    @GeneratedValue
    var internalId: Long = 0

    @Column(name = "small_file_id")
    var smallFileId: String = ""

    @Column(name = "small_file_download_id")
    var smallFileDownloadId: String = ""

    @Column(name = "big_file_id")
    var bigFileId: String = ""

    @Column(name = "big_file_download_id")
    var bigFileDownloadId: String = ""
}
package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(schema = "telegram", name = "videos")
class TelegramVideoEntity {
    @Id
    @Column(name = "file_unique_id")
    var fileUniqueId: String = ""

    @Column(name = "file_download_id")
    var fileDownloadId: String = ""

    @Column(name = "width")
    var width: Int = 0

    @Column(name = "height")
    var height: Int = 0

    @Column(name = "duration")
    var duration: Int = 0

    @ManyToOne
    @JoinColumn(name = "thumbnail_id", referencedColumnName = "file_unique_id")
    var thumbnail: TelegramPhotoSizeEntity? = null

    @Column(name = "start_timestamp")
    var startTimestamp: Int? = null

    @Column(name = "file_name")
    var fileName: String? = null

    @Column(name = "mime_type")
    var mimeType: String? = null

    @Column(name = "file_size")
    var fileSize: Long? = null

    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        schema = "telegram",
        name = "video_cover_cross_links",
        joinColumns = [JoinColumn(name = "video_file_unique_id", referencedColumnName = "file_unique_id")],
        inverseJoinColumns = [JoinColumn(name = "photo_size_id", referencedColumnName = "file_unique_id")]
    )
    var cover: MutableList<TelegramPhotoSizeEntity> = mutableListOf()
}

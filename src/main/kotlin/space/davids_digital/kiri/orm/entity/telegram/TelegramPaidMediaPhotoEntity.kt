package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.CascadeType
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany

@Entity
@DiscriminatorValue("photo")
class TelegramPaidMediaPhotoEntity : TelegramPaidMediaEntity() {
    @ManyToMany(cascade = [CascadeType.PERSIST, CascadeType.MERGE])
    @JoinTable(
        schema = "telegram",
        name = "paid_media_photo_sizes",
        joinColumns = [JoinColumn(name = "paid_media_id", referencedColumnName = "internal_id")],
        inverseJoinColumns = [JoinColumn(name = "photo_size_id", referencedColumnName = "file_unique_id")]
    )
    var photoSizes: MutableList<TelegramPhotoSizeEntity> = mutableListOf()
}
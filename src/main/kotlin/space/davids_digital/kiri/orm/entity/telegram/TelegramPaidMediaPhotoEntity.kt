package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.OneToMany

@Entity
@DiscriminatorValue("photo")
class TelegramPaidMediaPhotoEntity : TelegramPaidMediaEntity() {
    @OneToMany
    @JoinTable(
        name = "telegram_paid_media_photo_sizes",
        joinColumns = [JoinColumn(name = "paid_media_id", referencedColumnName = "internal_id")],
        inverseJoinColumns = [JoinColumn(name = "photo_size_id", referencedColumnName = "internal_id")]
    )
    var photoSizes: MutableList<TelegramPhotoSizeEntity> = mutableListOf()
}
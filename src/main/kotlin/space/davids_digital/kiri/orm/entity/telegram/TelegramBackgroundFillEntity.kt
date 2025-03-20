package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*

@Entity
@Table(name = "telegram_background_fills")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
abstract class TelegramBackgroundFillEntity {
    @Id
    @GeneratedValue
    @Column(name = "internal_id")
    var internalId: Long = 0
}
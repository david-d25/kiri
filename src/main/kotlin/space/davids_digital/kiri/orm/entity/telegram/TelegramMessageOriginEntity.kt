package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "telegram_message_origins")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
abstract class TelegramMessageOriginEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "internal_id")
    var internalId: Long = 0

    @Column(name = "date")
    var date: OffsetDateTime = OffsetDateTime.now()
}
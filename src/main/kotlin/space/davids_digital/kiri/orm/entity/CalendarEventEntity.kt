package space.davids_digital.kiri.orm.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.OffsetDateTime
import java.util.UUID

@Entity
@Table(schema = "main", name = "calendar_events")
class CalendarEventEntity {
    @Id
    @Column(name = "id")
    var id: UUID = UUID.randomUUID()

    @Column(name = "title")
    var title: String = ""

    @Column(name = "description")
    var description: String? = null

    @Column(name = "timezone")
    var timezone: String = "UTC"

    @Column(name = "fires_at")
    var firesAt: OffsetDateTime? = null

    @Column(name = "rrule")
    var rrule: String? = null

    @Column(name = "dtstart")
    var dtstart: OffsetDateTime? = null

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "exdates", columnDefinition = "timestamp with time zone[]")
    var exdates: Array<OffsetDateTime> = emptyArray()

    @Column(name = "next_fire_at")
    var nextFireAt: OffsetDateTime? = null

    @Column(name = "last_fired_at")
    var lastFiredAt: OffsetDateTime? = null

    @Column(name = "wake_agent")
    var wakeAgent: Boolean = true

    @Enumerated(EnumType.STRING)
    @Column(name = "missed_policy")
    var missedPolicy: MissedPolicy = MissedPolicy.FIRE_ONCE

    @Column(name = "enabled")
    var enabled: Boolean = true

    @Column(name = "created_at")
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime = OffsetDateTime.now()

    enum class MissedPolicy {
        FIRE_ONCE, SKIP
    }
}

package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.Column
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramPollAnswerEntityId
import java.time.OffsetDateTime

/**
 * A single voter's current selection in a non-anonymous poll (see V12 migration). Used to aggregate vote counts
 * from `poll_answer` updates, which carry the voter's full current selection rather than a delta.
 */
@Entity
@Table(schema = "telegram", name = "poll_answers")
class TelegramPollAnswerEntity {
    @EmbeddedId
    var id: TelegramPollAnswerEntityId = TelegramPollAnswerEntityId()

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "option_ids")
    var optionIds: Array<Int> = emptyArray()

    @Column(name = "updated_at")
    var updatedAt: OffsetDateTime = OffsetDateTime.now()
}

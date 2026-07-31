package space.davids_digital.kiri.orm.entity.telegram.id

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class TelegramPollAnswerEntityId(
    @Column(name = "poll_id")
    var pollId: String = "",
    @Column(name = "voter_id")
    var voterId: Long = 0
) : Serializable

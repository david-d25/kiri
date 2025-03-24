package space.davids_digital.kiri.orm.entity.telegram

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "telegram_polls")
class TelegramPollEntity {
    @Id
    @Column(name = "id")
    var id: String = ""

    @Column(name = "question")
    var question: String = ""

    @Column(name = "total_voter_count")
    var totalVoterCount: Int = 0

    @Column(name = "is_closed")
    var closed: Boolean = false

    @Column(name = "is_anonymous")
    var anonymous: Boolean = false

    @Column(name = "type")
    var type: String = "REGULAR" // or QUIZ

    @Column(name = "allows_multiple_answers")
    var allowsMultipleAnswers: Boolean = false

    @Column(name = "correct_option_id")
    var correctOptionId: Int? = null

    @Column(name = "explanation")
    var explanation: String? = null

    @Column(name = "open_period")
    var openPeriod: Int? = null

    @Column(name = "close_date")
    var closeDate: OffsetDateTime? = null

    @OneToMany(mappedBy = "parentPollQuestion", orphanRemoval = true)
    var questionEntities: MutableList<TelegramMessageEntityEntity> = mutableListOf()

    @OneToMany(mappedBy = "parentPollExplanation", orphanRemoval = true)
    var explanationEntities: MutableList<TelegramMessageEntityEntity> = mutableListOf()

    @OneToMany(orphanRemoval = true)
    @JoinTable(
        name = "telegram_poll_options_map",
        joinColumns = [JoinColumn(name = "poll_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "poll_option_id", referencedColumnName = "internal_id")]
    )
    var options: MutableList<TelegramPollOptionEntity> = mutableListOf()
}

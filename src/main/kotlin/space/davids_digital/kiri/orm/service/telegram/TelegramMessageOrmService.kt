package space.davids_digital.kiri.orm.service.telegram

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramInaccessibleMessage
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.orm.entity.telegram.id.TelegramMessageEntityId
import space.davids_digital.kiri.orm.mapper.telegram.TelegramMessageEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramInaccessibleMessageRepository
import space.davids_digital.kiri.orm.repository.telegram.TelegramMessageRepository
import kotlin.jvm.optionals.getOrNull

@Service
class TelegramMessageOrmService(
    private val repo: TelegramMessageRepository,
    private val inaccessibleMessagesRepository: TelegramInaccessibleMessageRepository,
    private val mapper: TelegramMessageEntityMapper,
    private val storyOrm: TelegramStoryOrmService,
    private val userOrm: TelegramUserOrmService,
    private val animationOrm: TelegramAnimationOrmService,
    private val audioOrm: TelegramAudioOrmService,
    private val documentOrm: TelegramDocumentOrmService,
    private val paidMediaInfoOrm: TelegramPaidMediaInfoOrmService,
    private val stickerOrm: TelegramStickerOrmService,
    private val videoOrm: TelegramVideoOrmService,
    private val videoNoteOrm: TelegramVideoNoteOrmService,
    private val voiceOrm: TelegramVoiceOrmService,
    private val gameOrm: TelegramGameOrmService,
    private val pollOrm: TelegramPollOrmService,
    private val passportDataOrm: TelegramPassportDataOrmService,
    private val chatBackgroundOrm: TelegramChatBackgroundOrmService,
    private val giveawayWinnersOrm: TelegramGiveawayWinnersOrmService,
    private val giveawayCompleted: TelegramGiveawayCompletedOrmService,
) {
    @Transactional
    fun save(message: TelegramMessage): TelegramMessage {
        message.replyToMessage?.let(::save)
        message.replyToStory?.let(storyOrm::save)
        message.viaBot?.let(userOrm::save)
        message.animation?.let(animationOrm::save)
        message.audio?.let(audioOrm::save)
        message.document?.let(documentOrm::save)
        message.paidMediaInfo?.let(paidMediaInfoOrm::save)
        message.sticker?.let(stickerOrm::save)
        message.story?.let(storyOrm::save)
        message.video?.let(videoOrm::save)
        message.videoNote?.let(videoNoteOrm::save)
        message.voice?.let(voiceOrm::save)
        message.game?.let(gameOrm::save)
        message.poll?.let(pollOrm::save)
        message.pinnedMessage?.let {
            when (it) {
                is TelegramMessage -> save(it)
                is TelegramInaccessibleMessage -> inaccessibleMessagesRepository.save(mapper.toEntity(it)!!)
            }
        }
        message.passportData?.let(passportDataOrm::save)
        message.chatBackgroundSet?.let(chatBackgroundOrm::save)
        message.giveawayWinners?.let(giveawayWinnersOrm::save)
        message.giveawayCompleted?.let(giveawayCompleted::save)
        return mapper.toModel(repo.save(mapper.toEntity(message)!!))!!
    }

    @Transactional(readOnly = true)
    fun find(chatId: Long, messageId: Int): TelegramMessage? {
        return mapper.toModel(repo.findById(TelegramMessageEntityId(chatId, messageId)).getOrNull())
    }

    @Transactional(readOnly = true)
    fun findFirstOrderedByMessageId(chatId: Long, limit: Int): Page<TelegramMessage> {
        return repo.findByIdChatId(chatId, PageRequest.of(0, limit, Sort.by("id.messageId"))).map(mapper::toModel)
    }

    @Transactional(readOnly = true)
    fun findOldestMessage(chatId: Long): TelegramMessage? {
        return repo.findFirstByIdChatId(chatId, Sort.by("date", "id.messageId"))?.let(mapper::toModel)
    }

    @Transactional(readOnly = true)
    fun findMostRecentMessage(chatId: Long): TelegramMessage? {
        return repo.findFirstByIdChatId(
            chatId,
            Sort.by(Sort.Direction.DESC, "date", "id.messageId")
        )?.let(mapper::toModel)
    }

    @Transactional(readOnly = true)
    fun findSinceMessageIdOrderedByMessageId(
        chatId: Long,
        sinceMessageId: Int,
        limit: Int
    ): Page<TelegramMessage> {
        return repo.findByIdChatIdAndIdMessageIdGreaterThanEqual(
            chatId,
            sinceMessageId,
            PageRequest.of(0, limit, Sort.by("date", "id.messageId"))
        ).map(mapper::toModel)
    }

    @Transactional(readOnly = true)
    fun findBeforeMessageIdOrderedByMessageIdDesc(
        chatId: Long,
        beforeMessageId: Int,
        limit: Int
    ): Page<TelegramMessage> {
        return repo.findByIdChatIdAndIdMessageIdLessThan(
            chatId,
            beforeMessageId,
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "date", "id.messageId"))
        ).map(mapper::toModel)
    }

    @Transactional(readOnly = true)
    fun countMessagesAfterId(chatId: Long, afterMessageId: Int): Long {
        return repo.countByIdChatIdAndIdMessageIdGreaterThan(chatId, afterMessageId)
    }
}
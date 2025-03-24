package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramInaccessibleMessage
import space.davids_digital.kiri.model.telegram.TelegramMessage
import space.davids_digital.kiri.orm.mapping.TelegramMessageEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramInaccessibleMessageRepository
import space.davids_digital.kiri.orm.repository.telegram.TelegramMessageRepository

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

    @Transactional
    fun getChatMessages(chatId: Long): List<TelegramMessage> {
        return repo.getAllByIdChatId(chatId).mapNotNull(mapper::toModel)
    }
}
package space.davids_digital.kiri.orm.service.telegram

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.telegram.TelegramExternalReplyInfo
import space.davids_digital.kiri.orm.mapping.telegram.TelegramExternalReplyInfoEntityMapper
import space.davids_digital.kiri.orm.repository.telegram.TelegramExternalReplyInfoRepository

@Service
class TelegramExternalReplyInfoOrmService(
    private val repo: TelegramExternalReplyInfoRepository,
    private val mapper: TelegramExternalReplyInfoEntityMapper,
    private val animationOrm: TelegramAnimationOrmService,
    private val audioOrm: TelegramAudioOrmService,
    private val documentOrm: TelegramDocumentOrmService,
    private val paidMediaInfoOrm: TelegramPaidMediaInfoOrmService,
    private val stickerOrm: TelegramStickerOrmService,
    private val storyOrm: TelegramStoryOrmService,
    private val videoOrm: TelegramVideoOrmService,
    private val videoNoteOrm: TelegramVideoNoteOrmService,
    private val voiceOrm: TelegramVoiceOrmService,
    private val giveawayWinnersOrm: TelegramGiveawayWinnersOrmService,
    private val gameOrm: TelegramGameOrmService,
    private val poll: TelegramPollOrmService
) {
    @Transactional
    fun save(model: TelegramExternalReplyInfo): TelegramExternalReplyInfo {
        model.animation?.let(animationOrm::save)
        model.audio?.let(audioOrm::save)
        model.document?.let(documentOrm::save)
        model.paidMedia?.let(paidMediaInfoOrm::save)
        model.sticker?.let(stickerOrm::save)
        model.story?.let(storyOrm::save)
        model.video?.let(videoOrm::save)
        model.videoNote?.let(videoNoteOrm::save)
        model.voice?.let(voiceOrm::save)
        model.giveawayWinners?.let(giveawayWinnersOrm::save)
        model.game?.let(gameOrm::save)
        model.poll?.let(poll::save)
        return mapper.toModel(repo.save(mapper.toEntity(model)!!))!!
    }
}
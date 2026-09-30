package space.davids_digital.kiri.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import space.davids_digital.kiri.AppProperties
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.model.telegram.TelegramChatPhoto
import space.davids_digital.kiri.orm.service.telegram.TelegramChatOrmService
import space.davids_digital.kiri.service.exception.ResourceNotFoundException
import space.davids_digital.kiri.service.exception.ServiceException

@Service
class TelegramChatService(
    private val appProperties: AppProperties,
    private val telegram: TelegramService,
    private val chatOrm: TelegramChatOrmService,
) {
    enum class PhotoSize { SMALL, BIG }

    class ChatPhotoContent(val fileId: String, val bytes: ByteArray)

    private val log = LoggerFactory.getLogger(javaClass)

    fun createChatPhotoUrl(chatId: Long, size: PhotoSize): String {
        val host = appProperties.backend.host
        val basePath = appProperties.backend.basePath
        val downloadPath = "/telegram/files/chat-photo/$chatId/${size.name.lowercase()}"
        return host.removeSuffix("/") + basePath.removeSuffix("/") + downloadPath
    }

    /**
     * Downloads the chat photo, refreshing the stored chat once if its photo file_id is rejected.
     *
     * Chat photo file_ids stay valid only until the photo changes, and chats are not re-fetched after the
     * first save, so the stored id goes stale whenever someone changes their avatar.
     */
    suspend fun getChatPhotoContent(chatId: Long, size: PhotoSize): ChatPhotoContent {
        val photo = chatOrm.findById(chatId)?.photo
            ?: throw ResourceNotFoundException("Chat $chatId has no photo")
        val fileId = photo.fileId(size)
        try {
            return ChatPhotoContent(fileId, telegram.getFileContent(fileId))
        } catch (e: ServiceException) {
            log.info("Chat $chatId photo file_id was rejected (${e.message}), refreshing chat")
        }
        val freshFileId = telegram.fetchAndSaveChatById(chatId)?.photo?.fileId(size)
        if (freshFileId == null || freshFileId == fileId) {
            throw ResourceNotFoundException("Chat $chatId photo is unavailable")
        }
        return ChatPhotoContent(freshFileId, telegram.getFileContent(freshFileId))
    }

    private fun TelegramChatPhoto.fileId(size: PhotoSize) = when (size) {
        PhotoSize.SMALL -> smallFileId
        PhotoSize.BIG -> bigFileId
    }
}

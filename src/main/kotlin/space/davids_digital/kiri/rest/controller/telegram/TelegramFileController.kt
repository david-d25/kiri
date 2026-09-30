package space.davids_digital.kiri.rest.controller.telegram

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.service.TelegramChatService
import space.davids_digital.kiri.service.exception.ResourceNotFoundException
import space.davids_digital.kiri.service.exception.ServiceException

@RestController
@RequestMapping("/telegram/files")
class TelegramFileController(
    private val service: TelegramService,
    private val chatService: TelegramChatService,
) {
    @GetMapping("{id}")
    suspend fun getFile(@PathVariable id: String): ByteArray {
        return try {
            service.getFileContent(id)
        } catch (e: ServiceException) {
            // Expired or foreign file_id — a missing file, not a server error.
            throw ResourceNotFoundException(e.message)
        }
    }

    @GetMapping("chat-photo/{chatId}/{size}")
    suspend fun getChatPhoto(
        @PathVariable chatId: Long,
        @PathVariable size: String,
    ): ByteArray {
        val photoSize = when (size) {
            "small" -> TelegramChatService.PhotoSize.SMALL
            "big" -> TelegramChatService.PhotoSize.BIG
            else -> throw ResourceNotFoundException("Unknown photo size '$size'")
        }
        return try {
            chatService.getChatPhotoContent(chatId, photoSize).bytes
        } catch (e: ServiceException) {
            throw ResourceNotFoundException(e.message)
        }
    }
}

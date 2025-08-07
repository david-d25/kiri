package space.davids_digital.kiri.rest.controller.telegram

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.integration.telegram.TelegramService

@RestController
@RequestMapping("/telegram/files")
class TelegramFileController(
    private val service: TelegramService
) {
    @GetMapping("{id}")
    suspend fun getFile(@PathVariable id: String): ByteArray {
        return service.getFileContent(id)
    }
}
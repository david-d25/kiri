package space.davids_digital.kiri.service

import org.springframework.stereotype.Service
import space.davids_digital.kiri.AppProperties

@Service
class TelegramChatService(private val appProperties: AppProperties) {
    fun createChatPhotoUrl(fileId: String): String {
        val host = appProperties.backend.host
        val basePath = appProperties.backend.basePath
        val downloadPath = "/telegram/files/$fileId"
        return host.removeSuffix("/") + basePath.removeSuffix("/") + downloadPath
    }
}
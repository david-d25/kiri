package space.davids_digital.kiri.rest.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.rest.dto.ChatCompletionModelDto
import space.davids_digital.kiri.rest.mapper.ChatCompletionModelDtoMapper
import space.davids_digital.kiri.service.ChatCompletionService
import kotlin.collections.flatMap
import kotlin.collections.map
import kotlin.jvm.javaClass

@RestController
@RequestMapping("/chat-completion")
class ChatCompletionController (
    private val mapper: ChatCompletionModelDtoMapper,
    private val chatCompletionServices: List<ChatCompletionService>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping("/models")
    suspend fun listModels(): List<ChatCompletionModelDto> {
        val models = chatCompletionServices.flatMap {
            try {
                it.getModels()
            } catch (e: Exception) {
                log.error("Error fetching models from service ${it.javaClass}", e)
                emptyList()
            }
        }
        return models.map { mapper.toDto(it) }
    }
}
package space.davids_digital.kiri.rest.controller.telegram

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.orm.service.telegram.TelegramChatOrmService
import space.davids_digital.kiri.rest.dto.telegram.TelegramChatDto
import space.davids_digital.kiri.rest.dto.telegram.TelegramChatFetchResultDto
import space.davids_digital.kiri.rest.dto.telegram.TelegramChatMetadataUpdateRequest
import space.davids_digital.kiri.rest.mapper.telegram.TelegramChatDtoMapper

@RestController
@RequestMapping("/telegram/chats")
class TelegramChatController(
    private val mapper: TelegramChatDtoMapper,
    private val orm: TelegramChatOrmService,
    private val telegram: TelegramService,
) {
    @GetMapping("search")
    fun search(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) titleContains: String?,
        @RequestParam(required = false) usernameContains: String?,
        @RequestParam(required = false) typeIn: List<TelegramChatDto.Type>?,
        @PageableDefault(size = 20, sort = ["title"]) pageable: Pageable
    ): Page<TelegramChatDto> {
        val typeInModel = typeIn?.mapNotNull(mapper::toModel) ?: emptyList()
        val modelsPage = orm.search(query, titleContains, usernameContains, typeInModel, pageable)
        return modelsPage.map(mapper::toDto)
    }

    @GetMapping("fetch")
    suspend fun fetch(
        @RequestParam(required = false) chatId: Long?,
        @RequestParam(required = false) username: String?,
    ): TelegramChatFetchResultDto {
        if (chatId == null && username == null) {
            throw IllegalArgumentException("Either chatId or username must be provided")
        }
        val chat = when {
            chatId != null -> telegram.fetchAndSaveChatById(chatId)
            username != null -> telegram.fetchAndSaveChatByUsername(username)
            else -> throw IllegalArgumentException("Either chatId or username must be provided")
        }
        return TelegramChatFetchResultDto(
            found = chat != null,
            chat = chat?.let { mapper.toDto(it) }
        )
    }

    @PostMapping("{id}/metadata")
    fun updateMetadata(@PathVariable id: Long, @RequestBody request: TelegramChatMetadataUpdateRequest) {
        orm.updateMetadata(id) {
            notificationMode = mapper.toModel(request.notificationMode)
            enabled = request.enabled
        }
    }
}
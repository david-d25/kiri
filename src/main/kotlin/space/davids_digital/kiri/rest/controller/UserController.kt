package space.davids_digital.kiri.rest.controller

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.model.User
import space.davids_digital.kiri.orm.service.UserOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramChatOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramUserOrmService
import space.davids_digital.kiri.rest.dto.UserCreateRequest
import space.davids_digital.kiri.rest.dto.UserDto
import space.davids_digital.kiri.rest.dto.UserUpdateRequest
import space.davids_digital.kiri.rest.mapper.UserDtoMapper
import space.davids_digital.kiri.security.RequiresOwnerRole
import space.davids_digital.kiri.service.TelegramChatService
import space.davids_digital.kiri.service.UserService
import space.davids_digital.kiri.service.exception.ServiceException

@RestController
@RequestMapping("/users")
class UserController(
    private val service: UserService,
    private val userOrm: UserOrmService,
    private val mapper: UserDtoMapper,
    private val chatService: TelegramChatService,
    private val chatOrm: TelegramChatOrmService,
    private val telegram: TelegramService,
    private val telegramUserOrmService: TelegramUserOrmService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @RequiresOwnerRole
    @GetMapping
    suspend fun getAll(): List<UserDto> {
        val users = userOrm.findAll()
        val userIds = users.map { it.id }
        val chats = chatOrm.findByIds(userIds).associateBy { it.id }
        val telegramUsers = telegramUserOrmService.findAllById(userIds).associateBy { it.id }
        return users.mapNotNull { user ->
            val chat = chats[user.id]
            val telegramUser = telegramUsers[user.id]
            if (telegramUser == null) {
                log.error("Telegram user with id ${user.id} not found")
                return@mapNotNull null
            }
            mapper.toDto(
                user,
                telegramUser,
                chat?.photo?.smallFileId?.let { chatService.createChatPhotoUrl(it) },
                chat?.photo?.bigFileId?.let { chatService.createChatPhotoUrl(it) }
            )
        }
    }

    @PutMapping
    suspend fun create(@RequestBody request: UserCreateRequest): UserDto {
        val createdUser = service.create(User(request.telegramUserId, mapper.toModel(request.role)))
        val chat = telegram.fetchAndSaveChatById(request.telegramUserId)
        val telegramUser = telegramUserOrmService.findById(request.telegramUserId)
            ?: throw ServiceException("Telegram user with id ${request.telegramUserId} not found")
        return mapper.toDto(
            createdUser,
            telegramUser,
            chat?.photo?.smallFileId?.let { chatService.createChatPhotoUrl(it) },
            chat?.photo?.bigFileId?.let { chatService.createChatPhotoUrl(it) }
        )!!
    }

    @PostMapping("{id}")
    suspend fun update(@PathVariable id: Long, @RequestBody request: UserUpdateRequest): UserDto {
        val updatedUser = service.changeRole(id, mapper.toModel(request.role))
        val chat = telegram.fetchAndSaveChatById(id)
        val telegramUser = telegramUserOrmService.findById(id)
            ?: throw ServiceException("Telegram user with id $id not found")
        return mapper.toDto(
            updatedUser,
            telegramUser,
            chat?.photo?.smallFileId?.let { chatService.createChatPhotoUrl(it) },
            chat?.photo?.bigFileId?.let { chatService.createChatPhotoUrl(it) }
        )!!
    }

    @DeleteMapping("{id}")
    suspend fun delete(@PathVariable id: Long) {
        service.delete(id)
    }
}
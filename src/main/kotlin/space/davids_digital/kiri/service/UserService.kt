package space.davids_digital.kiri.service

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.security.access.AccessDeniedException
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.model.User
import space.davids_digital.kiri.model.UserSession
import space.davids_digital.kiri.model.telegram.TelegramChat
import space.davids_digital.kiri.model.telegram.TelegramUser
import space.davids_digital.kiri.orm.service.UserOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramUserOrmService
import space.davids_digital.kiri.rest.auth.UserAuthentication
import space.davids_digital.kiri.service.exception.ResourceNotFoundException
import space.davids_digital.kiri.security.RequiresOwnerRole

@Service
class UserService(
    private val orm: UserOrmService,
    private val telegramService: TelegramService,
    private val telegramUserOrmService: TelegramUserOrmService,
    private val telegramUserMetadataService: TelegramUserMetadataService,
) {
    @RequiresOwnerRole
    @Transactional
    suspend fun create(user: User): User {
        var savedTelegramUser = telegramUserOrmService.findById(user.id)
        val chat = telegramService.fetchAndSaveChatById(user.id)
        if (savedTelegramUser == null) {
            savedTelegramUser = TelegramUser(
                id = user.id,
                isBot = false,
                firstName = "Unknown",
                metadata = telegramUserMetadataService.getOrCreateDefault(user.id)
            )
        }
        if (chat != null && chat.type == TelegramChat.Type.PRIVATE) {
            savedTelegramUser = savedTelegramUser.copy(
                id = chat.id,
                isBot = false,
                firstName = chat.firstName ?: "Unknown",
                lastName = chat.lastName,
                username = chat.username
            )
        }
        savedTelegramUser = telegramUserOrmService.save(savedTelegramUser)
        return orm.save(User(savedTelegramUser.id, user.role))
    }

    @RequiresOwnerRole
    fun changeRole(id: Long, role: User.Role): User {
        val requestingUserId = requireSession().userId
        if (requestingUserId == id) {
            throw AccessDeniedException("You cannot change role of yourself")
        }
        val user = orm.findById(id) ?: throw ResourceNotFoundException("User not found")
        return orm.save(user.copy(role = role))
    }

    @RequiresOwnerRole
    fun delete(id: Long) {
        val requestingUserId = requireSession().userId
        if (requestingUserId == id) {
            throw AccessDeniedException("You cannot delete yourself")
        }
        orm.deleteById(id)
    }

    private fun requireSession(): UserSession {
        val auth = SecurityContextHolder.getContext().authentication ?: throw AccessDeniedException("Unauthenticated")
        if (auth !is UserAuthentication) {
            throw AccessDeniedException("Not authenticated as user")
        }
        return auth.session
    }
}
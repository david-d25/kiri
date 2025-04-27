package space.davids_digital.kiri.service

import org.springframework.stereotype.Service
import space.davids_digital.kiri.orm.repository.AdminUserRepository

/**
 * Service to manage and lookup administrative users.
 */
@Service
class AdminUserService(
    private val adminUserRepository: AdminUserRepository
) {
    /**
     * Returns true if the given user ID is marked as an admin.
     */
    fun isAdmin(userId: Long): Boolean = adminUserRepository.existsByUserId(userId)
}
package space.davids_digital.kiri.orm.repository

import org.springframework.data.jpa.repository.JpaRepository
import space.davids_digital.kiri.orm.entity.AdminUserEntity

/**
 * Repository for admin users lookup.
 */
interface AdminUserRepository : JpaRepository<AdminUserEntity, Long> {
    /**
     * Checks whether the given user ID has administrative privileges.
     */
    fun existsByUserId(userId: Long): Boolean
}
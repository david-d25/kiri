package space.davids_digital.kiri.rest.controller

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.Settings
import space.davids_digital.kiri.rest.auth.UserAuthentication
import space.davids_digital.kiri.rest.dto.BootstrapDto
import space.davids_digital.kiri.service.AdminUserService

@RestController
@RequestMapping("/bootstrap")
class BootstrapController(
    private val settings: Settings,
    private val adminUserService: AdminUserService
) {
    @GetMapping
    fun bootstrap(): BootstrapDto {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.isAuthenticated && authentication is UserAuthentication) {
            val userId = authentication.principal
            val session = authentication.session
            val isAdmin = adminUserService.isAdmin(userId)
            return BootstrapDto(
                isAuthenticated = true,
                user = BootstrapDto.UserInfo(
                    id = userId,
                    isAdmin = isAdmin,
                    firstName = session.firstName,
                    lastName = session.lastName,
                    username = session.username,
                    photoUrl = session.photoUrl,
                ),
            )
        } else {
            return BootstrapDto(
                isAuthenticated = false,
                login = BootstrapDto.LoginInfo(
                    settings.auth.telegram.botUsername,
                    settings.auth.telegram.callbackUrl,
                )
            )
        }
    }
}
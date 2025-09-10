package space.davids_digital.kiri.rest.controller

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.AppProperties
import space.davids_digital.kiri.orm.service.UserOrmService
import space.davids_digital.kiri.rest.auth.UserAuthentication
import space.davids_digital.kiri.rest.dto.BootstrapDto
import space.davids_digital.kiri.rest.mapper.UserDtoMapper

@RestController
@RequestMapping("/bootstrap")
class BootstrapController(
    private val appProperties: AppProperties,
    private val userOrmService: UserOrmService,
    private val userDtoMapper: UserDtoMapper
) {
    @GetMapping
    fun bootstrap(): BootstrapDto {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication != null && authentication.isAuthenticated && authentication is UserAuthentication) {
            val userId = authentication.principal
            val session = authentication.session
            val user = userOrmService.findById(userId)
            if (user != null) {
                return BootstrapDto(
                    version = appProperties.version,
                    isAuthenticated = true,
                    user = BootstrapDto.UserInfo(
                        id = userId,
                        role = userDtoMapper.toDto(user.role),
                        firstName = session.firstName,
                        lastName = session.lastName,
                        username = session.username,
                        photoUrl = session.photoUrl,
                    ),
                )
            }
        }
        return BootstrapDto(
            version = appProperties.version,
            isAuthenticated = false,
            login = BootstrapDto.LoginInfo(
                appProperties.auth.telegram.botUsername,
                appProperties.auth.telegram.callbackUrl,
            )
        )
    }
}
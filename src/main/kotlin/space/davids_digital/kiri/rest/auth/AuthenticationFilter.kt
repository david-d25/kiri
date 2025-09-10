package space.davids_digital.kiri.rest.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.GenericFilterBean
import org.springframework.security.core.authority.SimpleGrantedAuthority
import space.davids_digital.kiri.model.UserSession
import space.davids_digital.kiri.orm.service.UserOrmService
import space.davids_digital.kiri.orm.service.UserSessionOrmService
import space.davids_digital.kiri.orm.service.UserSessionOrmService.UserSessionDecryptException
import space.davids_digital.kiri.rest.CookieName
import space.davids_digital.kiri.rest.exception.InvalidSessionStateException
import java.io.IOException

class AuthenticationFilter(
    private val userSessionOrmService: UserSessionOrmService,
    private val users: UserOrmService,
) : GenericFilterBean() {
    companion object {
        private const val ROLE_PREFIX = "ROLE_"
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, filterChain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val userId = getCookie(httpRequest, CookieName.USER_ID)?.toLongOrNull()
        val sessionToken = getCookie(httpRequest, CookieName.AUTH_TOKEN)
        if (userId != null && sessionToken != null) {
            val session = getValidatedSession(userId, sessionToken)
            val user = users.findById(userId)
            if (session != null && user != null) {
                val authorities = listOf(SimpleGrantedAuthority(ROLE_PREFIX + user.role.name))
                SecurityContextHolder.getContext().authentication = UserAuthentication(session, authorities)
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun getCookie(request: HttpServletRequest, name: String): String? {
        val cookies = request.cookies ?: return null
        for (cookie in cookies) {
            if (cookie.name == name) {
                return cookie.value
            }
        }
        return null
    }

    private fun getValidatedSession(userId: Long, sessionToken: String): UserSession? {
        val sessions = try {
            userSessionOrmService.getUnexpiredUserSessionsByUserId(userId)
        } catch (e: UserSessionDecryptException) {
            throw InvalidSessionStateException(e.message)
        }
        return sessions.firstOrNull { it.token == sessionToken }
    }
}
package space.davids_digital.kiri.rest.auth

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import space.davids_digital.kiri.model.UserSession
import kotlin.jvm.Throws

class UserAuthentication(
    val session: UserSession,
    private val authorities: Collection<GrantedAuthority> = emptyList()
) : Authentication {
    private var authenticated = true

    override fun getAuthorities(): Collection<GrantedAuthority> = authorities

    override fun getCredentials(): String = session.token

    override fun getDetails(): UserSession = session

    override fun getPrincipal(): Long {
        return session.userId
    }

    override fun isAuthenticated(): Boolean {
        return authenticated
    }

    @Throws(IllegalArgumentException::class)
    override fun setAuthenticated(isAuthenticated: Boolean) {
        authenticated = isAuthenticated
    }

    override fun getName(): String = session.firstName
}

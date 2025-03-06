package space.davids_digital.kiri.rest.auth

import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import space.davids_digital.kiri.model.UserSession
import kotlin.jvm.Throws

class UserAuthentication(val session: UserSession): Authentication {
    private var authenticated = true

    override fun getAuthorities(): Collection<GrantedAuthority?>? {
        return null
    }

    override fun getCredentials(): Any? {
        return null
    }

    override fun getDetails(): Any? {
        return null
    }

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

    override fun getName(): String? {
        return null
    }
}

package space.davids_digital.kiri.rest

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity

fun ResponseEntity.BodyBuilder.removeAuthCookies(cookiesDomain: String): ResponseEntity.BodyBuilder {
    this.header(HttpHeaders.SET_COOKIE, createExpiredUserIdCookie(cookiesDomain).toString())
    this.header(HttpHeaders.SET_COOKIE, createExpiredSessionTokenCookie(cookiesDomain).toString())
    return this
}

fun HttpServletResponse.removeAuthCookies(cookiesDomain: String) {
    this.addHeader(HttpHeaders.SET_COOKIE, createExpiredUserIdCookie(cookiesDomain).toString())
    this.addHeader(HttpHeaders.SET_COOKIE, createExpiredSessionTokenCookie(cookiesDomain).toString())
}

private fun createExpiredSessionTokenCookie(cookiesDomain: String): ResponseCookie {
    return ResponseCookie.from(CookieName.AUTH_TOKEN, "")
        .maxAge(0)
        .httpOnly(true)
        .secure(true)
        .path("/")
        .sameSite("Strict")
        .domain(cookiesDomain)
        .build()
}

private fun createExpiredUserIdCookie(cookiesDomain: String): ResponseCookie {
    return ResponseCookie.from(CookieName.USER_ID, "")
        .maxAge(0)
        .secure(true)
        .path("/")
        .sameSite("Strict")
        .domain(cookiesDomain)
        .build()
}
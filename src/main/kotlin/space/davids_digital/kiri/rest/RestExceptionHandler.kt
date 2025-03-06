package space.davids_digital.kiri.rest

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import space.davids_digital.kiri.Settings
import space.davids_digital.kiri.rest.exception.InvalidSessionStateException
import space.davids_digital.kiri.service.exception.ValidationException

@ControllerAdvice
class RestExceptionHandler(private val settings: Settings) {
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameters(exception: MissingServletRequestParameterException): ResponseEntity<String> {
        return ResponseEntity
            .badRequest()
            .body("'${exception.parameterName}' parameter is missing")
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(exception: ValidationException, request: WebRequest): ResponseEntity<String> {
        return ResponseEntity
            .badRequest()
            .body("Validation failed: ${exception.message}")
    }

    @ExceptionHandler(InvalidSessionStateException::class)
    fun handleInvalidSessionStateException(
        exception: ValidationException,
        request: WebRequest,
        response: HttpServletResponse
    ): ResponseEntity<String> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .removeAuthCookies(settings.frontend.cookiesDomain)
            .body("Invalid session state: ${exception.message}")
    }
}

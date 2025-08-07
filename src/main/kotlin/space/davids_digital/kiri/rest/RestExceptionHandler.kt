package space.davids_digital.kiri.rest

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import space.davids_digital.kiri.AppProperties
import space.davids_digital.kiri.rest.dto.ErrorResponseDto
import space.davids_digital.kiri.rest.exception.InvalidSessionStateException
import space.davids_digital.kiri.rest.exception.ResourceNotFoundException
import space.davids_digital.kiri.service.exception.ValidationException

@ControllerAdvice
class RestExceptionHandler(private val appProperties: AppProperties) {
    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParameters(
        exception: MissingServletRequestParameterException
    ): ResponseEntity<ErrorResponseDto> {
        return ResponseEntity
            .badRequest()
            .body(ErrorResponseDto("bad_request", "Missing parameter '${exception.parameterName}'"))
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(exception: ValidationException): ResponseEntity<ErrorResponseDto> {
        return ResponseEntity
            .badRequest()
            .body(ErrorResponseDto("bad_request", "Validation failed: ${exception.message}"))
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(): ResponseEntity<ErrorResponseDto> {
        return ResponseEntity.notFound().build()
    }

    @ExceptionHandler(InvalidSessionStateException::class)
    fun handleInvalidSessionStateException(exception: ValidationException): ResponseEntity<ErrorResponseDto> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .removeAuthCookies(appProperties.frontend.cookiesDomain)
            .body(ErrorResponseDto("forbidden", "Invalid session state: ${exception.message}"))
    }
}

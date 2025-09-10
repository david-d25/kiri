package space.davids_digital.kiri.security

import org.springframework.security.access.prepost.PreAuthorize
import space.davids_digital.kiri.model.User

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@PreAuthorize("hasRole('{value}')")
annotation class RequiresRole(val value: User.Role)
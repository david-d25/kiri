package space.davids_digital.kiri.security

import space.davids_digital.kiri.model.User

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@RequiresRole(User.Role.OWNER)
annotation class RequiresOwnerRole
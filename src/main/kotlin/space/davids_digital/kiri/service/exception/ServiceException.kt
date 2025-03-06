package space.davids_digital.kiri.service.exception

class ServiceException(override val message: String) : RuntimeException(message) {
    constructor(message: String, cause: Throwable) : this(message) {
        initCause(cause)
    }
}
package space.davids_digital.kiri.model

data class User (
    val id: Long,
    val role: Role
) {
    enum class Role {
        OWNER, ADMIN
    }
}
package space.davids_digital.kiri.rest.dto

data class SettingUpdateRequestDto (
    val value: String?,
    val encrypt: Boolean = false
)
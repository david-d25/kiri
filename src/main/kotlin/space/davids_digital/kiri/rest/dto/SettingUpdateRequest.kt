package space.davids_digital.kiri.rest.dto

data class SettingUpdateRequest (
    val value: String?,
    val encrypt: Boolean = false
)
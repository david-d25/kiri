package space.davids_digital.kiri.rest.dto

import java.time.ZonedDateTime

data class SettingDto (
    val key: String,
    val value: String?,
    val updatedAt: ZonedDateTime
)
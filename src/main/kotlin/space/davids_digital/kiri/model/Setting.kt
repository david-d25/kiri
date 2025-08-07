package space.davids_digital.kiri.model

import java.time.ZonedDateTime

data class Setting (
    val key: String,
    val value: String?,
    val updatedAt: ZonedDateTime
)
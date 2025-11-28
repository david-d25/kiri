package space.davids_digital.kiri.rest.dto

data class SettingsUpdateRequest (
    val updates: Map<String, SettingUpdateRequest>
)
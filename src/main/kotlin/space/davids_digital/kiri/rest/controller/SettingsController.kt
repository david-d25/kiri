package space.davids_digital.kiri.rest.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.orm.service.SettingOrmService
import space.davids_digital.kiri.rest.dto.SettingDto
import space.davids_digital.kiri.rest.dto.SettingUpdateRequestDto
import kotlin.let

@RestController
@RequestMapping("/settings")
class SettingsController(
    private val settings: SettingOrmService
) {
    @GetMapping("{key}")
    fun get(@PathVariable key: String): SettingDto {
        return settings.get(key).let { SettingDto(it.key, it.value, it.updatedAt) }
    }

    @PostMapping("{key}")
    fun set(@PathVariable key: String, @RequestBody request: SettingUpdateRequestDto): SettingDto {
        val newSetting = settings.set(key, request.value, request.encrypt)
        return SettingDto(newSetting.key, newSetting.value, newSetting.updatedAt)
    }
}
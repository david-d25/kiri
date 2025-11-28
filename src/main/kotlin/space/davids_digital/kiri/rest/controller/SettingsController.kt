package space.davids_digital.kiri.rest.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.orm.service.SettingOrmService
import space.davids_digital.kiri.rest.dto.SettingDto
import space.davids_digital.kiri.rest.dto.SettingUpdateRequest
import space.davids_digital.kiri.rest.dto.SettingsUpdateRequest
import kotlin.let
import kotlin.text.get
import kotlin.text.set

@RestController
@RequestMapping("/settings")
class SettingsController(
    private val settings: SettingOrmService
) {
    @GetMapping
    fun getByKeys(keys: Array<String>): List<SettingDto> {
        return settings.getByKeys(keys.toList()).map { SettingDto(it.key, it.value, it.updatedAt) }
    }

    @GetMapping("{key}")
    fun get(@PathVariable key: String): SettingDto {
        return settings.get(key).let { SettingDto(it.key, it.value, it.updatedAt) }
    }

    @PostMapping("{key}")
    fun set(@PathVariable key: String, @RequestBody request: SettingUpdateRequest): SettingDto {
        val newSetting = settings.set(key, request.value, request.encrypt)
        return SettingDto(newSetting.key, newSetting.value, newSetting.updatedAt)
    }

    @PostMapping
    fun setMany(@RequestBody request: SettingsUpdateRequest) {
        settings.setMany(
            request.updates.mapValues { SettingOrmService.UpdateRequest(it.value.value, it.value.encrypt) }
        )
    }
}
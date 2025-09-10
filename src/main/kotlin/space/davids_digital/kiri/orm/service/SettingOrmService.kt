package space.davids_digital.kiri.orm.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.slf4j.LoggerFactory.getLogger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.Setting
import space.davids_digital.kiri.orm.entity.SettingEntity
import space.davids_digital.kiri.orm.repository.SettingRepository
import space.davids_digital.kiri.service.CryptographyService
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Base64
import kotlin.collections.set
import kotlin.jvm.java
import kotlin.jvm.optionals.getOrNull
import kotlin.text.toByteArray

@Service
class SettingOrmService(
    private val repo: SettingRepository,
    private val cryptography: CryptographyService,
    private val objectMapper: ObjectMapper
) {
    private val log = getLogger(this::class.java)

    private val flows: MutableMap<String, MutableSharedFlow<Setting>> = mutableMapOf()

    @Transactional(readOnly = true)
    fun get(key: String): Setting { // TODO make it return `Setting?`
        val entity = repo.findById(key).getOrNull() ?: return Setting(
            key = key,
            value = null,
            updatedAt = ZonedDateTime.ofInstant(Instant.ofEpochSecond(0), ZoneId.systemDefault())
        )
        return toModel(entity)
    }

    @Transactional(readOnly = true)
    fun getValue(key: String): String? {
        val entity = repo.findById(key).getOrNull() ?: return null
        return entity.value
    }

    /**
     * Sets the value of a setting with the given key.
     */
    @Transactional(readOnly = true)
    fun listen(key: String): SharedFlow<Setting> {
        if (flows.containsKey(key)) {
            return flows[key]!!.asSharedFlow()
        } else {
            val flow = MutableSharedFlow<Setting>(replay = 1)
            flows[key] = flow
            flow.tryEmit(get(key)) // Emit the current value immediately
            return flow.asSharedFlow()
        }
    }

    @Transactional
    fun set(key: String, value: String?, encrypt: Boolean = false): Setting {
        val existingEntity = repo.findById(key).getOrNull()
        val entity = existingEntity ?: SettingEntity()

        entity.key = key
        entity.value = value
        entity.encrypted = encrypt
        entity.updatedAt = OffsetDateTime.now()

        if (encrypt && value != null) {
            val encryptedValue = cryptography.encrypt(value.toByteArray(Charsets.UTF_8))
            entity.value = Base64.getEncoder().encodeToString(encryptedValue)
        }

        val savedEntity = repo.save(entity)
        log.info("Set value for setting with key '$key'")

        // Emit the new value to the flow
        flows[key]?.tryEmit(toModel(entity))
        return toModel(savedEntity)
    }

    @Transactional
    fun delete(key: String) {
        val entity = repo.findById(key).getOrNull() ?: return
        repo.delete(entity)
        log.info("Deleted setting with key '$key'")
    }

    private fun toModel(entity: SettingEntity): Setting {
        val rawValue = entity.value
        if (entity.encrypted) {
            val byteArray = rawValue?.let { Base64.getDecoder().decode(it) }
            val decryptedValue = byteArray?.let { cryptography.decrypt(it) }
            return Setting(
                key = entity.key,
                value = decryptedValue?.toString(Charsets.UTF_8),
                updatedAt = entity.updatedAt.toZonedDateTime(),
            )
        } else {
            return Setting(
                key = entity.key,
                value = rawValue,
                updatedAt = entity.updatedAt.toZonedDateTime(),
            )
        }
    }
}
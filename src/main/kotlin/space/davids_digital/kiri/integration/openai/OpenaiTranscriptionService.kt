package space.davids_digital.kiri.integration.openai

import com.openai.models.audio.AudioModel
import com.openai.models.audio.transcriptions.TranscriptionCreateParams
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import space.davids_digital.kiri.orm.entity.AudioTranscriptionEntity
import space.davids_digital.kiri.orm.repository.AudioTranscriptionRepository
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeBytes
import kotlin.jvm.optionals.getOrNull
import java.nio.file.Files

@Service
class OpenaiTranscriptionService(
    private val clientHolder: OpenaiClientHolder,
    private val repository: AudioTranscriptionRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Returns the transcription for the given audio file.
     * Checks DB first; if not found, transcribes via OpenAI Whisper and saves.
     * [audioProvider] is only called if transcription is not already cached in DB.
     * Returns null if OpenAI client is not configured.
     */
    fun transcribe(fileUniqueId: String, audioProvider: () -> ByteArray): String? {
        repository.findById(fileUniqueId).getOrNull()?.let { return it.text }

        val client = clientHolder.getClient() ?: run {
            log.warn("OpenAI client is not configured, cannot transcribe audio")
            return null
        }

        val bytes = audioProvider()
        log.info("Transcribing audio {} ({} bytes)", fileUniqueId, bytes.size)

        val tempFile = Files.createTempFile("voice_", ".ogg")
        val text = try {
            tempFile.writeBytes(bytes)
            val response = client.audio().transcriptions().create(
                TranscriptionCreateParams.builder()
                    .file(tempFile)
                    .model(AudioModel.WHISPER_1)
                    .build()
            )
            response.asTranscription().text()
        } finally {
            tempFile.deleteIfExists()
        }
        log.info("Transcription complete for {}: {} chars", fileUniqueId, text.length)

        try {
            repository.save(AudioTranscriptionEntity().apply {
                this.fileUniqueId = fileUniqueId
                this.text = text
            })
        } catch (e: DataIntegrityViolationException) {
            log.debug("Transcription for {} already saved by concurrent request", fileUniqueId)
        }

        return text
    }
}

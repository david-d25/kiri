package space.davids_digital.kiri.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class TemporaryFilesService {
    val overallMaxSize = 256 * 1024 * 1024 // 256 MB
    val fileTtl = 60 * 60 * 1000L // 1 hour

    data class TemporaryFile(
        val content: ByteArray,
        val createdAt: Long,
    )

    private val mutex = Mutex()
    private val files = ConcurrentHashMap<String, TemporaryFile>()

    suspend fun create(name: String, content: ByteArray): TemporaryFile {
        if (content.size > overallMaxSize) {
            throw IllegalArgumentException("File size exceeds maximum allowed size of $overallMaxSize bytes")
        }
        mutex.withLock {
            val file = TemporaryFile(content, System.currentTimeMillis())
            files[name] = file
            cleanupUnsafe()
            return file
        }
    }

    suspend fun getContent(name: String): ByteArray? {
        mutex.withLock {
            return files[name]?.content
        }
    }

    /**
     * Remove old files and make sure total size is within limit.
     * If the limit is exceeded, remove the oldest files first.
     */
    private fun cleanupUnsafe() {
        val now = System.currentTimeMillis()
        // Remove expired files
        files.entries.removeIf { now - it.value.createdAt > fileTtl }

        // Check total size
        var totalSize = files.values.sumOf { it.content.size }
        if (totalSize <= overallMaxSize) {
            return
        }

        // Sort files by creation time (oldest first)
        val sortedFiles = files.entries.sortedBy { it.value.createdAt }
        for (entry in sortedFiles) {
            files.remove(entry.key)
            totalSize -= entry.value.content.size
            if (totalSize <= overallMaxSize) {
                break
            }
        }
    }
}
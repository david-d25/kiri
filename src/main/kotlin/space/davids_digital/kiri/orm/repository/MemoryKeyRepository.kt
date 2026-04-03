package space.davids_digital.kiri.orm.repository

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.MemoryKeyEntity
import java.sql.ResultSet
import java.util.*

@Repository
class MemoryKeyRepository(
    private val jdbcTemplate: NamedParameterJdbcTemplate,
    private val embeddingModelRepository: EmbeddingModelRepository,
) {
    fun save(memoryKey: MemoryKeyEntity): MemoryKeyEntity {
        val sql = """
            insert into main.memory_keys(id, key_text, embedding_model_id, embedding)
            values (:id, :keyText, :embeddingModelId, :embedding)
            on conflict (key_text) do update 
                set embedding_model_id = EXCLUDED.embedding_model_id,
                    embedding = EXCLUDED.embedding
            returning id, key_text, embedding_model_id, embedding
        """.trimIndent()
        val params = mapOf(
            "id" to memoryKey.id,
            "keyText" to memoryKey.keyText,
            "embeddingModelId" to memoryKey.embeddingModel.id,
            "embedding" to floatArrayToPgVector(memoryKey.embedding)
        )
        return jdbcTemplate.query(sql, params) { rs, _ ->
            mapRowToEntity(rs)
        }.first()
    }

    fun findByKeyText(keyText: String): MemoryKeyEntity? {
        val sql = """
            SELECT 
                id, 
                key_text, 
                embedding_model_id,
                embedding
            FROM main.memory_keys
            WHERE key_text = :keyText
        """.trimIndent()
        val params = mapOf("keyText" to keyText)
        return jdbcTemplate.query(sql, params) { rs, _ ->
            mapRowToEntity(rs)
        }.firstOrNull()
    }

    fun findByIdIn(ids: Collection<UUID>): List<MemoryKeyEntity> {
        val sql = """
            SELECT 
                id, 
                key_text, 
                embedding_model_id,
                embedding
            FROM main.memory_keys
            WHERE id IN (:ids)
        """.trimIndent()
        val params = mapOf("ids" to ids)
        return jdbcTemplate.query(sql, params) { rs, _ ->
            mapRowToEntity(rs)
        }
    }

    fun findAll(offset: Int, limit: Int): List<MemoryKeyEntity> {
        val sql = """
            SELECT id, key_text, embedding_model_id, embedding
            FROM main.memory_keys
            ORDER BY key_text
            LIMIT :limit OFFSET :offset
        """.trimIndent()
        val params = mapOf("offset" to offset, "limit" to limit)
        return jdbcTemplate.query(sql, params) { rs, _ -> mapRowToEntity(rs) }
    }

    fun count(): Long {
        val sql = "SELECT count(*) FROM main.memory_keys"
        return jdbcTemplate.queryForObject(sql, emptyMap<String, Any>(), Long::class.java) ?: 0
    }

    fun findByKeyTextContainingIgnoreCase(query: String, offset: Int, limit: Int): List<MemoryKeyEntity> {
        val sql = """
            SELECT id, key_text, embedding_model_id, embedding
            FROM main.memory_keys
            WHERE LOWER(key_text) LIKE LOWER(:query)
            ORDER BY key_text
            LIMIT :limit OFFSET :offset
        """.trimIndent()
        val params = mapOf("query" to "%$query%", "offset" to offset, "limit" to limit)
        return jdbcTemplate.query(sql, params) { rs, _ -> mapRowToEntity(rs) }
    }

    fun countByKeyTextContainingIgnoreCase(query: String): Long {
        val sql = """
            SELECT count(*) FROM main.memory_keys
            WHERE LOWER(key_text) LIKE LOWER(:query)
        """.trimIndent()
        val params = mapOf("query" to "%$query%")
        return jdbcTemplate.queryForObject(sql, params, Long::class.java) ?: 0
    }

    fun deleteById(id: UUID) {
        jdbcTemplate.update(
            "DELETE FROM main.memory_links WHERE memory_key_id = :id",
            mapOf("id" to id)
        )
        jdbcTemplate.update(
            "DELETE FROM main.memory_keys WHERE id = :id",
            mapOf("id" to id)
        )
    }

    fun findNearest(embedding: FloatArray, limit: Int): List<MemoryKeyEntity> {
        val sql = """
            select id, key_text, embedding_model_id, embedding
            from main.memory_keys
            order by l2_distance(embedding, :embedding::vector)
            limit :limit
        """.trimIndent()
        val params = mapOf(
            "embedding" to floatArrayToPgVector(embedding),
            "limit" to limit
        )
        return jdbcTemplate.query(sql, params) { rs, _ ->
            mapRowToEntity(rs)
        }
    }

    private fun mapRowToEntity(rs: ResultSet): MemoryKeyEntity {
        val id = rs.getObject("id", UUID::class.java)
        val keyText = rs.getString("key_text")
        val embeddingModelId = rs.getLong("embedding_model_id").let {
            if (rs.wasNull()) error("embedding_model_id is null") else it
        }
        val embeddingString = rs.getString("embedding")
        val embeddingArray = parsePgVectorString(embeddingString)

        return MemoryKeyEntity().also {
            it.id = id
            it.keyText = keyText
            it.embeddingModel = embeddingModelRepository.findById(embeddingModelId).orElseThrow()
            it.embedding = embeddingArray
        }
    }


    private fun floatArrayToString(array: FloatArray): String {
        return array.joinToString(prefix = "[", postfix = "]", separator = ",")
    }

    private fun floatArrayToPgVector(array: FloatArray): Any {
        if (array.isEmpty()) {
            val pgObject = org.postgresql.util.PGobject()
            pgObject.type = "vector"
            pgObject.value = "[]"
            return pgObject
        }
        val pgObject = org.postgresql.util.PGobject()
        pgObject.type = "vector"
        pgObject.value = array.joinToString(
            prefix = "[", postfix = "]", separator = ", "
        ) { it.toString() }
        return pgObject
    }

    private fun parsePgVectorString(text: String?): FloatArray {
        if (text.isNullOrBlank()) {
            return floatArrayOf()
        }
        val trimmed = text.trim()
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return floatArrayOf()
        }
        val inner = trimmed.substring(1, trimmed.length - 1).trim()
        if (inner.isEmpty()) {
            return floatArrayOf()
        }
        return inner.split(",").map { it.trim().toFloat() }.toFloatArray()
    }
}
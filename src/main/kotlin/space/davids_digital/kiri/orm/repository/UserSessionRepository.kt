package space.davids_digital.kiri.orm.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.UserSessionEntity
import java.time.OffsetDateTime
import java.util.*

@Repository
interface UserSessionRepository: CrudRepository<UserSessionEntity, UUID> {
    fun findAllByValidUntilBefore(validUntil: OffsetDateTime): Collection<UserSessionEntity>

    @Query("select e from UserSessionEntity e where e.userId = :userId and (e.validUntil > :validUntil or e.validUntil is null)")
    fun findAllByUserIdAndValidUntilAfterOrNull(userId: Long, validUntil: OffsetDateTime): Collection<UserSessionEntity>

    fun deleteByValidUntilBefore(validUntil: OffsetDateTime)
}

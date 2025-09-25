package space.davids_digital.kiri.orm.service.filesystem

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.filesystem.FileSystemSpace
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemSpaceEntity
import space.davids_digital.kiri.orm.mapper.filesystem.FileSystemSpaceEntityMapper
import space.davids_digital.kiri.orm.repository.filesystem.FileSystemSpaceRepository
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

@Service
class FileSystemSpaceOrmService(
    private val repo: FileSystemSpaceRepository,
    private val mapper: FileSystemSpaceEntityMapper,
) {
    @Transactional(readOnly = true)
    fun findById(id: UUID): FileSystemSpace? = mapper.toModel(repo.findById(id).getOrNull())

    @Transactional(readOnly = true)
    fun findEntityById(id: UUID): FileSystemSpaceEntity? = repo.findById(id).getOrNull()

    @Transactional(readOnly = true)
    fun findAll(): List<FileSystemSpace> = repo.findAll().mapNotNull(mapper::toModel)

    @Transactional(readOnly = true)
    fun findAllByOwner(ownerUserId: Long): List<FileSystemSpace> =
        repo.findAllByOwnerUserId(ownerUserId).mapNotNull(mapper::toModel)

    @Transactional(readOnly = true)
    fun findAllGlobal(): List<FileSystemSpace> = repo.findAllByOwnerUserIdIsNull().mapNotNull(mapper::toModel)

    @Transactional
    fun save(space: FileSystemSpaceEntity): FileSystemSpace {
        return mapper.toModel(repo.save(space))!!
    }

    @Transactional(readOnly = true)
    fun existsBySlugForOwner(slug: String, ownerUserId: Long): Boolean =
        repo.existsBySlugIgnoreCaseAndOwnerUserId(slug, ownerUserId)

    @Transactional(readOnly = true)
    fun existsBySlugForGlobal(slug: String): Boolean =
        repo.existsBySlugIgnoreCaseAndOwnerUserIdIsNull(slug)
}

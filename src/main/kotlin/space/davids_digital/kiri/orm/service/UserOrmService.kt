package space.davids_digital.kiri.orm.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import space.davids_digital.kiri.model.User
import space.davids_digital.kiri.orm.mapper.UserEntityMapper
import space.davids_digital.kiri.orm.repository.UserRepository
import space.davids_digital.kiri.security.RequiresOwnerRole
import kotlin.jvm.optionals.getOrNull

@Service
class UserOrmService(
    private val repo: UserRepository,
    private val mapper: UserEntityMapper
) {
    @Transactional(readOnly = true)
    fun findAll(): List<User> = repo.findAll().mapNotNull(mapper::toModel)

    @Transactional(readOnly = true)
    fun findById(id: Long): User? = mapper.toModel(repo.findById(id).getOrNull())

    @Transactional(readOnly = true)
    fun existsById(id: Long): Boolean = repo.existsById(id)

    @RequiresOwnerRole
    @Transactional
    fun save(user: User): User {
        val entity = mapper.toEntity(user)!!
        return mapper.toModel(repo.save(entity))!!
    }

    @RequiresOwnerRole
    @Transactional
    fun deleteById(id: Long) = repo.deleteById(id)
}
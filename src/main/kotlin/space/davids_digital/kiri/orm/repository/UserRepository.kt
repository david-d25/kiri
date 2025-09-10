package space.davids_digital.kiri.orm.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import space.davids_digital.kiri.orm.entity.UserEntity

@Repository
interface UserRepository : JpaRepository<UserEntity, Long>
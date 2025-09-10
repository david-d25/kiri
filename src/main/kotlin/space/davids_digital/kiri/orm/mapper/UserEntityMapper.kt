package space.davids_digital.kiri.orm.mapper

import org.mapstruct.AnnotateWith
import org.mapstruct.Mapper
import org.springframework.context.annotation.Primary
import space.davids_digital.kiri.model.User
import space.davids_digital.kiri.orm.entity.UserEntity

@Mapper
@AnnotateWith(Primary::class)
interface UserEntityMapper {
    fun toModel(entity: UserEntity?): User?
    fun toEntity(model: User?): UserEntity?
}
package space.davids_digital.kiri.orm.mapper.filesystem

import org.mapstruct.AnnotateWith
import org.mapstruct.Mapper
import org.springframework.context.annotation.Primary
import space.davids_digital.kiri.model.filesystem.FileSystemSpace
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemSpaceEntity

@Mapper
@AnnotateWith(Primary::class)
interface FileSystemSpaceEntityMapper {
    fun toModel(entity: FileSystemSpaceEntity?): FileSystemSpace?
    fun toEntity(model: FileSystemSpace?): FileSystemSpaceEntity?
}

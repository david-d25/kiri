package space.davids_digital.kiri.orm.mapper.filesystem

import org.mapstruct.AnnotateWith
import org.mapstruct.Mapper
import org.springframework.context.annotation.Primary
import space.davids_digital.kiri.model.filesystem.FileSystemNode
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemNodeEntity
import space.davids_digital.kiri.orm.mapper.DateTimeMapper

@Mapper(uses = [DateTimeMapper::class])
@AnnotateWith(Primary::class)
interface FileSystemNodeEntityMapper {
    fun toModel(entity: FileSystemNodeEntity?): FileSystemNode?
    fun toEntity(model: FileSystemNode?): FileSystemNodeEntity?
}

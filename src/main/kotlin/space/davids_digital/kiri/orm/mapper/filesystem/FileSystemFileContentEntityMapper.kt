package space.davids_digital.kiri.orm.mapper.filesystem

import org.mapstruct.AnnotateWith
import org.mapstruct.Mapper
import org.springframework.context.annotation.Primary
import space.davids_digital.kiri.model.filesystem.FileSystemFileContent
import space.davids_digital.kiri.orm.entity.filesystem.FileSystemFileContentEntity

@Mapper
@AnnotateWith(Primary::class)
interface FileSystemFileContentEntityMapper {
    fun toModel(entity: FileSystemFileContentEntity?): FileSystemFileContent?
    fun toEntity(model: FileSystemFileContent?): FileSystemFileContentEntity?
}

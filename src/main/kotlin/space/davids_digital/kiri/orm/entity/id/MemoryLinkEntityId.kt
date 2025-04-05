package space.davids_digital.kiri.orm.entity.id

import java.io.Serializable
import java.util.UUID

data class MemoryLinkEntityId (
    var memoryKeyId: UUID = UUID.randomUUID(),
    var memoryPointId: UUID = UUID.randomUUID(),
) : Serializable
package space.davids_digital.kiri.agent.frame

import java.util.UUID

sealed class Frame {
    val id: String = UUID.randomUUID().toString()
}
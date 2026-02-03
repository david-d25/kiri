package space.davids_digital.kiri.rest.dto

data class FrameBufferStateDto (
    val frames: List<FrameDto>,
    val hardLimit: Int,
)
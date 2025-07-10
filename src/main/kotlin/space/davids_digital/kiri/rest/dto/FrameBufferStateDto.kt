package space.davids_digital.kiri.rest.dto

data class FrameBufferStateDto (
    val fixedFrames: List<DataFrameDto>,
    val rollingFrames: List<FrameDto>,
    val hardLimit: Int,
)
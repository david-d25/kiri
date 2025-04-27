package space.davids_digital.kiri.rest.dto

/**
 * Lightweight events sent to the admin UI via SSE / WS to notify about runtime changes.
 * Each event carries a timestamp (epochMillis) so the client can order them.
 */
sealed interface AdminEventDto {
    val type: String
    val ts: Long
}

data class EngineStartedEventDto(override val ts: Long) : AdminEventDto {
    override val type: String = "engine.started"
}

data class EngineStoppedEventDto(override val ts: Long) : AdminEventDto {
    override val type: String = "engine.stopped"
}

/** A single engine tick finished. */
data class EngineTickEventDto(override val ts: Long) : AdminEventDto {
    override val type: String = "engine.tick"
}

/** A new frame has been added to the buffer. */
data class FrameAddedEventDto(
    val sequence: Long,
    override val ts: Long
) : AdminEventDto {
    override val type: String = "frame.added"
}

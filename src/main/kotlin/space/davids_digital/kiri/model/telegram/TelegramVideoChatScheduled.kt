package space.davids_digital.kiri.model.telegram

import java.time.ZonedDateTime

data class TelegramVideoChatScheduled (
    /**
     * Point in time (Unix timestamp) when the video chat is supposed to be started by a chat administrator.
     */
    val startDate: ZonedDateTime,
)
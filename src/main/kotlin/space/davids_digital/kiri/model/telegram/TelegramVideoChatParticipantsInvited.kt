package space.davids_digital.kiri.model.telegram

data class TelegramVideoChatParticipantsInvited (
    /**
     * New members that were invited to the video chat.
     */
    val users: List<Long>,
)
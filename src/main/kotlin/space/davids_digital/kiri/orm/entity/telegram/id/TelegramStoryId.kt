package space.davids_digital.kiri.orm.entity.telegram.id

import java.io.Serializable

data class TelegramStoryId(
    var chatId: Long = 0,
    var storyId: Long = 0
) : Serializable
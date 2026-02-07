package space.davids_digital.kiri.bot.telegram.command

import space.davids_digital.kiri.model.telegram.TelegramMessage

class CommandExecutionContext(
    val commandName: String,
    val arguments: List<String>,
    val message: TelegramMessage,
)
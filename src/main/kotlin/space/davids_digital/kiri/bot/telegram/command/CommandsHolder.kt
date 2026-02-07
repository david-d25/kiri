package space.davids_digital.kiri.bot.telegram.command

import org.springframework.stereotype.Component

@Component
class CommandsHolder(val commands: List<Command>)
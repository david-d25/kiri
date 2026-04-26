package space.davids_digital.kiri.bot.telegram.command

import org.slf4j.LoggerFactory

/**
 * Abstract class representing a command in the Telegram bot.
 * To execute a command, Telegram user sends a message like `/command arg1 arg2 ...`.
 */
abstract class Command {
    protected val log = LoggerFactory.getLogger(javaClass)

    /**
     * The name of the command without the leading slash.
     */
    abstract val name: String

    /**
     * If true, the command can be executed by any Telegram user, not only admins/owner.
     * Required for `/terms`, `/support`, `/paysupport` per Telegram Stars payments policy.
     */
    open val isPublic: Boolean = false

    /**
     * Executes the command with the provided arguments.
     *
     * @param context The context for command execution, containing arguments, message details, and maybe other data.
     */
    abstract suspend fun execute(context: CommandExecutionContext)
}
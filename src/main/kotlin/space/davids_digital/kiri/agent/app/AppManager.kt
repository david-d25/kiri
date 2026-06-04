package space.davids_digital.kiri.agent.app

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.app.calendar.CalendarApp
import space.davids_digital.kiri.agent.app.files.FilesApp
import space.davids_digital.kiri.agent.app.image.ImageApp
import space.davids_digital.kiri.agent.app.scratchpad.ScratchpadApp
import space.davids_digital.kiri.agent.app.svg.SvgApp
import space.davids_digital.kiri.agent.app.telegram.TelegramApp
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolProvider

/**
 * This component manages apps and their lifecycle.
 */
@Component
@AgentToolNamespace("apps")
class AppManager(
    private val telegramAppProvider: ObjectProvider<TelegramApp>,
    private val scratchpadAppProvider: ObjectProvider<ScratchpadApp>,
    private val imageAppProvider: ObjectProvider<ImageApp>,
    private val svgAppProvider: ObjectProvider<SvgApp>,
    private val filesAppProvider: ObjectProvider<FilesApp>,
    private val calendarAppProvider: ObjectProvider<CalendarApp>,
) : AgentToolProvider {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val availableApps = mutableMapOf<String, () -> AgentApp>()
    private val openedApps = mutableSetOf<AgentApp>()

    @PostConstruct
    private fun init() {
        availableApps["telegram"] = { telegramAppProvider.getObject() }
        availableApps["notepad"] = { scratchpadAppProvider.getObject() }
        availableApps["image"] = { imageAppProvider.getObject() }
        availableApps["svg"] = { svgAppProvider.getObject() }
        availableApps["files"] = { filesAppProvider.getObject() }
        availableApps["calendar"] = { calendarAppProvider.getObject() }
    }

    override fun getAvailableAgentToolMethods() = listOf(::listApps, ::open, ::close)
    override fun getSubProviders() = openedApps

    @AgentToolMethod(name = "list")
    fun listApps(): String {
        return buildString {
            appendLine("Available apps:")
            availableApps.forEach { (id, _) ->
                appendLine("- $id")
            }
        }
    }

    @AgentToolMethod
    suspend fun open(id: String): String {
        val foundApp = openedApps.find { it.id == id }
        if (foundApp != null) {
            return "App '$id' is already opened"
        }
        val appFactory = availableApps[id] ?: return "App with ID '$id' not found"
        val app = appFactory()
        openedApps.add(app)
        app.onOpened()
        log.info("Opened app '$id'")
        return "Opened '$id'"
    }

    @AgentToolMethod
    suspend fun close(id: String): String {
        val app = openedApps.find { it.id == id } ?: return "App with ID '$id' not found"
        app.onClose()
        openedApps.remove(app)
        log.info("Closed app '$id'")
        return "Closed '$id'"
    }
}
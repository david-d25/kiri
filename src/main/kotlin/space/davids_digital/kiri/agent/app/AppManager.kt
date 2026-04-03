package space.davids_digital.kiri.agent.app

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.app.image.ImageApp
import space.davids_digital.kiri.agent.app.scratchpad.ScratchpadApp
import space.davids_digital.kiri.agent.app.svg.SvgApp
import space.davids_digital.kiri.agent.app.telegram.TelegramApp
import space.davids_digital.kiri.agent.frame.DataFrame
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
    }

    override fun getAvailableAgentToolMethods() = listOf(::listApps, ::open, ::close, ::render, ::restart)
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

    @AgentToolMethod(description = "Render current state of all opened apps")
    suspend fun render(): List<DataFrame.ContentPart> {
        if (openedApps.isEmpty()) {
            return listOf(DataFrame.Text("<info>Currently, no apps are opened.</info>"))
        }
        val contents = mutableListOf<DataFrame.ContentPart>()
        for (app in openedApps) {
            val appContent = app.render()
            contents.add(DataFrame.Text("""<app id="${app.id}">"""))
            contents.addAll(appContent)
            contents.add(DataFrame.Text("</app>"))
        }
        return contents
    }

    @AgentToolMethod(description = "Opens an app and makes its tools available")
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

    @AgentToolMethod(
        description = "Closes the app and removes its functions from agent context. " +
                "Close unused apps to free resources."
    )
    suspend fun close(id: String): String {
        val app = openedApps.find { it.id == id } ?: return "App with ID '$id' not found"
        app.onClose()
        openedApps.remove(app)
        log.info("Closed app '$id'")
        return "Closed '$id'"
    }

    @AgentToolMethod
    suspend fun restart(id: String): String {
        close(id)
        open(id)
        return "Restarted '$id'"
    }
}
package space.davids_digital.kiri.agent.app

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import space.davids_digital.kiri.Settings
import space.davids_digital.kiri.agent.frame.DynamicDataFrame
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolProvider
import space.davids_digital.kiri.integration.telegram.TelegramService

/**
 * This component manages apps and their lifecycle.
 */
@Component
@AgentToolNamespace("apps")
class AppManager(
    private val settings: Settings,
    private val frames: FrameBuffer,
    private val telegramService: TelegramService,
) : AgentToolProvider {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val availableApps = mutableMapOf<String, () -> AgentApp>()
    private val openedApps = mutableSetOf<AgentApp>()

    @PostConstruct
    private fun init() {
        availableApps["telegram"] = { TelegramApp(telegramService) }
    }

    @AgentToolMethod(name = "list", description = "List all available apps")
    fun listApps(): String {
        return buildString {
            appendLine("Available apps:")
            availableApps.forEach { (id, _) ->
                appendLine("- $id")
            }
        }
    }

    @AgentToolMethod(name = "open", description = "Open app by ID")
    fun open(id: String): String {
        openedApps.find { it.id == id }?.let {
            return "App with ID '$id' is already opened"
        }
        val appFactory = availableApps[id] ?: return "App with ID '$id' not found"
        val app = appFactory()
        openedApps.add(app)
        app.onOpened()
        frames.add(DynamicDataFrame(
            tagProvider = { "app" },
            attributesProvider = { mapOf() },
            contentProvider = app::render
        ))
        log.info("Opened app '$id'")
        return "Opened '$id'"
    }

    @AgentToolMethod(name = "close", description = "Close app by ID")
    fun close(id: String): String {
        val app = openedApps.find { it.id == id } ?: return "App with ID '$id' not found"
        app.onClose()
        openedApps.remove(app)
        log.info("Closed app '$id'")
        return "Closed '$id'"
    }

    override fun getAvailableAgentToolMethods() = listOf(::listApps, ::open, ::close)
    override fun getSubProviders() = openedApps
}
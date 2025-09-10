package space.davids_digital.kiri.agent.app

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.app.telegram.TelegramApp
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.DynamicDataFrame
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolProvider
import java.util.function.Supplier

/**
 * This component manages apps and their lifecycle.
 */
@Component
@AgentToolNamespace("apps")
class AppManager(
    private val frames: FrameBuffer,
    private val telegramAppProvider: Supplier<TelegramApp>
) : AgentToolProvider {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val availableApps = mutableMapOf<String, () -> AgentApp>()
    private val openedApps = mutableSetOf<AgentApp>()
    private val appFrames = mutableMapOf<String, DataFrame>()

    @PostConstruct
    private fun init() {
        availableApps["telegram"] = { telegramAppProvider.get() }
    }

    fun getOpenedApp(id: String): AgentApp? {
        return openedApps.find { it.id == id }
    }

    override fun getAvailableAgentToolMethods() = listOf(::listApps, ::open, ::close)
    override fun getSubProviders() = openedApps

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
    suspend fun open(id: String): String {
        val foundApp = openedApps.find { it.id == id }
        if (foundApp != null) {
            return "App '$id' is already opened"
        }
        val appFactory = availableApps[id] ?: return "App with ID '$id' not found"
        val app = appFactory()
        openedApps.add(app)
        app.onOpened()
        val frame = DynamicDataFrame(
            tagProvider = { "app" },
            attributesProvider = { mapOf("id" to app.id) },
            contentProvider = app::render
        )
        appFrames[app.id] = frame
        frames.addFixed(frame)
        log.info("Opened app '$id'")
        return "Opened '$id'"
    }

    @AgentToolMethod(name = "close", description = "Close app by ID")
    suspend fun close(id: String): String {
        val app = openedApps.find { it.id == id } ?: return "App with ID '$id' not found"
        val frame = appFrames.remove(id)
        frame?.let(frames::removeFixed)
        app.onClose()
        openedApps.remove(app)
        log.info("Closed app '$id'")
        return "Closed '$id'"
    }
}
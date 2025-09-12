package space.davids_digital.kiri.agent.app.telegram

import org.springframework.stereotype.Service
import space.davids_digital.kiri.agent.engine.EngineEventBus
import space.davids_digital.kiri.agent.frame.FrameBuffer
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.orm.service.telegram.TelegramChatOrmService
import space.davids_digital.kiri.orm.service.telegram.TelegramMessageOrmService
import space.davids_digital.kiri.service.TelegramNotificationService
import java.util.function.Supplier

@Service
class TelegramAppProvider(
    private val telegramService: TelegramService,
    private val telegramAppRenderer: TelegramAppRenderer,
    private val telegramNotificationService: TelegramNotificationService,
    private val telegramChatOrmService: TelegramChatOrmService,
    private val telegramMessageOrmService: TelegramMessageOrmService,
    private val engineEventBus: EngineEventBus,
    private val frameBuffer: FrameBuffer
) : Supplier<TelegramApp> {
    override fun get(): TelegramApp {
        return TelegramApp(
            telegramService,
            telegramAppRenderer,
            telegramNotificationService,
            telegramChatOrmService,
            telegramMessageOrmService,
            engineEventBus,
            frameBuffer
        )
    }
}
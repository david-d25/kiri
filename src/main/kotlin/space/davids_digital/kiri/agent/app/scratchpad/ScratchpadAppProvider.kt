package space.davids_digital.kiri.agent.app.scratchpad

import org.springframework.stereotype.Service
import java.util.function.Supplier

@Service
class ScratchpadAppProvider : Supplier<ScratchpadApp> {
    override fun get(): ScratchpadApp {
        return ScratchpadApp()
    }
}
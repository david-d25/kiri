package space.davids_digital.kiri.agent.engine.event

import kotlinx.coroutines.CompletableDeferred

class SleepEvent(val seconds: Long, private val wake: CompletableDeferred<Unit>) : EngineEvent() {
    fun preventSleeping(): Boolean = wake.complete(Unit)
}

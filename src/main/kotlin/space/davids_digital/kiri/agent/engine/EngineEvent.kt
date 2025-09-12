package space.davids_digital.kiri.agent.engine

import kotlinx.coroutines.CompletableDeferred

open class EngineEvent

class TickEvent : EngineEvent()

class SleepEvent(val seconds: Long, private val wake: CompletableDeferred<Unit>) : EngineEvent() {
    fun preventSleeping(): Boolean = wake.complete(Unit)
}

class WakeUpRequestEvent : EngineEvent()
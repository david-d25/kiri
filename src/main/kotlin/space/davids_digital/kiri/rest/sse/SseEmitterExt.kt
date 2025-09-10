package space.davids_digital.kiri.rest.sse

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

fun <T : Any> SseEmitter.sendEvent(name: SseEventKey<T>, payload: T) {
    val builder = SseEmitter.event().name(name.value)
    if (payload is Unit) {
        builder.comment("keep-alive")
    } else {
        builder.data(payload)
    }
    this.send(builder)
}
package space.davids_digital.kiri.service

import org.springframework.stereotype.Service

@Service
class ChatCompletionServiceRegistry (services: List<ChatCompletionService>) {
    private val serviceMap: Map<String, ChatCompletionService> = services.associateBy { it.serviceHandle }

    fun findByModelHandle(modelHandle: String): ChatCompletionService? {
        val (serviceHandle, _) = modelHandle.split("/", limit = 2)
        return serviceMap[serviceHandle]
    }
}
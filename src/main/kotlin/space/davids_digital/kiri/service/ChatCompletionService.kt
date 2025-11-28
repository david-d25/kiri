package space.davids_digital.kiri.service

import space.davids_digital.kiri.llm.ChatCompletionRequest
import space.davids_digital.kiri.llm.ChatCompletionResponse
import space.davids_digital.kiri.model.ChatCompletionModel

interface ChatCompletionService : ExternalServiceGateway {
    suspend fun request(request: ChatCompletionRequest): ChatCompletionResponse
    suspend fun getModels(): List<ChatCompletionModel>
}
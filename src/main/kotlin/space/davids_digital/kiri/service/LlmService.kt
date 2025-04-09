package space.davids_digital.kiri.service

import space.davids_digital.kiri.llm.LlmMessageRequest
import space.davids_digital.kiri.llm.LlmMessageResponse

interface LlmService {
    suspend fun request(request: LlmMessageRequest): LlmMessageResponse
}
package space.davids_digital.kiri.service

import space.davids_digital.kiri.model.ExternalServiceGatewayStatus

interface ExternalServiceGateway {
    val serviceHandle: String
    suspend fun getStatus(): ExternalServiceGatewayStatus
}
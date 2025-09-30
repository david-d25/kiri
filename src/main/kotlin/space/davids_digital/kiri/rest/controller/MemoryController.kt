package space.davids_digital.kiri.rest.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.service.MemoryService

@RestController
@RequestMapping("/memory")
class MemoryController (
    private val service: MemoryService
) {
    @GetMapping
    fun query() {
        // TODO
    }
}
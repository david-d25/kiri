package space.davids_digital.kiri.rest.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ping")
class PingController {
    @RequestMapping
    fun ping(): String {
        return "pong"
    }
}
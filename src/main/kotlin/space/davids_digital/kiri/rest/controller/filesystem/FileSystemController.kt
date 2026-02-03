package space.davids_digital.kiri.rest.controller.filesystem

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import space.davids_digital.kiri.service.FileSystemService

@RestController
@RequestMapping("/filesystem")
class FileSystemController(
    private val service: FileSystemService,
) {
    // TODO
}

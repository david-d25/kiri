package space.davids_digital.kiri

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration

fun main(args: Array<String>) {
    SpringApplication.run(SpringConfig::class.java, *args)
}

@SpringBootApplication(exclude = [UserDetailsServiceAutoConfiguration::class])
class Main
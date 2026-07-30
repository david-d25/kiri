package space.davids_digital.kiri

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Duration

@Configuration
@EnableCaching
class CacheConfig {
    @Bean
    @Primary
    fun cacheManager(): CacheManager = caffeineCacheManager(Duration.ofSeconds(60))

    @Bean
    fun oneHour(): CacheManager = caffeineCacheManager(Duration.ofHours(1))

    private fun caffeineCacheManager(expireAfterWrite: Duration): CacheManager {
        val cacheManager = CaffeineCacheManager()
        cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(expireAfterWrite))
        cacheManager.setAsyncCacheMode(true)
        return cacheManager
    }
}

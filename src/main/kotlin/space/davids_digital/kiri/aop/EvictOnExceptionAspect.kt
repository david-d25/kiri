package space.davids_digital.kiri.aop

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component

@Aspect
@Component
class EvictOnExceptionAspect(private val cacheManagers: List<CacheManager>) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @AfterThrowing(pointcut = "@annotation(evict)", throwing = "ex")
    fun afterThrowing(joinPoint: JoinPoint, evict: EvictCacheOnException, ex: Throwable) {
        try {
            for (name in evict.cacheNames) {
                // Filtering by cacheNames avoids CaffeineCacheManager creating the cache just to clear it
                cacheManagers
                    .filter { name in it.cacheNames }
                    .forEach { it.getCache(name)?.clear() }
            }
        } catch (e: Exception) {
            log.warn("Failed to evict caches on exception", e)
        }
    }
}

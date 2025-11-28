package space.davids_digital.kiri.aop

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class EvictCacheOnException(val cacheNames: Array<String>)
package space.davids_digital.kiri.llm.tool.function.parameter

// TODO taken from Sweetie AI project, reference
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Enum(vararg val value: String)

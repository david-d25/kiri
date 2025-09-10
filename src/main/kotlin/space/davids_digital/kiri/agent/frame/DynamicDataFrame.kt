package space.davids_digital.kiri.agent.frame

class DynamicDataFrame(
    val tagProvider: () -> String,
    val attributesProvider: () -> Map<String, String>,
    val contentProvider: suspend () -> List<ContentPart>
) : DataFrame() {
    override val tag: String
        get() = tagProvider()

    override val attributes: Map<String, String>
        get() = attributesProvider()

    override suspend fun renderContent(): List<ContentPart> = contentProvider()
}
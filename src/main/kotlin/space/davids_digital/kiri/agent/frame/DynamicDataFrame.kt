package space.davids_digital.kiri.agent.frame

class DynamicDataFrame(
    val tagProvider: () -> String,
    val attributesProvider: () -> Map<String, String>,
    val contentProvider: () -> List<ContentPart>
) : DataFrame() {
    override val tag: String
        get() = tagProvider()

    override val attributes: Map<String, String>
        get() = attributesProvider()

    override fun renderContent(): List<ContentPart> = contentProvider()
}
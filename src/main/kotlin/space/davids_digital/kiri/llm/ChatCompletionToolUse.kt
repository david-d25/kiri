package space.davids_digital.kiri.llm

data class ChatCompletionToolUse(val id: String, val name: String, val input: Input) {
    sealed class Input {
        data class Text(val text: String) : Input()
        data class Number(val number: Double) : Input()
        data class Boolean(val boolean: kotlin.Boolean) : Input()
        data class Array(val items: List<Input>) : Input()
        data class Object(val items: Map<String, Input>) : Input()
    }
}
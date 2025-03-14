package space.davids_digital.kiri.llm.dsl

import space.davids_digital.kiri.llm.LlmToolUse

@DslMarker
annotation class LlmToolUseDsl

fun llmToolUse(block: LlmToolUseBuilder.() -> Unit) = LlmToolUseBuilder().apply(block).build()

@LlmToolUseDsl
class LlmToolUseBuilder {
    var id: String = ""
    var name: String = ""
    var input: LlmToolUse.Input = LlmToolUse.Input.Object(mapOf())

    fun input(block: LlmToolUseInputBuilder.() -> Unit) {
        input = LlmToolUseInputBuilder().apply(block).build()
    }

    fun build(): LlmToolUse {
        require(id.isNotBlank()) { "id must be set" }
        require(name.isNotBlank()) { "name must be set" }
        return LlmToolUse(id, name, input)
    }
}

@LlmToolUseDsl
class LlmToolUseInputBuilder {
    var text: String = ""
    var number: Double = 0.0
    var boolean: Boolean = false
    var items: MutableList<LlmToolUse.Input> = mutableListOf()
    var map: MutableMap<String, LlmToolUse.Input> = mutableMapOf()

    fun text(text: String) {
        this.text = text
    }

    fun number(number: Double) {
        this.number = number
    }

    fun boolean(boolean: Boolean) {
        this.boolean = boolean
    }

    fun array(block: LlmToolUseInputArrayBuilder.() -> Unit) {
        items.add(LlmToolUseInputArrayBuilder().apply(block).build())
    }

    fun objectValue(block: LlmToolUseInputObjectBuilder.() -> Unit) {
        map.putAll(LlmToolUseInputObjectBuilder().apply(block).build())
    }

    fun build(): LlmToolUse.Input {
        return when {
            text.isNotBlank() -> LlmToolUse.Input.Text(text)
            number != 0.0 -> LlmToolUse.Input.Number(number)
            boolean -> LlmToolUse.Input.Boolean(boolean)
            items.isNotEmpty() -> LlmToolUse.Input.Array(items)
            map.isNotEmpty() -> LlmToolUse.Input.Object(map)
            else -> throw IllegalStateException("No input value set")
        }
    }
}

@LlmToolUseDsl
class LlmToolUseInputArrayBuilder {
    var items: MutableList<LlmToolUse.Input> = mutableListOf()

    fun text(text: String) {
        items.add(LlmToolUse.Input.Text(text))
    }

    fun number(number: Double) {
        items.add(LlmToolUse.Input.Number(number))
    }

    fun boolean(boolean: Boolean) {
        items.add(LlmToolUse.Input.Boolean(boolean))
    }

    fun array(block: LlmToolUseInputArrayBuilder.() -> Unit) {
        items.add(LlmToolUse.Input.Array(LlmToolUseInputArrayBuilder().apply(block).items))
    }

    fun objectValue(block: LlmToolUseInputObjectBuilder.() -> Unit) {
        items.add(LlmToolUse.Input.Object(LlmToolUseInputObjectBuilder().apply(block).build()))
    }

    fun build(): LlmToolUse.Input {
        return LlmToolUse.Input.Array(items)
    }
}

@LlmToolUseDsl
class LlmToolUseInputObjectBuilder {
    var items: MutableMap<String, LlmToolUse.Input> = mutableMapOf()

    fun text(name: String, text: String) {
        items[name] = LlmToolUse.Input.Text(text)
    }

    fun number(name: String, number: Double) {
        items[name] = LlmToolUse.Input.Number(number)
    }

    fun boolean(name: String, boolean: Boolean) {
        items[name] = LlmToolUse.Input.Boolean(boolean)
    }

    fun array(name: String, block: LlmToolUseInputArrayBuilder.() -> Unit) {
        items[name] = LlmToolUse.Input.Array(LlmToolUseInputArrayBuilder().apply(block).items)
    }

    fun objectValue(name: String, block: LlmToolUseInputObjectBuilder.() -> Unit) {
        items[name] = LlmToolUse.Input.Object(LlmToolUseInputObjectBuilder().apply(block).build())
    }

    fun build(): Map<String, LlmToolUse.Input> {
        return items
    }
}
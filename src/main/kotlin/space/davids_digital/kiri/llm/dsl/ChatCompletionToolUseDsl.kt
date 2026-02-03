package space.davids_digital.kiri.llm.dsl

import space.davids_digital.kiri.llm.ChatCompletionToolUse

@DslMarker
annotation class ChatCompletionToolUseDsl

fun chatCompletionToolUse(block: ChatCompletionToolUseBuilder.() -> Unit) = ChatCompletionToolUseBuilder().apply(block).build()

@ChatCompletionToolUseDsl
class ChatCompletionToolUseBuilder {
    var id: String = ""
    var name: String = ""
    var input: ChatCompletionToolUse.Input = ChatCompletionToolUse.Input.Object(mapOf())

    fun input(block: ChatCompletionToolUseInputBuilder.() -> Unit) {
        input = ChatCompletionToolUseInputBuilder().apply(block).build()
    }

    fun build(): ChatCompletionToolUse {
        require(id.isNotBlank()) { "id must be set" }
        require(name.isNotBlank()) { "name must be set" }
        return ChatCompletionToolUse(id, name, input)
    }
}

@ChatCompletionToolUseDsl
interface GenericJsonInputBuilder {
    fun text(text: String)
    fun number(number: Double)
    fun boolean(boolean: Boolean)
    fun array(block: ChatCompletionToolUseInputArrayBuilder.() -> Unit)
    fun objectValue(block: ChatCompletionToolUseInputObjectBuilder.() -> Unit)
    fun build(): ChatCompletionToolUse.Input
}

@ChatCompletionToolUseDsl
class ChatCompletionToolUseInputBuilder : GenericJsonInputBuilder {
    var text: String? = null
    var number: Double? = null
    var boolean: Boolean? = null
    var items: MutableList<ChatCompletionToolUse.Input>? = null
    var map: MutableMap<String, ChatCompletionToolUse.Input>? = null

    override fun text(text: String) {
        this.text = text
    }

    override fun number(number: Double) {
        this.number = number
    }

    override fun boolean(boolean: Boolean) {
        this.boolean = boolean
    }

    override fun array(block: ChatCompletionToolUseInputArrayBuilder.() -> Unit) {
        val arr = ChatCompletionToolUseInputArrayBuilder().apply(block).items
        items = (items ?: mutableListOf()).apply { addAll(arr) }
    }

    override fun objectValue(block: ChatCompletionToolUseInputObjectBuilder.() -> Unit) {
        if (map == null) {
            map = mutableMapOf()
        }
        map!!.putAll(ChatCompletionToolUseInputObjectBuilder().apply(block).build())
    }

    override fun build(): ChatCompletionToolUse.Input {
        return when {
            text != null -> ChatCompletionToolUse.Input.Text(text!!)
            number != null -> ChatCompletionToolUse.Input.Number(number!!)
            boolean != null -> ChatCompletionToolUse.Input.Boolean(boolean!!)
            items != null -> ChatCompletionToolUse.Input.Array(items!!)
            map != null -> ChatCompletionToolUse.Input.Object(map!!)
            else -> throw IllegalStateException("No input value set")
        }
    }
}

@ChatCompletionToolUseDsl
class ChatCompletionToolUseInputArrayBuilder : GenericJsonInputBuilder {
    var items: MutableList<ChatCompletionToolUse.Input> = mutableListOf()

    override fun text(text: String) {
        items.add(ChatCompletionToolUse.Input.Text(text))
    }

    override fun number(number: Double) {
        items.add(ChatCompletionToolUse.Input.Number(number))
    }

    override fun boolean(boolean: Boolean) {
        items.add(ChatCompletionToolUse.Input.Boolean(boolean))
    }

    override fun array(block: ChatCompletionToolUseInputArrayBuilder.() -> Unit) {
        items.add(ChatCompletionToolUse.Input.Array(ChatCompletionToolUseInputArrayBuilder().apply(block).items))
    }

    override fun objectValue(block: ChatCompletionToolUseInputObjectBuilder.() -> Unit) {
        items.add(ChatCompletionToolUse.Input.Object(ChatCompletionToolUseInputObjectBuilder().apply(block).build()))
    }

    override fun build(): ChatCompletionToolUse.Input {
        return ChatCompletionToolUse.Input.Array(items)
    }
}

@ChatCompletionToolUseDsl
class ChatCompletionToolUseInputObjectBuilder {
    var items: MutableMap<String, ChatCompletionToolUse.Input> = mutableMapOf()

    fun text(name: String, text: String) {
        items[name] = ChatCompletionToolUse.Input.Text(text)
    }

    fun number(name: String, number: Double) {
        items[name] = ChatCompletionToolUse.Input.Number(number)
    }

    fun boolean(name: String, boolean: Boolean) {
        items[name] = ChatCompletionToolUse.Input.Boolean(boolean)
    }

    fun array(name: String, block: ChatCompletionToolUseInputArrayBuilder.() -> Unit) {
        items[name] = ChatCompletionToolUse.Input.Array(ChatCompletionToolUseInputArrayBuilder().apply(block).items)
    }

    fun objectValue(name: String, block: ChatCompletionToolUseInputObjectBuilder.() -> Unit) {
        items[name] = ChatCompletionToolUse.Input.Object(ChatCompletionToolUseInputObjectBuilder().apply(block).build())
    }

    fun build(): Map<String, ChatCompletionToolUse.Input> {
        return items
    }
}
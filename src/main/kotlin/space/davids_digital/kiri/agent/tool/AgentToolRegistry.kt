package space.davids_digital.kiri.agent.tool

import org.springframework.stereotype.Component

/**
 * Resolves tool calls to their respective methods. Thread-safe.
 *
 * How methods are resolved:
 * - Each method is registered with a path and a name.
 * - The path is a list of namespaces.
 * - Namespaces and the name are separated by an underscore.
 *
 * For example, the method `foo_bar_baz` would be registered with the path `["foo", "bar"]` and the name `baz`.
 */
@Component
class AgentToolRegistry {
    private val lock = Any()
    private val root = Node()

    /**
     * Registers a function in the registry.
     * If the function is already registered, it will be overwritten.
     */
    fun put(block: EntryBuilder.() -> Unit): Unit = synchronized(lock) {
        put(EntryBuilder().apply(block).build())
    }

    /**
     * Registers a function in the registry.
     * If the function is already registered, it will be overwritten.
     */
    fun put(entry: Entry): Unit = synchronized(lock) {
        var node = root
        for (segment in entry.path) {
            node = node.children.getOrPut(segment) { Node() }
        }
        node.functions[entry.name] = entry
    }

    fun find(path: List<String>, name: String): Entry? = synchronized(lock) {
        var node = root
        for (segment in path) {
            node = node.children[segment] ?: return null
        }
        return node.functions[name]
    }

    /**
     * Finds a function by its full name.
     * The full name is an underscore-separated string of the path and the name.
     * For example, `foo_bar_baz` would be resolved with the path `["foo", "bar"]` and the name `baz`.
     */
    fun find(fullName: String): Entry? {
        val parts = fullName.split('_')
        return find(parts.dropLast(1), parts.last())
    }

    fun has(path: List<String>, name: String): Boolean {
        return find(path, name) != null
    }

    fun clear(): Unit = synchronized(lock) {
        root.functions.clear()
        root.children.clear()
    }

    /**
     * Returns a snapshot of all registered functions.
     * The order of the functions is not guaranteed.
     */
    fun iterate(): Sequence<Entry> {
        val entries = synchronized(lock) { collectAll(root) }
        return entries.asSequence()
    }

    private fun collectAll(node: Node): List<Entry> {
        val result = mutableListOf<Entry>()
        for ((_, entry) in node.functions) {
            result.add(entry)
        }
        for ((_, child) in node.children) {
            result.addAll(collectAll(child))
        }
        return result
    }

    class EntryBuilder {
        var path: List<String> = emptyList()
        var name: String = ""
        var description: String? = null
        var createFrame: Boolean = true
        lateinit var callable: Function<*>
        lateinit var receiver: AgentToolProvider

        fun build(): Entry {
            return Entry(path, name, description, createFrame, callable, receiver)
        }
    }

    data class Entry (
        val path: List<String>,
        val name: String,
        val description: String?,
        val createFrame: Boolean,
        val callable: Function<*>,
        val receiver: AgentToolProvider,
    ) {
        val fullName = (path + name).joinToString("_")
    }

    private class Node {
        val functions = mutableMapOf<String, Entry>()
        val children = mutableMapOf<String, Node>()
    }
}

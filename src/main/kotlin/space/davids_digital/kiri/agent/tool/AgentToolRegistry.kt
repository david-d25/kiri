package space.davids_digital.kiri.agent.tool

import org.springframework.stereotype.Component

/**
 * Resolves tool calls to their respective methods.
 *
 * How methods are resolved:
 * - Each method is registered with a path and a name.
 * - The path is a list of namespaces.
 * - Namespaces and the name are separated by an underscore.
 *
 * Foe example, the method `foo_bar_baz` would be registered with the path `["foo", "bar"]` and the name `baz`.
 */
@Component
class AgentToolRegistry {
    private val root = Node()

    /**
     * Registers a function in the registry.
     * If the function is already registered, it will be overwritten.
     */
    fun put(block: EntryBuilder.() -> Unit) {
        put(EntryBuilder().apply(block).build())
    }

    /**
     * Registers a function in the registry.
     * If the function is already registered, it will be overwritten.
     */
    fun put(entry: Entry) {
        var node = root
        for (segment in entry.path) {
            node = node.children.getOrPut(segment) { Node() }
        }
        node.functions[entry.name] = entry
    }

    fun find(path: List<String>, name: String): Entry? {
        var node = root
        for (segment in path) {
            node = node.children[segment] ?: return null
        }
        return node.functions[name]
    }

    /**
     * Finds a function by its full name.
     * The full name is a dot-separated string of the path and the name.
     * For example, `foo.bar.baz` would be resolved with the path `["foo", "bar"]` and the name `baz`.
     */
    fun find(fullName: String): Entry? {
        val parts = fullName.split('_')
        return find(parts.dropLast(1), parts.last())
    }

    fun has(path: List<String>, name: String): Boolean {
        return find(path, name) != null
    }

    fun clear() {
        root.functions.clear()
        root.children.clear()
    }

    /**
     * Iterates over all registered functions.
     * The order of the functions is not guaranteed.
     * If registry is edited during iteration, the behavior is undefined.
     */
    fun iterate(): Sequence<Entry> {
        return iterate(root, emptyList())
    }

    private fun iterate(node: Node, path: List<String>): Sequence<Entry> {
        return sequence {
            for ((_, entry) in node.functions) {
                yield(entry)
            }
            for ((segment, child) in node.children) {
                yieldAll(iterate(child, path + segment))
            }
        }
    }

    class EntryBuilder {
        var path: List<String> = emptyList()
        var name: String = ""
        var description: String? = null
        lateinit var callable: Function<*>
        lateinit var receiver: AgentToolProvider

        fun build(): Entry {
            requireNotNull(callable) { "'callable' is required" }
            requireNotNull(receiver) { "'receiver' is required "}
            return Entry(path, name, description, callable, receiver)
        }
    }

    data class Entry (
        val path: List<String>,
        val name: String,
        val description: String?,
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
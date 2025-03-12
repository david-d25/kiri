package space.davids_digital.kiri.agent.tool

/**
 * Resolves tool calls to their respective methods.
 */
class AgentToolRegistry {
    private val root = Node()

    fun has(path: List<String>, name: String): Boolean {
        return resolve(path, name) != null
    }

    fun register(path: List<String>, name: String, function: Function<*>) {
        var node = root
        for (segment in path) {
            node = node.children.computeIfAbsent(segment) { Node() }
        }
        node.functions[name] = function
    }

    fun resolve(path: List<String>, name: String): Function<*>? {
        var node = root
        for (segment in path) {
            node = node.children[segment] ?: return null
        }
        return node.functions[name]
    }

    private class Node {
        val children = mutableMapOf<String, Node>()
        val functions = mutableMapOf<String, Function<*>>()
    }
}
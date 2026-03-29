package space.davids_digital.kiri.agent.frame

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.agent.tool.AgentToolParameter
import space.davids_digital.kiri.agent.tool.AgentToolProvider
import space.davids_digital.kiri.llm.ChatCompletionToolUse
import space.davids_digital.kiri.llm.ChatCompletionToolUseResult
import kotlin.reflect.KFunction

class SyntheticToolCallTrackerTest {

    private lateinit var buffer: FrameBuffer

    @BeforeEach
    fun setUp() {
        buffer = FrameBuffer()
    }

    // --- Test providers ---

    @AgentToolNamespace("testns")
    class NamespacedProvider : AgentToolProvider {
        override fun getAvailableAgentToolMethods(): Collection<KFunction<*>> = listOf(
            ::greet, ::add
        )

        @AgentToolMethod(description = "Greets user")
        fun greet(
            @AgentToolParameter(description = "User name") name: String
        ): String = "Hello, $name"

        @AgentToolMethod(description = "Adds two numbers")
        fun add(a: Int, b: Int): Int = a + b
    }

    class NoNamespaceProvider : AgentToolProvider {
        override fun getAvailableAgentToolMethods(): Collection<KFunction<*>> = listOf(::echo)

        @AgentToolMethod
        fun echo(text: String): String = text
    }

    @AgentToolNamespace("custom")
    class CustomNameProvider : AgentToolProvider {
        override fun getAvailableAgentToolMethods(): Collection<KFunction<*>> = listOf(::doWork)

        @AgentToolMethod(name = "myCustomTool")
        fun doWork(): String = "done"
    }

    @AgentToolNamespace("suspend")
    class SuspendProvider : AgentToolProvider {
        override fun getAvailableAgentToolMethods(): Collection<KFunction<*>> = listOf(::fetch)

        @AgentToolMethod
        suspend fun fetch(
            @AgentToolParameter(name = "count") n: Int
        ): String = "fetched $n"
    }

    @AgentToolNamespace("multi")
    class MultiReturnProvider : AgentToolProvider {
        override fun getAvailableAgentToolMethods(): Collection<KFunction<*>> = listOf(
            ::returnUnit, ::returnContentParts, ::returnObject
        )

        @AgentToolMethod
        fun returnUnit() { }

        @AgentToolMethod
        fun returnContentParts(): List<DataFrame.ContentPart> = listOf(
            DataFrame.Text("line1"),
            DataFrame.Text("line2")
        )

        @AgentToolMethod
        fun returnObject(): Any = 42
    }

    // --- Tests ---

    @Test
    fun `tool name includes namespace and method name`() = runBlocking {
        val provider = NamespacedProvider()
        buffer.trackToolCall(provider::greet, "Alice")

        val frame = buffer.snapshot().frames.single() as ToolCallFrame
        assertEquals("testns_greet", frame.toolUse.name)
    }

    @Test
    fun `tool name without namespace`() = runBlocking {
        val provider = NoNamespaceProvider()
        buffer.trackToolCall(provider::echo, "hello")

        val frame = buffer.snapshot().frames.single() as ToolCallFrame
        assertEquals("echo", frame.toolUse.name)
    }

    @Test
    fun `custom tool name from annotation`() = runBlocking {
        val provider = CustomNameProvider()
        buffer.trackToolCall(provider::doWork)

        val frame = buffer.snapshot().frames.single() as ToolCallFrame
        assertEquals("custom_myCustomTool", frame.toolUse.name)
    }

    @Test
    fun `arguments are serialized with correct names`() = runBlocking {
        val provider = NamespacedProvider()
        buffer.trackToolCall(provider::greet, "Bob")

        val frame = buffer.snapshot().frames.single() as ToolCallFrame
        val input = frame.toolUse.input as ChatCompletionToolUse.Input.Object
        assertEquals(ChatCompletionToolUse.Input.Text("Bob"), input.items["name"])
    }

    @Test
    fun `multiple arguments serialized by position`() = runBlocking {
        val provider = NamespacedProvider()
        buffer.trackToolCall(provider::add, 3, 7)

        val frame = buffer.snapshot().frames.single() as ToolCallFrame
        val input = frame.toolUse.input as ChatCompletionToolUse.Input.Object
        assertEquals(ChatCompletionToolUse.Input.Number(3.0), input.items["a"])
        assertEquals(ChatCompletionToolUse.Input.Number(7.0), input.items["b"])
    }

    @Test
    fun `annotated parameter name is used`() = runBlocking {
        val provider = SuspendProvider()
        buffer.trackToolCall(provider::fetch, 5)

        val frame = buffer.snapshot().frames.single() as ToolCallFrame
        val input = frame.toolUse.input as ChatCompletionToolUse.Input.Object
        assertNotNull(input.items["count"])
        assertNull(input.items["n"])
    }

    @Test
    fun `suspend function is called and result recorded`() = runBlocking {
        val provider = SuspendProvider()
        val result = buffer.trackToolCall(provider::fetch, 5)

        assertEquals("fetched 5", result)
        val frame = buffer.snapshot().frames.single() as ToolCallFrame
        val output = frame.resultProvider().output.single() as ChatCompletionToolUseResult.Output.Text
        assertEquals("fetched 5", output.text)
    }

    @Test
    fun `function return value is returned to caller`() = runBlocking {
        val provider = NamespacedProvider()
        val result = buffer.trackToolCall(provider::greet, "World")
        assertEquals("Hello, World", result)
    }

    @Test
    fun `Unit result formatted as ok`() = runBlocking {
        val provider = MultiReturnProvider()
        buffer.trackToolCall(provider::returnUnit)

        val frame = buffer.snapshot().frames.single() as ToolCallFrame
        val output = frame.resultProvider().output.single() as ChatCompletionToolUseResult.Output.Text
        assertEquals("ok", output.text)
    }

    @Test
    fun `ContentPart list result formatted correctly`() = runBlocking {
        val provider = MultiReturnProvider()
        buffer.trackToolCall(provider::returnContentParts)

        val frame = buffer.snapshot().frames.single() as ToolCallFrame
        val outputs = frame.resultProvider().output
        assertEquals(2, outputs.size)
        assertEquals("line1", (outputs[0] as ChatCompletionToolUseResult.Output.Text).text)
        assertEquals("line2", (outputs[1] as ChatCompletionToolUseResult.Output.Text).text)
    }

    @Test
    fun `non-string result formatted via toString`() = runBlocking {
        val provider = MultiReturnProvider()
        buffer.trackToolCall(provider::returnObject)

        val frame = buffer.snapshot().frames.single() as ToolCallFrame
        val output = frame.resultProvider().output.single() as ChatCompletionToolUseResult.Output.Text
        assertEquals("42", output.text)
    }

    @Test
    fun `tool call id starts with synthetic`() = runBlocking {
        val provider = NoNamespaceProvider()
        buffer.trackToolCall(provider::echo, "test")

        val frame = buffer.snapshot().frames.single() as ToolCallFrame
        assertTrue(frame.toolUse.id.startsWith("synthetic_"))
    }

    @Test
    fun `multiple tracked calls produce multiple frames`() = runBlocking {
        val provider = NamespacedProvider()
        buffer.trackToolCall(provider::greet, "A")
        buffer.trackToolCall(provider::greet, "B")
        buffer.trackToolCall(provider::add, 1, 2)

        val frames = buffer.snapshot().frames.filterIsInstance<ToolCallFrame>()
        assertEquals(3, frames.size)
        assertEquals("testns_greet", frames[0].toolUse.name)
        assertEquals("testns_greet", frames[1].toolUse.name)
        assertEquals("testns_add", frames[2].toolUse.name)
    }

    @Test
    fun `no-arg function produces empty input`() = runBlocking {
        val provider = CustomNameProvider()
        buffer.trackToolCall(provider::doWork)

        val frame = buffer.snapshot().frames.single() as ToolCallFrame
        val input = frame.toolUse.input as ChatCompletionToolUse.Input.Object
        assertTrue(input.items.isEmpty())
    }

    @Test
    fun `boolean argument serialized correctly`() = runBlocking {
        val provider = object : AgentToolProvider {
            override fun getAvailableAgentToolMethods(): Collection<KFunction<*>> = listOf(::toggle)
            @AgentToolMethod fun toggle(enabled: Boolean): String = "enabled=$enabled"
        }
        buffer.trackToolCall(provider::toggle, true)

        val frame = buffer.snapshot().frames.single() as ToolCallFrame
        val input = frame.toolUse.input as ChatCompletionToolUse.Input.Object
        assertEquals(ChatCompletionToolUse.Input.Boolean(true), input.items["enabled"])
    }
}

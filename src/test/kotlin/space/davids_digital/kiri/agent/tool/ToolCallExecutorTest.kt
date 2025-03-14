package space.davids_digital.kiri.agent.tool

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import space.davids_digital.kiri.llm.LlmToolUse

/**
 * I wrote these tests with AI because I'm lazy.
 * Feel free to rewrite them in a more human way.
 */
class ToolCallExecutorTest {

    private val executor = ToolCallExecutor()

    fun noParamFunction(): String = "no params"

    fun echo(input: String): String = input

    fun add(a: Int, b: Int): Int = a + b

    fun greet(@ToolParameter(name = "username", description = "User's name") name: String): String =
        "Hello, $name"

    fun sumList(numbers: List<Int>): Int = numbers.sum()

    data class Person(val name: String, val age: Int)
    fun describePerson(person: Person): String = "${person.name} is ${person.age} years old"

    data class Address(val city: String, val zip: String?)
    data class User(val name: String, val address: Address)
    fun userInfo(user: User): String = "${user.name} lives in ${user.address.city} with zip ${user.address.zip}"

    enum class Color { RED, GREEN, BLUE }
    fun favoriteColor(color: Color): String = "Favorite is ${color.name}"

    suspend fun echoSuspend(input: String): String = input

    @Test
    fun `test no parameters function`() = runBlocking {
        val result = executor.execute(::noParamFunction, LlmToolUse.Input.Text("unused"), receiver = this)
        assertEquals("no params", result)
    }

    @Test
    fun `test single parameter function`() = runBlocking {
        val result = executor.execute(::echo, LlmToolUse.Input.Text("hello"), receiver = this)
        assertEquals("hello", result)
    }

    @Test
    fun `test multiple parameters function`() = runBlocking {
        val input = LlmToolUse.Input.Object(
            mapOf(
                "a" to LlmToolUse.Input.Number(1.0),
                "b" to LlmToolUse.Input.Number(2.0)
            )
        )
        val result = executor.execute(::add, input, receiver = this)
        assertEquals(3, result)
    }

    @Test
    fun `test annotated parameter function`() = runBlocking {
        val input = LlmToolUse.Input.Object(
            mapOf("username" to LlmToolUse.Input.Text("Alice"))
        )
        val result = executor.execute(::greet, input, receiver = this)
        assertEquals("Hello, Alice", result)
    }

    @Test
    fun `test array conversion`() = runBlocking {
        val input = LlmToolUse.Input.Array(
            listOf(
                LlmToolUse.Input.Number(1.0),
                LlmToolUse.Input.Number(2.0),
                LlmToolUse.Input.Number(3.0)
            )
        )
        val result = executor.execute(::sumList, input, receiver = this)
        assertEquals(6, result)
    }

    @Test
    fun `test data class conversion`() = runBlocking {
        val input = LlmToolUse.Input.Object(
            mapOf(
                "name" to LlmToolUse.Input.Text("Bob"),
                "age" to LlmToolUse.Input.Number(30.0)
            )
        )
        val result = executor.execute(::describePerson, input, receiver = this)
        assertEquals("Bob is 30 years old", result)
    }

    @Test
    fun `test nested object conversion`() = runBlocking {
        val input = LlmToolUse.Input.Object(
            mapOf(
                "name" to LlmToolUse.Input.Text("Carol"),
                "address" to LlmToolUse.Input.Object(
                    mapOf(
                        "city" to LlmToolUse.Input.Text("NYC"),
                        "zip" to LlmToolUse.Input.Text("10001")
                    )
                )
            )
        )
        val result = executor.execute(::userInfo, input, receiver = this)
        assertEquals("Carol lives in NYC with zip 10001", result)
    }

    @Test
    fun `test enum conversion`() = runBlocking {
        val result = executor.execute(::favoriteColor, LlmToolUse.Input.Text("GREEN"), receiver = this)
        assertEquals("Favorite is GREEN", result)
    }

    @Test
    fun `test suspend function`() = runBlocking {
        val result = executor.execute(::echoSuspend, LlmToolUse.Input.Text("suspend hello"), receiver = this)
        assertEquals("suspend hello", result)
    }

    @Test
    fun `test missing required parameter throws exception`() = runBlocking {
        val input = LlmToolUse.Input.Object(
            mapOf("a" to LlmToolUse.Input.Number(1.0))
        )
        val exception = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { executor.execute(::add, input, receiver = this) }
        }
        assertTrue(exception.message!!.contains("Missing required parameter"))
    }
}

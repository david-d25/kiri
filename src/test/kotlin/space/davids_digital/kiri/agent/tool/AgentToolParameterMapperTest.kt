package space.davids_digital.kiri.agent.tool

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import space.davids_digital.kiri.llm.LlmMessageRequest.Tools.Function.ParameterValue

/**
 * I wrote these tests with AI because I'm lazy.
 * Feel free to rewrite them in a more human way.
 */
class AgentToolParameterMapperTest {

    private lateinit var mapper: AgentToolParameterMapper

    @BeforeEach
    fun setUp() {
        mapper = AgentToolParameterMapper()
    }

    @Test
    fun `map function with no parameters`() {
        // Given a function with no parameters
        val function = ::functionWithNoParams

        // When mapping the function
        val result = mapper.map(function)

        // Then expect an empty object value
        assertEquals(0, result.properties.size)
        assertEquals(0, result.required.size)
        assertNull(result.description)
    }

    @Test
    fun `map function with primitive parameters`() {
        // Given a function with primitive parameters
        val function = ::functionWithPrimitiveParams

        // When mapping the function
        val result = mapper.map(function)

        // Then expect correct parameter mapping
        assertEquals(3, result.properties.size)

        // Check parameter types
        assertTrue(result.properties["stringParam"] is ParameterValue.StringValue)
        assertTrue(result.properties["numberParam"] is ParameterValue.NumberValue)
        assertTrue(result.properties["booleanParam"] is ParameterValue.BooleanValue)

        // Check required parameters
        assertEquals(3, result.required.size)
        assertTrue(result.required.contains("stringParam"))
        assertTrue(result.required.contains("numberParam"))
        assertTrue(result.required.contains("booleanParam"))
    }

    @Test
    fun `map function with optional parameters`() {
        // Given a function with optional parameters
        val function = ::functionWithOptionalParams

        // When mapping the function
        val result = mapper.map(function)

        // Then expect correct parameter mapping
        assertEquals(3, result.properties.size)

        // Check required parameters - only requiredParam should be required
        assertEquals(1, result.required.size)
        assertTrue(result.required.contains("requiredParam"))
        assertFalse(result.required.contains("optionalParam"))
        assertFalse(result.required.contains("nullableParam"))
    }

    @Test
    fun `map function with single data class parameter`() {
        // Given a function with a single data class parameter
        val function = ::functionWithDataClassParam

        // When mapping the function
        val result = mapper.map(function)

        // Then expect data class properties to be mapped
        assertEquals(3, result.properties.size)

        // Check property types
        assertTrue(result.properties["name"] is ParameterValue.StringValue)
        assertTrue(result.properties["age"] is ParameterValue.NumberValue)
        assertTrue(result.properties["isActive"] is ParameterValue.BooleanValue)

        // Check required properties
        assertEquals(2, result.required.size)
        assertTrue(result.required.contains("name"))
        assertTrue(result.required.contains("age"))
        assertFalse(result.required.contains("isActive"))
    }

    @Test
    fun `map function with nested data class parameter`() {
        // Given a function with a nested data class parameter
        val function = ::functionWithNestedDataClassParam

        // When mapping the function
        val result = mapper.map(function)

        // Then expect nested structure to be mapped correctly
        val addressProperty = result.properties["address"]

        // Check that address is an object
        assertTrue(addressProperty is ParameterValue.ObjectValue)

        if (addressProperty is ParameterValue.ObjectValue) {
            // Check address properties
            assertEquals(3, addressProperty.properties.size)
            assertTrue(addressProperty.properties["street"] is ParameterValue.StringValue)
            assertTrue(addressProperty.properties["city"] is ParameterValue.StringValue)
            assertTrue(addressProperty.properties["zip"] is ParameterValue.StringValue)

            // Check required address properties
            assertEquals(2, addressProperty.required.size)
            assertTrue(addressProperty.required.contains("street"))
            assertTrue(addressProperty.required.contains("city"))
            assertFalse(addressProperty.required.contains("zip"))
        }
    }

    @Test
    fun `map function with enum parameter`() {
        // Given a function with an enum parameter
        val function = ::functionWithEnumParam

        // When mapping the function
        val result = mapper.map(function)

        // Then expect enum to be mapped as a string with options
        val statusProperty = result.properties["status"]
        assertTrue(statusProperty is ParameterValue.StringValue)

        if (statusProperty is ParameterValue.StringValue) {
            assertNotNull(statusProperty.enum)
            assertEquals(3, statusProperty.enum?.size)
            assertTrue(statusProperty.enum?.contains("ACTIVE") ?: false)
            assertTrue(statusProperty.enum?.contains("INACTIVE") ?: false)
            assertTrue(statusProperty.enum?.contains("PENDING") ?: false)
        }
    }

    @Test
    fun `map function with annotated parameters`() {
        // Given a function with annotated parameters
        val function = ::functionWithAnnotatedParams

        // When mapping the function
        val result = mapper.map(function)

        // Then expect annotations to be respected
        // Check custom name from annotation
        assertTrue(result.properties.containsKey("userId"))

        // Check description from annotation
        val userIdProperty = result.properties["userId"]
        if (userIdProperty is ParameterValue.StringValue) {
            assertEquals("The unique identifier for the user", userIdProperty.description)
        }
    }

    @Test
    fun `map function with mixed parameter types`() {
        // Given a function with mixed parameter types
        val function = ::functionWithMixedParams

        // When mapping the function
        val result = mapper.map(function)

        // Then expect all parameters to be mapped correctly
        assertEquals(4, result.properties.size)

        // Check parameter types
        assertTrue(result.properties["text"] is ParameterValue.StringValue)
        assertTrue(result.properties["count"] is ParameterValue.NumberValue)
        assertTrue(result.properties["enabled"] is ParameterValue.BooleanValue)
        assertTrue(result.properties["status"] is ParameterValue.StringValue)

        // Check enum values
        val statusProperty = result.properties["status"]
        if (statusProperty is ParameterValue.StringValue) {
            assertNotNull(statusProperty.enum)
            assertEquals(3, statusProperty.enum?.size)
        }
    }

    // Test functions and data classes

    private fun functionWithNoParams() {
        // No parameters
    }

    private fun functionWithPrimitiveParams(stringParam: String, numberParam: Int, booleanParam: Boolean) {
        // Basic primitive parameters
    }

    private fun functionWithOptionalParams(
        requiredParam: String,
        optionalParam: String = "default",
        nullableParam: String?
    ) {
        // Mix of required, optional and nullable parameters
    }

    private fun functionWithDataClassParam(user: User) {
        // Single data class parameter
    }

    private fun functionWithNestedDataClassParam(user: UserWithAddress) {
        // Nested data class parameter
    }

    private fun functionWithEnumParam(status: UserStatus) {
        // Enum parameter
    }

    private fun functionWithAnnotatedParams(
        @ToolParameter(name = "userId", description = "The unique identifier for the user")
        id: String
    ) {
        // Annotated parameter
    }

    private fun functionWithMixedParams(
        text: String,
        count: Int,
        enabled: Boolean,
        status: UserStatus
    ) {
        // Mix of different parameter types
    }

    // Test data classes and enums

    data class User(
        val name: String,
        val age: Int,
        val isActive: Boolean = false
    )

    data class Address(
        val street: String,
        val city: String,
        val zip: String? = null
    )

    data class UserWithAddress(
        val name: String,
        val address: Address
    )

    enum class UserStatus {
        ACTIVE,
        INACTIVE,
        PENDING
    }
}
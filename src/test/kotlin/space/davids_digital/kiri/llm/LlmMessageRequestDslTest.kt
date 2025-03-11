package space.davids_digital.kiri.llm

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import space.davids_digital.kiri.llm.LlmMessageRequest.Message
import space.davids_digital.kiri.llm.LlmMessageRequest.Message.ContentItem
import space.davids_digital.kiri.llm.LlmMessageRequest.Tools
import space.davids_digital.kiri.llm.LlmMessageRequest.Tools.Function.ParameterValue

class LlmMessageRequestDslTest {

    @Test
    fun `create simple request`() {
        val model = "test-model"
        val request = llmMessageRequest<String> {
            this.model = model
            system = "You are a helpful assistant."
            maxOutputTokens = 100
            temperature = 0.7
            userMessage {
                text("Hello, how are you?")
            }
        }

        assertEquals(model, request.model)
        assertEquals("You are a helpful assistant.", request.system)
        assertEquals(100, request.maxOutputTokens)
        assertEquals(0.7, request.temperature)
        assertEquals(1, request.messages.size)
        assertEquals(Message.Role.USER, request.messages[0].role)
        assertEquals(1, request.messages[0].content.size)
        assertTrue(request.messages[0].content[0] is ContentItem.Text)
        assertEquals("Hello, how are you?", (request.messages[0].content[0] as ContentItem.Text).text)
    }

    @Test
    fun `create request with multiple messages`() {
        val request = llmMessageRequest<String> {
            model = "test-model"
            system = "You are a helpful assistant."
            maxOutputTokens = 100
            temperature = 0.7

            userMessage {
                text("Hello, how are you?")
            }

            assistantMessage {
                text("I'm doing well, thank you! How can I help you today?")
            }

            userMessage {
                text("I need help with coding.")
            }
        }

        assertEquals(3, request.messages.size)
        assertEquals(Message.Role.USER, request.messages[0].role)
        assertEquals(Message.Role.ASSISTANT, request.messages[1].role)
        assertEquals(Message.Role.USER, request.messages[2].role)

        assertEquals("Hello, how are you?",
            (request.messages[0].content[0] as ContentItem.Text).text)
        assertEquals("I'm doing well, thank you! How can I help you today?",
            (request.messages[1].content[0] as ContentItem.Text).text)
        assertEquals("I need help with coding.",
            (request.messages[2].content[0] as ContentItem.Text).text)
    }

    @Test
    fun `create request with tools`() {
        val request = llmMessageRequest<String> {
            model = "test-model"
            system = "You are a helpful assistant."
            maxOutputTokens = 150
            temperature = 0.5

            userMessage {
                text("Calculate 2 + 2")
            }

            tools {
                choice = Tools.ToolChoice.REQUIRED
                allowParallelUse = true

                function {
                    name = "calculator"
                    description = "Perform mathematical calculations"
                    parameters = ParameterValue.ObjectValue(
                        description = "Calculator function parameters",
                        properties = mapOf(
                            "expression" to ParameterValue.StringValue(
                                description = "Mathematical expression to evaluate",
                                enum = null
                            )
                        ),
                        required = listOf("expression")
                    )
                }
            }
        }

        assertEquals("test-model", request.model)
        assertEquals(Tools.ToolChoice.REQUIRED, request.tools.choice)
        assertTrue(request.tools.allowParallelUse)

        assertEquals(1, request.tools.functions.size)
        val function = request.tools.functions[0]
        assertEquals("calculator", function.name)
        assertEquals("Perform mathematical calculations", function.description)

        val params = function.parameters
        assertEquals("Calculator function parameters", params.description)
        assertEquals(1, params.properties.size)
        assertEquals(listOf("expression"), params.required)

        val expressionParam = params.properties["expression"]
        assertTrue(expressionParam is ParameterValue.StringValue)
        assertEquals("Mathematical expression to evaluate",
            (expressionParam as ParameterValue.StringValue).description)
    }

    @Test
    fun `create request with complex message content`() {
        val imageData = ByteArray(10) { it.toByte() }
        val jsonNode = ObjectNode(JsonNodeFactory.instance).put("value", 42)

        val request = llmMessageRequest<String> {
            model = "test-model"
            system = "You are a helpful assistant."

            userMessage {
                text("Analyze this image:")
                image(imageData, ContentItem.MediaType.JPEG)
            }

            assistantMessage {
                toolUse {
                    id = "tool-123"
                    name = "image_analyzer"
                    input = jsonNode
                }
            }

            userMessage {
                text("What do you see in the image?")
            }
        }

        assertEquals(3, request.messages.size)

        // Check first user message
        val firstMessage = request.messages[0]
        assertEquals(2, firstMessage.content.size)
        assertTrue(firstMessage.content[0] is ContentItem.Text)
        assertTrue(firstMessage.content[1] is ContentItem.Image)
        assertEquals("Analyze this image:", (firstMessage.content[0] as ContentItem.Text).text)
        assertEquals(imageData, (firstMessage.content[1] as ContentItem.Image).data)
        assertEquals(ContentItem.MediaType.JPEG, (firstMessage.content[1] as ContentItem.Image).mediaType)

        // Check assistant message
        val assistantMessage = request.messages[1]
        assertEquals(1, assistantMessage.content.size)
        assertTrue(assistantMessage.content[0] is ContentItem.ToolUse)
        assertEquals("tool-123", (assistantMessage.content[0] as ContentItem.ToolUse).id)
        assertEquals("image_analyzer", (assistantMessage.content[0] as ContentItem.ToolUse).name)
        assertEquals(jsonNode, (assistantMessage.content[0] as ContentItem.ToolUse).input)
    }

    @Test
    fun `create request with tool result`() {
        val imageData = ByteArray(5) { it.toByte() }

        val request = llmMessageRequest<String> {
            model = "test-model"

            assistantMessage {
                toolUse {
                    id = "tool-456"
                    name = "image_generator"
                    input = ObjectNode(JsonNodeFactory.instance).put("prompt", "landscape")
                }

                toolResult {
                    toolUseId = "tool-456"
                    text("Generated image result:")
                    image(imageData, ContentItem.MediaType.PNG)
                }

                text("Here's the generated landscape image.")
            }
        }

        assertEquals(1, request.messages.size)
        assertEquals(Message.Role.ASSISTANT, request.messages[0].role)
        assertEquals(3, request.messages[0].content.size)

        assertTrue(request.messages[0].content[0] is ContentItem.ToolUse)
        assertTrue(request.messages[0].content[1] is ContentItem.ToolResult)
        assertTrue(request.messages[0].content[2] is ContentItem.Text)

        val toolResult = request.messages[0].content[1] as ContentItem.ToolResult
        assertEquals("tool-456", toolResult.toolUseId)
        assertEquals(2, toolResult.content.size)

        assertTrue(toolResult.content[0] is ContentItem.Text)
        assertTrue(toolResult.content[1] is ContentItem.Image)
        assertEquals("Generated image result:", (toolResult.content[0] as ContentItem.Text).text)
        assertEquals(imageData, (toolResult.content[1] as ContentItem.Image).data)
        assertEquals(ContentItem.MediaType.PNG, (toolResult.content[1] as ContentItem.Image).mediaType)
    }

    @Test
    fun `create request with different parameter value types`() {
        val request = llmMessageRequest<String> {
            model = "test-model"

            tools {
                function {
                    name = "test_function"
                    description = "A test function with different parameter types"
                    parameters = ParameterValue.ObjectValue(
                        description = "Parameters description",
                        properties = mapOf(
                            "stringParam" to ParameterValue.StringValue(
                                description = "A string parameter",
                                enum = listOf("option1", "option2", "option3")
                            ),
                            "numberParam" to ParameterValue.NumberValue(
                                description = "A number parameter"
                            ),
                            "booleanParam" to ParameterValue.BooleanValue(
                                description = "A boolean parameter"
                            ),
                            "objectParam" to ParameterValue.ObjectValue(
                                description = "A nested object parameter",
                                properties = mapOf(
                                    "nestedString" to ParameterValue.StringValue(
                                        description = "A nested string parameter",
                                        enum = null
                                    )
                                ),
                                required = listOf("nestedString")
                            )
                        ),
                        required = listOf("stringParam", "numberParam")
                    )
                }
            }
        }

        val function = request.tools.functions[0]
        assertEquals("test_function", function.name)

        val params = function.parameters
        assertEquals(4, params.properties.size)
        assertEquals(listOf("stringParam", "numberParam"), params.required)

        // Check string parameter
        val stringParam = params.properties["stringParam"] as ParameterValue.StringValue
        assertEquals("A string parameter", stringParam.description)
        assertEquals(listOf("option1", "option2", "option3"), stringParam.enum)

        // Check number parameter
        val numberParam = params.properties["numberParam"] as ParameterValue.NumberValue
        assertEquals("A number parameter", numberParam.description)

        // Check boolean parameter
        val booleanParam = params.properties["booleanParam"] as ParameterValue.BooleanValue
        assertEquals("A boolean parameter", booleanParam.description)

        // Check nested object parameter
        val objectParam = params.properties["objectParam"] as ParameterValue.ObjectValue
        assertEquals("A nested object parameter", objectParam.description)
        assertEquals(1, objectParam.properties.size)
        assertEquals(listOf("nestedString"), objectParam.required)

        val nestedString = objectParam.properties["nestedString"] as ParameterValue.StringValue
        assertEquals("A nested string parameter", nestedString.description)
        assertNull(nestedString.enum)
    }
}
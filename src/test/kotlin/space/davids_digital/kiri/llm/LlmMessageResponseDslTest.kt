package space.davids_digital.kiri.llm

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import space.davids_digital.kiri.llm.LlmMessageResponse.ContentItem
import space.davids_digital.kiri.llm.LlmToolUse.Input
import space.davids_digital.kiri.llm.dsl.llmMessageResponse

/**
 * I wrote these tests with AI because I'm lazy.
 * Feel free to rewrite them in a more human way.
 */
class LlmMessageResponseDslTest {

    @Test
    fun `create simple response`() {
        val response = llmMessageResponse {
            id = "resp-123"
            stopReason = LlmMessageResponse.StopReason.END_TURN
            content {
                text("Hello world")
            }
            usage {
                inputTokens = 5
                outputTokens = 10
            }
        }

        assertEquals("resp-123", response.id)
        assertEquals(LlmMessageResponse.StopReason.END_TURN, response.stopReason)
        assertEquals(1, response.content.size)
        assertTrue(response.content[0] is ContentItem.Text)
        assertEquals("Hello world", (response.content[0] as ContentItem.Text).text)
        assertEquals(5, response.usage.inputTokens)
        assertEquals(10, response.usage.outputTokens)
    }

    @Test
    fun `create response with tool use`() {
        val response = llmMessageResponse {
            id = "resp-456"
            stopReason = LlmMessageResponse.StopReason.TOOL_USE
            content {
                toolUse {
                    id = "tool-123"
                    name = "calculator"
                    input {
                        objectValue {
                            text("expression", "1 + 1")
                        }
                    }
                }
            }
            usage {
                inputTokens = 7
                outputTokens = 15
            }
        }

        assertEquals("resp-456", response.id)
        assertEquals(LlmMessageResponse.StopReason.TOOL_USE, response.stopReason)
        assertEquals(1, response.content.size)
        assertTrue(response.content[0] is ContentItem.ToolUse)

        val toolUse = (response.content[0] as ContentItem.ToolUse).toolUse
        assertEquals("tool-123", toolUse.id)
        assertEquals("calculator", toolUse.name)
        assertTrue(toolUse.input is Input.Object)

        val inputObj = toolUse.input as Input.Object
        assertEquals(1, inputObj.items.size)
        assertTrue(inputObj.items["expression"] is Input.Text)
        assertEquals("1 + 1", (inputObj.items["expression"] as Input.Text).text)

        assertEquals(7, response.usage.inputTokens)
        assertEquals(15, response.usage.outputTokens)
    }

    @Test
    fun `create complex response with mixed content`() {
        val response = llmMessageResponse {
            id = "resp-789"
            stopReason = LlmMessageResponse.StopReason.MAX_TOKENS
            content {
                text("Let me calculate this for you")
                toolUse {
                    id = "tool-456"
                    name = "calculator"
                    input {
                        objectValue {
                            text("expression", "2 * 3")
                        }
                    }
                }
            }
            usage {
                inputTokens = 10
                outputTokens = 20
            }
        }

        assertEquals("resp-789", response.id)
        assertEquals(LlmMessageResponse.StopReason.MAX_TOKENS, response.stopReason)
        assertEquals(2, response.content.size)
        assertTrue(response.content[0] is ContentItem.Text)
        assertTrue(response.content[1] is ContentItem.ToolUse)

        assertEquals("Let me calculate this for you", (response.content[0] as ContentItem.Text).text)

        val toolUse = (response.content[1] as ContentItem.ToolUse).toolUse
        assertEquals("tool-456", toolUse.id)
        assertEquals("calculator", toolUse.name)

        val inputObj = toolUse.input as Input.Object
        assertEquals("2 * 3", (inputObj.items["expression"] as Input.Text).text)

        assertEquals(10, response.usage.inputTokens)
        assertEquals(20, response.usage.outputTokens)
    }

    @Test
    fun `test complex tool use input structure`() {
        val response = llmMessageResponse {
            id = "resp-complex"
            content {
                toolUse {
                    id = "complex-tool"
                    name = "dataProcessor"
                    input {
                        objectValue {
                            text("name", "John Doe")
                            number("age", 30.0)
                            boolean("isActive", true)
                            array("scores") {
                                number(85.5)
                                number(92.0)
                                number(78.5)
                            }
                            objectValue("contact") {
                                text("email", "john@example.com")
                                text("phone", "+1234567890")
                            }
                        }
                    }
                }
            }
        }

        val toolUse = (response.content[0] as ContentItem.ToolUse).toolUse
        val input = toolUse.input as Input.Object

        // Verify basic fields
        assertEquals("John Doe", (input.items["name"] as Input.Text).text)
        assertEquals(30.0, (input.items["age"] as Input.Number).number)
        assertEquals(true, (input.items["isActive"] as Input.Boolean).boolean)

        // Verify nested array
        val scores = input.items["scores"] as Input.Array
        assertEquals(3, scores.items.size)
        assertEquals(85.5, (scores.items[0] as Input.Number).number)
        assertEquals(92.0, (scores.items[1] as Input.Number).number)
        assertEquals(78.5, (scores.items[2] as Input.Number).number)

        // Verify nested object
        val contact = input.items["contact"] as Input.Object
        assertEquals("john@example.com", (contact.items["email"] as Input.Text).text)
        assertEquals("+1234567890", (contact.items["phone"] as Input.Text).text)
    }
}
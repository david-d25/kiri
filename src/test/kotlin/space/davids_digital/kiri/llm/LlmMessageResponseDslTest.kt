package space.davids_digital.kiri.llm

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import space.davids_digital.kiri.llm.LlmMessageResponse.ContentItem.ToolUse.Input

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
        assertTrue(response.content[0] is LlmMessageResponse.ContentItem.Text)
        assertEquals("Hello world", (response.content[0] as LlmMessageResponse.ContentItem.Text).text)
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
                    input = Input.ObjectValue(mapOf(
                        "expression" to Input.TextValue("1 + 1")
                    ))
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
        assertTrue(response.content[0] is LlmMessageResponse.ContentItem.ToolUse)

        val toolUse = response.content[0] as LlmMessageResponse.ContentItem.ToolUse
        assertEquals("tool-123", toolUse.id)
        assertEquals("calculator", toolUse.name)
        assertTrue(toolUse.input is Input.ObjectValue)

        val inputObj = toolUse.input as Input.ObjectValue
        assertEquals(1, inputObj.items.size)
        assertTrue(inputObj.items["expression"] is Input.TextValue)
        assertEquals("1 + 1", (inputObj.items["expression"] as Input.TextValue).text)

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
                    input = Input.ObjectValue(mapOf(
                        "expression" to Input.TextValue("2 * 3")
                    ))
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
        assertTrue(response.content[0] is LlmMessageResponse.ContentItem.Text)
        assertTrue(response.content[1] is LlmMessageResponse.ContentItem.ToolUse)

        assertEquals("Let me calculate this for you", (response.content[0] as LlmMessageResponse.ContentItem.Text).text)

        val toolUse = response.content[1] as LlmMessageResponse.ContentItem.ToolUse
        assertEquals("tool-456", toolUse.id)
        assertEquals("calculator", toolUse.name)

        val inputObj = toolUse.input as Input.ObjectValue
        assertEquals("2 * 3", (inputObj.items["expression"] as Input.TextValue).text)

        assertEquals(10, response.usage.inputTokens)
        assertEquals(20, response.usage.outputTokens)
    }

    @Test
    fun `toolUseInput builder`() {
        val simpleTextInput = toolUseInput {
            text("simple text")
        }

        assertTrue(simpleTextInput is Input.TextValue)
        assertEquals("simple text", (simpleTextInput as Input.TextValue).text)

        val numberInput = toolUseInput {
            number(42.5)
        }

        assertTrue(numberInput is Input.NumberValue)
        assertEquals(42.5, (numberInput as Input.NumberValue).number)

        val booleanInput = toolUseInput {
            boolean(true)
        }

        assertTrue(booleanInput is Input.BooleanValue)
        assertEquals(true, (booleanInput as Input.BooleanValue).boolean)

        val arrayInput = toolUseInput {
            array {
                add(Input.TextValue("item1"))
                add(Input.NumberValue(123.0))
                add(Input.BooleanValue(false))
            }
        }

        assertTrue(arrayInput is Input.ArrayValue)
        assertEquals(3, (arrayInput as Input.ArrayValue).items.size)
        assertEquals("item1", (arrayInput.items[0] as Input.TextValue).text)
        assertEquals(123.0, (arrayInput.items[1] as Input.NumberValue).number)
        assertEquals(false, (arrayInput.items[2] as Input.BooleanValue).boolean)

        val objectInput = toolUseInput {
            `object` {
                put("name", Input.TextValue("John"))
                put("age", Input.NumberValue(30.0))
                put("isAdmin", Input.BooleanValue(true))
            }
        }

        assertTrue(objectInput is Input.ObjectValue)
        val objMap = (objectInput as Input.ObjectValue).items
        assertEquals(3, objMap.size)
        assertEquals("John", (objMap["name"] as Input.TextValue).text)
        assertEquals(30.0, (objMap["age"] as Input.NumberValue).number)
        assertEquals(true, (objMap["isAdmin"] as Input.BooleanValue).boolean)
    }

    @Test
    fun `nested input structures`() {
        val complexInput = toolUseInput {
            `object` {
                put("name", Input.TextValue("John"))
                put("contact", Input.ObjectValue(mapOf(
                    "email" to Input.TextValue("john@example.com"),
                    "phone" to Input.TextValue("+1234567890")
                )))
                put("scores", Input.ArrayValue(listOf(
                    Input.NumberValue(85.0),
                    Input.NumberValue(92.0),
                    Input.NumberValue(78.5)
                )))
            }
        }

        assertTrue(complexInput is Input.ObjectValue)
        val objMap = (complexInput as Input.ObjectValue).items
        assertEquals(3, objMap.size)

        // Verify name field
        assertEquals("John", (objMap["name"] as Input.TextValue).text)

        // Verify nested contact object
        val contact = objMap["contact"] as Input.ObjectValue
        assertEquals("john@example.com", (contact.items["email"] as Input.TextValue).text)
        assertEquals("+1234567890", (contact.items["phone"] as Input.TextValue).text)

        // Verify scores array
        val scores = objMap["scores"] as Input.ArrayValue
        assertEquals(3, scores.items.size)
        assertEquals(85.0, (scores.items[0] as Input.NumberValue).number)
        assertEquals(92.0, (scores.items[1] as Input.NumberValue).number)
        assertEquals(78.5, (scores.items[2] as Input.NumberValue).number)
    }
}
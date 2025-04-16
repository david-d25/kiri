package space.davids_digital.kiri.integration.telegram

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import space.davids_digital.kiri.model.telegram.TelegramMessageEntity
import space.davids_digital.kiri.model.telegram.TelegramMessageEntity.Type

class TelegramHtmlMapperTest {
    @Test
    fun simpleBoldRoundtrip() {
        val text = "Hello"
        val entities = listOf(
            TelegramMessageEntity(Type.BOLD, 0, 5)
        )
        val html = TelegramHtmlMapper.toHtml(text, entities)
        assertEquals("<b>Hello</b>", html)
        val parsed = TelegramHtmlMapper.fromHtml(html)
        assertEquals(text, parsed.text)
        assertEquals(entities, parsed.entities)
    }

    @Test
    fun nestedBoldItalic() {
        val text = "Hello World"
        val entities = listOf(
            TelegramMessageEntity(Type.BOLD, 0, 11),
            TelegramMessageEntity(Type.ITALIC, 6, 5)
        )
        val html = TelegramHtmlMapper.toHtml(text, entities)
        assertEquals("<b>Hello <i>World</i></b>", html)
        val parsed = TelegramHtmlMapper.fromHtml(html)
        assertEquals(text, parsed.text)
        // order may differ, so compare sets
        assertEquals(entities.toSet(), parsed.entities.toSet())
    }

    @Test
    fun textLink() {
        val text = "Click me"
        val url = "https://example.com"
        val entities = listOf(
            TelegramMessageEntity(Type.TEXT_LINK, 0, 8, url = url)
        )
        val html = TelegramHtmlMapper.toHtml(text, entities)
        assertEquals("<a data-entity=\"text_link\" href=\"https://example.com\">Click me</a>", html)
        val parsed = TelegramHtmlMapper.fromHtml(html)
        assertEquals(text, parsed.text)
        assertEquals(entities, parsed.entities)
    }

    @Test
    fun surrogatePairEmojiHandling() {
        val text = "😀Bold"
        // 😀 is a surrogate pair (length 2), BOLD wraps whole string after emoji
        val entities = listOf(
            TelegramMessageEntity(Type.BOLD, 2, 4)
        )
        val html = TelegramHtmlMapper.toHtml(text, entities)
        assertEquals("😀<b>Bold</b>", html)
        val parsed = TelegramHtmlMapper.fromHtml(html)
        assertEquals(text, parsed.text)
        assertEquals(entities, parsed.entities)
    }
}
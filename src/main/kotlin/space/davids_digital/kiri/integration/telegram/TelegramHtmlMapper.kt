package space.davids_digital.kiri.integration.telegram

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import space.davids_digital.kiri.model.telegram.TelegramMessageEntity
import space.davids_digital.kiri.model.telegram.TelegramMessageEntity.Type

/**
 * Bidirectional mapper between Telegram raw text + entities and lightweight HTML understood by LLM.
 *
 * Design principles:
 * 1.  HTML is used instead of Markdown – richer and less escaping.
 * 2.  Only meaningful formatting/link entities are mapped. `CUSTOM_EMOJI` is currently kept<br>
 *     as the raw character without additional markup to avoid context bloat.
 * 3.  Offsets/lengths reported by Telegram are counted in UTF‑16 code units, exactly the same
 *     as Kotlin `Char` indices, so we can work with the standard `String` API.
 * 4.  Entities are assumed to be **properly nested** (Telegram guarantee).
 */
object TelegramHtmlMapper {

    /** Plain old data‑holder for the result of [fromHtml]. */
    data class Parsed(val text: String, val entities: List<TelegramMessageEntity>)

    // ---------------------------------------------------------------------------------------------
    // Public API
    // ---------------------------------------------------------------------------------------------

    /**
     * Convert Telegram raw [text] + [entities] into HTML string understood by LLM.
     */
    fun toHtml(text: String, entities: List<TelegramMessageEntity>): String {
        if (entities.isEmpty()) return text

        // EVENTS: map from position -> list of tags (with length for ordering by nesting depth)
        data class TagEvent(val length: Int, val tag: String)
        val opens = mutableMapOf<Int, MutableList<TagEvent>>()
        val closes = mutableMapOf<Int, MutableList<TagEvent>>()

        // 1. build open/close lists
        for (e in entities) {
            val (open, close) = htmlTagsForEntity(e) ?: continue // skip unsupported types
            opens.computeIfAbsent(e.offset) { mutableListOf() }.add(TagEvent(e.length, open))
            closes.computeIfAbsent(e.offset + e.length) { mutableListOf() }.add(TagEvent(e.length, close))
        }
        // 2. sort to guarantee valid nesting: open longer first, close shorter first
        opens.values.forEach { it.sortByDescending { ev -> ev.length } }
        closes.values.forEach { it.sortBy { ev -> ev.length } }

        // 3. walk through the text and weave tags
        val sb = StringBuilder()
        for (i in 0..text.length) { // iterate *between* characters, inclusive upper bound for trailing closes
            closes[i]?.forEach { sb.append(it.tag) }
            if (i == text.length) break // we are past last char
            opens[i]?.forEach { sb.append(it.tag) }
            sb.append(text[i])
        }
        return sb.toString()
    }

    /**
     * Parse HTML produced by [toHtml] (or compatible) back into raw text and Telegram entities.
     * Complexity: O(totalDomNodes).
     */
    fun fromHtml(html: String): Parsed {
        val doc = Jsoup.parseBodyFragment(html)
        val body = doc.body()
        val textBuilder = StringBuilder()
        val entities = mutableListOf<TelegramMessageEntity>()
        traverse(body, 0, textBuilder, entities)
        return Parsed(textBuilder.toString(), entities.sortedWith(compareBy({ it.offset }, { it.length })))
    }

    // ---------------------------------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------------------------------

    // Recursively traverse DOM, accumulating text and entities.
    private fun traverse(node: Node, depth: Int, acc: StringBuilder, out: MutableList<TelegramMessageEntity>) {
        when (node) {
            is TextNode -> acc.append(node.wholeText)
            is Element  -> processElement(node, depth, acc, out)
        }
    }

    private fun processElement(el: Element, depth: Int, acc: StringBuilder, out: MutableList<TelegramMessageEntity>) {
        val mapping = entityTypeForElement(el)
        val startOffset = acc.length
        // children first (depth‑first order)
        for (child in el.childNodes()) traverse(child, depth + 1, acc, out)
        val endOffset = acc.length
        if (mapping != null && endOffset > startOffset) {
            val (type, url, userId, language) = mapping
            out += TelegramMessageEntity(
                type = type,
                offset = startOffset,
                length = endOffset - startOffset,
                url = url,
                userId = userId,
                language = language
            )
        }
    }

    /** Return open & close HTML tags for the given entity (or null to ignore). */
    private fun htmlTagsForEntity(e: TelegramMessageEntity): Pair<String, String>? = when (e.type) {
        Type.BOLD          -> "<b>" to "</b>"
        Type.ITALIC        -> "<i>" to "</i>"
        Type.UNDERLINE     -> "<u>" to "</u>"
        Type.STRIKETHROUGH -> "<s>" to "</s>"
        Type.SPOILER       -> "<span data-entity=\"spoiler\" class=\"tg-spoiler\">" to "</span>"
        Type.BLOCKQUOTE,
        Type.EXPANDABLE_BLOCKQUOTE -> "<blockquote>" to "</blockquote>"
        Type.CODE          -> "<code>" to "</code>"
        Type.PRE           -> {
            val langAttr = e.language?.let { " class=\"language-${it}\"" } ?: ""
            "<pre$langAttr>" to "</pre>"
        }
        Type.TEXT_LINK     -> {
            val href = e.url ?: return null
            "<a data-entity=\"text_link\" href=\"${href.htmlEscape()}\">" to "</a>"
        }
        Type.TEXT_MENTION  -> {
            val id = e.userId ?: return null
            "<a data-entity=\"text_mention\" data-user-id=\"$id\" href=\"tg://user?id=$id\">" to "</a>"
        }
        // Link‑like entities – wrap into <a data-entity="..."> so they can be restored.
        Type.MENTION,
        Type.HASHTAG,
        Type.CASHTAG,
        Type.URL,
        Type.EMAIL,
        Type.PHONE_NUMBER -> "<a data-entity=\"${e.type.name.lowercase()}\">" to "</a>"
        // CUSTOM_EMOJI currently kept as raw char without markup.
        Type.CUSTOM_EMOJI -> null
        // Bot commands are rare in content destined for LLM, still map as <span>.
        Type.BOT_COMMAND  -> "<span data-entity=\"bot_command\">" to "</span>"
    }

    /** Determine entity type & extras for the given HTML element, if any. */
    private fun entityTypeForElement(el: Element): Quad<Type, String?, Long?, String?>? {
        return when (el.tagName().lowercase()) {
            "b"  -> Quad(Type.BOLD, null, null, null)
            "i"  -> Quad(Type.ITALIC, null, null, null)
            "u"  -> Quad(Type.UNDERLINE, null, null, null)
            "s"  -> Quad(Type.STRIKETHROUGH, null, null, null)
            "blockquote" -> Quad(Type.BLOCKQUOTE, null, null, null)
            "code" -> {
                // If parent is <pre> we don't create CODE entity (Telegram uses PRE only)
                if (el.parent()?.tagName()?.lowercase() == "pre") null else Quad(Type.CODE, null, null, null)
            }
            "pre" -> {
                val lang = el.classNames().firstOrNull { it.startsWith("language-") }?.removePrefix("language-")
                Quad(Type.PRE, null, null, lang)
            }
            "a"  -> parseAnchor(el)
            "span" -> when {
                el.attr("data-entity") == "spoiler" || el.hasClass("tg-spoiler") -> Quad(Type.SPOILER, null, null, null)
                el.attr("data-entity") == "bot_command" -> Quad(Type.BOT_COMMAND, null, null, null)
                else -> null
            }
            else -> null
        }
    }

    /** Parse <a> into the corresponding entity mapping. */
    private fun parseAnchor(a: Element): Quad<Type, String?, Long?, String?>? {
        val dataEntity = a.attr("data-entity")
        return when {
            dataEntity == "text_link" -> Quad(Type.TEXT_LINK, a.attr("href"), null, null)
            dataEntity == "text_mention" -> {
                val id = a.attr("data-user-id").toLongOrNull() ?: return null
                Quad(Type.TEXT_MENTION, null, id, null)
            }
            dataEntity.isNotBlank() -> when (dataEntity) {
                "mention"     -> Quad(Type.MENTION, null, null, null)
                "hashtag"     -> Quad(Type.HASHTAG, null, null, null)
                "cashtag"     -> Quad(Type.CASHTAG, null, null, null)
                "url"         -> Quad(Type.URL, null, null, null)
                "email"       -> Quad(Type.EMAIL, null, null, null)
                "phone_number"-> Quad(Type.PHONE_NUMBER, null, null, null)
                else -> null
            }
            // fallback – treat as TEXT_LINK if href present
            a.hasAttr("href") -> Quad(Type.TEXT_LINK, a.attr("href"), null, null)
            else -> null
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Util
    // ---------------------------------------------------------------------------------------------

    private fun String.htmlEscape(): String =
        this.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    /** Simple 4‑tuple substitute to avoid extra Pair/Triple nesting. */
    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
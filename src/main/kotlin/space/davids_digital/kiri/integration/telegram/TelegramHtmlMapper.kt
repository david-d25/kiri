package space.davids_digital.kiri.integration.telegram

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.nodes.Node
import org.jsoup.nodes.TextNode
import org.jsoup.parser.Parser
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
        // IMPORTANT: entity offsets/lengths from Telegram are calculated on the *raw* message text.
        // If some upstream code already HTML-escaped the text (e.g. turned "&&" into "&amp;&amp;"),
        // then all entity offsets after the first '&' will shift and tags will "slide".
        //
        // We *normally* expect raw text here. As a safety net we detect common HTML entities and
        // unescape them back to raw text so that offsets line up again.
        val sourceText = if (looksHtmlEscaped(text)) Parser.unescapeEntities(text, false) else text

        if (entities.isEmpty()) return sourceText

        // Telegram Bot API offsets are UTF‑16 code units, but some client libraries (or intermediate
        // pipelines) may accidentally provide offsets in Unicode *code points*.
        // When that happens, entity boundaries can fall **inside** surrogate pairs / emoji sequences,
        // which leads to:
        //   1) tags "shifting" (e.g. only part of a link is wrapped),
        //   2) U+FFFD replacement chars ("�") when an unpaired surrogate is rendered.
        // We defensively normalize entity indices to safe UTF‑16 boundaries.
        val normalizedEntities = normalizeEntityIndices(sourceText, entities)

        // EVENTS: map from position -> list of tags (with length for ordering by nesting depth)
        data class TagEvent(val length: Int, val seq: Int, val tag: String)
        val opens = mutableMapOf<Int, MutableList<TagEvent>>()
        val closes = mutableMapOf<Int, MutableList<TagEvent>>()

        // 1. build open/close lists
        for ((seq, e) in normalizedEntities.withIndex()) {
            val (open, close) = htmlTagsForEntity(e) ?: continue // skip unsupported types
            opens.computeIfAbsent(e.offset) { mutableListOf() }.add(TagEvent(e.length, seq, open))
            closes.computeIfAbsent(e.offset + e.length) { mutableListOf() }.add(TagEvent(e.length, seq, close))
        }
        // 2. sort to guarantee valid nesting:
        //    - open longer first
        //    - close shorter first
        //    - BUT if two entities share the same [offset,length] (e.g., bold + link applied to the
        //      exact same range), then close order MUST be the reverse of open order, otherwise
        //      you'll produce crossing tags (<a><b>..</a></b>).
        //    We achieve this by using a stable sequence number.
        opens.values.forEach { it.sortWith(compareByDescending<TagEvent> { ev -> ev.length }.thenBy { it.seq }) }
        closes.values.forEach { it.sortWith(compareBy<TagEvent> { ev -> ev.length }.thenByDescending { it.seq }) }

        // 3. walk through the text and weave tags
        val sb = StringBuilder()
        for (i in 0..sourceText.length) { // iterate *between* characters, inclusive upper bound for trailing closes
            closes[i]?.forEach { sb.append(it.tag) }
            if (i == sourceText.length) break // we are past last char
            opens[i]?.forEach { sb.append(it.tag) }
            sb.append(sourceText[i].toString().htmlEscape())
        }
        return sb.toString()
    }


    private fun looksHtmlEscaped(s: String): Boolean {
        // Heuristic: we only care about the common escapes that are likely introduced by an HTML-escape pass.
        // If the user literally typed "&amp;" in Telegram, this will unescape it back to "&".
        // In practice this is the right trade-off to keep entity offsets consistent.
        return s.contains("&amp;") || s.contains("&lt;") || s.contains("&gt;") || s.contains("&quot;") || s.contains("&#39;") || s.contains("&#x")
    }

    /**
     * Normalize entity offsets/lengths to safe UTF‑16 indices.
     *
     * Why:
     * - Some sources accidentally provide offsets in code points.
     * - Or intermediate text processing may change indices.
     *
     * Strategy:
     * 1) If an entity boundary splits a surrogate pair or falls out of bounds, try interpreting
     *    the offset/length as *code point* indices and convert to UTF‑16 indices.
     * 2) If it still splits a surrogate pair, expand the range to fully include the surrogate pair.
     * 3) Clamp everything to [0..text.length].
     */
    private fun normalizeEntityIndices(text: String, entities: List<TelegramMessageEntity>): List<TelegramMessageEntity> {
        if (entities.isEmpty()) return entities

        val cpCount = text.codePointCount(0, text.length)

        fun splitsSurrogate(boundary: Int): Boolean = text.isSurrogatePairBoundary(boundary)

        fun clamp(v: Int): Int = v.coerceIn(0, text.length)

        fun expandToAvoidSurrogates(start0: Int, end0: Int): Pair<Int, Int> {
            var start = clamp(start0)
            var end = clamp(end0)
            // If boundary falls between high+low surrogate, expand to include full pair.
            if (splitsSurrogate(start)) start = clamp(start - 1)
            if (splitsSurrogate(end)) end = clamp(end + 1)
            if (end < start) end = start
            return start to end
        }

        return entities.map { e ->
            var start = e.offset
            var end = e.offset + e.length

            val inBounds = start >= 0 && end >= 0 && start <= end && end <= text.length
            val boundariesSafe = inBounds && !splitsSurrogate(start) && !splitsSurrogate(end)
            if (!boundariesSafe) {
                // Try treating offsets as code points.
                val cpInBounds = e.offset >= 0 && e.length >= 0 && (e.offset + e.length) <= cpCount
                if (cpInBounds) {
                    start = text.offsetByCodePoints(0, e.offset)
                    end = text.offsetByCodePoints(0, e.offset + e.length)
                }
            }

            val (safeStart, safeEnd) = expandToAvoidSurrogates(start, end)
            if (safeEnd <= safeStart) {
                // Empty entity – drop by returning a zero-length clone that will be ignored later.
                e.copy(offset = safeStart, length = 0)
            } else {
                e.copy(offset = safeStart, length = safeEnd - safeStart)
            }
        }.filter { it.length > 0 }
    }

    /**
     * Parse HTML produced by [toHtml] (or compatible) back into raw text and Telegram entities.
     * Complexity: O(totalDomNodes).
     */
    fun fromHtml(html: String): Parsed {
        val doc = Jsoup.parse(html)
        val body = doc.body()
        val textBuilder = StringBuilder()
        val entities = mutableListOf<TelegramMessageEntity>()
        traverse(body, 0, textBuilder, entities)
        return Parsed(textBuilder.toString(), entities.sortedWith(compareBy({ it.offset }, { it.length })))
    }

    // Recursively traverse DOM, accumulating text and entities.
    private fun traverse(node: Node, depth: Int, acc: StringBuilder, out: MutableList<TelegramMessageEntity>) {
        if (node is TextNode) {
            acc.append(node.wholeText)
        } else if (node is Element) {
            val mapping = entityTypeForElement(node)
            if (mapping == null && node.tagName() != "body") {
                // No mapping – output raw HTML tag with attributes
                // TODO handle void/self-closing tags properly
                val rawOpenTag = buildString {
                    append("<")
                    append(node.tagName())
                    for (attr in node.attributes()) {
                        append(" ")
                        append(attr.key)
                        if (attr.hasDeclaredValue()) {
                            append("=\"")
                            append(attr.value.htmlEscape())
                            append("\"")
                        }
                    }
                    append(">")
                }
                acc.append(rawOpenTag)
            }
            val startOffset = acc.length
            // children first (depth‑first order)
            for (child in node.childNodes()) {
                traverse(child, depth + 1, acc, out)
            }
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
            if (mapping == null && !node.tag().isSelfClosing && node.tagName() != "body") {
                acc.append("</").append(node.tagName()).append(">")
            }
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
        // DATE_TIME is an auto-detected entity (Bot API 9.5); keep the underlying text as-is, no markup.
        Type.DATE_TIME -> null
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

    /**
     * @return true if [index] is a *boundary* that splits a surrogate pair.
     * Boundary means "between (index-1) and index" in UTF‑16 code units.
     */
    private fun String.isSurrogatePairBoundary(index: Int): Boolean {
        if (index <= 0 || index >= this.length) return false
        val prev = this[index - 1]
        val cur = this[index]
        return Character.isHighSurrogate(prev) && Character.isLowSurrogate(cur)
    }

    /** Simple 4‑tuple substitute to avoid extra Pair/Triple nesting. */
    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
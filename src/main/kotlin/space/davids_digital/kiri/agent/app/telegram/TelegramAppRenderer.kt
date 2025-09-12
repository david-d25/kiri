package space.davids_digital.kiri.agent.app.telegram

import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import space.davids_digital.kiri.agent.frame.asPrettyString
import space.davids_digital.kiri.agent.frame.dsl.FrameContentBuilder
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.integration.telegram.TelegramHtmlMapper.toHtml
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.llm.LlmImageType
import space.davids_digital.kiri.model.telegram.*
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit.MINUTES

@Component
class TelegramAppRenderer (private val service: TelegramService) {
    companion object {
        private const val MAX_MEDIA_SIZE_BYTES = 5 * 1024 * 1024
        private const val MAX_IMAGE_SIDE_LENGTH = 1568
        private const val MAX_IMAGE_PIXELS = 1150000
        private const val MAX_LIST_ITEMS = 50
        private const val MAX_TEXT_LENGTH = 4000
    }

    fun render(state: TelegramAppViewState) = dataFrameContent {
        if (state.openedChat != null) {
            renderChat(state, state.openedChat)
        } else {
            renderChatList(state)
        }
    }

    private fun FrameContentBuilder.renderChatList(state: TelegramAppViewState) {
        line("""<chats page="${state.chatsPage}" total-pages="${state.chatsTotalPages}">""")
        if (state.chatsView.isNotEmpty()) {
            for (chat in state.chatsView.take(MAX_LIST_ITEMS)) {
                renderChatListItem(chat)
            }
        } else {
            line("<empty/>")
        }
        line("</chats>")
    }

    private fun FrameContentBuilder.renderChat(state: TelegramAppViewState, chat: TelegramChat) {
        val messages = state.messagesView
        val unsafeTitle = chat.title ?: (chat.firstName + (chat.lastName?.let { " $it" } ?: ""))
        val title = unsafeTitle.safe()
        val attrString = """id="${chat.id}" title="$title""""
        val tag = chatTypeToTag(chat.type)
        line("<$tag $attrString>")
        renderChatMessages(state.oldLastReadMessageId, messages, state)
        line("</$tag>")
    }

    private fun ByteArray.getImageType(): LlmImageType? {
        if (this.size < 12) return null

        return when {
            this[0] == 0xFF.toByte() && this[1] == 0xD8.toByte() -> LlmImageType.JPEG
            this[0] == 0x89.toByte() && this[1] == 0x50.toByte() -> LlmImageType.PNG
            this[0] == 0x47.toByte() && this[1] == 0x49.toByte() -> LlmImageType.GIF
            this[0] == 0x52.toByte() && this[1] == 0x49.toByte() && // 'RIFF'
                    this[2] == 0x46.toByte() && this[3] == 0x46.toByte() &&
                    this[8] == 0x57.toByte() && this[9] == 0x45.toByte() &&
                    this[10] == 0x42.toByte() && this[11] == 0x50.toByte() -> LlmImageType.WEBP
            else -> null
        }
    }

    private fun chatTypeToTag(type: TelegramChat.Type): String {
        return when (type) {
            TelegramChat.Type.PRIVATE -> "private-chat"
            TelegramChat.Type.GROUP -> "group-chat"
            TelegramChat.Type.SUPERGROUP -> "supergroup-chat"
            else -> "chat"
        }
    }

    private fun getUserDisplayName(userId: Long): String {
        return getUserDisplayNameOrNull(userId) ?: "id $userId (could not load user name)"
    }

    private fun getUserDisplayNameOrNull(userId: Long?): String? {
        if (userId == null) {
            return null
        }
        val user = service.getUser(userId)
        return if (user != null) {
            getDisplayName(user)
        } else {
            null
        }
    }

    private fun getDisplayName(user: TelegramUser): String {
        return buildString {
            append(user.firstName)
            if (user.username != null) {
                append(" (@")
                append(user.username)
                append(")")
            }
        }
    }

    private fun String.safe(maxLength: Int = MAX_TEXT_LENGTH): String {
        return if (length > maxLength) {
            val length = length
            substring(0, maxLength).escapeHTML() +
                    "...<warning>text too long: $length/$maxLength, truncated</warning>"
        } else {
            escapeHTML()
        }
    }

    private fun FrameContentBuilder.renderChatListItem(chat: TelegramChat) {
        val tag = chatTypeToTag(chat.type)
        when (chat.type) {
            TelegramChat.Type.PRIVATE -> {
                val id = chat.id
                val displayName = getUserDisplayNameOrNull(id) ?: "(unknown user)"
                line("""<$tag chat-id="$id">$displayName</$tag>""")
            }
            TelegramChat.Type.GROUP, TelegramChat.Type.SUPERGROUP -> {
                val id = chat.id
                val title = chat.title?.safe() ?: "<no_title/>"
                line("""<$tag chat-id="$id">$title</$tag>""")
            }
            else -> {
                val id = chat.id
                val type = chat.type.name
                line("""<$tag chat-id="$id" type="$type"/>""")
            }
        }
    }

    private fun FrameContentBuilder.renderChatMessages(
        lastReadMessageId: Int?,
        messages: List<TelegramMessage>,
        state: TelegramAppViewState
    ) {
        var sentAt: ZonedDateTime? = null
        line("<messages>")
        for (message in messages) {
            if (sentAt == null || !sentAt.truncatedTo(MINUTES).isEqual(message.date.truncatedTo(MINUTES))) {
                sentAt = message.date
                line("<!-- ${sentAt.asPrettyString()} -->")
            }
            renderMessage(message, lastReadMessageId)
        }
        val laterMessagesRemaining = state.laterMessagesRemaining
        val laterNewMessagesRemaining = state.laterNewMessagesRemaining
        if (laterMessagesRemaining > 0) {
            line(buildString {
                append("<!-- ...$laterMessagesRemaining more messages")
                if (laterNewMessagesRemaining > 0) {
                    append(", $laterNewMessagesRemaining new")
                }
                append(" -->")
            })
        }
        if (messages.isEmpty()) {
            line("<empty/>")
        }
        line("</messages>")
    }

    private fun FrameContentBuilder.renderMessage(message: TelegramMessage, lastReadMessageId: Int? = null) {
        val fromId = message.fromId
        val from = getUserDisplayNameOrNull(message.fromId)
        val forwarded = message.forwardOrigin != null

        text("<message id=\"${message.messageId}\"")
        if (from != null) {
            text(""" from="$from"""")
        }
        if (message.editDate != null) {
            text(""" edited-at="${message.editDate.asPrettyString()}" """)
        }
        if (message.viaBot != null) {
            val value = if (message.viaBot.username != null) {
                "@" + message.viaBot.username
            } else {
                "id " + message.viaBot.id.toString()
            }
            text(""" via-bot="$value"""")
        }
        if (message.hasProtectedContent) {
            text(" forwarding-prohibited")
        }
        if (message.hasMediaSpoiler) {
            text(" has-media-spoiler")
        }
        if (message.isFromOffline) {
            text(" sent-automatically")
        }
        if (lastReadMessageId != null && message.messageId > lastReadMessageId) {
            text(" new")
        }
        line(">")
        if (forwarded) {
            text("<forwarded")
            renderMessageOriginAttributes(message.forwardOrigin)
            line(">")
        }

        if (message.caption != null && message.showCaptionAboveMedia) {
            renderCaption(message.caption, message.captionEntities)
        }
        message.text?.let {                             line(toHtml(it.safe(), message.entities))                   }
        message.sticker?.let {                          renderSticker(it)                                           }
        message.webAppData?.let {                       renderWebAppData()                                          }
        message.giveaway?.let {                         renderGiveaway(it)                                          }
        message.giveawayCreated?.let {                  renderGiveawayCreated(it)                                   }
        message.giveawayWinners?.let {                  renderGiveawayWinners()                                     }
        message.giveawayCompleted?.let {                renderGiveawayCompleted(it)                                 }
        message.newChatTitle?.let {                     renderNewChatTitle(it)                                      }
        message.leftChatMemberId?.let {                 renderLeftChatMember(it)                                    }
        message.videoChatScheduled?.let {               renderVideoChatScheduled(it)                                }
        message.videoChatEnded?.let {                   renderVideoChatEnded(it)                                    }
        message.videoChatParticipantsInvited?.let {     renderVideoChatParticipantsInvited(message.fromId ?: 0, it) }
        message.replyMarkup?.let {                      renderReplyMarkup(it)                                       }
        message.generalForumTopicUnhidden?.let {        line("<service>General forum topic hidden</service>")       }
        message.generalForumTopicHidden?.let {          line("<service>General forum topic hidden</service>")       }
        message.forumTopicCreated?.let {                renderForumTopicCreated(it)                                 }
        message.forumTopicEdited?.let {                 renderForumTopicEdited(it)                                  }
        message.forumTopicClosed?.let {                 line("<service>Forum topic closed</service>")               }
        message.forumTopicReopened?.let {               line("<service>Forum topic reopened</service>")             }
        message.chatBackgroundSet?.let {                line("<service>Chat background was changed</service>")      }
        message.replyToMessage?.let {                   renderReplyToMessage(it)                                    }
        message.contact?.let {                          renderContact(it)                                           }
        message.quote?.let {                            renderQuote(it)                                             }
        message.replyToStory?.let {                     renderReplyToStory(it)                                      }
        message.story?.let {                            renderStory(it)                                             }
        message.voice?.let {                            renderVoice(it)                                             }
        message.videoNote?.let {                        renderVideoNote(it)                                         }
        message.video?.let {                            renderVideo(it)                                             }
        message.animation?.let {                        renderAnimation(it)                                         }
        message.audio?.let {                            renderAudio(it)                                             }
        message.document?.let {                         renderDocument(it)                                          }
        message.poll?.let {                             renderPoll(it)                                              }
        message.venue?.let {                            renderVenue(it)                                             }
        message.location?.let {                         renderLocation(it)                                          }
        message.paidMediaInfo?.let {                    renderPaidMediaInfo(it)                                     }
        message.chatBoostAdded?.let {                   renderChatBoostAdded(fromId ?: -1, it)                      }
        message.dice?.let {                             renderDice(it)                                              }
        message.game?.let {                             renderGame(it)                                              }
        message.invoice?.let {                          renderInvoice(it)                                           }
        message.successfulPayment?.let {                renderSuccessfulPayment(it)                                 }
        message.refundedPayment?.let {                  renderRefundedPayment(it)                                   }
        message.usersShared?.let {                      renderUsersShared(fromId ?: -1, it)                         }
        message.chatShared?.let {                       renderChatShared(it)                                        }
        message.passportData?.let {                     renderPassportData()                                        }
        message.migrateToChatId?.let {                  renderMigrateToChatId(it)                                   }
        message.migrateFromChatId?.let {                renderMigrateFromChatId(it)                                 }
        message.messageAutoDeleteTimerChanged?.let {    renderMessageAutoDeleteTimerChanged(it)                     }
        message.pinnedMessage?.let {                    renderPinnedMessage(fromId ?: -1, it)                       }
        message.connectedWebsite?.let {                 renderConnectedWebsite(it)                                  }
        message.writeAccessAllowed?.let {               renderWriteAccessAllowed()                                  }
        message.proximityAlertTriggered?.let {          renderProximityAlertTriggered(it)                           }
        message.externalReplyInfo?.let {                renderExternalReplyInfo(it)                                 }
        message.authorSignature?.let {                  renderAuthorSignature(it)                                   }
        if (message.photo.isNotEmpty()) {
            renderImage(message.photo)
        }
        if (message.newChatPhoto.isNotEmpty()) {
            renderNewChatPhoto(message.newChatPhoto)
        }
        if (message.caption != null && !message.showCaptionAboveMedia) {
            renderCaption(message.caption, message.captionEntities)
        }
        if (message.deleteChatPhoto) {
            renderDeleteChatPhoto()
        }
        if (message.supergroupChatCreated) {
            renderSupergroupChatCreated()
        }
        if (message.channelChatCreated) {
            renderChannelChatCreated()
        }
        if (message.newChatMembers.isNotEmpty()) {
            renderNewChatMembers(message.newChatMembers)
        }
        if (message.groupChatCreated) {
            renderGroupChatCreated()
        }
        if (message.isTopicMessage) {
            line("<service>Message sent to forum topic.</service>")
        }
        if (message.isAutomaticForward) {
            line("<service>Automatically forwarded channel post</service>")
        }
        if (forwarded) {
            line("</forwarded>")
        }
        line("</message>")
    }

    private fun FrameContentBuilder.renderDice(dice: TelegramDice) {
        line("""<dice emoji="${dice.emoji}" value="${dice.value}"/>""")
    }

    private fun FrameContentBuilder.renderLocation(location: TelegramLocation) {
        line("<location>")
        line("Latitude: ${location.latitude}")
        line("Longitude: ${location.longitude}")
        location.horizontalAccuracy?.let { line("Horizontal accuracy: $it") }
        location.livePeriod?.let { line("Live period: $it seconds") }
        location.heading?.let { line("Heading: $it degrees") }
        location.proximityAlertRadius?.let { line("Proximity alert radius: $it meters") }
        line("</location>")
    }

    private fun FrameContentBuilder.renderVenue(venue: TelegramVenue) {
        line("<venue>")
        line("Location: ${venue.location.latitude}, ${venue.location.longitude}")
        line("Title: ${venue.title.safe()}")
        line("Address: ${venue.address.safe()}")
        venue.foursquareId?.let { line("Foursquare id: $it") }
        venue.foursquareType?.let { line("Foursquare type: $it") }
        venue.googlePlaceId?.let { line("Google place id: $it") }
        venue.googlePlaceType?.let { line("Google place type: $it") }
        line("</venue>")
    }

    private fun FrameContentBuilder.renderPoll(poll: TelegramPoll) {
        line("""<poll id="${poll.id}">""")
        line("<question>${toHtml(poll.question.safe(), poll.questionEntities)}</question>")
        line("<options>")
        for (option in poll.options) {
            line(
                """<option voters="${option.voterCount}">${toHtml(option.text.safe(), option.textEntities)}</option>"""
            )
        }
        line("</options>")
        line("</poll>")
    }

    private fun FrameContentBuilder.renderDocument(document: TelegramDocument) {
        line("""<document id="${document.fileUniqueId}">""")
        line("This Telegram App does not support documents yet.")
        document.fileName?.let { line("File name: " + it.safe()) }
        document.mimeType?.let { line("MIME type: " + it.safe()) }
        document.thumbnail?.let {
            line("<thumbnail>")
            renderImage(it)
            line("</thumbnail>")
        }
        line("</document>")
    }

    private fun FrameContentBuilder.renderGame(game: TelegramGame) {
        line("<game>")
        line("This message has a game, but your Telegram App does not support it yet.")
        line("Title: ${game.title.safe()}")
        line("Description: ${game.description.safe()}")
        if (game.photo.isNotEmpty()) {
            renderImage(game.photo)
        }
        game.animation?.let {
            renderAnimation(it)
        }
        line("</game>")
    }

    private fun FrameContentBuilder.renderAudio(audio: TelegramAudio) {
        line("""<audio id="${audio.fileUniqueId}" duration="${audio.duration}">""")
        line("This message has an audio, but your Telegram App does not support it yet.")
        audio.title?.let { line("Title: " + it.safe()) }
        audio.performer?.let { line("Performer: " + it.safe()) }
        audio.fileName?.let { line("File name: " + it.safe()) }
        audio.mimeType?.let { line("MIME type: " + it.safe()) }
        audio.thumbnail?.let {
            line("<thumbnail>")
            renderImage(it)
            line("</thumbnail>")
        }
        line("</audio>")
    }

    private fun FrameContentBuilder.renderReplyToStory(story: TelegramStory) {
        line("""<reply-to-story id="${story.id}" chat-id="${story.chatId}">""")
        line("This message is a reply to a story, but your Telegram App does not support it yet.")
        line("</reply-to-story>")
    }

    private fun FrameContentBuilder.renderRefundedPayment(payment: TelegramRefundedPayment) {
        line("<refunded-payment>")
        line("This message has a refunded payment, but your Telegram App does not support it yet.")
        line("Currency: ${payment.currency.safe()}")
        line("</refunded-payment>")
    }

    private fun FrameContentBuilder.renderSuccessfulPayment(payment: TelegramSuccessfulPayment) {
        line("<successful-payment>")
        line("This message has a successful payment, but your Telegram App does not support it yet.")
        line("Currency: ${payment.currency.safe()}")
        line("</successful-payment>")
    }

    private fun FrameContentBuilder.renderInvoice(invoice: TelegramInvoice) {
        line("<invoice>")
        line("This message has an invoice, but your Telegram App does not support it yet.")
        line("Title: ${invoice.title.safe()}")
        line("Description: ${invoice.description.safe()}")
        line("Currency: ${invoice.currency.safe()}")
        line("</invoice>")
    }

    private fun FrameContentBuilder.renderAnimation(animation: TelegramAnimation) {
        line("""<animation id="${animation.fileUniqueId}" duration="${animation.duration}">""")
        line("This message has an animation, but your Telegram App does not support it yet.")
        animation.thumbnail?.let {
            line("<thumbnail>")
            renderImage(it)
            line("</thumbnail>")
        }
        line("</animation>")
    }

    private fun FrameContentBuilder.renderVideo(video: TelegramVideo) {
        line("""<video id="${video.fileUniqueId}" duration="${video.duration}">""")
        line("This message has a video, but your Telegram App does not support it yet.")
        if (video.cover.isNotEmpty()) {
            line("<cover>")
            renderImage(video.cover)
            line("</cover>")
        }
        line("</video>")
    }

    private fun FrameContentBuilder.renderStory(story: TelegramStory) {
        line("""<story id="${story.id}" chat-id="${story.chatId}">""")
        line("This message has a story, but your Telegram App does not support it yet.")
        line("</story>")
    }

    private fun FrameContentBuilder.renderUsersShared(who: Long, usersShared: TelegramUsersShared) {
        line("<shared-users>")
        if (usersShared.users.size < MAX_LIST_ITEMS) {
            line("Users shared with the bot:")
            for (user in usersShared.users) {
                line("<user>")
                line("Id: ${user.userId}")
                user.firstName?.let { line("First name: " + it.safe()) }
                user.lastName?.let { line("Last name: " + it.safe()) }
                user.username?.let { line("Username: $it") }
                if (user.photo?.isNotEmpty() == true) {
                    line("Photo:")
                    renderImage(user.photo)
                }
                line("</user>")
            }
        } else {
            line("${usersShared.users.size} users shared with the bot.")
        }
        if (who != 0L) {
            line("Shared by ${getUserDisplayName(who)}")
        }
        line("</shared-users>")
    }

    private fun FrameContentBuilder.renderChatBoostAdded(who: Long, chatBoostAdded: TelegramChatBoostAdded) {
        line("<service>")
        line("User ${getUserDisplayName(who)} added ${chatBoostAdded.boostCount} boosts to the chat.")
        line("</service>")
    }

    private fun FrameContentBuilder.renderChatShared(chatShared: TelegramChatShared) {
        line("<shared-chat>")
        line("Id: ${chatShared.chatId}")
        chatShared.title?.let { line("Title: " + it.safe()) }
        chatShared.username?.let { line("Username: $it") }
        if (chatShared.photo?.isNotEmpty() == true) {
            line("Photo:")
            renderImage(chatShared.photo)
        }
        line("</shared-chat>")
    }

    private fun FrameContentBuilder.renderPaidMediaInfo(paidMediaInfo: TelegramPaidMediaInfo) {
        line("<paid-media price=\"${paidMediaInfo.starCount} Telegram Stars\">")
        for (item in paidMediaInfo.paidMedia) {
            when (item) {
                is TelegramPaidMedia.Preview -> {
                    line("""<preview width="${item.width}" height="${item.height}" duration="${item.duration}"/>""")
                }
                is TelegramPaidMedia.Photo -> renderImage(item.photo)
                is TelegramPaidMedia.Video -> renderVideo(item.video)
                else -> line("<unknown/>")
            }
        }
        line("</paid-media>")
    }

    private fun FrameContentBuilder.renderQuote(quote: TelegramTextQuote) {
        line("<quote>")
        line(toHtml(quote.text.safe(), quote.entities))
        line("</quote>")
    }

    private fun FrameContentBuilder.renderAuthorSignature(signature: String) {
        line("<signature>${signature.safe()}</signature>")
    }

    private fun FrameContentBuilder.renderCaption(text: String, entities: List<TelegramMessageEntity>) {
        line("<caption>${toHtml(text, entities)}</caption>")
    }

    private fun FrameContentBuilder.renderContact(contact: TelegramContact) {
        line("<shared-contact>")
        line("First name: " + contact.firstName.safe())
        contact.lastName?.let { line("Last name: " + it.safe()) }
        line("Phone number: " + contact.phoneNumber.safe())
        contact.userId?.let { line("Telegram user id: $it") }
        contact.vcard?.let { line("VCard: " + it.safe()) }
        line("</shared-contact>")
    }

    private fun FrameContentBuilder.renderReplyToMessage(message: TelegramMessage) {
        line("<reply-to>")
        renderMessage(message)
        line("</reply-to>")
    }

    private fun FrameContentBuilder.renderMessageOriginAttributes(origin: TelegramMessageOrigin) {
        when (origin) {
            is TelegramMessageOrigin.User -> {
                val fromName = getUserDisplayNameOrNull(origin.senderUserId) ?: "id ${origin.senderUserId}"
                val fromId = origin.senderUserId
                val sentAt = origin.date.asPrettyString()
                text(""" from-name="$fromName" from-id="$fromId" sent-at="$sentAt"""")
            }
            is TelegramMessageOrigin.HiddenUser -> {
                val fromName = origin.senderUserName.safe()
                val sentAt = origin.date.asPrettyString()
                text(""" from-hidden-user="$fromName" sent-at="$sentAt"""")
            }
            is TelegramMessageOrigin.Chat -> {
                val chatId = origin.senderChatId
                val sentAt = origin.date.asPrettyString()
                text(""" from-chat-id="$chatId" sent-at="$sentAt"""")
                origin.authorSignature?.let {
                    text(""" author-signature="${it.safe()}"""")
                }
            }
            is TelegramMessageOrigin.Channel -> {
                val chatId = origin.chatId
                val sentAt = origin.date.asPrettyString()
                val messageId = origin.messageId
                text(""" from-chat-id="$chatId" chat-message-id="$messageId" sent-at="$sentAt"""")
                origin.authorSignature?.let {
                    text(""" author-signature="${it.safe()}"""")
                }
            }
            is TelegramMessageOrigin.Unknown -> {
                val sentAt = origin.date.asPrettyString()
                text(""" sent-at="$sentAt"""")
            }
        }
    }

    private fun FrameContentBuilder.renderExternalReplyInfo(replyInfo: TelegramExternalReplyInfo) {
        text("<reply-to external")
        if (replyInfo.hasMediaSpoiler) {
            text(""" has-media-spoiler""")
        }
        renderMessageOriginAttributes(replyInfo.origin)
        line(">")
        replyInfo.animation?.let { renderAnimation(it) }
        replyInfo.audio?.let { renderAudio(it) }
        replyInfo.document?.let { renderDocument(it) }
        replyInfo.photo?.let { renderImage(it) }
        replyInfo.sticker?.let { renderSticker(it) }
        replyInfo.video?.let { renderVideo(it) }
        replyInfo.videoNote?.let { renderVideoNote(it) }
        replyInfo.voice?.let { renderVoice(it) }
        replyInfo.paidMedia?.let { renderPaidMediaInfo(it) }
        replyInfo.story?.let { renderStory(it) }
        replyInfo.contact?.let { renderContact(it) }
        replyInfo.dice?.let { renderDice(it) }
        replyInfo.game?.let { renderGame(it) }
        replyInfo.giveaway?.let { renderGiveaway(it) }
        replyInfo.giveawayWinners?.let { renderGiveawayWinners() }
        replyInfo.invoice?.let { renderInvoice(it) }
        replyInfo.location?.let { renderLocation(it) }
        replyInfo.poll?.let { renderPoll(it) }
        replyInfo.venue?.let { renderVenue(it) }
        line("</reply-to>")
    }

    private fun FrameContentBuilder.renderVideoNote(videoNote: TelegramVideoNote) {
        line("""<video-note id="${videoNote.fileUniqueId}" duration="${videoNote.duration}">""")
        line("This message has a video note, but your Telegram App does not support it yet.")
        videoNote.thumbnail?.let {
            line("<thumbnail>")
            renderImage(it)
            line("</thumbnail>")
        }
        line("</video-note>")
    }

    private fun FrameContentBuilder.renderVoice(voice: TelegramVoice) {
        line("""<voice id="${voice.fileUniqueId}" duration="${voice.duration}">""")
        line("This message has a voice message, but your Telegram App does not support it yet.")
        line("</voice>")
    }

    private fun FrameContentBuilder.renderForumTopicEdited(topicEdited: TelegramForumTopicEdited) {
        if (topicEdited.name != null) {
            line("<service>Forum topic name changed to ${topicEdited.name.safe()}</service>")
        }
        if (topicEdited.iconCustomEmojiId != null) {
            line("<service>Forum topic icon changed</service>")
        }
    }

    private fun FrameContentBuilder.renderForumTopicCreated(topicCreated: TelegramForumTopicCreated) {
        line("<service>New forum topic created: ${topicCreated.name.escapeHTML()}</service>")
    }

    private fun FrameContentBuilder.renderReplyMarkup(markup: TelegramInlineKeyboardMarkup) {
        line("<inline-keyboard>")
        line("Buttons attached to the message:")
        for (row in markup.inlineKeyboard) {
            line("<row>")
            for (button in row) {
                line("<button>${button.text.safe()}</button>")
            }
            line("</row>")
        }
        line("</inline-keyboard>")
    }

    private fun FrameContentBuilder.renderVideoChatParticipantsInvited(
        whoInvited: Long,
        invited: TelegramVideoChatParticipantsInvited
    ) {
        if (invited.users.isEmpty()) {
            return
        }
        line("<service>")
        if (invited.users.size < MAX_LIST_ITEMS) {
            line("Users invited to join video chat:")
            for (userId in invited.users) {
                line("- ${getUserDisplayName(userId)}")
            }
        } else {
            line("${invited.users.size} users invited to join video chat.")
        }
        if (whoInvited != 0L) {
            line("Invited by ${getUserDisplayName(whoInvited)}")
        }
        line("</service>")
    }

    private fun FrameContentBuilder.renderVideoChatEnded(videoChatEnded: TelegramVideoChatEnded) {
        line("<service>Video chat ended. Duration: ${videoChatEnded.duration} seconds</service>")
    }

    private fun FrameContentBuilder.renderVideoChatScheduled(videoChatScheduled: TelegramVideoChatScheduled) {
        line("<service>Video chat scheduled to start at ${videoChatScheduled.startDate.asPrettyString()}</service>")
    }

    private fun FrameContentBuilder.renderSticker(sticker: TelegramSticker) {
        line("""<sticker id="${sticker.fileUniqueId}" type="${sticker.type.name.lowercase()}" emoji="${sticker.emoji}" set-name="${sticker.setName}">""")
        sticker.thumbnail?.let {
            line("<thumbnail>")
            renderImage(listOf(it), withTag = false)
            line("</thumbnail>")
        }
        line("</sticker>")
    }

    private fun FrameContentBuilder.renderNewChatPhoto(sizes: List<TelegramPhotoSize>) {
        line("<service>")
        line("New chat photo:")
        renderImage(sizes)
        line("</service>")
    }

    private fun FrameContentBuilder.renderNewChatTitle(newChatTitle: String) {
        line("<service>New chat title: ${newChatTitle.safe()}</service>")
    }

    private fun FrameContentBuilder.renderNewChatMembers(newChatMembers: List<Long>) {
        line("<service>")
        line("New members joined:")
        for (userId in newChatMembers) {
            line("- ${getUserDisplayName(userId)}")
        }
        line("</service>")
    }

    private fun FrameContentBuilder.renderLeftChatMember(leftChatMemberId: Long) {
        line("<service>User ${getUserDisplayName(leftChatMemberId)}} left the chat.</service>")
    }

    private fun FrameContentBuilder.renderSupergroupChatCreated() {
        line("<service>Supergroup chat was created.</service>")
    }

    private fun FrameContentBuilder.renderChannelChatCreated() {
        line("<service>Channel chat was created.</service>")
    }

    private fun FrameContentBuilder.renderGroupChatCreated() {
        line("<service>Group chat was created.</service>")
    }

    private fun FrameContentBuilder.renderDeleteChatPhoto() {
        line("<service>Chat photo was deleted.</service>")
    }

    private fun FrameContentBuilder.renderGiveawayCompleted(giveawayCompleted: TelegramGiveawayCompleted) {
        line("<giveaway-completed>")
        line("A giveaway was completed.")
        line("Winners: ${giveawayCompleted.winnerCount}")
        if (giveawayCompleted.unclaimedPrizeCount != null) {
            line("Unclaimed prizes: ${giveawayCompleted.unclaimedPrizeCount}")
        }
        if (giveawayCompleted.isStarGiveaway) {
            line("Giveaway type: Telegram Stars")
        } else {
            line("Giveaway type: Telegram Premium")
        }
        if (giveawayCompleted.message != null) {
            line("<internal>Original giveaway message id: ${giveawayCompleted.message.messageId}</internal>")
        }
        line("</giveaway-completed>")
    }

    private fun FrameContentBuilder.renderGiveawayWinners() {
        line("<giveaway-winners>")
        line("Your Telegram App does not support rendering additional info yet.")
        line("</giveaway-winners>")
    }

    private fun FrameContentBuilder.renderGiveawayCreated(giveawayCreated: TelegramGiveawayCreated) {
        val stars = giveawayCreated.prizeStarCount
        text("<service>")
        text("A scheduled giveaway was created.")
        if (stars != null) {
            text(" The prize is $stars Telegram Stars.")
        }
        text("</service>")
    }

    private fun FrameContentBuilder.renderGiveaway(giveaway: TelegramGiveaway) {
        line("<giveaway>")
        line("Giveaway information:")
        line("- Winners will be selected on ${giveaway.winnersSelectionDate.asPrettyString()}")
        line("- Number of winners: ${giveaway.winnerCount}")
        if (giveaway.onlyNewMembers) {
            line("- Only new members are eligible to win")
        }
        if (giveaway.hasPublicWinners) {
            line("- The list of winners will be public")
        }
        if (giveaway.prizeDescription != null) {
            line("- Prize: " + giveaway.prizeDescription.safe())
        }
        if (giveaway.countryCodes != null) {
            line("- Eligible countries: ${giveaway.countryCodes.joinToString(", ")}")
        }
        if (giveaway.prizeStarCount != null) {
            line("- Prize: ${giveaway.prizeStarCount} Telegram Stars")
        }
        if (giveaway.premiumSubscriptionMonthCount != null) {
            line("- Prize: ${giveaway.premiumSubscriptionMonthCount} months of Telegram Premium")
        }
        if (giveaway.chats.isNotEmpty()) {
            line("- To participate, user must join the following chats:")
            for (chatId in giveaway.chats) {
                val chat = try {
                    runBlocking { service.getChat(chatId) }
                } catch (_: Exception) {
                    null
                }
                if (chat != null) {
                    line("- ${chat.title?.safe()} (id: $chatId)")
                } else {
                    line("- id: $chatId (could not load chat title)")
                }
            }
        }
        line("</giveaway>")
    }

    private fun FrameContentBuilder.renderPassportData() {
        line("<passport-data>")
        line("Passport data is not supported by your Telegram App.")
        line("</passport-data>")
    }

    private fun FrameContentBuilder.renderMigrateToChatId(migrateToChatId: Long) {
        line("<service>Chat was migrated to a supergroup. New chat id: $migrateToChatId</service>")
    }

    private fun FrameContentBuilder.renderMigrateFromChatId(migrateFromChatId: Long) {
        line("<service>Chat was migrated from a supergroup. Old chat id: $migrateFromChatId</service>")
    }

    private fun FrameContentBuilder.renderMessageAutoDeleteTimerChanged(
        timerChanged: TelegramMessageAutoDeleteTimerChanged
    ) {
        val seconds = timerChanged.messageAutoDeleteTime
        line("<service>Message auto-delete timer changed to $seconds seconds</service>")
    }

    private fun FrameContentBuilder.renderPinnedMessage(who: Long, pinnedMessage: TelegramMaybeInaccessibleMessage) {
        line("<service>")
        val user = getUserDisplayNameOrNull(who) ?: "(unknown user id $who)"
        line("$user pinned this message:")
        when (pinnedMessage) {
            is TelegramMessage -> renderMessage(pinnedMessage)
            is TelegramInaccessibleMessage ->
                line("<inaccessible-message id=${pinnedMessage.messageId} chat-id=${pinnedMessage.chatId}/>")
        }
        line("</service>")
    }

    private fun FrameContentBuilder.renderConnectedWebsite(connectedWebsite: String) {
        line("<connected-website>")
        line("Connected website: ${connectedWebsite.safe()}")
        line("</connected-website>")
    }

    private fun FrameContentBuilder.renderWriteAccessAllowed() {
        line("<service>")
        line("User allowed you to write in the chat.")
        line("</service>")
    }

    private fun FrameContentBuilder.renderProximityAlertTriggered(alert: TelegramProximityAlertTriggered) {
        val traveler = getUserDisplayNameOrNull(alert.traveler) ?: "(unknown user id ${alert.traveler})"
        val watcher = getUserDisplayNameOrNull(alert.watcher) ?: "(unknown user id ${alert.watcher})"
        line("<proximity-alert>$traveler is within ${alert.distance} meters of $watcher</proximity-alert>")
    }

    private fun FrameContentBuilder.renderWebAppData() {
        line("<web-app-data>")
        line("This message has web app data, but your Telegram App does not support it yet.")
        line("</web-app-data>")
    }

    private fun FrameContentBuilder.renderImage(image: TelegramPhotoSize) {
        renderImage(listOf(image))
    }

    private fun FrameContentBuilder.renderImage(sizes: List<TelegramPhotoSize>, withTag: Boolean = true) {
        val image = pickOptimalImage(sizes)
        if (withTag) {
            line("<image>")
        }
        if (image == null) {
            line("This image is too large. Requirements: ")
            line("- Size: <= ${MAX_MEDIA_SIZE_BYTES / 1024} KB")
            line("- Side length: <= $MAX_IMAGE_SIDE_LENGTH px")
            line("- Total pixels: <= $MAX_IMAGE_PIXELS px")
        } else {
            val fileId = image.fileId
            val file = runBlocking { service.getFileContent(fileId) }
            val imageType = file.getImageType()
            if (imageType == null) {
                line("This image type is not supported by your Telegram App. " +
                        "Supported types: ${LlmImageType.entries.joinToString(", ")}")
            } else {
                image(file, imageType)
            }
        }
        if (withTag) {
            line("</image>")
        }
    }

    private fun pickOptimalImage(sizes: List<TelegramPhotoSize>): TelegramPhotoSize? {
        if (sizes.isEmpty()) return null
        val sorted = sizes.sortedByDescending { it.width * it.height }
        for (size in sorted) {
            if (size.fileSize == null) {
                continue
            }
            if (size.fileSize <= MAX_MEDIA_SIZE_BYTES
                && size.width <= MAX_IMAGE_SIDE_LENGTH
                && size.height <= MAX_IMAGE_SIDE_LENGTH
                && size.width * size.height <= MAX_IMAGE_PIXELS) {
                return size
            }
        }
        return null
    }
}
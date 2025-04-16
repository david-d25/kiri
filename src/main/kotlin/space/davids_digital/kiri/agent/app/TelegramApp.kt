package space.davids_digital.kiri.agent.app

import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import space.davids_digital.kiri.agent.frame.DataFrame
import space.davids_digital.kiri.agent.frame.asPrettyString
import space.davids_digital.kiri.agent.frame.dsl.FrameContentBuilder
import space.davids_digital.kiri.agent.frame.dsl.dataFrameContent
import space.davids_digital.kiri.agent.tool.AgentToolMethod
import space.davids_digital.kiri.agent.tool.AgentToolNamespace
import space.davids_digital.kiri.integration.telegram.TelegramHtmlMapper.toHtml
import space.davids_digital.kiri.integration.telegram.TelegramService
import space.davids_digital.kiri.llm.LlmImageType
import space.davids_digital.kiri.model.telegram.*
import java.time.ZonedDateTime

private const val MAX_TEXT_LENGTH = 4000
private const val MAX_LIST_ITEMS = 50
private const val MAX_MEDIA_SIZE_BYTES = 5 * 1024 * 1024 // 5MB
private const val MAX_DISPLAYED_MESSAGES = 16 // Max number of messages to display
private const val TIMEOUT_MS = 5000 // Timeout for external calls in ms

@AgentToolNamespace("telegram")
class TelegramApp(
    private val service: TelegramService
): AgentApp("telegram") {
    private var openedChatId: Long? = null

    override fun render(): List<DataFrame.ContentPart> = dataFrameContent {
        if (openedChatId == null) {
            renderChatList()
        } else {
            renderChat(openedChatId!!)
        }
    }

    private fun getUserDisplayName(userId: Long): String {
        return getUserDisplayNameOrNull(userId) ?: "id $userId (could not load user name)"
    }

    private fun getUserDisplayNameOrNull(userId: Long?): String? {
        if (userId == null) {
            return null
        }
        val user = runBlocking { service.getUser(userId) }
        return if (user != null) {
            getDisplayName(user)
        } else {
            null
        }
    }

    private fun getDisplayName(user: TelegramUser): String {
        return "${user.firstName} ${user.lastName?.let { " $it" } ?: ""}".safe()
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

    private fun chatTypeToTag(type: TelegramChat.Type): String {
        return when (type) {
            TelegramChat.Type.PRIVATE -> "private-chat"
            TelegramChat.Type.GROUP -> "group-chat"
            TelegramChat.Type.SUPERGROUP -> "supergroup-chat"
            else -> "chat"
        }
    }

    @AgentToolMethod
    suspend fun openChat(id: Long): String {
        if (service.chatExists(id)) {
            openedChatId = id
        } else {
            return "Chat with id $id does not exist"
        }
        return "ok"
    }

    @AgentToolMethod(description = "Go back to chat list")
    fun closeChat() {
        openedChatId = null
    }

    @AgentToolMethod(description = "Send message")
    fun send(message: String): String {
        val chatId = openedChatId ?: return "Chat is not opened"
        runBlocking {
            service.sendText(chatId, message)
        }
        return "Message sent"
    }

    override fun getAvailableAgentToolMethods(): List<Function<*>> {
        val result = mutableListOf<Function<*>>(::openChat)
        if (openedChatId != null) {
            result.add(::send)
            result.add(::closeChat)
        }
        return result
    }

    // region rendering

    private fun FrameContentBuilder.renderChatList() {
        val chats = runBlocking { service.getChats() }
        line("<chats>")
        if (chats.isNotEmpty()) {
            for (i in 0 until chats.size.coerceAtMost(MAX_LIST_ITEMS)) {
                renderChatListItem(chats[i])
            }
        } else {
            line("<no_chats/>")
        }
        line("</chats>")
    }

    private fun FrameContentBuilder.renderChatListItem(chat: TelegramChat) {
        val tag = chatTypeToTag(chat.type)
        when (chat.type) {
            TelegramChat.Type.PRIVATE -> {
                val id = chat.id
                val displayName = getUserDisplayNameOrNull(id) ?: "(unknown user)"
                line("""<$tag id="$id">$displayName</$tag>""")
            }
            TelegramChat.Type.GROUP, TelegramChat.Type.SUPERGROUP -> {
                val id = chat.id
                val title = chat.title?.safe() ?: "<no_title/>"
                line("""<$tag id="$id">$title</$tag>""")
            }
            else -> {
                val id = chat.id
                val type = chat.type.name
                line("""<$tag id="$id" type="$type"/>""")
            }
        }
    }

    private fun FrameContentBuilder.renderChat(id: Long) {
        val chat: TelegramChat
        val messages: List<TelegramMessage>
        runBlocking {
            chat = service.getChat(id)
            messages = service.getChatMessages(id, 16)
        }
        val unsafeTitle = chat.title ?: (chat.firstName + (chat.lastName?.let { " $it" } ?: ""))
        val title = unsafeTitle.safe()
        val attrString = """id = "$id" title="$title""""
        val tag = chatTypeToTag(chat.type)
        line("<$tag $attrString>")
        renderChatMessages(messages)
        line("</$tag>")
    }

    private fun FrameContentBuilder.renderChatMessages(messages: List<TelegramMessage>) {
        var sentAt: ZonedDateTime? = null
        line("<messages>")
        for (message in messages) {
            if (sentAt != message.date) {
                sentAt = message.date
                line("<!-- ${sentAt.asPrettyString()} -->")
            }
            renderMessage(message)
        }
        if (messages.isEmpty()) {
            line("<empty/>")
        }
        line("</messages>")
    }

    private fun FrameContentBuilder.renderMessage(message: TelegramMessage) {
        val from = getUserDisplayNameOrNull(message.fromId)

        line("""<message from="$from">""") // todo 'from' not always needed
        // todo maybe 'message' is not needed?
        // todo editDate
        // todo viaBot
        // todo hasProtectedContent
        // todo isFromOffline
        // todo effectId
        // todo hasMediaSpoiler

        // todo entities
        message.text?.let {                         line(it.escapeHTML())                                       }
        message.sticker?.let {                      renderSticker(it)                                           }
        message.photo.firstOrNull()?.let {          renderImage(it)                                             }
        message.webAppData?.let {                   renderWebAppData(it)                                        }
        message.giveaway?.let {                     renderGiveaway(it)                                          }
        message.giveawayCreated?.let {              renderGiveawayCreated(it)                                   }
        message.giveawayWinners?.let {              renderGiveawayWinners(it)                                   }
        message.giveawayCompleted?.let {            renderGiveawayCompleted(it)                                 }
        message.newChatPhoto.firstOrNull()?.let {   renderNewChatPhoto(it)                                      }
        message.newChatTitle?.let {                 renderNewChatTitle(it)                                      }
        message.leftChatMemberId?.let {             renderLeftChatMember(it)                                    }
        message.videoChatScheduled?.let {           renderVideoChatScheduled(it)                                }
        message.videoChatEnded?.let {               renderVideoChatEnded(it)                                    }
        message.videoChatParticipantsInvited?.let { renderVideoChatParticipantsInvited(message.fromId ?: 0, it) }
        message.replyMarkup?.let {                  renderReplyMarkup(it)                                       }
        message.generalForumTopicUnhidden?.let {    line("<service>General forum topic hidden</service>")       }
        message.generalForumTopicHidden?.let {      line("<service>General forum topic hidden</service>")       }
        message.forumTopicCreated?.let {            renderForumTopicCreated(it)                                 }
        message.forumTopicEdited?.let {             renderForumTopicEdited(it)                                  }
        message.forumTopicClosed?.let {             line("<service>Forum topic closed</service>")               }
        message.forumTopicReopened?.let {           line("<service>Forum topic reopened</service>")             }
        message.chatBackgroundSet?.let {            line("<service>Chat background was changed</service>")      }
        message.replyToMessage?.let {               renderReplyToMessage(it)                                    }
        message.contact?.let {                      renderContact(it)                                           }
        message.quote?.let {                        renderQuote(it)                                             }
        message.replyToStory?.let {                 renderReplyToStory(it)                                      }
        message.story?.let {                        renderStory(it)                                             }
        message.video?.let {                        renderVideo(it)                                             }
        message.animation?.let {                    renderAnimation(it)                                         }
        message.audio?.let {                        renderAudio(it)                                             }
        message.document?.let {                     renderDocument(it)                                          }
        message.poll?.let {                         renderPoll(it)                                              }
        message.venue?.let {                        renderVenue(it)                                             }
        message.location?.let {                     renderLocation(it)                                          }
        message.paidMediaInfo?.let {                renderPaidMediaInfo(it)                                     }
        message.chatBoostAdded?.let {               renderChatBoostAdded(it)                                    }
        message.dice?.let {                         renderDice(it)                                              }
        message.game?.let {                         renderGame(it)                                              }
        message.invoice?.let {                      renderInvoice(it)                                           }
        message.successfulPayment?.let {            renderSuccessfulPayment(it)                                 }
        message.refundedPayment?.let {              renderRefundedPayment(it)                                   }
        message.usersShared?.let {                  renderUsersShared(it)                                       }
        message.chatShared?.let {                   renderChatShared(it)                                        }

        // todo caption
        // todo captionEntities
        // todo showCaptionAboveMedia

        // todo messageAutoDeleteTimerChanged
        // todo migrateToChatId
        // todo migrateFromChatId
        // todo pinnedMessage
        // todo connectedWebsite
        // todo writeAccessAllowed
        // todo passportData
        // todo proximityAlertTriggered
        // todo forwardOrigin
        // todo externalReplyInfo

        message.authorSignature?.let {              renderAuthorSignature(it)                                   }

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
        line("This message has a document, but your Telegram App does not support it yet.")
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
        game.photo.firstOrNull()?.let {
            renderImage(it)
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
        video.cover.firstOrNull()?.let {
            line("<cover>")
            renderImage(it)
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
                user.photo?.firstOrNull()?.let {
                    line("Photo:")
                    renderImage(it)
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
        chatShared.photo?.firstOrNull()?.let {
            line("Photo:")
            renderImage(it)
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
                is TelegramPaidMedia.Photo -> renderImage(item.photo.first())
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
        TODO()
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
        TODO()
    }

    private fun FrameContentBuilder.renderNewChatPhoto(newChatPhoto: TelegramPhotoSize) {
        line("<service>")
        line("New chat photo:")
        renderImage(newChatPhoto)
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

    private fun FrameContentBuilder.renderGiveawayWinners(giveawayWinners: TelegramGiveawayWinners) {
        line("<giveaway-winners>")
        line("Your Telegram App does not support rendering additional info yet.")
        line("</giveaway-winners>")
    }

    private fun FrameContentBuilder.renderGiveawayCreated(giveawayCreated: TelegramGiveawayCreated) {
        var stars = giveawayCreated.prizeStarCount
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

    private fun FrameContentBuilder.renderWebAppData(webAppData: TelegramWebAppData) {
        line("<web-app-data>")
        line("This message has web app data, but your Telegram App does not support it yet.")
        line("</web-app-data>")
    }

    private fun FrameContentBuilder.renderImage(image: TelegramPhotoSize) {
        line("<image>")
        val fileId = image.fileId
        val file = runBlocking { service.getFileContent(fileId) } // TODO check image size
        image(file, LlmImageType.JPEG) // TODO check mime type
        line("</image>")
    }

    // endregion
}
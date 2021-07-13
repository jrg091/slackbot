package com.xmartlabs.slackbot

import com.slack.api.bolt.App
import com.slack.api.bolt.jetty.SlackAppServer
import com.slack.api.model.event.AppHomeOpenedEvent
import com.slack.api.model.event.MemberJoinedChannelEvent
import com.xmartlabs.slackbot.handlers.AppHomeOpenedEventEventHandler
import com.xmartlabs.slackbot.handlers.ApprovalRequestViewSubmissionHandler
import com.xmartlabs.slackbot.handlers.CommandActionHandler
import com.xmartlabs.slackbot.handlers.CreateAnnouncementGlobalShortcutHandler
import com.xmartlabs.slackbot.handlers.MemberJoinedChannelEventHandler
import com.xmartlabs.slackbot.handlers.OnboardingSlashCommandHandler
import com.xmartlabs.slackbot.handlers.ProcessXlBotHelpCommandCommandHandler
import com.xmartlabs.slackbot.view.AnnouncementViewCreator

val PROTECTED_CHANNELS_NAMES = listOf("general", "announcements")

@Suppress("MagicNumber")
private val PORT = System.getenv("PORT")?.toIntOrNull() ?: 3000
val BOT_USER_ID = System.getenv("BOT_USER_ID") ?: "U025KD1C28K"
val XL_PASSWORD = System.getenv("XL_PASSWORD") ?: "*********"
val XL_GUEST_PASSWORD = System.getenv("XL_GUEST_PASSWORD") ?: "*********"
const val ACTION_VALUE_VISIBLE = "visible-in-channel"

val USERS_WITH_ADMIN_PRIVILEGES =
    System.getenv("USERS_WITH_ADMIN_PRIVILEGES")?.split(",") ?: emptyList()
val ANNOUNCEMENTS_ENABLED =
    System.getenv("ANNOUNCEMENTS_ENABLED")?.toBoolean() ?: false
val ANNOUNCEMENTS_PROTECTED_FEATURE =
    System.getenv("ANNOUNCEMENTS_PROTECTED_FEATURE")?.toBoolean() ?: true

val WELCOME_CHANNEL = System.getenv("WELCOME_CHANNEL_NAME") ?: "random"

fun main() {
    val app = App()
        .command("/xlbot", ProcessXlBotHelpCommandCommandHandler(visibleInChannel = false))
        .command("/xlbot-visible", ProcessXlBotHelpCommandCommandHandler(visibleInChannel = true))
        .command("/onboarding", OnboardingSlashCommandHandler())

    handleMemberJoinedChannelEvent(app)
    handleAppOpenedEvent(app)
    handleShortcut(app)
    val server = SlackAppServer(app, "/slack/events", PORT)
    server.start() // http://localhost:3000/slack/events
}

fun handleShortcut(app: App) {
    // Handles global shortcut requests
    app.globalShortcut(
        AnnouncementViewCreator.CREATE_ANNOUNCEMENT_MODAL_CALLBACK_ID,
        CreateAnnouncementGlobalShortcutHandler()
    )
    app.viewSubmission(
        AnnouncementViewCreator.CREATE_ANNOUNCEMENT_REQUEST_CALLBACK_ID,
        ApprovalRequestViewSubmissionHandler()
    )
    app.blockAction(AnnouncementViewCreator.userFilterModeInputActionId) { _, req -> req.ack() }
}

private fun handleAppOpenedEvent(app: App) {
    app.event(AppHomeOpenedEvent::class.java, AppHomeOpenedEventEventHandler())
    CommandManager.commands
        .forEach { command -> app.blockAction(command.buttonActionId, CommandActionHandler(command)) }
}

private fun handleMemberJoinedChannelEvent(app: App) =
    app.event(MemberJoinedChannelEvent::class.java, MemberJoinedChannelEventHandler())

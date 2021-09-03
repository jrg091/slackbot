package com.xmartlabs.slackbot.handlers

import com.slack.api.app_backend.events.payload.EventsApiPayload
import com.slack.api.bolt.context.builtin.EventContext
import com.slack.api.bolt.handler.BoltEventHandler
import com.slack.api.bolt.response.Response
import com.slack.api.model.event.MemberJoinedChannelEvent
import com.xmartlabs.slackbot.Config
import com.xmartlabs.slackbot.repositories.UserSlackRepository
import com.xmartlabs.slackbot.usecases.SendWelcomeMessageUseCase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MemberJoinedChannelEventHandler(
    private val sendWelcomeMessageUseCase: SendWelcomeMessageUseCase = SendWelcomeMessageUseCase(),
) : BoltEventHandler<MemberJoinedChannelEvent> {

    override fun apply(eventPayload: EventsApiPayload<MemberJoinedChannelEvent>, ctx: EventContext): Response {
        val event = eventPayload.event
        val user = UserSlackRepository.getUser(event.user)
        if (user?.isBot == true) {
            ctx.logger.info("Onboarding message ignored, ${user.name}:${event.user} is a bot user")
        } else {
            ctx.logger.info("New member added to ${event.channel} - ${event.user}")
            if (event.channel.equals(Config.WELCOME_CHANNEL_ID, true)) {
                GlobalScope.launch {
                    sendWelcomeMessageUseCase.execute(SendWelcomeMessageUseCase.Param(event.user))
                }
            }
        }
        return ctx.ack()
    }
}

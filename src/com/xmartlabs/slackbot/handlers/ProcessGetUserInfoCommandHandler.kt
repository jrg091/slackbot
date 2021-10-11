package com.xmartlabs.slackbot.handlers

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload
import com.slack.api.bolt.context.builtin.SlashCommandContext
import com.slack.api.bolt.handler.builtin.SlashCommandHandler
import com.slack.api.bolt.request.builtin.SlashCommandRequest
import com.slack.api.bolt.response.Response
import com.slack.api.model.User
import com.xmartlabs.slackbot.Config
import com.xmartlabs.slackbot.extensions.toPrettyString
import com.xmartlabs.slackbot.extensions.toRegularFormat
import com.xmartlabs.slackbot.logger
import com.xmartlabs.slackbot.model.BambooUser
import com.xmartlabs.slackbot.repositories.BambooUserRepository
import com.xmartlabs.slackbot.repositories.SlackUserRepository
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.Period

class ProcessGetUserInfoCommandHandler : SlashCommandHandler {
    override fun apply(req: SlashCommandRequest, ctx: SlashCommandContext): Response {
        ctx.logger.info("User profile requested by ${req.payload?.userName}. Requested profile: ${req.payload?.text}")

        val message = if (Config.PROFILE_COMMAND_ENABLED) {
            val result = kotlin.runCatching {
                val slackUser = requireNotNull(getSlackUserFromCommand(req.payload)) {
                    "User not found"
                }
                val bambooUser = runBlocking {
                    requireNotNull(BambooUserRepository.getUser(slackUser.profile.email)) {
                        "The required user has not a bamboo linked account"
                    }
                }
                generateMessage(slackUser, bambooUser)
            }
                .onFailure {
                    logger.error("Error processing user info, command: ${req.payload.text} ${it.toPrettyString()}")
                }
            when {
                result.isSuccess -> result.getOrThrow()
                result.exceptionOrNull() is IllegalArgumentException ->
                    "An error has occurred, ${result.exceptionOrNull()?.message}"
                else -> "An error has occurred"
            }
        } else {
            "The feature is not enabled."
        }
        return ctx.ack(message)
    }

    private fun generateMessage(slackUser: User, bambooUser: BambooUser) = buildString {
        append("*${slackUser.profile.realName}*")
        if (bambooUser.customFields.department.isNullOrEmpty()) {
            appendLine()
        } else {
            appendLine(" - ${bambooUser.customFields.department}")
        }
        appendPropertyLineIfExist("Email", bambooUser.workEmail)
        appendPropertyLineIfExist("Phone", bambooUser.customFields.mobilePhone)

        addDate("Birthday", bambooUser.customFields.birthday)
        addDate("Hire date", bambooUser.hireDate)

        val githubId = bambooUser.customFields.githubId
        if (!githubId.isNullOrEmpty()) {
            appendLine("*GitHub Account:* <https://github.com/$githubId | $githubId>")
        }
    }

    private fun StringBuilder.addDate(propertyName: String, date: LocalDate?) {
        if (date != null) {
            appendProperty(propertyName, date.toRegularFormat())
            val period: Period = Period.between(date, LocalDate.now())
            appendLine("  -  ${period.toPrettyString()}")
        }
    }

    private fun StringBuilder.appendPropertyLineIfExist(propertyName: String, value: Any?): Boolean =
        if (value != null) {
            appendLine("*$propertyName:* $value")
            true
        } else {
            false
        }

    private fun StringBuilder.appendProperty(propertyName: String, value: String) {
        append("*$propertyName:* $value")
    }

    private fun getSlackUserFromCommand(payload: SlashCommandPayload): User? = payload.text?.split(" ")
        .let {
            requireNotNull(it) {
                "Invalid command format, user not specified. Use `${payload.command} @someone`"
            }
            require(it.size == 1) {
                "Invalid command format, use /${payload.command} @someone"
            }
            require(it.first().startsWith("@")) {
                "User format is not valid"
            }
            it.first()
        }
        .let { SlackUserRepository.getUserFromName(it) }
}

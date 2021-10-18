package com.xmartlabs.slackbot

import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.context.Context
import com.slack.api.bolt.response.ResponseTypes
import java.util.Locale

sealed interface Command {
    val mainKey: String
    val title: String
    val description: String?
    val visible: Boolean
}
sealed interface CommandWithText : Command {
    val answerText: (command: String?, ctx: Context) -> String?
}
class TextCommand(
    vararg val keys: String,
    override val title: String = capitalizeFirstLetter(keys) ?: "",
    override val description: String? = null,
    override val visible: Boolean = true,
    val answerResponse: (command: String?, ctx: Context, visibleInChannel: Boolean) -> SlashCommandResponse =
        { text, ctx, visibleInChannel -> generateAnswerResponse(answerText(text, ctx), visibleInChannel) },
    override val answerText: (command: String?, ctx: Context) -> String,
) : CommandWithText {
    override val mainKey: String
        get() = keys.first()

    override fun equals(other: Any?) = (other is TextCommand) && keys.contentEquals(other.keys)

    override fun hashCode(): Int = keys.hashCode()
}

data class UrlActionCommand(
    override val mainKey: String,
    override val title: String,
    override val description: String?,
    override val visible: Boolean = true,
    val url: String,
    val answerResponse: (command: String?, ctx: Context, visibleInChannel: Boolean) -> SlashCommandResponse =
        { text, ctx, visibleInChannel -> generateAnswerResponse(answerText(text, ctx), visibleInChannel) },
    override val answerText: (command: String?, ctx: Context) -> String? = { _, _ -> null },
) : CommandWithText

data class ActionCommand(
    override val mainKey: String,
    override val title: String,
    override val description: String?,
    override val visible: Boolean,
) : Command

val Command.buttonActionId
    get() = "button-action-$mainKey"

private fun capitalizeFirstLetter(keys: Array<out String>) = keys.firstOrNull()
    ?.replaceFirstChar { key -> if (key.isLowerCase()) key.titlecase(Locale.getDefault()) else key.toString() }

private fun generateAnswerResponse(answer: String?, visibleInChannel: Boolean) =
    SlashCommandResponse.builder()
        .apply { if (!answer.isNullOrBlank()) text(answer) }
        .responseType(if (visibleInChannel) ResponseTypes.inChannel else ResponseTypes.ephemeral)
        .build()

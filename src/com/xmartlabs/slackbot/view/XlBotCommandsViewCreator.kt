package com.xmartlabs.slackbot.view

import com.slack.api.bolt.context.Context
import com.slack.api.model.kotlin_extension.block.dsl.LayoutBlockDsl
import com.slack.api.model.kotlin_extension.view.blocks
import com.slack.api.model.view.View
import com.slack.api.model.view.Views.view
import com.xmartlabs.slackbot.Command
import com.xmartlabs.slackbot.CommandWithText
import com.xmartlabs.slackbot.UrlActionCommand
import com.xmartlabs.slackbot.buttonActionId
import com.xmartlabs.slackbot.manager.CommandManager

object XlBotCommandsViewCreator {
    private const val NUMBER_OF_COLUMNS = 5

    fun createHomeView(
        ctx: Context,
        userId: String,
        isAdmin: Boolean,
        selectedCommand: CommandWithText? = null,
        commandsWithAssociatedAction: List<Command> = CommandManager.commands.filter(Command::visible),
    ): View = view { viewBuilder ->
        viewBuilder
            .type("home")
            .blocks {
                section {
                    markdownText(
                        """
                         Hi <@$userId>, I'm here to help you! :xl:
                         """.trimIndent()
                    )
                }
                divider()
                addCommands(commandsWithAssociatedAction, ctx)

                if (isAdmin) {
                    divider()
                    section {
                        markdownText("Admin commands:")
                    }
                    addCommands(CommandManager.adminCommands, ctx)
                }

                if (selectedCommand != null) {
                    val selectedCommandText = selectedCommand.answerText(null, ctx)
                    if (selectedCommandText != null) {
                        divider()
                        section {
                            markdownText(selectedCommandText)
                        }
                    }
                }
            }
    }

    private fun LayoutBlockDsl.addCommands(
        commandsWithAssociatedAction: List<Command>,
        ctx: Context,
    ) {
        commandsWithAssociatedAction
            .withIndex()
            .groupBy { it.index / NUMBER_OF_COLUMNS }
            .forEach { (_, rawCommands) ->
                actions {
                    rawCommands
                        .forEach { (_, command) ->
                            ctx.logger.debug("Adding button ${command.title}")
                            button {
                                if (command is UrlActionCommand) {
                                    url(command.url)
                                }
                                actionId(command.buttonActionId)
                                text(command.title, emoji = true)
                                value(command.mainKey)
                            }
                        }
                }
            }
    }
}

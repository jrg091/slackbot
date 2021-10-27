package com.xmartlabs.slackbot.manager

import com.slack.api.app_backend.slash_commands.payload.SlashCommandPayload
import com.slack.api.app_backend.slash_commands.response.SlashCommandResponse
import com.slack.api.bolt.context.Context
import com.slack.api.bolt.response.ResponseTypes
import com.slack.api.model.kotlin_extension.block.withBlocks
import com.xmartlabs.slackbot.ActionCommand
import com.xmartlabs.slackbot.Command
import com.xmartlabs.slackbot.Config
import com.xmartlabs.slackbot.TextCommand
import com.xmartlabs.slackbot.UrlActionCommand
import com.xmartlabs.slackbot.buttonActionId
import com.xmartlabs.slackbot.repositories.SlackUserRepository

@Suppress("MaxLineLength")
object CommandManager {
    val onboarding =
        TextCommand(
            "onboarding", "setup process",
            title = "Onboarding :wave: :xl:",
            description = "Do you know what you have to do when you onboard to :xl: ?"
        ) { payloadText, context ->
            val peoplePayloadText = getMembersFromCommandText(payloadText)
            MessageManager.getOngoardingMessage(Config.BOT_USER_ID, peoplePayloadText)
        }

    val commands: List<Command> = listOf(onboarding) + listOf(
        TextCommand(
            "anniversary",
            title = "Anniversary :tada: :birthday:",
            description = "What happens in my anniversary/birthday? :tada: :birthday:"
        ) { _, _ ->
            """

                *Anniversary* :tada: :birthday:

                • *Aniversarios y 3 meses en la empresa:*
                    • Cada mes contactamos a los que cumplen su aniversario y 3 meses en la empresa.
                    • Entre ustedes se organizan y eligen una forma de celebrar con todos (masitas, helado, pizza, etc) :masitas: :ice_cream: :pizza: :cookie:. Puede ser lo que ustedes quieran y cuándo quieran
                    • En los aniversarios la empresa les organiza una comida, regalitos y sorpresas por definir :eyes:

                • *Cumpleaños:*
                    • La empresa le regala al cumpleañero un desayuno :coffee: :croissant: :chocolate_bar:
                    • A fin de mes se hace un festejo para todos los cumpleañeros! La empresa compra tortas, bebidas y algo para picar. :birthday: :pizza:
                """.trimIndent()
        },
        UrlActionCommand(
            "calendar",
            title = "Calendars setup :calendar:",
            description = "Who is in PTO? When is the next lightning talk? :calendar:",
            url = "https://www.notion.so/xmartlabs/Setup-Calendars-URLs-40a4c5506a03429dbdccea169646a8a3"
        ) { _, _ ->
            """

                *Calendars* :calendar:
                    - <https://www.notion.so/xmartlabs/Setup-Calendars-URLs-40a4c5506a03429dbdccea169646a8a3 | Calendar Setup>
                """.trimIndent()
        },
        TextCommand(
            "lightning",
            title = "Lightning talks :flashlight:",
            description = "WTF is a lightning talk :zap:?"
        ) { _, _ ->
            """

                *Lightning talks* :flashlight::
                Hi! The following is useful information about our lightning talks :zap:, 30 min talks where someone exposes something interesting he/she found.

                • Lightning talks are added to the Eventos XL calendar :calendar: You will find the Zoom link meeting in there too :zoom:
                • Do you want to share something interesting with the team? <https://docs.google.com/forms/d/e/1FAIpQLSfvXceDAb9Eijkzkvi7-WUvvAHDh48NYEEIRDkTds6zZeXaUw/viewform | Please follow this link:> :memo:
                • We record every lightning talk so don’t care if you are not able to join that day. <https://www.notion.so/xmartlabs/Lightning-Talks-f21bb5e19823415fba3dcd2efc42d4ac | You can find all the recorded lightning talks here:>  :movie_camera:
                • Remember to track the talk in Toggl (Lightning talk -> Xmartlabs) :toggl_on:
                """.trimIndent()
        },
        TextCommand(
            "recycling",
            title = "Recycling :recycle:",
            description = "Recycling help! :recycle:"
        ) { _, _ ->
            """

                *Recycling* :recycle:

                • *Reciclables varios (verde):*
                    • Caja de leche (Tetrapak)
                    • Envoltorios de galletas (Todos)
                    • Doypack (Envases de mayonesa, etc)
                    • Tapas de vasos descartables (Morgan, McCafe, etc)

                • *Envases plásticos (Amarillo):*
                    • Botellas de plástico
                    • Cualquier plástico que tenga el símbolo :recycle: 1 - PET o :recycle: 2 - PEAD
                • *Papel y cartón (Azul):*
                    • Cajas de te, edulcorante
                    • Papel limpio y seco
                • *Compostable (Rojo):*
                    • Yerba
                    • Café
                    • Sobrecitos de té (con la bolsita y cuerda)
                    • Cascaras de frutas y verduras
                    • Restos de comida
                    • Servilletas de papel usadas
                • *Mezclados (Negro)*:
                    • Paquetes de yerba
                    • Paquetes de café
                    • Boletos y tickets (papel térmico)
                    • Vasos descartables de papel (Morgan, McCafe, etc)


                *Recordá que todo lo reciclable tiene que estar limpio, seco y compactado!* :recycle:
                """.trimIndent()
        },
        TextCommand(
            "toggl",
            title = "Toggl :toggl_on:",
            description = "Where should I track this? :toggl_on:"
        ) { _, _ ->
            """

                *Toggl* :toggl_on:

                • ¿Entrevistas? _Seleccion y entrevistas -> Xmartlabs_
                • ¿Reviews? _Team Reviews -> Xmartlabs_
                • ¿Lightning talks? _Lightning talk -> Xmartlabs_
                • ¿1:1 con PM? _En tu proyecto._
                • ¿1:1 con tu TL? _En tu equipo, Team X -> Xmartlabs._
                • ¿Iniciativas? _Consulta a tu TL si la iniciativa en la que estás trabajando tiene un proyecto específico ya creado. Si es así, va en ese proyecto. En caso de que no, va dentro de tu equipo._
                • ¿Code review o soporte a un compañero de otro proyecto? _Se trackea dentro de ese proyecto. En caso de que no lo veas en Toggl, comunicate con el PM de ese proyecto para que te agregue al team :)_

                En caso de tener alguna duda, consultá al equipo de operaciones o RRHH.

                """.trimIndent()
        },
        TextCommand(
            "time off", "vacations",
            title = " Time Off :beach_with_umbrella:",
            description = "How should I request my days off? :beach_with_umbrella:"
        ) { _, _ ->
            """

                *Time Off* :beach_with_umbrella:

                The vacation policy can be checked in <https://www.notion.so/xmartlabs/Vacation-Policy-c528945eaa5741259b686442233c8021 | Notion>. It has useful information that you should check before requesting time off.
                
                In order to do so, <https://xmartlabs.bamboohr.com/time_off/requests/create | please follow the steps in this form>. To expedite the process,  validate your request with your manager/PM before submitting it. Make sure you plan ahead of time since we always need to validate your time off with the client.
                """.trimIndent()
        },
        TextCommand(
            "wifi",
            title = "Wifi pass :signal_strength:",
            description = "Do you know Xmartlabs' office WIFI password? :signal_strength: :key:"
        ) { _, _ ->
            """
                *Wifi pass* :signal_strength: :key:
                Internal: `${Config.XL_PASSWORD}`, Guests: `${Config.XL_GUEST_PASSWORD}`
            """.trimIndent()
        },
        TextCommand(
            "frogs",
            title = "Frogs information :frog:",
            description = "Give me more information about Frogs :pray:"
        ) { _, _ ->
            """
               Hey, here there is useful information about Frogs :frog: 

               • If you have no idea what is Frogs and how it works, check this <https://www.notion.so/xmartlabs/Frogs-2668aa7ab5d64f0d99f16d4233e6d5fa | link>.
               • We have new website! Check it out at <https://frogs.xmartlabs.com/ | frogs.xmartlabs.com>
               • Do you have any feedback about it? Please fill in <https://docs.google.com/forms/d/1XjSTctVWQkcD7M3tx65L-2Qf0BmSg03l13agDmpfOfE/edit | this form>.

               If there is anything else you want to ask, contact our amazing chef Enzo :cook:
            """.trimIndent()
        },
        UrlActionCommand(
            "peoplearesaying",
            title = "People are saying :speaking_head_in_silhouette:",
            description = "Share what you want, what happens in the corridors and upload material! (anonymously) " +
                    ":loudspeaker:",
            url = "https://docs.google.com/forms/d/e/1FAIpQLSetdH0R3E5eopGQ3WRS_ukqQm67bU4BbHD0LP6bNU0BF2YBJw/viewform"
        ) { _, _ ->
            """

                *People are saying* :speaking_head_in_silhouette:
                    - <https://docs.google.com/forms/d/e/1FAIpQLSetdH0R3E5eopGQ3WRS_ukqQm67bU4BbHD0LP6bNU0BF2YBJw/viewform | Form Url>
                """.trimIndent()
        },
        UrlActionCommand(
            "shopping",
            title = "Shopping requests :shopping_trolley:",
            description = "If you need something fill the form! :memo:",
            url = "https://forms.gle/z84j7JSw2YAPD3fv7"
        ) { _, _ ->
            """
                *Shopping requests* :shopping_trolley:
                    - <https://forms.gle/z84j7JSw2YAPD3fv7 | Form Url>
                """.trimIndent()
        },
        TextCommand(
            "slack", "guidelines",
            title = "XmartLabs' Slack Guidelines :slack:",
            description = "Which are the XmartLabs' Slack guidelines? :slack:"
        ) { _, _ ->
            """
               <https://www.notion.so/xmartlabs/XmartLabs-guide-to-Slack-88a2386a9a6943c0bb2c9d44b316a4ac | XmartLabs' guide to Slack> :slack: :xl:

               *Slack* is a communicative platform which enables you to have a simple instant messaging system, having all the collaboration features you need in one place.
               It provides direct message, public and private shared, and video calls within the platform.
               <https://www.notion.so/xmartlabs/XmartLabs-guide-to-Slack-88a2386a9a6943c0bb2c9d44b316a4ac | XmartLabs' Slack Guidelines> is a quick guide on how to effectively use Slack for both internal and external communications.

            """.trimIndent()
        },
        TextCommand(
            "feedback",
            title = "Share XlBot feedback! :writing_hand:",
            description = "How can I share XlBot feedback? :robot_face: :writing_hand:"
        ) { _, _ ->
            """
               Hey, thanks for sharing your feedback! :muscle:
               If you want to propose a new feature you can post a message in <#${Config.BOT_FEEDBACK_CHANNEL_ID}>, open a <https://github.com/xmartlabs/slackbot/discussions | GitHub Discussion> or an <https://github.com/xmartlabs/slackbot/issues | issue>.

               All contributions are welcome! :github:
            """.trimIndent()
        },
        UrlActionCommand(
            "after",
            title = "Reserve a house for an after! :tada:",
            description = "If you are planning an team bonding activity, you can reserve a house! :tada:",
            url = "https://docs.google.com/forms/d/e/1FAIpQLSeBGDnfbiBSTMMYm27X2K6kch2G21HE3zSgedbzlJwsK4GW0A/viewform"
        ) { _, _ ->
            """
                *House reservation* :house:
                    - <https://docs.google.com/forms/d/e/1FAIpQLSeBGDnfbiBSTMMYm27X2K6kch2G21HE3zSgedbzlJwsK4GW0A/viewform | Solicitud de Instalaciones de XL>
            """.trimIndent()
        },
    )

    private val default = TextCommand(
        title = "Help Command",
        answerResponse = { _, _, visibleInChannel ->
            SlashCommandResponse.builder()
                .blocks(withBlocks {
                    section {
                        markdownText(
                            "Hi :wave:! Check XL useful <@${Config.BOT_USER_ID}> commands! :slack:"
                        )
                    }

                    commands
                        .filter(Command::visible)
                        .forEach { command ->
                            section {
                                button {
                                    actionId(command.buttonActionId)
                                    text(command.title, emoji = true)
                                    if (visibleInChannel) {
                                        value(Config.ACTION_VALUE_VISIBLE)
                                    }
                                }
                                if (!command.description.isNullOrBlank()) {
                                    markdownText("• ${command.description}", true)
                                }
                            }
                        }
                })
                .responseType(ResponseTypes.ephemeral) // Force it
                .build()
        },
        answerText = { _, _ ->
            val options = commands
                .filter(Command::visible)
                .joinToString(" \n") { command ->
                    "• *${command.title}*: $${command.description}"
                }
            "\nHi :wave:! Check XL useful <@${Config.BOT_USER_ID}> commands! :slack:\n\n$options"
        },
    )

    val toggleReportCommand = ActionCommand(
        mainKey = "toggle-report",
        title = "Generates toggl report :toggl_on:",
        description = "Generates toggl report",
        visible = false
    )
    val announcementCommand = ActionCommand(
        mainKey = "announcement",
        title = "Create announcement :loudspeaker:",
        description = "Create announcement :loudspeaker:",
        visible = false
    )
    val adminCommands: List<Command> = listOf(announcementCommand, toggleReportCommand)

    init {
        require(
            commands
                .flatMap { if (it is TextCommand) it.keys.asList() else listOf(it.mainKey) }
                .groupBy { it.lowercase() }
                .values
                .map { it.size }
                .maxOrNull() == 1
        ) {
            "Duplicate commands are not allowed"
        }
    }

    private fun String.sanitizeKey() = replace(" ", "_")
        .lowercase()

    fun processTextCommand(
        ctx: Context,
        payload: SlashCommandPayload?,
        visibleInChannel: Boolean,
    ): SlashCommandResponse? = (
            payload?.text?.let { userKey ->
                commands
                    .firstOrNull { command ->
                        when (command) {
                            is TextCommand -> command.keys.any { commandKey ->
                                userKey.sanitizeKey().contains(commandKey.sanitizeKey())
                            }
                            is ActionCommand -> userKey.sanitizeKey().contains(command.mainKey.sanitizeKey())
                            is UrlActionCommand -> userKey.sanitizeKey().contains(command.mainKey.sanitizeKey())
                        }
                    }
            } ?: default)
        .let { command ->
            when (command) {
                is ActionCommand -> null
                is TextCommand -> command.answerResponse(payload?.text, ctx, visibleInChannel)
                is UrlActionCommand -> command.answerResponse(payload?.text, ctx, visibleInChannel)
            }
        }
}

private fun getMembersFromCommandText(peopleCommandText: String?): List<String>? =
    peopleCommandText
        ?.split("@")
        ?.map(String::trim)
        ?.filterNot(String::isBlank)
        ?.let { userName -> SlackUserRepository.getUsersFromNames(userName).map { it.id } }

package com.xmartlabs.slackbot.manager

import com.xmartlabs.slackbot.extensions.isLastWorkingDayOfTheMonth
import com.xmartlabs.slackbot.extensions.toPrettyString
import com.xmartlabs.slackbot.model.FullTogglUserEntryReport
import com.xmartlabs.slackbot.model.ReportErrorType
import org.atteo.evo.inflector.English
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Suppress("MaxLineLength")
object MessageManager {
    @Suppress("unused")
    val LOCAL_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        .withLocale(Locale.US)
    val LOCAL_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd LLLL")

    fun getOngoardingMessage(xlBotUserId: String, newMembersIds: List<String>?): String {
        val joinedIds = newMembersIds?.joinToString(" ") { "<@$it>" }
        val peopleWithSpace = if (joinedIds.isNullOrBlank()) "" else "$joinedIds "
        return """

                :agite-izq: *Team, say hi to our new team ${English.plural("member", newMembersIds?.size ?: 0)}!* :agite:
                 ${peopleWithSpace}Hi, welcome to Xmartlabs! :wave: :xl: We are very happy for having you on board, our entire team is here to help you with whatever you need :muscle:

                To know what your next steps are, <https://www.notion.so/xmartlabs/Onboarding-c092b413380341948aabffa17bd85647 | go to the onboarding page> :notion-logo:

                Additionally, please: 
                • Add a Profile picture to Slack & Bamboo :camera: :star: 
                • <https://www.notion.so/xmartlabs/Setup-Calendars-URLs-40a4c5506a03429dbdccea169646a8a3 | Add calendar URLs :calendar:>
                • Shot :absenta: (in next XL after)
                
                Regards <@$xlBotUserId>
                """.trimIndent()
    }

    fun getInvalidTogglEntriesMessage(
        userId: String,
        untrackedTime: Duration,
        from: LocalDate,
        to: LocalDate,
        report: FullTogglUserEntryReport,
    ) = if (to.isLastWorkingDayOfTheMonth()) {
        getInvalidTogglEntriesMonthlyMessage(userId, untrackedTime, from, to, report)
    } else {
        getInvalidTogglEntriesWeeklyMessage(userId, untrackedTime, from, to, report)
    }

    private fun getReportUrlMessage(report: FullTogglUserEntryReport): String {
        val templateMessage = "Please, take a look at <%s | yours entries>."
        return when (report.reportErrorType) {
            ReportErrorType.ENTRIES_WITH_INVALID_PROJECT -> templateMessage.format(report.reportInvalidProjectsUrl)
            ReportErrorType.ENTRIES_WITH_INVALID_DESCRIPTION ->
                templateMessage.format(report.reportInvalidEntryDescriptionsUrl)
            ReportErrorType.ENTRIES_WITH_INVALID_PROJECT_AND_DESCRIPTION ->
                "Please, take a look at your " +
                        "<${report.reportInvalidEntryDescriptionsUrl} | entries without description> and your " +
                        "<${report.reportInvalidProjectsUrl} | entries without project>."
            null -> templateMessage.format(report.reportUrl)
        }
    }

    private fun getInvalidTogglEntriesWeeklyMessage(
        userId: String,
        untrackedTime: Duration,
        from: LocalDate,
        to: LocalDate,
        report: FullTogglUserEntryReport,
    ) = """
        Hi <@$userId>, you have *${report.wrongFormatEntries.size} tracked toggl ${English.plural("entry", report.wrongFormatEntries.size)} (${untrackedTime.toPrettyString()})* with an invalid format (no project assigned or an empty description).
        Report generated from ${from.format(LOCAL_DATE_FORMATTER)} to ${to.format(LOCAL_DATE_FORMATTER)}.
        It's important to keep your toggl up to date.
        ${getReportUrlMessage(report)}
        
        If you have any questions, you can use `/xlbot toggl` slack command.
    """.trimIndent()

    private fun getInvalidTogglEntriesMonthlyMessage(
        userId: String,
        untrackedTime: Duration,
        from: LocalDate,
        to: LocalDate,
        report: FullTogglUserEntryReport,
    ) = """
        :warning::warning::warning:
        Hi <@$userId>, *we're closing the month* and you have *${report.wrongFormatEntries.size} tracked toggl ${English.plural("entry", report.wrongFormatEntries.size)} (${untrackedTime.toPrettyString()})* with an invalid format (no project assigned or an empty description).
        Report generated from ${from.format(LOCAL_DATE_FORMATTER)} to ${to.format(LOCAL_DATE_FORMATTER)}.
        It's important to keep your toggl up to date.
        ${getReportUrlMessage(report)}
        
        If you have any questions, you can use `/xlbot toggl` slack command.
    """.trimIndent()
}

package com.xmartlabs.slackbot.usecases

import com.xmartlabs.slackbot.Config
import com.xmartlabs.slackbot.extensions.TimeHelper
import com.xmartlabs.slackbot.extensions.isLastWorkingDayOfTheMonth
import com.xmartlabs.slackbot.extensions.toLastWorkingDayOfTheMonth
import com.xmartlabs.slackbot.logger
import com.xmartlabs.slackbot.manager.ReportPeriodType
import com.xmartlabs.slackbot.manager.WorkTimeReportManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

@OptIn(ExperimentalTime::class)
class RemindInvalidEntryTogglUseCase : CoroutineUseCase<RemindInvalidEntryTogglUseCase.Param> {
    object Param

    override suspend fun execute(param: Param) = withContext(Dispatchers.IO) {
        while (true) {
            val nextReview = durationToNextReminder()
            logger.info("Toogle automatically report will be checked in ${nextReview.toKotlinDuration()})")
            delay(nextReview.toKotlinDuration())

            val to = calculateTo()
            val from = calculateFrom(to)
            WorkTimeReportManager.sendUntrackedTimeReport(LocalDate.now().reportType, from, to)
        }
    }

    private fun calculateTo(): LocalDate =
        if (LocalDate.now().reportType == ReportPeriodType.MONTHLY) {
            LocalDate.now()
        } else {
            LocalDate.now()
                .let {
                    if (Config.TOGGLE_WEEKLY_REPORT_EXCLUDE_CURRENT_WEEK) {
                        it.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
                    } else {
                        it
                    }
                }
        }

    private fun calculateFrom(to: LocalDate): LocalDate =
        if (LocalDate.now().reportType == ReportPeriodType.MONTHLY) {
            LocalDate.now()
                .withDayOfMonth(1)
        } else {
            to.minusDays((Config.TOGGL_WEEKLY_REPORT_DAYS_TO_CHECK.toLong() - 1).coerceAtLeast(0))
        }

    private fun durationToNextReminder(): Duration =
        listOf(durationToNextMonthReminder(), durationToNextWeeklyReminder())
            .minByOrNull { it.toMillis() }!!

    private fun durationToNextMonthReminder(): Duration {
        val now = LocalDateTime.now()
        val sendTime = Config.TOGGL_TIME_TO_REMIND
        return when {
            now.toLocalDate().isLastWorkingDayOfTheMonth() && now.toLocalTime() < sendTime ->
                Duration.between(now.toLocalTime(), sendTime)
            now.toLocalDate().isLastWorkingDayOfTheMonth() -> {
                val nextDate = now
                    .minusDays(1)
                    .plusMonths(1)
                    .toLocalDate()
                    .toLastWorkingDayOfTheMonth()
                Duration.between(now, nextDate.atTime(sendTime))
            }
            else -> Duration.between(now, now.toLocalDate().toLastWorkingDayOfTheMonth().atTime(sendTime))
        }
    }

    private fun durationToNextWeeklyReminder(): Duration =
        TimeHelper.durationToNextAvailableDate(Config.TOGGL_TIME_TO_REMIND, Config.TOGGL_DAYS_TO_REMIND)

    private val LocalDate.reportType: ReportPeriodType
        get() = if (isLastWorkingDayOfTheMonth()) ReportPeriodType.MONTHLY else ReportPeriodType.WEEKLY
}

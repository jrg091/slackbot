package com.xmartlabs.slackbot.repositories

import com.xmartlabs.slackbot.Config
import com.xmartlabs.slackbot.data.sources.TogglReportsRemoteSource
import com.xmartlabs.slackbot.data.sources.UserTogglRemoteSource
import com.xmartlabs.slackbot.model.FullTogglUserEntryReport
import com.xmartlabs.slackbot.model.SimpleTogglUserEntryReport
import com.xmartlabs.slackbot.model.ToggleSummarySubGroupType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

object TogglReportRepository {
    private val logger = LoggerFactory.getLogger(TogglReportRepository::class.java)

    suspend fun generateFullReport(
        since: LocalDateTime,
        until: LocalDateTime,
        excludeActiveEntries: Boolean = Config.TOGGL_EXCLUDE_ACTIVE_ENTRIES,
    ): List<FullTogglUserEntryReport> = coroutineScope {
        kotlin.runCatching {
            val invalidEntries = async {
                TogglReportsRemoteSource.getInvalidTasks(since, until)
            }
            val togglUsers = UserTogglRemoteSource.getTogglUsers()
                .associateBy { it.userId }
            val userWorkTime = getUserWorkTime(since.toLocalDate(), until.toLocalDate())
            invalidEntries.await()
                .filter { if (excludeActiveEntries) it.end != null else true }
                .groupBy { it.userId }
                .toList()
                .map { (id, entries) ->
                    val user = togglUsers[id]!!
                    FullTogglUserEntryReport(
                        togglUser = user,
                        workTime = userWorkTime.getOrDefault(user.userId, Duration.ZERO),
                        wrongFormatEntries = entries,
                        reportUrl = TogglReportsRemoteSource.generateReportUrl(
                            user, since.toLocalDate(), until.toLocalDate()
                        ),
                        reportInvalidEntryDescriptionsUrl = TogglReportsRemoteSource.generateReportUrl(
                            user, since.toLocalDate(), until.toLocalDate(), filterInvalidDescriptions = true
                        ),
                        reportInvalidProjectsUrl = TogglReportsRemoteSource.generateReportUrl(
                            user, since.toLocalDate(), until.toLocalDate(), filterInvalidProjects = true
                        ),
                    )
                }
        }.onFailure { logger.error("Error fetching toggl data", it) }
            .getOrNull() ?: listOf()
    }

    suspend fun generateSimpleReport(
        since: LocalDate,
        until: LocalDate,
    ): List<SimpleTogglUserEntryReport> = coroutineScope {
        kotlin.runCatching {
            val togglUsers = async { UserTogglRemoteSource.getTogglUsers() }
            val userTime: Map<Long, Duration> = getUserWorkTime(since, until)
            togglUsers.await()
                .map { user ->
                    SimpleTogglUserEntryReport(
                        togglUser = user,
                        workTime = userTime.getOrDefault(user.userId, Duration.ZERO),
                        reportUrl = TogglReportsRemoteSource.generateReportUrl(user, since, until)
                    )
                }
        }.onFailure { logger.error("Error fetching toggl data", it) }
            .getOrNull() ?: listOf()
    }

    private suspend fun getUserWorkTime(
        since: LocalDate,
        until: LocalDate,
    ): Map<Long, Duration> = UserTogglRemoteSource.getTogglUserSummary(since, until, ToggleSummarySubGroupType.PROJECT)
        .groups
        .associateBy { it.id.toLong() }
        .mapValues { (_, entries) -> Duration.ofSeconds(entries.subGroups.sumOf { it.seconds ?: 0 }) }
}

package com.xmartlabs.slackbot.data.sources

import com.xmartlabs.slackbot.Config
import com.xmartlabs.slackbot.extensions.toTogglApiFormat
import com.xmartlabs.slackbot.model.TogglUser
import io.rocketbase.toggl.api.model.TimeEntry
import io.rocketbase.toggl.api.util.FetchAllDetailed
import java.time.LocalDate
import java.time.LocalDateTime

object TogglReportsRemoteSource {
    fun generateReportUrl(
        togglUser: TogglUser,
        from: LocalDate,
        to: LocalDate,
        filterInvalidProjects: Boolean = false,
        filterInvalidDescriptions: Boolean = false,
    ) = "https://track.toggl.com/reports/detailed/${Config.TOGGL_XL_WORKSPACE}" +
            (if (filterInvalidDescriptions) "/description/0" else "") +
            "/from/${from.toTogglApiFormat()}" +
            (if (filterInvalidProjects) "/projects/0" else "") +
            "/to/${to.toTogglApiFormat()}" +
            "/users/${togglUser.userId}"

    fun getTasks(since: LocalDateTime, until: LocalDateTime): List<TimeEntry> {
        val userWithoutProjects = TogglApi.togglReportApi.detailed()
            .since(since)
            .until(until)
        return FetchAllDetailed.getAll(userWithoutProjects)
    }

    fun getInvalidTasks(since: LocalDateTime, until: LocalDateTime): List<TimeEntry> {
        val entriesWithoutDescription = TogglApi.togglReportApi.detailed()
            .since(since)
            .until(until)
            .withoutDescription(true)
        val entriesWithoutProject = TogglApi.togglReportApi.detailed()
            .since(since)
            .until(until)
            .projectIds("0")
        return (FetchAllDetailed.getAll(entriesWithoutDescription) + FetchAllDetailed.getAll(entriesWithoutProject))
            .distinct()
            .sortedBy { it.start }
    }
}

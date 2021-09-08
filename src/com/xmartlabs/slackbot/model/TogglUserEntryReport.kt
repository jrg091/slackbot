package com.xmartlabs.slackbot.model

import io.rocketbase.toggl.api.model.TimeEntry
import java.time.Duration

sealed interface TogglUserEntryReport {
    val togglUser: TogglUser
    val reportUrl: String
    val workTime: Duration
}

data class FullTogglUserEntryReport(
    override val togglUser: TogglUser,
    override val workTime: Duration,
    override val reportUrl: String,
    val wrongFormatEntries: List<TimeEntry>,
    val reportInvalidProjectsUrl: String,
    val reportInvalidEntryDescriptionsUrl: String,
) : TogglUserEntryReport {
    val wrongFormatTrackedTime: Duration = Duration.ofMillis(wrongFormatEntries.sumOf { it.duration })

    val reportErrorType: ReportErrorType? by lazy {
        val entriesWithWrongProject = wrongFormatEntries.filter { (it.projectId ?: 0L) == 0L }.toSet()
        val entriesWithWrongDescription = wrongFormatEntries.filter { it.description.isNullOrBlank() }.toSet()
        when {
            entriesWithWrongProject.isEmpty() && entriesWithWrongDescription.isEmpty() -> null
            entriesWithWrongProject.isNotEmpty() && (entriesWithWrongDescription.isEmpty() ||
                    entriesWithWrongDescription.all { timeEntry -> timeEntry in entriesWithWrongProject }) ->
                ReportErrorType.ENTRIES_WITH_INVALID_PROJECT
            entriesWithWrongDescription.isNotEmpty() && (entriesWithWrongProject.isEmpty() ||
                    entriesWithWrongProject.all { timeEntry -> timeEntry in entriesWithWrongDescription }) ->
                ReportErrorType.ENTRIES_WITH_INVALID_DESCRIPTION
            else -> ReportErrorType.ENTRIES_WITH_INVALID_PROJECT_AND_DESCRIPTION
        }
    }
}

data class SimpleTogglUserEntryReport(
    override val togglUser: TogglUser,
    override val reportUrl: String,
    override val workTime: Duration,
) : TogglUserEntryReport

enum class ReportErrorType {
    ENTRIES_WITH_INVALID_PROJECT,
    ENTRIES_WITH_INVALID_DESCRIPTION,
    ENTRIES_WITH_INVALID_PROJECT_AND_DESCRIPTION,
}

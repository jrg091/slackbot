package com.xmartlabs.slackbot.data.sources

import com.xmartlabs.slackbot.Config
import com.xmartlabs.slackbot.data.sources.serializer.TogglBambooDateSerializer
import com.xmartlabs.slackbot.logger
import com.xmartlabs.slackbot.model.BambooEmployeeDirectoryResponse
import com.xmartlabs.slackbot.model.BambooHrUserCustomFields
import com.xmartlabs.slackbot.model.BambooTimeOff
import com.xmartlabs.slackbot.model.BambooUser
import io.ktor.client.request.get
import java.time.LocalDate

object BambooHrReportsRemoteSource {
    private suspend fun getUserFields(userId: String) =
        BambooHrApi.client.get<BambooHrUserCustomFields>(
            "${BambooHrApi.BASE_URL}/employees/$userId/?fields=" +
                    BambooHrCustomFieldsInfo.VALUES.joinToString(",")
        )

    suspend fun getUsers(): List<BambooUser> =
        BambooHrApi.client.get<BambooEmployeeDirectoryResponse>("${BambooHrApi.BASE_URL}/employees/directory")
            .employees
            .mapNotNull { directoryUser ->
                val userExtraFields = getUserFields(userId = directoryUser.id)
                val workingHours = userExtraFields.workingHours
                    ?.let { workingHours ->
                        // Some users use 30 and 40 hr and other users use 6 and 8.
                        // This should be normalized by hr
                        if (workingHours <= Config.MAX_WORKING_HOURS_PER_DAY) {
                            workingHours * Config.WORK_DAYS
                        } else {
                            workingHours
                        }
                    }
                if (directoryUser.workEmail == null ||
                    workingHours == null ||
                    userExtraFields.hireDate == LocalDate.MIN
                ) {
                    logger.warn(
                        "Invalid user, user: $directoryUser, " +
                                "workingHours: $workingHours, " +
                                "hireDate: ${userExtraFields.hireDate}"
                    )
                    null
                } else {
                    BambooUser(
                        id = directoryUser.id,
                        displayName = directoryUser.displayName,
                        workEmail = directoryUser.workEmail,
                        workingHours = workingHours,
                        hireDate = userExtraFields.hireDate,
                        customFields = userExtraFields,
                    )
                }
            }

    suspend fun getTimeOff(start: LocalDate, end: LocalDate) =
        BambooHrApi.client.get<List<BambooTimeOff>>("${BambooHrApi.BASE_URL}/time_off/whos_out/?" +
                "start=${TogglBambooDateSerializer.formatter.format(start)}&" +
                "end=${TogglBambooDateSerializer.formatter.format(end)}"
        )
}

package com.xmartlabs.slackbot.usecases

import com.xmartlabs.slackbot.Config
import com.xmartlabs.slackbot.extensions.TimeHelper
import com.xmartlabs.slackbot.logger
import com.xmartlabs.slackbot.model.BambooUser
import com.xmartlabs.slackbot.repositories.BambooUserRepository
import com.xmartlabs.slackbot.repositories.SlackUserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalTime
import kotlin.time.toKotlinDuration

@OptIn(ExperimentalStdlibApi::class, kotlin.time.ExperimentalTime::class)
class RemindInvalidBambooHrUserDataUseCase : CoroutineUseCase<RemindInvalidBambooHrUserDataUseCase.Param> {
    companion object {
        private const val DEBUG_MODE = false
    }

    object Param

    override suspend fun execute(param: Param) {
        val nextReview =
            TimeHelper.durationToNextAvailableDate(Config.BAMBOO_TIME_TO_REMIND, Config.BAMBOO_DAYS_TO_REMIND)
        logger.info("Bamboo automatically report will be checked in ${nextReview.toKotlinDuration()})")
        if (!DEBUG_MODE) {
            delay(nextReview.toKotlinDuration())
        }

        sendMessageToUserWithInvalidData()
    }

    private suspend fun sendMessageToUserWithInvalidData() = coroutineScope {
        val startTime = LocalTime.now()
        val bambooUsers = async { BambooUserRepository.getUsers() }
        val slackUsers = SlackUserRepository.getUsers()
            .associateBy { it.profile.email }
        bambooUsers.await()
            .map { user -> user to calculateMissingRequiredFields(user) }
            .filter { (_, missingFields) -> missingFields.isNotEmpty() }
            .also { logGeneratedReport(startTime, it) }
            .forEach { (bambooUser, missingFields) ->
                val slackUser = requireNotNull(slackUsers[bambooUser.workEmail]) {
                    "Slack user not found. WorkEmail: ${bambooUser.workEmail}"
                }
                val message = generateMessage(bambooUser, missingFields)
                if (!DEBUG_MODE) {
                    SlackUserRepository.sendMessage(slackUser.id, message)
                }
            }
    }

    private fun generateMessage(
        bambooUser: BambooUser,
        missingFields: List<String>,
    ) = buildString {
        appendLine(
            "We detected that some important information in your Bamboo profile is missing! " +
                    "You can complete it following " +
                    "<https://xmartlabs.bamboohr.com/employees/employee.php?id=${bambooUser.id}&page=2095 | " +
                    "this link>."
        )
        appendLine()
        if (Config.BAMBOO_REMINDERS_INCLUDE_MISSING_FIELD_IN_REPORT) {
            append("The required missing information is:")
            appendLine(missingFields.joinToString("\n") { "• $it" })
            appendLine()
            appendLine(
                "_Note: If you have any *Dietary restrictions* or *allergy*, " +
                        "it's extremely important that this information is up to date._"
            )
        }
    }

    private fun logGeneratedReport(
        startTime: LocalTime?,
        calculatedReport: List<Pair<BambooUser, List<String>>>,
    ) {
        val duration = Duration.between(startTime, LocalTime.now()).toKotlinDuration()
        logger.info("BambooMissingFieldsReport: Report generated in $duration, " +
                "${calculatedReport.size} users with bamboo missing fields:\n" +
                calculatedReport.joinToString("\n") { (bambooUser, missingFields) ->
                    "${bambooUser.workEmail}: ${missingFields.joinToString(", ")}"
                })
    }

    private fun calculateMissingRequiredFields(user: BambooUser) = buildList {
        with(user.customFields) {
            if (isPhotoUploaded == false) add("Picture")
            if (birthday.isNullOrBlank()) add("Birthday")
            if (ci.isNullOrBlank()) add("CI")
            if (address.isNullOrBlank()) add("Address")
            if (mobilePhone.isNullOrBlank()) add("Mobile Phone")
            if (gender.isNullOrBlank()) add("Gender")
            if (emergencyContact.isNullOrBlank()) add("Emergency Contact")
            if (Config.DEPARTMENTS_WITH_GITHUB_ACCOUNTS.any { it.equals(department, true) } &&
                githubId.isNullOrBlank()) {
                add("GitHub ID")
            }
            if (drinkPreferences.isNullOrBlank()) add("Drink Preferences")
            if (shirtSize.isNullOrBlank()) add("Shirt Size")
        }
    }
}

package com.xmartlabs.slackbot.repositories

import com.xmartlabs.slackbot.data.sources.BambooHrReportsRemoteSource
import com.xmartlabs.slackbot.model.BambooUser
import java.time.LocalDate

object BambooUserRepository {
    suspend fun getUser(email: String) = BambooHrReportsRemoteSource.getUser(email)

    suspend fun getUsers(): List<BambooUser> = BambooHrReportsRemoteSource.getUsers()

    suspend fun getTimeOff(start: LocalDate, end: LocalDate) = BambooHrReportsRemoteSource.getTimeOff(start, end)
}

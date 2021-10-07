package com.xmartlabs.slackbot.extensions

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object TimeHelper {
    fun durationToNextAvailableDate(sendTime: LocalTime, daysToSend: List<DayOfWeek>): Duration {
        val now = LocalDateTime.now()

        return if (now.dayOfWeek in daysToSend && now.toLocalTime() < sendTime) {
            Duration.between(now.toLocalTime(), sendTime)
        } else {
            var nextDay = LocalDate.now()
                .plusDays(1)
            while (nextDay.dayOfWeek !in daysToSend) {
                nextDay = nextDay.plusDays(1)
            }
            Duration.between(now, nextDay.atTime(sendTime))
        }
    }
}

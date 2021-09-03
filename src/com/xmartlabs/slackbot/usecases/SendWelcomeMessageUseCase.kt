package com.xmartlabs.slackbot.usecases

import com.xmartlabs.slackbot.Config
import com.xmartlabs.slackbot.extensions.toPrettyString
import com.xmartlabs.slackbot.logger
import com.xmartlabs.slackbot.manager.MessageManager
import com.xmartlabs.slackbot.repositories.ConversationSlackRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime
import kotlin.time.toKotlinDuration

@OptIn(ExperimentalTime::class)
class SendWelcomeMessageUseCase : CoroutineUseCase<SendWelcomeMessageUseCase.Param> {
    class Param(val userId: String)

    private val mutex = Mutex()
    private val newUsers = mutableSetOf<String>()

    init {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                logger.info("System is shouting down, welcome schedule messages will be sent now.")
                runBlocking {
                    sendWelcomeMessageIfNeeded()
                }
            }
        })
    }

    override suspend fun execute(param: Param) = withContext(Dispatchers.IO) {
        val isFirstUser: Boolean
        mutex.withLock {
            isFirstUser = newUsers.isEmpty()
            newUsers.add(param.userId)
        }
        if (isFirstUser) {
            logger.info("Welcome message will be sent in ${Config.WELCOME_MESSAGE_DELAY.toPrettyString()}")
            delay(Config.WELCOME_MESSAGE_DELAY.toKotlinDuration())
            sendWelcomeMessageIfNeeded()
        } else {
            logger.info("Welcome message is already scheduled, event is ignored")
        }
    }

    private suspend fun sendWelcomeMessageIfNeeded() = mutex.withLock {
        // The check is done to avoid multiple messages when the system is shouting down
        if (newUsers.isNotEmpty()) {
            val welcomeMessage = MessageManager.getOngoardingMessage(Config.BOT_USER_ID, newUsers.toList())
            ConversationSlackRepository.sendMessage(Config.WELCOME_CHANNEL_ID, welcomeMessage)
            ConversationSlackRepository.getChannels().find { it.name == Config.WELCOME_CHANNEL_ID }
            logger.info("Send welcome to $newUsers")
            newUsers.clear()
        }
    }
}

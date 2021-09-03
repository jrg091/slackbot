package com.xmartlabs.slackbot.usecases

interface CoroutineUseCase<P> {
    suspend fun execute(param: P)
}

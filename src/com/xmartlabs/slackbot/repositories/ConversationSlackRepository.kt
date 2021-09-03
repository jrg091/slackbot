package com.xmartlabs.slackbot.repositories

import com.slack.api.model.Conversation
import com.slack.api.model.block.LayoutBlock
import com.xmartlabs.slackbot.data.sources.ConversationSlackRemoteSource
import com.xmartlabs.slackbot.data.sources.SlackRemoteSource

object ConversationSlackRepository : SlackEntityRepository<Conversation>() {
    override val remoteSource: SlackRemoteSource<Conversation>
        get() = ConversationSlackRemoteSource

    fun toConversationId(channelName: String): String? {
        val name = channelName.replaceFirst("#", "").trim()
        return getAndCacheRemoteEntities()
            .firstOrNull { it.name.equals(name, true) }
            ?.id
    }

    fun getChannel(channelId: String) = getEntity { it.id == channelId }

    fun getChannels() = getEntities()

    suspend fun sendMessage(channelId: String, text: String, blocks: List<LayoutBlock>? = null) =
        ConversationSlackRemoteSource.sendMessage(channelId, text, blocks)

    suspend fun sendCsvFile(channelId: String, fileName: String, title: String, content: String) =
        ConversationSlackRemoteSource.sendCsvFile(channelId, fileName, title, content)
}

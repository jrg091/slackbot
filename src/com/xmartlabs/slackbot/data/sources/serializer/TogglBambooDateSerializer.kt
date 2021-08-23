package com.xmartlabs.slackbot.data.sources.serializer

import com.xmartlabs.slackbot.logger
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializer(forClass = LocalDate::class)
object TogglBambooDateSerializer : KSerializer<LocalDate> {
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.LONG)

    override fun serialize(encoder: Encoder, value: LocalDate) =
        encoder.encodeString(value.format(formatter))

    override fun deserialize(decoder: Decoder): LocalDate {
        val dateAsString = decoder.decodeString()
        return kotlin.runCatching { LocalDate.parse(dateAsString, formatter) }
            .onFailure { logger.warn("Invalid date, $dateAsString") }
            .getOrNull()
            ?: LocalDate.MIN
    }
}

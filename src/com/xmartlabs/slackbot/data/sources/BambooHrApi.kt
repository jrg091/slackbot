package com.xmartlabs.slackbot.data.sources

import com.xmartlabs.slackbot.Config
import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.BasicAuthCredentials
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.Logging
import io.ktor.client.request.header
import kotlinx.serialization.json.Json as JsonBuilder

object BambooHrApi {
    val BASE_URL = "https://api.bamboohr.com/api/gateway.php/${Config.BAMBOO_ORG_NAME_KEY}/v1"

    val client = HttpClient {
        defaultRequest {
            header("accept-language", "en-US,en;q=0.9,es;q=0.8")
        }
        install(Auth) {
            basic {
                sendWithoutRequest { true }
                credentials {
                    BasicAuthCredentials(
                        username = Config.BAMBOO_API_KEY,
                        password = "x"
                    )
                }
            }
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer(JsonBuilder {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        if (Config.DEBUG) {
            install(Logging)
        }
    }
}

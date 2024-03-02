package eu.miaplatform.commons.ktor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.logging.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.util.*
import net.logstash.logback.argument.StructuredArguments
import org.slf4j.LoggerFactory

val MiaLoggerPlugin = createApplicationPlugin(name = "MiaLoggerPlugin") {
    val headerRequestId = "x-request-id"
    val headerUserId = System.getenv("USERID_HEADER_KEY") ?: "miauserid"
    val headerUserGroups = System.getenv("GROUPS_HEADER_KEY") ?: "miausergroups"
    val headerClientType = System.getenv("CLIENTTYPE_HEADER_KEY") ?: "client-type"
    val headerIsBackoffice = System.getenv("BACKOFFICE_HEADER_KEY") ?: "isbackoffice"
    val headerUserProperties = System.getenv("USER_PROPERTIES_HEADER_KEY") ?: "miauserproperties"

    val logger = LoggerFactory.getLogger("MiaLogger")

    val onCallTimeKey = AttributeKey<Long>("miaLogger.callStartTime")

    onCall { call ->
        val onCallTime = System.currentTimeMillis()
        call.attributes.put(onCallTimeKey, onCallTime)

        logger.info("[HTTP REQUEST - START] {}",
            call.request.toLogString(),
            StructuredArguments.keyValue(
                "url",
                call.request.origin.let { "${it.scheme}://${it.localHost}:${it.localPort}${it.uri}" }
            ),
            StructuredArguments.keyValue("request_id", call.request.header(headerRequestId)),
            StructuredArguments.keyValue("user_id", call.request.header(headerUserId)),
            StructuredArguments.keyValue("user_groups", call.request.header(headerUserGroups)),
            StructuredArguments.keyValue("client_type", call.request.header(headerClientType)),
            StructuredArguments.keyValue("is_backoffice", call.request.header(headerIsBackoffice)),
            StructuredArguments.keyValue("user_properties", call.request.header(headerUserProperties)),
        )
    }

    onCallRespond { call ->
        val onCallTime = call.attributes[onCallTimeKey]
        val onCallReceiveTime = System.currentTimeMillis()
        val callDuration = onCallReceiveTime - onCallTime
        logger.info(
            "[HTTP REQUEST - END] {}",
            call.ktorResponseLogMessage(),
            StructuredArguments.keyValue("status", call.response.status()),
            StructuredArguments.keyValue(
                "url",
                call.request.origin.let { "${it.scheme}://${it.localHost}:${it.localPort}${it.uri}" }),
            StructuredArguments.keyValue("duration_ms", callDuration),
            StructuredArguments.keyValue("request_id", call.request.header(headerRequestId)),
            StructuredArguments.keyValue("user_id", call.request.header(headerUserId)),
            StructuredArguments.keyValue("user_groups", call.request.header(headerUserGroups)),
            StructuredArguments.keyValue("client_type", call.request.header(headerClientType)),
            StructuredArguments.keyValue("is_backoffice", call.request.header(headerIsBackoffice)),
            StructuredArguments.keyValue("user_properties", call.request.header(headerUserProperties)),
        )
    }
}

private fun ApplicationCall.ktorResponseLogMessage(): String {
    return when (val status = response.status() ?: "Unhandled") {
        HttpStatusCode.Found ->
            "${status as HttpStatusCode}: " +
                    "${request.toLogString()} -> ${response.headers[HttpHeaders.Location]}"

        "Unhandled" -> "$status: ${request.toLogString()}"
        else -> "${status as HttpStatusCode}: ${request.toLogString()}"
    }
}
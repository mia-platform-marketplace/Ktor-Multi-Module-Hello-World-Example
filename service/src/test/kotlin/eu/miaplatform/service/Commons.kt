package eu.miaplatform.service

import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest
import org.mockserver.model.HttpResponse

internal fun MockServerClient.setupResponse(
    requestMethod: String,
    requestPath: String,
    responseStatus: Int,
    responseBody: String
) {

    `when`(
        HttpRequest.request()
            .withMethod(requestMethod)
            .withPath(requestPath)

    )
        .respond(
            HttpResponse.response()
                .withStatusCode(responseStatus)
                .withBody(responseBody)
        )
}
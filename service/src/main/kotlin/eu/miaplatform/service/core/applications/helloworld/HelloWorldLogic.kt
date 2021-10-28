package eu.miaplatform.service.core.applications.helloworld

import eu.miaplatform.commons.client.CrudClientInterface
import eu.miaplatform.commons.client.HeadersToProxy
import eu.miaplatform.commons.model.InternalServerErrorException
import eu.miaplatform.service.model.request.HelloWorldGetRequest
import eu.miaplatform.service.model.request.HelloWorldPostRequest
import eu.miaplatform.service.model.request.HelloWorldRequestBody
import eu.miaplatform.service.model.response.HelloWorldResponse
import io.ktor.application.*

class HelloWorldLogic(
    private val crudClient: CrudClientInterface,
    private val headersToProxy: HeadersToProxy
) {
    fun helloGet(params: HelloWorldGetRequest): HelloWorldResponse {
        return HelloWorldResponse(
            null,
            params.queryParam,
            "Hello world!"
        )
    }

    fun helloWithParamPost(params: HelloWorldPostRequest, requestBody: HelloWorldRequestBody): HelloWorldResponse {
        return HelloWorldResponse(
            params.pathParam,
            null,
            "Hello world ${requestBody.name} ${requestBody.surname}!"
        )
    }

    suspend fun helloWithCallGet(params: HelloWorldGetRequest, context: ApplicationCall): HelloWorldResponse {
        val headers = headersToProxy.proxy(context)
        val books = try {
            crudClient.getBooks(headers)
        } catch (e: Exception) {
            throw InternalServerErrorException(1002, "books call failed")
        }

        return HelloWorldResponse(
            null,
            params.queryParam,
            "Hello world! Book list: ${books.joinToString()}"
        )
    }
}
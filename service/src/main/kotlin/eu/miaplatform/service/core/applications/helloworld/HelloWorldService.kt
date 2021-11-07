package eu.miaplatform.service.core.applications.helloworld

import eu.miaplatform.commons.client.CrudClientInterface
import eu.miaplatform.commons.client.HeadersToProxy
import eu.miaplatform.commons.model.InternalServerErrorException
import eu.miaplatform.service.model.request.HelloWorldGetRequest
import eu.miaplatform.service.model.request.HelloWorldPostRequest
import eu.miaplatform.service.model.request.HelloWorldRequestBody
import eu.miaplatform.service.model.response.HelloWorldResponse
import io.ktor.application.*

class HelloWorldService(
    private val crudClient: CrudClientInterface
) {
    suspend fun getBooksByHeaders(headers: Map<String, String>): List<String> {
        return try {
            crudClient.getBooks(headers)
        } catch (e: Exception) {
            throw Exception("books call failed")
        }
    }
}
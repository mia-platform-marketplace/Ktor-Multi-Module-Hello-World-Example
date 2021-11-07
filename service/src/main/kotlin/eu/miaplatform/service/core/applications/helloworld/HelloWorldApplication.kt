package eu.miaplatform.service.core.applications.helloworld

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.route.tag
import eu.miaplatform.commons.ktor.HeadersToProxy
import eu.miaplatform.commons.model.InternalServerErrorException
import eu.miaplatform.service.core.applications.CustomApiApplication
import eu.miaplatform.service.model.ServiceTag
import eu.miaplatform.service.model.request.HelloWorldGetRequest
import eu.miaplatform.service.model.request.HelloWorldPostRequest
import eu.miaplatform.service.model.request.HelloWorldRequestBody
import eu.miaplatform.service.model.response.HelloWorldResponse

class HelloWorldApplication(
    private val service: HelloWorldService,
    private val headersToProxy: HeadersToProxy
): CustomApiApplication {

    override fun install(apiRouting: NormalOpenAPIRoute): Unit = apiRouting.run {
        route("/hello") {
            tag(ServiceTag) {
                get<HelloWorldGetRequest, HelloWorldResponse>(
                    info("The description of the endpoint")
                ) { params ->
                    respond(
                        HelloWorldResponse(
                            null,
                            params.queryParam,
                            "Hello world!"
                        )
                    )
                }

                route("/{pathParam}") {
                    post<HelloWorldPostRequest, HelloWorldResponse, HelloWorldRequestBody>(
                        info("The description of the endpoint")
                    ) { params, requestBody ->
                        respond(
                            HelloWorldResponse(
                                params.pathParam,
                                null,
                                "Hello world ${requestBody.name} ${requestBody.surname}!"
                            )
                        )
                    }
                }

                route("/with-call") {
                    get<HelloWorldGetRequest, HelloWorldResponse>(
                        info("The description of the endpoint")
                    ) { params ->
                        val books = try {
                            service.getBooksByHeaders(headersToProxy.proxy(pipeline.context))
                        } catch (e: Exception) {
                            throw InternalServerErrorException(1000, e.localizedMessage)
                        }

                        respond(
                            HelloWorldResponse(
                                null,
                                params.queryParam,
                                "Hello world! Book list: ${books.joinToString()}"
                            )
                        )
                    }
                }
            }
        }
    }
}

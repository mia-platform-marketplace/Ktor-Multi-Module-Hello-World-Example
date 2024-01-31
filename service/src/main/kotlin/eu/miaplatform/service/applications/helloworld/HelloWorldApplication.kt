package eu.miaplatform.service.applications.helloworld

import eu.miaplatform.commons.Serialization
import eu.miaplatform.commons.ktor.CustomApplication
import eu.miaplatform.service.model.request.HelloWorldRequestBody
import eu.miaplatform.service.model.response.HelloWorldResponse
import eu.miaplatform.service.services.HelloWorldService
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.MediaType
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class HelloWorldApplication(
    private val additionalHeadersToProxy: String,
    private val service: HelloWorldService
): CustomApplication {

    override fun install(routing: Routing): Unit = routing.run {
        route("/hello") {
            install(NotarizedRoute()) {
                get = GetInfo.builder {
                    tags("Hello")
                    summary("Summary")
                    description("The description of the endpoint")
                    parameters(
                        Parameter(
                            name = "param",
                            `in` = Parameter.Location.query,
                            schema = TypeDefinition.STRING,
                            description = "Description of the query param"
                        )
                    )
                    response {
                        description("Response body")
                        responseCode(HttpStatusCode.OK)
                        responseType<HelloWorldResponse>()
                        examples(
                            "Example" to MediaType.Example(HelloWorldResponse(null, "param", null))
                        )
                    }
                }
            }
            get {
                call.respond(
                    HelloWorldResponse(
                        null,
                        call.parameters["param"],
                        "Hello world!"
                    )
                )
            }


            route("/{pathParam}") {
                install(NotarizedRoute()) {
                    post = PostInfo.builder {
                        tags("Hello")
                        summary("Summary")
                        description("The description of the endpoint")
                        parameters(
                            Parameter(
                                name = "pathParam",
                                `in` = Parameter.Location.path,
                                schema = TypeDefinition.STRING,
                                description = "Description of the path param"
                            )
                        )
                        request {
                            description("Request body")
                            requestType<HelloWorldRequestBody>()
                            examples(
                                "Example" to MediaType.Example(
                                    HelloWorldRequestBody(name = "John", surname = "Doe")
                                )
                            )
                        }
                        response {
                            description("Response body")
                            responseCode(HttpStatusCode.OK)
                            responseType<HelloWorldResponse>()
                            examples(
                                "Example" to MediaType.Example(
                                    HelloWorldResponse(null, "param", null)
                                )
                            )
                        }
                    }
                }
                post {
                    val requestBody = call.receive<HelloWorldRequestBody>()
                    val pathParam = call.parameters["pathParam"]

                    call.respond(
                        HelloWorldResponse(
                            pathParam,
                            null,
                            "Hello world ${requestBody.name} ${requestBody.surname}!"
                        )
                    )
                }
            }

//                route("/with-call") {
//                    get<HelloWorldGetRequest, HelloWorldResponse>(
//                        info("The description of the endpoint")
//                    ) { params ->
//                        val books = try {
//                            service.getBooksByHeaders(pipeline.context.headersToProxy(additionalHeadersToProxy))
//                        } catch (e: Exception) {
//                            throw InternalServerErrorException(1000, e.localizedMessage)
//                        }
//
//                        respond(
//                            HelloWorldResponse(
//                                null,
//                                params.queryParam,
//                                "Hello world! Book list: ${books.joinToString()}"
//                            )
//                        )
//                    }
//                }
        }
    }
}

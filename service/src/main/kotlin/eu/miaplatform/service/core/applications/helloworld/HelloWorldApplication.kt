package eu.miaplatform.service.core.applications.helloworld

import com.papsign.ktor.openapigen.route.info
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import com.papsign.ktor.openapigen.route.path.normal.get
import com.papsign.ktor.openapigen.route.path.normal.post
import com.papsign.ktor.openapigen.route.response.respond
import com.papsign.ktor.openapigen.route.route
import com.papsign.ktor.openapigen.route.tag
import eu.miaplatform.commons.client.CrudClientInterface
import eu.miaplatform.commons.client.HeadersToProxy
import eu.miaplatform.commons.model.InternalServerErrorException
import eu.miaplatform.service.core.applications.CustomApiApplication
import eu.miaplatform.service.model.ServiceTag
import eu.miaplatform.service.model.request.HelloWorldGetRequest
import eu.miaplatform.service.model.request.HelloWorldPostRequest
import eu.miaplatform.service.model.request.HelloWorldRequestBody
import eu.miaplatform.service.model.response.HelloWorldResponse
import io.ktor.application.*
import kotlinx.coroutines.async

class HelloWorldApplication(private val logic: HelloWorldLogic): CustomApiApplication {

    override fun install(apiRouting: NormalOpenAPIRoute): Unit = apiRouting.run {
        route("/hello") {
            tag(ServiceTag) {
                get<HelloWorldGetRequest, HelloWorldResponse>(
                    info("The description of the endpoint")
                ) { params ->
                    respond(logic.helloGet(params))
                }

                route("/{pathParam}") {
                    post<HelloWorldPostRequest, HelloWorldResponse, HelloWorldRequestBody>(
                        info("The description of the endpoint")
                    ) { params, requestBody ->
                        respond(logic.helloWithParamPost(params, requestBody))
                    }
                }

                route("/with-call") {
                    get<HelloWorldGetRequest, HelloWorldResponse>(
                        info("The description of the endpoint")
                    ) { params ->
                        respond(logic.helloWithCallGet(params, pipeline.context))
                    }
                }
            }
        }
    }
}

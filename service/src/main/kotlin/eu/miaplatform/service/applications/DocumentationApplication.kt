package eu.miaplatform.service.applications

import com.papsign.ktor.openapigen.openAPIGen
import eu.miaplatform.commons.ktor.CustomApplication
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class DocumentationApplication : CustomApplication {
    override fun install(routing: Routing): Unit =
        routing.run {
            route("/documentation") {
                get {
                    call.respondRedirect("/swagger-ui/index.html?url=/documentation/openapi.json", true)
                }
                get("/openapi.json") {
                    call.respond(application.openAPIGen.api.serialize())
                }
            }
        }
}

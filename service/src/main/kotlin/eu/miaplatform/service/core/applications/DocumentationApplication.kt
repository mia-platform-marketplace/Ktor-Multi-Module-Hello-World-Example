package eu.miaplatform.service.core.applications

import com.papsign.ktor.openapigen.openAPIGen
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*

class DocumentationApplication : CustomApplication {
    override fun install(routing: Routing): Unit = routing.run {
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

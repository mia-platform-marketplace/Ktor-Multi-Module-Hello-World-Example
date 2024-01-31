package eu.miaplatform.service.applications

import eu.miaplatform.commons.StatusService
import eu.miaplatform.commons.ktor.CustomApplication
import eu.miaplatform.commons.model.HealthBodyResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class HealthApplication : CustomApplication {
    private val version = StatusService.getVersion()
    private val healthBodyResponse = HealthBodyResponse("service-api", version, HealthBodyResponse.Status.OK.value)

    override fun install(routing: Routing): Unit = routing.run {
        route("/-") {
            get("/healthz") {
                call.respond(HttpStatusCode.OK, healthBodyResponse)
            }
            get("/ready") {
                call.respond(HttpStatusCode.OK, healthBodyResponse)
            }
        }
    }
}

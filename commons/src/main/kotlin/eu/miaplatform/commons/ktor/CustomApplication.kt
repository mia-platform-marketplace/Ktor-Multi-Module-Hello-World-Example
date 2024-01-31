package eu.miaplatform.commons.ktor

import com.papsign.ktor.openapigen.route.apiRouting
import com.papsign.ktor.openapigen.route.path.normal.NormalOpenAPIRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*

interface CustomApplication {
    fun install(routing: Routing)
}

interface CustomApiApplication {
    fun install(apiRouting: NormalOpenAPIRoute)
}

fun Application.install(app: CustomApplication) {
    routing { app.install(this) }
}

fun Application.install(app: CustomApiApplication) {
    apiRouting { app.install(this) }
}
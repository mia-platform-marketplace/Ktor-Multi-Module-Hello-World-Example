package eu.miaplatform.commons.ktor

import io.ktor.server.application.*
import io.ktor.server.routing.*


interface CustomApplication {
    fun install(routing: Routing)
}

//interface CustomApiApplication {
//    fun install(apiRouting: NormalOpenAPIRoute)
//}

fun Application.install(app: CustomApplication) {
    routing { app.install(this) }
}

//fun Application.install(app: CustomApiApplication) {
//    apiRouting { app.install(this) }
//}
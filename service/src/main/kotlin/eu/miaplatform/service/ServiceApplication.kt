package eu.miaplatform.service

import ch.qos.logback.classic.util.ContextInitializer
import eu.miaplatform.commons.Serialization
import eu.miaplatform.commons.StatusService
import eu.miaplatform.commons.client.CrudClientInterface
import eu.miaplatform.commons.client.RetrofitClient
import eu.miaplatform.commons.ktor.install
import eu.miaplatform.service.applications.HealthApplication
import eu.miaplatform.service.applications.helloworld.HelloWorldApplication
import eu.miaplatform.service.services.HelloWorldService
import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.core.routes.redoc
import io.bkbn.kompendium.core.routes.swagger
import io.bkbn.kompendium.json.schema.KotlinXSchemaConfigurator
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.info.Info
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.event.Level
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.reflect.typeOf

fun main(args: Array<String>) {
    System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, System.getenv("LOG_CONFIG_FILE"))
    EngineMain.main(args)
}

fun Application.module() {

    val httpLogLevel = when (environment.config.property("ktor.log.httpLogLevel").getString().uppercase()) {
        "BASIC" -> HttpLoggingInterceptor.Level.BASIC
        "BODY" -> HttpLoggingInterceptor.Level.BODY
        "HEADERS" -> HttpLoggingInterceptor.Level.HEADERS
        else -> HttpLoggingInterceptor.Level.NONE
    }

    val additionalHeadersToProxy = System.getenv("ADDITIONAL_HEADERS_TO_PROXY") ?: ""

    val crudClient = RetrofitClient.build<CrudClientInterface>(
        basePath = "http://crud-service/",
        logLevel = httpLogLevel
    )

    baseModule()
    val helloWorldService = HelloWorldService(crudClient)
    install(HelloWorldApplication(additionalHeadersToProxy, helloWorldService))
    install(HealthApplication())
}


/**
 * Install common functionalities like open api, logging, metrics, serialization, etc. for this application.
 */
fun Application.baseModule() {
    install(CallLogging) {
        level = Level.INFO
        disableDefaultColors()
        filter { call -> !call.request.path().startsWith("/-/") }
    }

    install(ContentNegotiation) {
        jackson {
            Serialization.apply { defaultKtorLiteral() }
        }
    }

    install(NotarizedApplication()) {
        spec = {
            OpenApiSpec(
                info = Info(
                    title = "Hello World",
                    version = StatusService.getVersion(),
                    summary = "An introductory Hello World service"
                )
            )
        }
        this.schemaConfigurator = KotlinXSchemaConfigurator()
        specRoute = { spec, routing ->
            routing {
                route("/documentation") {
                    get("/openapi.json") {
                        spec.openapi
                        call.respond(spec)
                    }
                    // exposed under /swagger-ui
                    swagger("Service documentation", specUrl = "/documentation/openapi.json")
                    // exposed under /docs
                    redoc("Service documentation", specUrl = "/documentation/openapi.json")
                }
            }
        }
        customTypes = mapOf(
            typeOf<Instant>() to TypeDefinition(type = "string", format = "date-time"),
            typeOf<LocalDate>() to TypeDefinition(type = "string", format = "date"),
            typeOf<LocalTime>() to TypeDefinition(type = "string", format = "time"),
            typeOf<LocalDateTime>() to TypeDefinition(type = "string", format = "local-date-time"),
        )
    }

//    install(StatusPages) {
//        withAPI(api) {
//            exception<UnauthorizedException, ErrorResponse>(HttpStatusCode.Unauthorized) {
//                ErrorResponse(it.code, it.errorMessage)
//            }
//            exception<NotFoundException, ErrorResponse>(HttpStatusCode.NotFound) {
//                ErrorResponse(it.code, it.errorMessage)
//            }
//            exception<BadRequestException, ErrorResponse>(HttpStatusCode.BadRequest) {
//                ErrorResponse(it.code, it.errorMessage)
//            }
//            exception<MissingKotlinParameterException, ErrorResponse>(HttpStatusCode.BadRequest) {
//                ErrorResponse(1000, it.localizedMessage)
//            }
//            exception<InvocationTargetException, ErrorResponse>(HttpStatusCode.BadRequest) {
//                ErrorResponse(1000, it.targetException.localizedMessage)
//            }
//            exception<InvalidFormatException, ErrorResponse>(HttpStatusCode.BadRequest) {
//                ErrorResponse(1000, it.localizedMessage)
//            }
//            exception<InternalServerErrorException, ErrorResponse>(HttpStatusCode.InternalServerError) {
//                ErrorResponse(it.code, it.errorMessage)
//            }
//            exception<Exception, ErrorResponse>(HttpStatusCode.InternalServerError) {
//                ErrorResponse(1000, it.localizedMessage ?: "Generic error")
//            }
//        }
//    }

}
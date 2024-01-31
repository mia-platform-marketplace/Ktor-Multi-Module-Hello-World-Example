package eu.miaplatform.service

import ch.qos.logback.classic.util.ContextInitializer
import com.papsign.ktor.openapigen.OpenAPIGen
import com.papsign.ktor.openapigen.schema.builder.provider.DefaultObjectSchemaProvider
import com.papsign.ktor.openapigen.schema.namer.DefaultSchemaNamer
import com.papsign.ktor.openapigen.schema.namer.SchemaNamer
import eu.miaplatform.commons.Serialization
import eu.miaplatform.commons.StatusService
import eu.miaplatform.commons.client.CrudClientInterface
import eu.miaplatform.commons.client.RetrofitClient
import eu.miaplatform.commons.ktor.install
import eu.miaplatform.service.applications.DocumentationApplication
import eu.miaplatform.service.applications.HealthApplication
import eu.miaplatform.service.applications.helloworld.HelloWorldApplication
import eu.miaplatform.service.core.openapi.CustomJacksonObjectSchemaProvider
import eu.miaplatform.service.services.HelloWorldService
import io.ktor.serialization.jackson.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import okhttp3.logging.HttpLoggingInterceptor
import org.slf4j.event.Level
import kotlin.reflect.KType

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
    install(DocumentationApplication())
    install(HealthApplication())
}


/**
 * Install common functionalities like open api, logging, metrics, serialization, etc. for this application.
 */
fun Application.baseModule() {
    install(CallLogging) {
        level = Level.INFO
        disableDefaultColors()
        // don't log calls to health
        filter { call -> !call.request.path().startsWith("/-/") }
    }

    // Documentation here: https://github.com/papsign/Ktor-OpenAPI-Generator
    val api = install(OpenAPIGen) {
        info {
            version = StatusService().getVersion()
            title = "Service name"
            description = "The service description"
            contact {
                name = "Name of the contact"
                email = "contact@email.com"
            }
        }
        server("https://test.host/") {
            description = "Test environment"
        }
        server("https://preprod.host/") {
            description = "Preproduction environment"
        }
        server("https://cloud.host/") {
            description = "Production environment"
        }
        replaceModule(DefaultSchemaNamer, object: SchemaNamer {
            val regex = Regex("[A-Za-z0-9_.]+")
            override fun get(type: KType): String {
                return type.toString().replace(regex) { it.value.split(".").last() }.replace(Regex(">|<|, "), "_")
            }
        })
        replaceModule(DefaultObjectSchemaProvider, CustomJacksonObjectSchemaProvider)
    }

    install(ContentNegotiation) {
        jackson {
            Serialization.apply { defaultKtorLiteral() }
        }
    }

//    install() {
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
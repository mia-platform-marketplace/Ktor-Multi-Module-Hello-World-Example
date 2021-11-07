package eu.miaplatform.service.core.applications

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import eu.miaplatform.commons.client.CrudClientInterface
import eu.miaplatform.commons.client.HeadersToProxy
import eu.miaplatform.commons.client.RetrofitClient
import eu.miaplatform.service.baseModule
import eu.miaplatform.service.core.applications.helloworld.HelloWorldApplication
import eu.miaplatform.service.core.applications.helloworld.HelloWorldService
import eu.miaplatform.service.model.ErrorResponse
import eu.miaplatform.service.model.request.HelloWorldRequestBody
import eu.miaplatform.service.model.response.HelloWorldResponse
import eu.miaplatform.service.setupResponse
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.clearAllMocks
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.jupiter.api.*
import org.mockserver.client.MockServerClient
import org.mockserver.integration.ClientAndServer
import org.slf4j.event.Level

/**
 * End-to-end tests.
 * You could test separately endpoints and logic in two different test classes.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HelloWorldApplicationTest {

    private val objectMapper = ObjectMapper().apply {
        setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }
    private val host = "localhost"
    private val port = 3000
    private var mockServer: MockServerClient = MockServerClient(host, port)

    private val crudClient = RetrofitClient.build<CrudClientInterface>("http://$host:$port", HttpLoggingInterceptor.Level.NONE)
    private val service = HelloWorldService(crudClient)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Nested
    inner class Get {
        @Test
        fun `Get should return success object message`() {
            withTestApplication({
                baseModule(Level.DEBUG)
                install(HelloWorldApplication(service, HeadersToProxy()))
            }) {
                handleRequest(HttpMethod.Get, "/hello") {
                }.apply {
                    assertThat(response.status()?.value).isEqualTo(HttpStatusCode.OK.value)

                    val expectedBody = objectMapper.writeValueAsString(
                        HelloWorldResponse(
                            null,
                            null,
                            "Hello world!"
                        )
                    )
                    assertThat(response.content).isEqualTo(expectedBody)
                }
            }
        }

        @Test
        fun `Get should return success object message with query parameter`() {
            withTestApplication({
                baseModule(Level.DEBUG)
                install(HelloWorldApplication(service, HeadersToProxy()))
            }) {
                handleRequest(HttpMethod.Get, "/hello?queryParam=param") {
                }.apply {
                    assertThat(response.status()?.value).isEqualTo(HttpStatusCode.OK.value)

                    val expectedBody = objectMapper.writeValueAsString(
                        HelloWorldResponse(
                            null,
                            "param",
                            "Hello world!"
                        )
                    )
                    assertThat(response.content).isEqualTo(expectedBody)
                }
            }
        }
    }

    @Nested
    inner class Post {
        @Test
        fun `Post should return success object message with path param`() {

            withTestApplication({
                baseModule(Level.DEBUG)
                install(HelloWorldApplication(service, HeadersToProxy()))
            }) {
                val body = objectMapper.writeValueAsString(
                    HelloWorldRequestBody("name", "surname")
                )

                handleRequest(HttpMethod.Post, "/hello/1234") {
                    addHeader("Content-Type", "application/json")
                    setBody(body)
                }.apply {
                    assertThat(response.status()?.value).isEqualTo(HttpStatusCode.OK.value)

                    val expectedBody = objectMapper.writeValueAsString(
                        HelloWorldResponse(
                            "1234",
                            null,
                            "Hello world name surname!"
                        )
                    )
                    assertThat(response.content).isEqualTo(expectedBody)
                }
            }
        }

        @Test
        fun `Post should return bad request if body is malformed`() {
            withTestApplication({
                baseModule(Level.DEBUG)
                install(HelloWorldApplication(service, HeadersToProxy()))
            }) {
                val body = objectMapper.writeValueAsString(
                    mapOf("name" to "name")
                )

                handleRequest(HttpMethod.Post, "/hello/1234") {
                    addHeader("Content-Type", "application/json")
                    setBody(body)
                }.apply {
                    assertThat(response.status()?.value).isEqualTo(HttpStatusCode.BadRequest.value)
                    val servicePackage = "eu.miaplatform.service"
                    val expectedBody = objectMapper.writeValueAsString(
                        // if you change the package name, remember to update it in the following error or the test will fail
                        ErrorResponse(1000, "Instantiation of [simple type, class $servicePackage.model.request.HelloWorldRequestBody] value failed for JSON property surname due to missing (therefore NULL) value for creator parameter surname which is a non-nullable type\n at [Source: (InputStreamReader); line: 1, column: 15] (through reference chain: $servicePackage.model.request.HelloWorldRequestBody[\"surname\"])")
                    )
                    assertThat(response.content).isEqualTo(expectedBody)
                }
            }
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class GetWithCall {

        @BeforeEach
        fun setUp() {
            mockServer = ClientAndServer.startClientAndServer(port)
        }

        @AfterEach
        fun tearDown() {
            mockServer.close()
        }

        @Test
        fun `Get with call should return success object message`() {
            mockServer.setupResponse(
                "GET",
                "/v2/books",
                200,
                objectMapper.writeValueAsString(listOf("book1", "book2"))
            )

            withTestApplication({
                baseModule(Level.DEBUG)
                install(HelloWorldApplication(service, HeadersToProxy()))
            }) {
                handleRequest(HttpMethod.Get, "/hello/with-call") {
                }.apply {
                    assertThat(response.status()?.value).isEqualTo(HttpStatusCode.OK.value)

                    val expectedBody = objectMapper.writeValueAsString(
                        HelloWorldResponse(
                            null,
                            null,
                            "Hello world! Book list: book1, book2"
                        )
                    )
                    assertThat(response.content).isEqualTo(expectedBody)
                }
            }
        }

        @Test
        fun `Get with call should return error if call fails`() {
            mockServer.setupResponse(
                "GET",
                "/v2/books",
                500,
                "{\"error\": \"error\"}"
            )

            withTestApplication({
                baseModule(Level.DEBUG)
                install(HelloWorldApplication(service, HeadersToProxy()))
            }) {
                handleRequest(HttpMethod.Get, "/hello/with-call") {
                }.apply {
                    assertThat(response.status()?.value).isEqualTo(HttpStatusCode.InternalServerError.value)

                    val expectedBody = objectMapper.writeValueAsString(
                        ErrorResponse(1000, "books call failed")
                    )
                    assertThat(response.content).isEqualTo(expectedBody)
                }
            }
        }

        @Test
        fun `Get with call should return success object message with query parameter`() {
            mockServer.setupResponse(
                "GET",
                "/v2/books",
                200,
                objectMapper.writeValueAsString(listOf("book1", "book2"))
            )
            withTestApplication({
                baseModule(Level.DEBUG)
                install(HelloWorldApplication(service, HeadersToProxy()))
            }) {
                handleRequest(HttpMethod.Get, "/hello/with-call?queryParam=param") {
                }.apply {
                    assertThat(response.status()?.value).isEqualTo(HttpStatusCode.OK.value)

                    val expectedBody = objectMapper.writeValueAsString(
                        HelloWorldResponse(
                            null,
                            "param",
                            "Hello world! Book list: book1, book2"
                        )
                    )
                    assertThat(response.content).isEqualTo(expectedBody)
                }
            }
        }
    }

}
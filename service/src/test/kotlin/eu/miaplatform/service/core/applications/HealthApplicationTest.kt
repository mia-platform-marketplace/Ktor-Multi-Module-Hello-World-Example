package eu.miaplatform.service.core.applications

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.fasterxml.jackson.databind.ObjectMapper
import eu.miaplatform.commons.StatusService
import eu.miaplatform.commons.model.HealthBodyResponse
import eu.miaplatform.service.baseModule
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.slf4j.event.Level

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HealthApplicationTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `Health should return OK`() {
        withTestApplication({
            baseModule (Level.DEBUG)
            install(HealthApplication())
        }) {
            handleRequest(HttpMethod.Get, "/-/healthz") {

            }.apply {
                assertThat(response.status()?.value).isEqualTo(HttpStatusCode.OK.value)

                val version = StatusService().getVersion()
                val body = objectMapper.readValue(response.content, HealthBodyResponse::class.java)
                val expectedRes = HealthBodyResponse("service-api", version, HealthBodyResponse.Status.OK.value)
                assertThat(expectedRes).isEqualTo(body)
            }
        }
    }

    @Test
    fun `Ready should return OK`() {
        withTestApplication({
            baseModule (Level.DEBUG)
            install(HealthApplication())
        }) {
            handleRequest(HttpMethod.Get, "/-/ready") {

            }.apply {
                assertThat(response.status()?.value).isEqualTo(HttpStatusCode.OK.value)

                val version = StatusService().getVersion()
                val body = objectMapper.readValue(response.content, HealthBodyResponse::class.java)
                val expectedRes = HealthBodyResponse("service-api", version, HealthBodyResponse.Status.OK.value)
                assertThat(expectedRes).isEqualTo(body)
            }
        }

    }

    @Test
    fun `Check Up should return OK`() {
        withTestApplication({
            baseModule (Level.DEBUG)
            install(HealthApplication())
        }) {
            handleRequest(HttpMethod.Get, "/-/check-up") {

            }.apply {
                assertThat(response.status()?.value).isEqualTo(HttpStatusCode.OK.value)

                val version = StatusService().getVersion()
                val body = objectMapper.readValue(response.content, HealthBodyResponse::class.java)
                val expectedRes = HealthBodyResponse("service-api", version, HealthBodyResponse.Status.OK.value)
                assertThat(expectedRes).isEqualTo(body)
            }
        }

    }
}
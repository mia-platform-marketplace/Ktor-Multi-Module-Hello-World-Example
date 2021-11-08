package eu.miaplatform.commons.client

import assertk.assertThat
import assertk.assertions.*
import eu.miaplatform.commons.ktor.headersToProxy
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Test

// TODO use kotest
class HeadersToProxyTest {

    @Test
    fun `Returns empty header map when no header is present`() {
        withTestApplication {
            handleRequest(HttpMethod.Get, "/proxy-headers"){
            }.apply {
                val headers = headersToProxy()
                assertThat(mapOf<String, String>()).isEqualTo(headers)
            }
        }
    }

    @Test
    fun `Returns the correct header map when x-request-id has a value`() {
        withTestApplication {
            handleRequest(HttpMethod.Get, "/proxy-headers"){
                addHeader("x-request-id", "1234abcd")
            }.apply {
                val headers = headersToProxy()
                assertThat(mapOf("x-request-id" to "1234abcd")).isEqualTo(headers)
            }
        }
    }

    @Test
    fun `Returns the correct header map when miauserid has a value`() {
        withTestApplication {
            handleRequest(HttpMethod.Get, "/proxy-headers"){
                addHeader("miauserid", "userid")
            }.apply {
                val headers = headersToProxy()
                assertThat(mapOf("miauserid" to "userid")).isEqualTo(headers)
            }
        }
    }

    @Test
    fun `Returns the correct header map when miausergroups has a value`() {
        withTestApplication {
            handleRequest(HttpMethod.Get, "/proxy-headers"){
                addHeader("miausergroups", "group")
            }.apply {
                assertThat(headersToProxy()).isEqualTo(mapOf("miausergroups" to "group"))
            }
        }
    }

    @Test
    fun `Returns the correct header map when client-type has a value`() {
        withTestApplication {

            handleRequest(HttpMethod.Get, "/proxy-headers"){
                addHeader("client-type", "type")
            }.apply {
                assertThat(headersToProxy()).isEqualTo(mapOf("client-type" to "type"))
            }
        }
    }

    @Test
    fun `Returns the correct header map when isbackoffice has a value`() {
        withTestApplication {

            handleRequest(HttpMethod.Get, "/proxy-headers"){
                addHeader("isbackoffice", "true")
            }.apply {
                assertThat(headersToProxy()).isEqualTo(mapOf("isbackoffice" to "true"))
            }
        }
    }

    @Test
    fun `Returns the correct header map when miauserproperties has a value`() {
        withTestApplication {
            handleRequest(HttpMethod.Get, "/proxy-headers"){
                addHeader("miauserproperties", "property")
            }.apply {
                assertThat(headersToProxy()).isEqualTo(mapOf("miauserproperties" to "property"))
            }
        }
    }

    @Test
    fun `Returns the correct header map when all platform headers have value`() {
        withTestApplication {
            handleRequest(HttpMethod.Get, "/proxy-headers"){
                addHeader("x-request-id", "1234abcd")
                addHeader("miauserid", "userid")
                addHeader("miausergroups", "group")
                addHeader("miauserproperties", "property")
                addHeader("client-type", "type")
                addHeader("miauserproperties", "property")
            }.apply {
                assertThat(headersToProxy()).isEqualTo(
                    mapOf(
                        "x-request-id" to "1234abcd",
                        "miauserid" to "userid",
                        "miausergroups" to "group",
                        "miauserproperties" to "property",
                        "client-type" to "type",
                        "miauserproperties" to "property"
                    )
                )
            }
        }
    }

    @Test
    fun `Returns the header map with only headers to proxy when there are more`() {
        withTestApplication {
            handleRequest(HttpMethod.Get, "/proxy-headers"){
                addHeader("x-request-id", "1234abcd")
                addHeader("miauserid", "userid")
                addHeader("miausergroups", "group")
                addHeader("miauserproperties", "property")
                addHeader("client-type", "type")
                addHeader("miauserproperties", "property")
                addHeader("some-other-header", "other")
            }.apply {
                assertThat(headersToProxy()).isEqualTo(
                    mapOf(
                        "x-request-id" to "1234abcd",
                        "miauserid" to "userid",
                        "miausergroups" to "group",
                        "miauserproperties" to "property",
                        "client-type" to "type",
                        "miauserproperties" to "property"
                    )
                )
            }
        }
    }

    @Test
    fun `Returns the header map with additional headers to proxy if present`() {
        withTestApplication {
            handleRequest(HttpMethod.Get, "/proxy-headers"){
                addHeader("x-request-id", "1234abcd")
                addHeader("miauserid", "userid")
                addHeader("miausergroups", "group")
                addHeader("miauserproperties", "property")
                addHeader("client-type", "type")
                addHeader("miauserproperties", "property")
                addHeader("some-other-header-to-proxy", "other")
            }.apply {
                System.getProperty("ADDITIONAL_HEADERS_TO_PROXY", "some-other-header-to-proxy")

                assertThat(headersToProxy("some-other-header-to-proxy")).isEqualTo(
                    mapOf(
                        "x-request-id" to "1234abcd",
                        "miauserid" to "userid",
                        "miausergroups" to "group",
                        "miauserproperties" to "property",
                        "client-type" to "type",
                        "miauserproperties" to "property",
                        "some-other-header-to-proxy" to "other"
                    )
                )
            }
        }
    }
}
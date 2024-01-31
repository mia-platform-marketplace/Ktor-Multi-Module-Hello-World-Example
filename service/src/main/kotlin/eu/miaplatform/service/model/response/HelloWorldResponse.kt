package eu.miaplatform.service.model.response

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.Instant
import java.time.LocalDate

data class HelloWorldResponse (
    val pathParam: String?,
    val queryParam: String?,
    val helloWorld: String?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val localDate: LocalDate = LocalDate.now(),
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    val instant: Instant = Instant.now(),
)
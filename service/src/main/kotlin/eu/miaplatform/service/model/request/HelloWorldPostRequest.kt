package eu.miaplatform.service.model.request

import com.papsign.ktor.openapigen.annotations.parameters.HeaderParam
import com.papsign.ktor.openapigen.annotations.parameters.PathParam
import com.papsign.ktor.openapigen.annotations.parameters.QueryParam

data class HelloWorldPostRequest (
    @PathParam("Description of the param")
    val pathParam: String?
)
package com.cs398.team106.applicationcall

import com.cs398.team106.ErrorResponse
import com.cs398.team106.RESPONSE_ERRORS
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.serialization.SerializationException

suspend inline fun <reified T : Any> ApplicationCall.receiveOrBadRequest(): T? {
    return try {
        receive()
    } catch (e: SerializationException) {
        respond(HttpStatusCode.BadRequest, ErrorResponse(RESPONSE_ERRORS.ERR_MALFORMED, "Malformed request body!"))
        null
    }
}
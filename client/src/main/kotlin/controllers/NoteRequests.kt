package controllers

import PrivateJSONToken
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import nHttpClient
import nHttpClient.client

object NoteRequests {
    suspend fun addCollaborators(noteID: Int, emails: List<String>): Boolean {
        val httpResponse: HttpResponse = client.post(nHttpClient.URL + "/note/add_collaborator") {
            contentType(ContentType.Application.Json)
            headers.append(HttpHeaders.Authorization, "Bearer ${PrivateJSONToken.token}")
            body =
                mapOf(
                    "noteID" to JsonPrimitive(noteID),
                    "userEmails" to JsonArray(
                        emails.map { JsonPrimitive(it) }
                    )
                )

//            hashMapOf("noteID" to noteID, "userEmails" to emails)
        }
        val stringBody: String = httpResponse.receive()
//        println("REACHEEDDD")
        println(stringBody)
        println(httpResponse.status)
        if (httpResponse.status == HttpStatusCode.OK) {
//            println(stringBody)
            return true
        }
        return false
    }
}

package controllers

import NotesDTOOut
import PrivateJSONToken
import UpdateNoteData
import androidx.compose.ui.unit.IntOffset
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import nHttpClient
import nHttpClient.client
import screens.canvas.NoteData

object NoteRequests {
    suspend fun addCollaborators(noteID: Int, emails: List<String>): Boolean {
        println("IN FUNC addCollaborators")
        println(noteID)
        println(emails.toString())
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
            println(body.toString())
            println("SENT")
        }
        println("REACIVIING STRING BODY")
        val stringBody: String = httpResponse.receive()
        println("REACEIVEDL")
        println(stringBody)
        println(httpResponse.status)
        if (httpResponse.status == HttpStatusCode.OK) {
            return true
        }
        return false
    }

    suspend fun createNote(title: String, position: IntOffset): NotesDTOOut? {
        val httpResponse: HttpResponse = client.post(nHttpClient.URL + "/note") {
            contentType(ContentType.Application.Json)
            headers.append(HttpHeaders.Authorization, "Bearer ${PrivateJSONToken.token}")
            body =
                mapOf(
                    "title" to JsonPrimitive(title),
                    "positionX" to JsonPrimitive(position.x),
                    "positionY" to JsonPrimitive(position.y),
                    "plainTextContent" to JsonPrimitive(""),
                    "formattedContent" to JsonPrimitive(""),
                )
        }
        val stringBody: String = httpResponse.receive()

        return try {
            val note = Json.decodeFromString<NotesDTOOut>(stringBody)
            print(note)
            note
        } catch (t: Throwable) {
            println("Error: ${t.message}")
            null
        }
    }

    suspend fun fetchNotes(): List<NotesDTOOut>? {
        val httpResponse: HttpResponse = client.get(nHttpClient.URL + "/notes") {
            contentType(ContentType.Application.Json)
            headers.append(HttpHeaders.Authorization, "Bearer ${PrivateJSONToken.token}")
        }
        val stringBody: String = httpResponse.receive()

        return try {
            val notes = Json.decodeFromString<List<NotesDTOOut>>(stringBody)
            print(notes)
            notes
        } catch (t: Throwable) {
            println("Error: ${t.message}")
            null
        }
    }

    suspend fun updateNote(data: UpdateNoteData): Boolean {
        val (id, title, positionX, positionY, plainTextContent, formattedContent, ownerID) = data

        val requestBody = mutableMapOf<String, Any>()
        title?.let { requestBody.put("title", JsonPrimitive(it)) }
        positionX?.let { requestBody.put("positionX", JsonPrimitive(it)) }
        positionY?.let { requestBody.put("positionY", JsonPrimitive(it)) }
        plainTextContent?.let { requestBody.put("plainTextContent", JsonPrimitive(it)) }
        formattedContent?.let { requestBody.put("formattedContent", JsonPrimitive(it)) }
        ownerID?.let { requestBody.put("ownerID", JsonPrimitive(it)) }

        println("Request body: ${requestBody}")

        val httpResponse: HttpResponse = client.patch(nHttpClient.URL + "/note/" + id) {
            contentType(ContentType.Application.Json)
            headers.append(HttpHeaders.Authorization, "Bearer ${PrivateJSONToken.token}")
            body = requestBody
        }
        val stringBody: String = httpResponse.receive()

        println(stringBody)
        println(httpResponse.status)
        if (httpResponse.status == HttpStatusCode.OK) {
            return true
        }
        return false
    }
    
    suspend fun fetchNote(noteID: Int): NotesDTOOut? {
        val httpResponse: HttpResponse = client.get(nHttpClient.URL + "/note/${noteID}") {
            contentType(ContentType.Application.Json)
            headers.append(HttpHeaders.Authorization, "Bearer ${PrivateJSONToken.token}")
        }
        val stringBody: String = httpResponse.receive()

        return try {
            val note = Json.decodeFromString<NotesDTOOut>(stringBody)
            print(note)
            note
        } catch (t: Throwable) {
            println("Error: ${t.message}")
            null
        }
    }
}

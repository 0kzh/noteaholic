package controllers

import PrivateJSONToken
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nHttpClient
import nHttpClient.client

object Authentication {
    @Serializable
    data class Token(val token: String)

    suspend fun login(email: String, password: String): Boolean {

        val httpResponse: HttpResponse = client.post(nHttpClient.URL + "/login") {
            contentType(ContentType.Application.Json)
            body = hashMapOf("email" to email, "password" to password)
        }

        val stringBody: String = httpResponse.receive()

        if (httpResponse.status == HttpStatusCode.OK) {
            val res = Json.decodeFromString<Token>(stringBody)
            PrivateJSONToken.saveToAppData(res.token)
            return true
        }
        return false
    }

    suspend fun signup(firstName: String, lastName: String, email: String, password: String): Boolean {
        val httpResponse: HttpResponse = client.post(nHttpClient.URL + "/signup") {
            contentType(ContentType.Application.Json)
            body = hashMapOf("lastName" to lastName, "firstName" to firstName, "email" to email, "password" to password)
        }
        if (httpResponse.status == HttpStatusCode.Created) {
            return true
        }
        return false
    }

    fun validate(email: String, password: String): String {
        if (email.isEmpty()) {
            return "Email invalid"
        } else if (password.isEmpty()) {
            return "Password invalid"
        }
        return ""
    }
}
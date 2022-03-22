package controllers

import PrivateJSONToken
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nHttpClient
import nHttpClient.client
import TokenResponse
import ErrorResponse

object Authentication {
    // Regex used from K-9 mail (https://k9mail.app/) - standard industry email validation regex
    private val emailRegex = (
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
            ).toRegex()

    private data class PasswordTokenized(val digits: Int, val upperCase: Int, val lowerCase: Int, val special: Int)

    fun logout() {
        PrivateJSONToken.saveToAppData("")
    }

    suspend fun login(email: String, password: String, setError: (error: String) -> Unit = {}): Boolean {

        val httpResponse: HttpResponse = client.post(nHttpClient.URL + "/login") {
            contentType(ContentType.Application.Json)
            body = hashMapOf("email" to email, "password" to password)
        }

        val stringBody: String = httpResponse.receive()

        if (httpResponse.status == HttpStatusCode.OK) {
            val res = Json.decodeFromString<TokenResponse>(stringBody)
            PrivateJSONToken.saveToAppData(res.token)
            return true
        } else if (httpResponse.status == HttpStatusCode.Forbidden) {
            setError("Invalid Username or Password")
            return false
        } else {
            val res = Json.decodeFromString<ErrorResponse>(stringBody)
            setError(res.errorMessage)
        }
        return false
    }

    suspend fun signup(firstName: String, lastName: String, email: String, password: String, setError: (error: String) -> Unit): Boolean {
        val httpResponse: HttpResponse = client.post(nHttpClient.URL + "/signup") {
            contentType(ContentType.Application.Json)
            body = hashMapOf("lastName" to lastName, "firstName" to firstName, "email" to email, "password" to password)
        }
        return if (httpResponse.status == HttpStatusCode.Created) {
            true
        } else {
            val res = Json.decodeFromString<ErrorResponse>(httpResponse.receive())
            setError(res.errorMessage)
            false
        }
    }

    fun validateEmail(email: String): String? {
        return if (email.isEmpty()) {
            "Email is required"
        } else if (!emailRegex.matches(email)) {
            "Email format is invalid"
        } else {
            null
        }
    }

    private fun tokenizePassword(password: String): PasswordTokenized {
        var digitCount = 0
        var lowerCaseCount = 0
        var upperCaseCount = 0
        var specialCount = 0

        for (character in password) {
            if (character.isDigit()) {
                digitCount++
            } else if (character.isUpperCase()) {
                upperCaseCount++
            } else if (character.isLowerCase()) {
                lowerCaseCount++
            } else if (!character.isLetterOrDigit()) {
                specialCount++
            }
        }
        return PasswordTokenized(digitCount, upperCaseCount, lowerCaseCount, specialCount)
    }

    fun validatePassword(password: String): String? {
        if (password.isEmpty()) {
            return "Password is required"
        } else {
            val passwordTokenized = tokenizePassword(password)
            return if (passwordTokenized.digits < 2)
                "At least 2 digits required"
            else if (passwordTokenized.special < 1)
                "At least 1 special character required"
            else if (passwordTokenized.lowerCase < 1)
                "At least 1 lowercase character required"
            else if (passwordTokenized.upperCase < 1)
                "At least 1 uppercase character required"
            else if (password.length < 8)
                "At least 8 characters required"
            else null
        }
    }

    suspend fun isJWTValid(): Boolean {
        val httpResponse: HttpResponse = client.get(nHttpClient.URL + "/me")
        return httpResponse.status == HttpStatusCode.OK
    }
}

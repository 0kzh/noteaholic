package com.cs398.team106

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cs398.team106.authentication.UserAuthentication
import com.cs398.team106.repository.NoteRepository
import com.cs398.team106.repository.UserRepository
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NoteOperationsTest {
    companion object {
        @BeforeClass
        @JvmStatic fun setup() {
            DatabaseInit.connect(true)
            DatabaseInit.resetTables()
        }
    }

    private val testEnv = createTestEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load("application.conf"))
    }

    @Before
    fun beforeEach() {
        DatabaseInit.resetTables()
    }

    @Test
    fun testCreateNote() {
        withApplication(testEnv) {
            val jsonCreateNote = JsonObject(
                mapOf(
                    "title" to JsonPrimitive("title"),
                    "plainTextContent" to JsonPrimitive("plaintext"),
                    "formattedContent" to JsonPrimitive("formatted")
                )
            )
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = JWT.create()
                .withIssuer("http://0.0.0.0:8080/")
                .withClaim(UserAuthentication.emailClaim, "email@test.com")
                .withClaim(UserAuthentication.userIdClaim, 1)
                .withExpiresAt(Date(System.currentTimeMillis() + UserAuthentication.jwtTTL))
                .sign(Algorithm.HMAC256("secret"))
            NoteRepository.createNote("title", "plain", "formatted", 1)

            with(handleRequest(HttpMethod.Post, "/note") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
                setBody(jsonCreateNote.toString())
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
                val actualResponse: JsonObject? = response.content?.let { Json.decodeFromString(it) }
                if (actualResponse != null) {
                    assertTrue(actualResponse.keys.contains("id"))
                } else {
                    throw Error("Null response received")
                }
            }
        }
    }

    @Test
    fun testGetNote() {
        withApplication(testEnv) {
            val jsonCreateNote = JsonObject(
                mapOf(
                    "title" to JsonPrimitive("title"),
                    "plainTextContent" to JsonPrimitive("plaintext"),
                    "formattedContent" to JsonPrimitive("formatted")
                )
            )
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = JWT.create()
                .withIssuer("http://0.0.0.0:8080/")
                .withClaim(UserAuthentication.emailClaim, "email@test.com")
                .withClaim(UserAuthentication.userIdClaim, 1)
                .withExpiresAt(Date(System.currentTimeMillis() + UserAuthentication.jwtTTL))
                .sign(Algorithm.HMAC256("secret"))
            NoteRepository.createNote("title", "plain", "formatted", 1)
            with(handleRequest(HttpMethod.Get, "/note/1") {
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                val actualResponse: JsonObject? = response.content?.let { Json.decodeFromString(it) }
                if (actualResponse != null) {
                    assertTrue(actualResponse.keys.contains("id"))
                } else {
                    throw Error("Null response received")
                }
            }
        }
    }
}

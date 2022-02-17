package com.cs398.team106

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
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class UserAuthenticationTest {
    companion object {
        @BeforeClass
        @JvmStatic fun setup() {
            DatabaseInit.connect(true)
            DatabaseInit.resetTables()
        }
    }

    private val testEnv = createTestEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load("application"))
    }

    @Before
    fun beforeEach() {
        DatabaseInit.resetTables()
    }


    @Test
    fun testSignup() {
        withApplication(testEnv) {
            val jsonToSend = JsonObject(
                mapOf(
                    "email" to JsonPrimitive("email@test.com"),
                    "password" to JsonPrimitive("password"),
                    "firstName" to JsonPrimitive("fn"),
                    "lastName" to JsonPrimitive("ln")
                )
            )

            val expectedJson = JsonObject(
                mapOf(
                    "id" to JsonPrimitive(1),
                    "email" to JsonPrimitive("email@test.com"),
                    "firstName" to JsonPrimitive("fn"),
                    "lastName" to JsonPrimitive("ln")
                )
            )

            with(handleRequest(HttpMethod.Post, "/signup") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(jsonToSend.toString())
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
                // Check subset of map (ignore fields such as timestamp of last sign-in)
                val actualResponse: JsonObject? = response.content?.let { Json.decodeFromString(it) }
                if (actualResponse != null) {
                    assertTrue(actualResponse.entries.containsAll(expectedJson.entries))
                } else {
                    fail("Null response received")
                }
            }
        }
    }

    @Test
    fun testSignupMissingEmail() {
        withApplication(testEnv) {
            val jsonToSend = JsonObject(
                mapOf(
                    "password" to JsonPrimitive("password"),
                    "firstName" to JsonPrimitive("fn"),
                    "lastName" to JsonPrimitive("ln")
                )
            )

            val expectedJson = JsonObject(
                mapOf(
                    "error" to JsonPrimitive("ERR_MALFORMED"),
                    "errorMessage" to JsonPrimitive("Malformed request body!")
                )
            )

            with(handleRequest(HttpMethod.Post, "/signup") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(jsonToSend.toString())
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val actualResponse: JsonObject? = response.content?.let { Json.decodeFromString(it) }
                assertEquals(actualResponse, expectedJson)
            }
        }
    }

    @Test
    fun testSignupEmptyEmail() {
        withApplication(testEnv) {
            val jsonToSend = JsonObject(
                mapOf(
                    "email" to JsonPrimitive(""),
                    "password" to JsonPrimitive("password"),
                    "firstName" to JsonPrimitive("fn"),
                    "lastName" to JsonPrimitive("ln")
                )
            )

            val expectedJson = JsonObject(
                mapOf(
                    "error" to JsonPrimitive("ERR_EMPTY"),
                    "errorMessage" to JsonPrimitive("Email, Password, First Name and Last Name must not be empty")
                )
            )

            with(handleRequest(HttpMethod.Post, "/signup") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(jsonToSend.toString())
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val actualResponse: JsonObject? = response.content?.let { Json.decodeFromString(it) }
                assertEquals(actualResponse, expectedJson)
            }
        }
    }

    @Test
    fun testSignupInvalidEmail() {
        withApplication(testEnv) {
            val jsonToSend = JsonObject(
                mapOf(
                    "email" to JsonPrimitive("fakeemail@"),
                    "password" to JsonPrimitive("password"),
                    "firstName" to JsonPrimitive("fn"),
                    "lastName" to JsonPrimitive("ln")
                )
            )

            val expectedJson = JsonObject(
                mapOf(
                    "error" to JsonPrimitive("ERR_MALFORMED"),
                    "errorMessage" to JsonPrimitive("Invalid email")
                )
            )

            with(handleRequest(HttpMethod.Post, "/signup") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(jsonToSend.toString())
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val actualResponse: JsonObject? = response.content?.let { Json.decodeFromString(it) }
                assertEquals(actualResponse, expectedJson)
            }
        }
    }

    @Test
    fun testSignupLengthConstraint() {
        withApplication(testEnv) {
            val jsonToSend = JsonObject(
                mapOf(
                    "email" to JsonPrimitive("test@test.com"),
                    "password" to JsonPrimitive("password"),
                    "firstName" to JsonPrimitive("long-string-long-string-long-string-long-string" +
                            "-long-string-long-string-long-string-long-string-long-string-long-string-long-" +
                            "string-long-string-long-string-long-string-long-string-long-string-"),
                    "lastName" to JsonPrimitive("ln")
                )
            )

            val expectedJson = JsonObject(
                mapOf(
                    "error" to JsonPrimitive("ERR_LENGTH"),
                    "errorMessage" to JsonPrimitive("Length constraint violated")
                )
            )

            with(handleRequest(HttpMethod.Post, "/signup") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(jsonToSend.toString())
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val actualResponse: JsonObject? = response.content?.let { Json.decodeFromString(it) }
                assertEquals(actualResponse, expectedJson)
            }
        }
    }

    @Test
    fun testSignupDuplicate() {
        withApplication(testEnv) {
            val jsonToSend = JsonObject(
                mapOf(
                    "email" to JsonPrimitive("email@test.com"),
                    "password" to JsonPrimitive("password"),
                    "firstName" to JsonPrimitive("fn"),
                    "lastName" to JsonPrimitive("ln")
                )
            )
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            with(handleRequest(HttpMethod.Post, "/signup") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(jsonToSend.toString())
            }) {
                assertEquals(HttpStatusCode.Conflict, response.status())
            }
        }
    }

    @Test
    fun testSignupLogin() {
        withApplication(testEnv) {
            val loginJson = JsonObject(
                mapOf(
                    "email" to JsonPrimitive("email@test.com"),
                    "password" to JsonPrimitive("password")
                )
            )
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            with(handleRequest(HttpMethod.Post, "/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(loginJson.toString())
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                val actualResponse: JsonObject? = response.content?.let { Json.decodeFromString(it) }
                if (actualResponse != null) {
                    assertTrue(actualResponse.keys.contains("token"))
                } else {
                    fail("Null response received")
                }
            }
        }
    }

    @Test
    fun testSignupLoginEmptyPassword() {
        withApplication(testEnv) {
            val loginJson = JsonObject(
                mapOf(
                    "email" to JsonPrimitive("email@test.com"),
                    "password" to JsonPrimitive("")
                )
            )
            val expectedJson = JsonObject(
                mapOf(
                    "error" to JsonPrimitive("ERR_EMPTY"),
                    "errorMessage" to JsonPrimitive("Email and Password must not be empty")
                )
            )

            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            with(handleRequest(HttpMethod.Post, "/login") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(loginJson.toString())
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val actualResponse: JsonObject? = response.content?.let { Json.decodeFromString(it) }
                assertEquals(actualResponse, expectedJson)
            }
        }
    }
}

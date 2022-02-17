package com.cs398.team106

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
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class NoteOperationsTest {
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
    fun testCreateNote() {
        withApplication(testEnv) {
            val jsonCreateNote = JsonObject(
                mapOf(
                    "title" to JsonPrimitive("title"),
                    "plainTextContent" to JsonPrimitive("plaintext"),
                    "formattedContent" to JsonPrimitive("formatted")
                )
            )
            val expectedJson = JsonObject(
                mapOf(
                    "id" to JsonPrimitive(1),
                    "owner" to JsonPrimitive(1),
                    "title" to JsonPrimitive("title"),
                    "plainTextContent" to JsonPrimitive("plaintext"),
                    "formattedContent" to JsonPrimitive("formatted")
                )
            )
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)

            with(handleRequest(HttpMethod.Post, "/note") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
                setBody(jsonCreateNote.toString())
            }) {
                assertEquals(HttpStatusCode.Created, response.status())
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
    fun testCreateNoteEmptyTitle() {
        withApplication(testEnv) {
            val jsonCreateNote = JsonObject(
                mapOf(
                    "title" to JsonPrimitive(""),
                    "plainTextContent" to JsonPrimitive("plaintext"),
                    "formattedContent" to JsonPrimitive("formatted")
                )
            )
            val expectedJson = JsonObject(
                mapOf(
                    "error" to JsonPrimitive("ERR_MALFORMED"),
                    "errorMessage" to JsonPrimitive("Title is required")
                )
            )
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)

            with(handleRequest(HttpMethod.Post, "/note") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
                setBody(jsonCreateNote.toString())
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
                val actualResponse: JsonObject? = response.content?.let { Json.decodeFromString(it) }
                assertEquals(actualResponse, expectedJson)
            }
        }
    }

    @Test
    fun testGetNote() {
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            NoteRepository.createNote("title", "plain", "formatted", 1)
            with(handleRequest(HttpMethod.Get, "/note/1") {
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                val actualResponse: JsonObject? = response.content?.let { Json.decodeFromString(it) }
                if (actualResponse != null) {
                    assertTrue(actualResponse.keys.contains("id"))
                } else {
                    fail("Null response received")
                }
            }
        }
    }

    @Test
    fun testGetNoteNonExistent() {
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            with(handleRequest(HttpMethod.Get, "/note/1") {
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
            }) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun testGetNoteInvalidID() {
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            with(handleRequest(HttpMethod.Get, "/note/abc") {
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testUpdateNote() {
        val jsonUpdateNote = JsonObject(
            mapOf(
                "plainTextContent" to JsonPrimitive("new-plaintext")
            )
        )
        val expectedJson = JsonObject(
            mapOf(
                "id" to JsonPrimitive(1),
                "owner" to JsonPrimitive(1),
                "title" to JsonPrimitive("title"),
                "plainTextContent" to JsonPrimitive("new-plaintext"),
                "formattedContent" to JsonPrimitive("formatted")
            )
        )
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            NoteRepository.createNote("title", "plain", "formatted", 1)
            with(handleRequest(HttpMethod.Patch, "/note/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
                setBody(jsonUpdateNote.toString())
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
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
    fun testUpdateNoteNonExistent() {
        val jsonUpdateNote = JsonObject(
            mapOf(
                "plainTextContent" to JsonPrimitive("new-plaintext")
            )
        )
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            with(handleRequest(HttpMethod.Patch, "/note/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
                setBody(jsonUpdateNote.toString())
            }) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun testUpdateNoteInvalidID() {
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            with(handleRequest(HttpMethod.Patch, "/note/abc") {
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testDeleteNote() {
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            NoteRepository.createNote("title", "plain", "formatted", 1)
            with(handleRequest(HttpMethod.Delete, "/note/1") {
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                val originalNote: DBNote? = NoteRepository.getNote(1)
                assertNull(originalNote)
            }
        }
    }

    @Test
    fun testDeleteNoteNonExistent() {
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            with(handleRequest(HttpMethod.Delete, "/note/1") {
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
            }) {
                assertEquals(HttpStatusCode.NotFound, response.status())
            }
        }
    }

    @Test
    fun testDeleteNoteInvalidID() {
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            with(handleRequest(HttpMethod.Delete, "/note/abc") {
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }
}

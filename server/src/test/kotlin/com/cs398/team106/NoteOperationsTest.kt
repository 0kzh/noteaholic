package com.cs398.team106

import com.cs398.team106.Notes.formattedContent
import com.cs398.team106.repository.NOTE_ACCESS_LEVEL
import com.cs398.team106.repository.NoteRepository
import com.cs398.team106.repository.UserRepository
import com.typesafe.config.ConfigFactory
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.json.simple.JSONArray
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
                    "positionX" to JsonPrimitive(2),
                    "positionY" to JsonPrimitive(3),
                    "plainTextContent" to JsonPrimitive("plaintext"),
                    "formattedContent" to JsonPrimitive("formatted"),
                    "colour" to JsonPrimitive("#FFFFED")
                )
            )
            val expectedJson = JsonObject(
                mapOf(
                    "id" to JsonPrimitive(1),
                    "ownerID" to JsonPrimitive(1),
                    "title" to JsonPrimitive("title"),
                    "positionX" to JsonPrimitive(2),
                    "positionY" to JsonPrimitive(3),
                    "plainTextContent" to JsonPrimitive("plaintext"),
                    "formattedContent" to JsonPrimitive("formatted"),
                    "colour" to JsonPrimitive("#FFFFED")
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
                    println(actualResponse.entries)
                    println(expectedJson.entries)
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
                    "positionX" to JsonPrimitive(2),
                    "positionY" to JsonPrimitive(3),
                    "plainTextContent" to JsonPrimitive("plaintext"),
                    "formattedContent" to JsonPrimitive("formatted"),
                    "colour" to JsonPrimitive("#FFFFED")
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
            NoteRepository.createNote(
                CreateNoteData("title", 1, 2, "plain", "formatted", "#FFFFED"),
                1)
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
    fun testGetNoteNoAccess() {
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email2@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            NoteRepository.createNote(
                CreateNoteData("title", 1, 2, "plain", "formatted", "#FFFFED"),
                2)
            with(handleRequest(HttpMethod.Get, "/note/1") {
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testGetNoteReadOnlyCollaborator() {
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email2@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            NoteRepository.createNote(
                CreateNoteData("title", 1, 2, "plain", "formatted", "#FFFFED"),
                2)
            NoteRepository.addSharedNotes(1, mutableListOf(1))
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
                assertEquals(HttpStatusCode.BadRequest, response.status())
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
                "ownerID" to JsonPrimitive(1),
                "positionX" to JsonPrimitive(1),
                "positionY" to JsonPrimitive(2),
                "title" to JsonPrimitive("title"),
                "plainTextContent" to JsonPrimitive("new-plaintext"),
                "formattedContent" to JsonPrimitive("formatted"),
                "colour" to JsonPrimitive("#FFFFED")
            )
        )
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            NoteRepository.createNote(
                CreateNoteData("title", 1, 2, "plain", "formatted", "#FFFFED"),
                1)
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
    fun testUpdateNoteNoAccess() {
        val jsonUpdateNote = JsonObject(
            mapOf(
                "plainTextContent" to JsonPrimitive("new-plaintext")
            )
        )
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email2@test.com", "password")
            NoteRepository.createNote(
                CreateNoteData("title", 1, 2, "plain", "formatted", "#FFFFED"),
                2)

            val userToken = TestUtil.getJWT("email@test.com", 1)
            with(handleRequest(HttpMethod.Patch, "/note/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
                setBody(jsonUpdateNote.toString())
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testUpdateNoteNoAccessReadOnly() {
        val jsonUpdateNote = JsonObject(
            mapOf(
                "plainTextContent" to JsonPrimitive("new-plaintext")
            )
        )
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email2@test.com", "password")
            NoteRepository.createNote(
                CreateNoteData("title", 1, 2, "plain", "formatted", "#FFFFED"),
                2)
            NoteRepository.addSharedNotes(1, mutableListOf(1))

            val userToken = TestUtil.getJWT("email@test.com", 1)
            with(handleRequest(HttpMethod.Patch, "/note/1") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
                setBody(jsonUpdateNote.toString())
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
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
                assertEquals(HttpStatusCode.BadRequest, response.status())
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
            NoteRepository.createNote(
                CreateNoteData("title", 1, 2, "plain", "formatted", "#FFFFED"),
                1)
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
    fun testDeleteNoteNoAccess() {
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email2@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            NoteRepository.createNote(
                CreateNoteData("title", 1, 2, "plain", "formatted", "#FFFFED"),
                2)
            with(handleRequest(HttpMethod.Delete, "/note/1") {
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testDeleteNoteNoAccessReadOnly() {
        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email2@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            NoteRepository.createNote(
                CreateNoteData("title", 1, 2, "plain", "formatted", "#FFFFED"),
                2)
            NoteRepository.addSharedNotes(1, mutableListOf(1))
            with(handleRequest(HttpMethod.Delete, "/note/1") {
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
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
                assertEquals(HttpStatusCode.BadRequest, response.status())
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

    @Test
    fun testAddCollaborator() {
        val jsonAddCollaborator = JsonObject(
            mapOf(
                "noteID" to JsonPrimitive(1),
                "userEmails" to JsonArray(mutableListOf(
                    JsonPrimitive("email@test.com"),
                    JsonPrimitive("email2@test.com"))
                )
            )
        )
        val expectedJson = JsonArray(
            mutableListOf(
                JsonObject(
                    mapOf(
                        "id" to JsonPrimitive(1),
                        "noteID" to JsonPrimitive(1),
                        "userID" to JsonPrimitive(2),
                    )
                ),
                JsonObject(
                    mapOf(
                        "id" to JsonPrimitive(2),
                        "noteID" to JsonPrimitive(1),
                        "userID" to JsonPrimitive(3),
                    )
                )
            )
        )

        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email_owner@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email2@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            NoteRepository.createNote(
                CreateNoteData("title", 1, 2, "plain", "formatted", "#FFFFED"),
                1)
            with(handleRequest(HttpMethod.Post, "/note/add_collaborator") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
                setBody(jsonAddCollaborator.toString())
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                val actualResponse: JsonArray? = response.content?.let { Json.decodeFromString(it) }
                if (actualResponse != null) {
                    assertEquals(actualResponse, expectedJson)
                } else {
                    fail("Null response received")
                }

                val accessLevel1: String = NoteRepository.getNoteAccessLevel(1, 2)
                assertEquals(accessLevel1, NOTE_ACCESS_LEVEL.READ)
                val accessLevel2: String = NoteRepository.getNoteAccessLevel(1, 3)
                assertEquals(accessLevel2, NOTE_ACCESS_LEVEL.READ)
            }
        }
    }

    @Test
    fun testAddCollaboratorDuplicate() {
        val jsonAddCollaborator = JsonObject(
            mapOf(
                "noteID" to JsonPrimitive(1),
                "userEmails" to JsonArray(mutableListOf(
                    JsonPrimitive("email@test.com"),
                    JsonPrimitive("email2@test.com"))
                )
            )
        )
        val expectedJson = JsonArray(
            mutableListOf(
                JsonObject(
                    mapOf(
                        "id" to JsonPrimitive(1),
                        "noteID" to JsonPrimitive(1),
                        "userID" to JsonPrimitive(2),
                    )
                ),
                JsonObject(
                    mapOf(
                        "id" to JsonPrimitive(2),
                        "noteID" to JsonPrimitive(1),
                        "userID" to JsonPrimitive(3),
                    )
                )
            )
        )

        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email_owner@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email2@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            NoteRepository.createNote(
                CreateNoteData("title", 1, 2, "plain", "formatted", "#FFFFED"),
                1)
            // Here, we create the duplicate collaborator row, but we do not expect errors from this
            NoteRepository.addSharedNotes(1, mutableListOf(2))
            with(handleRequest(HttpMethod.Post, "/note/add_collaborator") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
                setBody(jsonAddCollaborator.toString())
            }) {
                assertEquals(HttpStatusCode.OK, response.status())
                val actualResponse: JsonArray? = response.content?.let { Json.decodeFromString(it) }
                if (actualResponse != null) {
                    assertEquals(actualResponse, expectedJson)
                } else {
                    fail("Null response received")
                }

                val accessLevel1: String = NoteRepository.getNoteAccessLevel(1, 2)
                assertEquals(accessLevel1, NOTE_ACCESS_LEVEL.READ)
                val accessLevel2: String = NoteRepository.getNoteAccessLevel(1, 3)
                assertEquals(accessLevel2, NOTE_ACCESS_LEVEL.READ)
            }
        }
    }

    @Test
    fun testAddCollaboratorInvalidInput() {
        val jsonAddCollaborator = JsonObject(
            mapOf(
                "noteID" to JsonPrimitive(1),
                "userEmails" to JsonArray(mutableListOf(
                    JsonPrimitive("email-fake@test.com"),
                    JsonPrimitive("email2@test.com"))
                )
            )
        )

        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email_owner@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email2@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            NoteRepository.createNote(
                CreateNoteData("title", 1, 2, "plain", "formatted", "#FFFFED"),
                1)
            with(handleRequest(HttpMethod.Post, "/note/add_collaborator") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
                setBody(jsonAddCollaborator.toString())
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }

    @Test
    fun testAddCollaboratorInvalidNoteID() {
        val jsonAddCollaborator = JsonObject(
            mapOf(
                "noteID" to JsonPrimitive(200),
                "userEmails" to JsonArray(mutableListOf(
                    JsonPrimitive("email@test.com"),
                    JsonPrimitive("email2@test.com"))
                )
            )
        )

        withApplication(testEnv) {
            UserRepository.createNewUser("fn", "ln", "email_owner@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email@test.com", "password")
            UserRepository.createNewUser("fn", "ln", "email2@test.com", "password")
            val userToken = TestUtil.getJWT("email@test.com", 1)
            NoteRepository.createNote(
                CreateNoteData("title", 1, 2, "plain", "formatted", "#FFFFED"),
                1)
            with(handleRequest(HttpMethod.Post, "/note/add_collaborator") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                addHeader(HttpHeaders.Authorization, "Bearer $userToken")
                setBody(jsonAddCollaborator.toString())
            }) {
                assertEquals(HttpStatusCode.BadRequest, response.status())
            }
        }
    }
}

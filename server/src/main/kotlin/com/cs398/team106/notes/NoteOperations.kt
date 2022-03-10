package com.cs398.team106.notes

import com.cs398.team106.*
import com.cs398.team106.applicationcall.receiveOrBadRequest
import com.cs398.team106.authentication.UserAuthentication
import com.cs398.team106.repository.NOTE_ACCESS_LEVEL
import com.cs398.team106.repository.NoteRepository
import com.cs398.team106.repository.UserRepository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.response.*


object NoteOperations {
    fun getJWTUserID(call: ApplicationCall): Int {
        val principal = call.principal<JWTPrincipal>()
        return principal!!.payload.getClaim(UserAuthentication.userIdClaim).asInt()
    }

    suspend fun createNote(call: ApplicationCall) {
        val ownerID = getJWTUserID(call)
        call.receiveOrBadRequest<CreateNoteData>()?.let { createNoteData ->
            if (!createNoteData.isValid()) {
                call.respond(
                    HttpStatusCode.BadRequest, ErrorResponse(
                        RESPONSE_ERRORS.ERR_MALFORMED, "Title is required"
                    )
                )
                return
            }
            val createdNote = NoteRepository.createNote(
                createNoteData,
                ownerID
            )

            call.respond(HttpStatusCode.Created, createdNote.toModel())
        }
    }

    suspend fun getNote(call: ApplicationCall) {
        val userAuthID = getJWTUserID(call)
        val intID = call.parameters["id"]?.toIntOrNull()
        if (intID == null) {
            call.respond(
                HttpStatusCode.BadRequest, ErrorResponse(
                    RESPONSE_ERRORS.ERR_MALFORMED, "Please provide note ID (integer)"
                )
            )
            return
        }
        val accessLevel = NoteRepository.getNoteAccessLevel(intID, userAuthID)
        if (accessLevel != NOTE_ACCESS_LEVEL.WRITE && accessLevel != NOTE_ACCESS_LEVEL.READ) {
            call.respond(
                HttpStatusCode.BadRequest, ErrorResponse(
                    RESPONSE_ERRORS.ERR_ACCESS, "Cannot retrieve note, please check your access"
                )
            )
            return
        }
        val retrievedNote = NoteRepository.getNote(intID)
        if (retrievedNote == null) {
            call.respond(
                HttpStatusCode.NotFound, ErrorResponse(
                    RESPONSE_ERRORS.ERR_NOT_FOUND, "Could not find note to retrieve"
                )
            )
            return
        }
        call.respond(HttpStatusCode.OK, retrievedNote.toModel())
    }

    suspend fun getNotes(call: ApplicationCall) {
        val userAuthID = getJWTUserID(call)
        val retrievedNotes = NoteRepository.getNotes(userAuthID)
        call.respond(HttpStatusCode.OK, retrievedNotes.map { it.toModel() })
    }

    suspend fun addSharedNotes(call: ApplicationCall) {
        val userAuthID = getJWTUserID(call)
        call.receiveOrBadRequest<CreateSharedNoteData>()?.let { createSharedNoteData ->
            val accessLevel = NoteRepository.getNoteAccessLevel(createSharedNoteData.noteID, userAuthID)
            if (accessLevel != NOTE_ACCESS_LEVEL.WRITE) {
                call.respond(
                    HttpStatusCode.BadRequest, ErrorResponse(
                        RESPONSE_ERRORS.ERR_ACCESS,
                        "Cannot add collaborators for a note you do not own!"
                    )
                )
                return
            }
            val allUsers = mutableListOf<DBUser>()
            for (userEmail in createSharedNoteData.userEmails) {
                val user = UserRepository.getUserByEmail(userEmail)
                if (user == null) {
                    call.respond(
                        HttpStatusCode.BadRequest, ErrorResponse(
                            RESPONSE_ERRORS.ERR_MALFORMED,
                            "Invalid email: $userEmail"
                        )
                    )
                    return
                }
                if (user.id.value == userAuthID) {
                    call.respond(
                        HttpStatusCode.BadRequest, ErrorResponse(
                            RESPONSE_ERRORS.ERR_MALFORMED,
                            "Cannot add owner of note as collaborator!"
                        )
                    )
                    return
                }
                allUsers.add(user)
            }
            val allUserIDs = allUsers.map { it.id.value }
            val sharedNotes = NoteRepository.addSharedNotes(
                createSharedNoteData.noteID,
                allUserIDs
            )
            if (sharedNotes == null) {
                call.respond(
                    HttpStatusCode.BadRequest, ErrorResponse(
                        RESPONSE_ERRORS.ERR_MALFORMED,
                        "Could not create shared notes (invalid IDs)!"
                    )
                )
                return
            }
            call.respond(HttpStatusCode.OK, sharedNotes.map { it.toModel() })
        }
    }

    suspend fun updateNote(call: ApplicationCall) {
        val intID = call.parameters["id"]?.toIntOrNull()
        if (intID == null) {
            call.respond(
                HttpStatusCode.BadRequest, ErrorResponse(
                    RESPONSE_ERRORS.ERR_MALFORMED, "Please provide note ID (integer)"
                )
            )
            return
        }

        val userAuthID = getJWTUserID(call)
        val accessLevel = NoteRepository.getNoteAccessLevel(intID, userAuthID)
        if (accessLevel != NOTE_ACCESS_LEVEL.WRITE) {
            call.respond(
                HttpStatusCode.BadRequest, ErrorResponse(
                    RESPONSE_ERRORS.ERR_ACCESS, "Cannot update note, please check your access"
                )
            )
            return
        }
        call.receiveOrBadRequest<UpdateNoteData>()?.let { updateNoteData ->
            val updatedNote = NoteRepository.updateNote(
                intID,
                updateNoteData
            )
            if (updatedNote == null) {
                call.respond(
                    HttpStatusCode.NotFound, ErrorResponse(
                        RESPONSE_ERRORS.ERR_NOT_FOUND, "Could not find note to update"
                    )
                )
                return
            }
            call.respond(HttpStatusCode.OK, updatedNote.toModel())
        }
    }

    suspend fun deleteNote(call: ApplicationCall) {
        val intID = call.parameters["id"]?.toIntOrNull()
        if (intID == null) {
            call.respond(
                HttpStatusCode.BadRequest, ErrorResponse(
                    RESPONSE_ERRORS.ERR_MALFORMED, "Please provide note ID (integer)"
                )
            )
            return
        }
        val userAuthID = getJWTUserID(call)
        val accessLevel = NoteRepository.getNoteAccessLevel(intID, userAuthID)
        if (accessLevel != NOTE_ACCESS_LEVEL.WRITE) {
            call.respond(
                HttpStatusCode.BadRequest, ErrorResponse(
                    RESPONSE_ERRORS.ERR_ACCESS, "Cannot delete note, please check your access"
                )
            )
            return
        }
        val deleteSuccess = NoteRepository.deleteNote(intID)
        if (!deleteSuccess) {
            call.respond(
                HttpStatusCode.NotFound, ErrorResponse(
                    RESPONSE_ERRORS.ERR_NOT_FOUND, "Could not find note to delete"
                )
            )
            return
        }
        call.respond(HttpStatusCode.OK)
    }
}
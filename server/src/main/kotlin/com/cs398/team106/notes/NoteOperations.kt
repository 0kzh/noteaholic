package com.cs398.team106.notes

import com.cs398.team106.CreateNoteData
import com.cs398.team106.ErrorResponse
import com.cs398.team106.RESPONSE_ERRORS
import com.cs398.team106.UpdateNoteData
import com.cs398.team106.applicationcall.receiveOrBadRequest
import com.cs398.team106.authentication.UserAuthentication
import com.cs398.team106.repository.NoteRepository
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.response.*


object NoteOperations {
    suspend fun createNote(call: ApplicationCall) {
        val principal = call.principal<JWTPrincipal>()
        val ownerID = principal!!.payload.getClaim(UserAuthentication.userIdClaim).asInt()
        call.receiveOrBadRequest<CreateNoteData>()?.let { createNoteData ->
            if (!createNoteData.isValid()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    RESPONSE_ERRORS.ERR_MALFORMED, "Title is required"
                ))
                return
            }
            val createdNote = NoteRepository.createNote(
                createNoteData.title,
                createNoteData.plainTextContent,
                createNoteData.formattedContent,
                ownerID
            )

            call.respond(HttpStatusCode.Created, createdNote.toModel())
        }
    }

    suspend fun getNote(call: ApplicationCall) {
        val intID = call.parameters["id"]?.toIntOrNull()
        if (intID == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                RESPONSE_ERRORS.ERR_MALFORMED, "Please provide note ID (integer)"
            ))
            return
        }
        val retrievedNote = NoteRepository.getNote(intID)
        if (retrievedNote == null) {
            call.respond(HttpStatusCode.NotFound, ErrorResponse(
                RESPONSE_ERRORS.ERR_NOT_FOUND, "Could not find note to retrieve"
            ))
            return
        }
        call.respond(HttpStatusCode.OK, retrievedNote.toModel())
    }

    suspend fun updateNote(call: ApplicationCall) {
        val intID = call.parameters["id"]?.toIntOrNull()
        if (intID == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                RESPONSE_ERRORS.ERR_MALFORMED, "Please provide note ID (integer)"
            ))
            return
        }
        call.receiveOrBadRequest<UpdateNoteData>()?.let { updateNoteData ->
            val updatedNote = NoteRepository.updateNote(intID, updateNoteData.title, updateNoteData.plainTextContent,
                updateNoteData.formattedContent, updateNoteData.ownerID)
            if (updatedNote == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(
                    RESPONSE_ERRORS.ERR_NOT_FOUND, "Could not find note to update"
                ))
                return
            }
            call.respond(HttpStatusCode.OK, updatedNote.toModel())
        }
    }

    suspend fun deleteNote(call: ApplicationCall) {
        val intID = call.parameters["id"]?.toIntOrNull()
        if (intID == null) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                RESPONSE_ERRORS.ERR_MALFORMED, "Please provide note ID (integer)"
            ))
            return
        }
        val deleteSuccess = NoteRepository.deleteNote(intID)
        if (!deleteSuccess) {
            call.respond(HttpStatusCode.NotFound, ErrorResponse(
                RESPONSE_ERRORS.ERR_NOT_FOUND, "Could not find note to delete"
            ))
            return
        }
        call.respond(HttpStatusCode.OK)
    }
}
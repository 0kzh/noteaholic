package com.cs398.team106

import kotlinx.serialization.Serializable

object RESPONSE_ERRORS {
    const val ERR_EXISTS = "ERR_EXISTS"
    const val ERR_LENGTH = "ERR_LENGTH"
    const val ERR_EMPTY = "ERR_EMPTY"
    const val ERR_NOT_FOUND = "ERR_NOT_FOUND"
    const val ERR_MALFORMED = "ERR_MALFORMED"
    const val ERR_ACCESS = "ERR_ACCESS"
    const val ERR_PASSWORD = "ERR_PASSWORD"
}

@Serializable
data class ErrorResponse(val error: String, val errorMessage: String = "", val code: Int = 0)

interface UserLoginRequest {
    val email: String
    val password: String

    fun isValid(): Boolean
}

@Serializable
data class Login(
    override val email: String,
    override val password: String
) : UserLoginRequest {
    override fun isValid(): Boolean {
        return password.isNotBlank() && email.isNotBlank()
    }
}

@Serializable
data class CreateNoteData(
    val title: String,
    val positionX: Int,
    val positionY: Int,
    val plainTextContent: String,
    val formattedContent: String,
    val colour: String
) {
    fun isValid(): Boolean {
        return title.isNotBlank()
    }
}

// Note: array of emails helps avoid N+1 issue
// and makes it easier for client (to call a single request)
@Serializable
data class CreateSharedNoteData(
    val noteID: Int,
    val userEmails: Array<String>,
) {
    // We override methods due to array type (we want to compare content)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreateSharedNoteData

        if (noteID != other.noteID) return false
        if (!userEmails.contentEquals(other.userEmails)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = noteID
        result = 31 * result + userEmails.contentHashCode()
        return result
    }
}

@Serializable
data class UpdateNoteData(
    val title: String? = null,
    val positionX: Int? = null,
    val positionY: Int? = null,
    val plainTextContent: String? = null,
    val formattedContent: String? = null,
    val colour: String? = null,
    val ownerID: Int? = null,
) {}

@Serializable
data class User(
    val firstName: String,
    val lastName: String,
    override val email: String,
    override val password: String
) : UserLoginRequest {

    override fun isValid(): Boolean {
        return email.isNotBlank() && password.isNotBlank() && firstName.isNotBlank() && lastName.isNotBlank()
    }
}

@Serializable
data class UserDTOOut(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val lastSignInDate: String
)

@Serializable
data class NotesDTOOut(
    val id: Int,
    val title: String,
    val positionX: Int,
    val positionY: Int,
    val plainTextContent: String,
    val formattedContent: String,
    val colour: String,
    val createdAt: String,
    val modifiedAt: String,
    val ownerID: Int,
)

@Serializable
data class SearchNoteDTOOut(val note: NotesDTOOut, val matchingBody: String)

@Serializable
data class SharedNotesDTOOut(
    val id: Int,
    val noteID: Int,
    val userID: Int,
)

@Serializable
data class TokenResponse(val token: String)
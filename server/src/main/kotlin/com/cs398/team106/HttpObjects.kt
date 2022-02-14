package com.cs398.team106

import kotlinx.serialization.Serializable

object RESPONSE_ERRORS {
    const val ERR_EXISTS = "ERR_EXISTS"
    const val ERR_LENGTH = "ERR_LENGTH"
    const val ERR_EMPTY = "ERR_EMPTY"
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
data class Note(
    val title: String,
    val plainTextContent: String,
    val formattedContent: String,
    val ownerID: Int,
) {
    fun isValid(): Boolean {
        return title.isNotBlank()
    }
}

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
    val plainText: String,
    val formattedContent: String,
    val createdAt: String,
    val modifiedAt: String,
    val owner: Int,
)

@Serializable
data class TokenResponse(val token: String)
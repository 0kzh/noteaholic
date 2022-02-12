package com.cs398.team106

import kotlinx.serialization.Serializable

interface UserLogin {
    val email: String
    val password: String

    fun isValid(): Boolean
}

@Serializable
data class Login(
    override val email: String,
    override val password: String
) : UserLogin {
    override fun isValid(): Boolean {
        return password.isNotBlank() && email.isNotBlank()
    }
}

@Serializable
data class User(
    val firstName: String,
    val lastName: String,
    override val email: String,
    override val password: String
) : UserLogin {

    override fun isValid(): Boolean {
        return email.isNotBlank() && password.isNotBlank() && firstName.isNotBlank() && lastName.isNotBlank()
    }
}

@Serializable
data class TokenResponse(val token: String)
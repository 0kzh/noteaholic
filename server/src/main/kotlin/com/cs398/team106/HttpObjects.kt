package com.cs398.team106

import kotlinx.serialization.Serializable

interface UserLogin {
    val email: String
    val password: String
}

@Serializable
data class Login(
    override val email: String,
    override val password: String
) : UserLogin

@Serializable
data class User(
    val firstName: String,
    val lastName: String,
    override val email: String,
    override val password: String
) : UserLogin
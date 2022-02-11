package com.cs398.team106.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cs398.team106.Login
import com.cs398.team106.User
import com.cs398.team106.Users
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

object UserAuthentication {
    suspend fun signUp(call: ApplicationCall) {
        val userSignup = call.receive<User>()
        if (!userSignup.isValid()) {
            call.respond(HttpStatusCode.BadRequest)
        } else {
            val hashedPassword = BCrypt.hashpw(userSignup.password, BCrypt.gensalt(15))
            transaction {
                Users.insert {
                    it[lastName] = userSignup.firstName
                    it[firstName] = userSignup.lastName
                    it[email] = userSignup.email
                    it[password] = hashedPassword
                }
            }
            call.respond(HttpStatusCode.Created)
        }
    }

    suspend fun login(issuer: String, secret: String, call: ApplicationCall) {
        val userLogin = call.receive<Login>()
        if (userLogin.email.isEmpty() || userLogin.password.isEmpty() || !isUserAuthenticated(userLogin)) {
            call.respond(HttpStatusCode.Forbidden)
        } else {
            val token = JWT.create()
                .withIssuer(issuer)
                .withClaim("email", userLogin.email)
                .withExpiresAt(Date(System.currentTimeMillis() + 600000))
                .sign(Algorithm.HMAC256(secret))
            call.respond(hashMapOf("token" to token))
        }
    }

    private fun isUserAuthenticated(userLogin: Login): Boolean {
        return transaction {
            val dbUser = Users.select { Users.email eq userLogin.email }.single()
            val dbPasswordHash = dbUser[Users.password]
            return@transaction BCrypt.checkpw(userLogin.password, dbPasswordHash)
        }
    }
}
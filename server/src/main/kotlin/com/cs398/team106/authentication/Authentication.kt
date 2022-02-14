package com.cs398.team106.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cs398.team106.*
import com.cs398.team106.repository.UserRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import org.mindrot.jbcrypt.BCrypt
import java.util.*
import kotlin.time.Duration.Companion.hours


object UserAuthentication {
    private val jwtTTL = 1.hours.inWholeMilliseconds

    suspend fun signUp(call: ApplicationCall) {
        val userSignup = call.receive<User>()
        if (!userSignup.isValid() || !isUserDataInRange(userSignup)) {
            call.respond(HttpStatusCode.BadRequest)
        } else {
            if (UserRepository.getUserByEmail(userSignup.email) != null) {
                call.respond(HttpStatusCode.Conflict)
                return
            }
            UserRepository.createNewUser(
                userSignup.firstName,
                userSignup.lastName,
                userSignup.email,
                userSignup.password
            )

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
                .withExpiresAt(Date(System.currentTimeMillis() + jwtTTL))
                .sign(Algorithm.HMAC256(secret))
            call.respond(TokenResponse(token))
        }
    }

    private fun isUserDataInRange(user: User): Boolean {
        return DatabaseFieldLimits.isInRange(user.firstName, DatabaseFieldLimits.nameLength) &&
                DatabaseFieldLimits.isInRange(user.lastName, DatabaseFieldLimits.nameLength) &&
                DatabaseFieldLimits.isInRange(user.password, DatabaseFieldLimits.passwordLength) &&
                DatabaseFieldLimits.isInRange(user.email, DatabaseFieldLimits.emailLength)
    }

    private fun isUserAuthenticated(userLogin: Login): Boolean {
        val dbUser = UserRepository.getUserByEmail(userLogin.email)
        return if (dbUser != null) {
            val dbPasswordHash = dbUser[Users.password]
            BCrypt.checkpw(userLogin.password, dbPasswordHash)
        } else {
            false
        }
    }
}
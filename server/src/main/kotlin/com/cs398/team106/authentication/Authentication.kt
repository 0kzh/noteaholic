package com.cs398.team106.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cs398.team106.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*
import kotlin.time.Duration.Companion.hours


object UserAuthentication {
    private const val numberOfSaltGenerationRounds = 12
    private val jwtTTL = 1.hours.inWholeMilliseconds

    suspend fun signUp(call: ApplicationCall) {
        val userSignup = call.receive<User>()
        if (!userSignup.isValid() || !isUserDataInRange(userSignup)) {
            call.respond(HttpStatusCode.BadRequest)
        } else {
            var userExists = false
            transaction {
                userExists = getUser(userSignup.email) != null
            }
            if (userExists) {
                call.respond(HttpStatusCode.Conflict)
                return
            }
            val hashedPassword = BCrypt.hashpw(userSignup.password, BCrypt.gensalt(numberOfSaltGenerationRounds))
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
        return transaction {
            val dbUser = getUser(userLogin.email)
            return@transaction if (dbUser != null) {
                val dbPasswordHash = dbUser[Users.password]
                BCrypt.checkpw(userLogin.password, dbPasswordHash)
            } else {
                false
            }
        }
    }

    private fun getUser(email: String) =
        Users.select { Users.email eq email }.singleOrNull()
}
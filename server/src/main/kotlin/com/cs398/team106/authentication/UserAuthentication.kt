package com.cs398.team106.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cs398.team106.*
import com.cs398.team106.repository.UserRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import kotlinx.serialization.SerializationException
import org.mindrot.jbcrypt.BCrypt
import java.util.*
import kotlin.time.Duration.Companion.hours

object UserAuthentication {
    private val jwtTTL = 1.hours.inWholeMilliseconds
    private const val emailClaim = "email"
    private const val userIdClaim = "userId"

    suspend fun signUp(call: ApplicationCall) {
        val userSignup: User;
        try {
            userSignup = call.receive<User>()
        } catch (e: SerializationException) {
            call.respond(HttpStatusCode.BadRequest)
            return
        }

        if (!userSignup.isValid()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    RESPONSE_ERRORS.ERR_EMPTY,
                    "Email, Password, First Name and Last Name must not be empty "
                )
            )
        } else if (!isUserDataValid(userSignup)) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(RESPONSE_ERRORS.ERR_LENGTH, "Length constraint violated")
            )
        } else {
            if (UserRepository.getUserByEmail(userSignup.email) != null) {
                call.respond(
                    HttpStatusCode.Conflict,
                    ErrorResponse(RESPONSE_ERRORS.ERR_EXISTS, "User already registered")
                )
                return
            }
            val newUser = UserRepository.createNewUser(
                userSignup.firstName,
                userSignup.lastName,
                userSignup.email,
                userSignup.password
            )

            call.respond(HttpStatusCode.Created, newUser.toModel())
        }
    }

    suspend fun login(issuer: String, secret: String, call: ApplicationCall) {
        val userLogin: Login;
        try {
            userLogin = call.receive()
        } catch (e: SerializationException) {
            call.respond(HttpStatusCode.BadRequest)
            return
        }

        if (userLogin.email.isEmpty() || userLogin.password.isEmpty()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(RESPONSE_ERRORS.ERR_EMPTY, "Email and Password must not be empty")
            )
            return
        }
        val dbUser = UserRepository.getUserByEmail(userLogin.email)
        if (!isUserAuthenticated(dbUser, userLogin)) {
            call.respond(HttpStatusCode.Forbidden)
            return
        }
        UserRepository.login(dbUser!!)
        val token = JWT.create()
            .withIssuer(issuer)
            .withClaim(emailClaim, userLogin.email)
            .withClaim(userIdClaim, dbUser.id.value)
            .withExpiresAt(Date(System.currentTimeMillis() + jwtTTL))
            .sign(Algorithm.HMAC256(secret))
        call.respond(TokenResponse(token))
    }

    private fun isUserDataValid(user: User): Boolean {
        if (!validateEmail(user.email)) {
            return false
        }
        return DatabaseFieldLimits.isInRange(user.firstName, DatabaseFieldLimits.nameLength) &&
                DatabaseFieldLimits.isInRange(user.lastName, DatabaseFieldLimits.nameLength) &&
                DatabaseFieldLimits.isInRange(user.password, DatabaseFieldLimits.passwordLength) &&
                DatabaseFieldLimits.isInRange(user.email, DatabaseFieldLimits.emailLength)
    }

    private fun isUserAuthenticated(dbUser: DBUser?, userLoginRequest: UserLoginRequest): Boolean {
        return if (dbUser != null) {
            val dbPasswordHash = dbUser.password
            BCrypt.checkpw(userLoginRequest.password, dbPasswordHash)
        } else {
            false
        }
    }

    private fun validateEmail(email: String): Boolean {
        // Regex used from K-9 mail (https://k9mail.app/) - standard industry email validation regex
        val regex = (
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                        ")+"
                ).toRegex()
        return regex.matches(email)
    }
}
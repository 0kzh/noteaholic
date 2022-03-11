package com.cs398.team106.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cs398.team106.*
import com.cs398.team106.applicationcall.receiveOrBadRequest
import com.cs398.team106.repository.UserRepository
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import org.mindrot.jbcrypt.BCrypt
import java.util.*
import kotlin.time.Duration.Companion.hours

object UserAuthentication {
    val jwtTTL = 1.hours.inWholeMilliseconds
    const val emailClaim = "email"
    const val userIdClaim = "userId"
    const val name = "name"

    private data class PasswordTokenized(val digits: Int, val upperCase: Int, val lowerCase: Int, val special: Int)

    suspend fun signUp(call: ApplicationCall) {
        call.receiveOrBadRequest<User>()?.let { userSignup ->
            if (!userSignup.isValid()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(
                        RESPONSE_ERRORS.ERR_EMPTY, "Email, Password, First Name and Last Name must not be empty"
                    )
                )
            } else if (!validateEmail(userSignup.email)) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(RESPONSE_ERRORS.ERR_MALFORMED, "Invalid email")
                )
            } else if (!isUserDataValid(userSignup)) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(RESPONSE_ERRORS.ERR_LENGTH, "Length constraint violated")
                )
            } else {
                val passwordError = validatePassword(userSignup.password)
                if (passwordError != null) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(RESPONSE_ERRORS.ERR_PASSWORD, passwordError)
                    )
                    return

                } else if (UserRepository.getUserByEmail(userSignup.email) != null) {
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
    }

    suspend fun login(issuer: String, secret: String, call: ApplicationCall) {
        call.receiveOrBadRequest<Login>()?.let { userLogin ->
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
                .withClaim(name, "${dbUser.firstName} ${dbUser.lastName}")
                .withExpiresAt(Date(System.currentTimeMillis() + jwtTTL))
                .sign(Algorithm.HMAC256(secret))
            call.respond(TokenResponse(token))
        }
    }

    private fun isUserDataValid(user: User): Boolean {
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

    private fun tokenizePassword(password: String): PasswordTokenized {
        var digitCount = 0
        var lowerCaseCount = 0
        var upperCaseCount = 0
        var specialCount = 0

        for (character in password) {
            if (character.isDigit()) {
                digitCount++
            } else if (character.isUpperCase()) {
                upperCaseCount++
            } else if (character.isLowerCase()) {
                lowerCaseCount++
            } else if (!character.isLetterOrDigit()) {
                specialCount++
            }
        }
        return PasswordTokenized(digitCount, upperCaseCount, lowerCaseCount, specialCount)
    }

    private fun validatePassword(password: String): String? {
        if (password.isEmpty()) {
            return "Password is required"
        } else {
            val passwordTokenized = tokenizePassword(password)
            return if (passwordTokenized.digits < 2)
                "At least 2 digits required"
            else if (passwordTokenized.special < 1)
                "At least 1 special character required"
            else if (passwordTokenized.lowerCase < 1)
                "At least 1 lowercase character required"
            else if (passwordTokenized.upperCase < 1)
                "At least 1 uppercase character required"
            else if (password.length < 8)
                "At least 8 characters required"
            else null
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

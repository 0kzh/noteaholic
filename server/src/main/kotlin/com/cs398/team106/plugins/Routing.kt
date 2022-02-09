package com.cs398.team106.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cs398.team106.Login
import com.cs398.team106.User
import com.cs398.team106.Users
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*


fun Application.configureRouting() {
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }

    // Authentication Routes
    routing {
        post("/signup") {
            val userSignup = call.receive<User>()
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
        post("/login") {
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

        authenticate {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("email").asString()
                val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
                call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
            }
        }

    }

}

private fun isUserAuthenticated(userLogin: Login): Boolean {
    return transaction {
        val dbUser = Users.select { Users.email eq userLogin.email }.single()
        val dbPasswordHash = dbUser[Users.password]
        return@transaction BCrypt.checkpw(userLogin.password, dbPasswordHash)
    }
}

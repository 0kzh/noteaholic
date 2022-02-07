package com.cs398.team106.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import java.util.*


fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
    // Authentication Routes
    routing {
        post("/login") {
            call.respond(hashMapOf("token" to "tokenTemp"))
//            val user = call.receive<User>()
//            // Check username and password
//            // ...
//            val token = JWT.create()
//                .withIssuer(issuer)
//                .withClaim("username", user.username)
//                .withExpiresAt(Date(System.currentTimeMillis() + 60000))
//                .sign(Algorithm.HMAC256(secret))
//            call.respond(hashMapOf("token" to token))
        }
    }

}

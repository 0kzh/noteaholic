package com.cs398.team106.plugins

import com.cs398.team106.authentication.UserAuthentication
import com.cs398.team106.notes.NoteOperations
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.response.*
import io.ktor.routing.*


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
            UserAuthentication.signUp(call)
        }

        post("/login") {
            UserAuthentication.login(issuer, secret, call)
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

    // Note Routes
    routing {
        authenticate {
            post("/note") {
                NoteOperations.createNote(call)
            }

            get("/search") {
                NoteOperations.searchNote(call)
            }

            post("/note/add_collaborator") {
                NoteOperations.addSharedNotes(call)
            }

            get("/note/{id}") {
                NoteOperations.getNote(call)
            }

            get("/notes") {
                NoteOperations.getNotes(call)
            }

            patch("/note/{id}") {
                NoteOperations.updateNote(call)
            }

            delete("/note/{id}") {
                NoteOperations.deleteNote(call)
            }
        }
    }
}



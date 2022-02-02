package com.cs398.team106

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.cs398.team106.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
    }.start(wait = true)
}

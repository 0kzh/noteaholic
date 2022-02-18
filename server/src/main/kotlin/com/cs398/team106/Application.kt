package com.cs398.team106

import com.cs398.team106.plugins.configureAuthentication
import com.cs398.team106.plugins.configureRouting
import com.cs398.team106.plugins.configureSerialization
import io.ktor.application.*
import io.ktor.server.jetty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    DatabaseInit.connect(environment.config.property("ktor.isTest").getString().toBooleanStrict())
    DatabaseInit.createTablesIfNotExist()
    configureSerialization()
    configureAuthentication()
    configureRouting()
}

package com.cs398.team106

import com.cs398.team106.plugins.configureRouting
import com.cs398.team106.plugins.configureSerialization
import io.ktor.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module(testing: Boolean = false) {
    DatabaseInit.connect()
    DatabaseInit.createTablesIfNotExist()
    configureSerialization()
    configureRouting()
}

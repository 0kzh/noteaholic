package com.cs398.team106

import com.typesafe.config.ConfigFactory
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseInit {
    fun connect() {
        val conf = ConfigFactory.load("database").getConfig("database")
        Database.connect(
            conf.getString("connectionString"),
            driver = conf.getString("driver"),
            user = conf.getString("username"),
            password = conf.getString("password")
        )
    }

    fun createTablesIfNotExist() {
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users)
            SchemaUtils.create(Notes)
            SchemaUtils.create(Titles)
            SchemaUtils.create(SharedNotes)
            SchemaUtils.create(CanvasObjects)
        }
    }
}
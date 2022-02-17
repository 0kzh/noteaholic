package com.cs398.team106

import com.typesafe.config.ConfigFactory
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.DriverManager

object DatabaseInit {
    fun connect(testing: Boolean = true) {
        val conf = ConfigFactory.load("application")
        if (testing) {
            val keepAliveConnection = DriverManager.getConnection(conf.getString("database.connectionString"))
            Database.connect(
                conf.getString("database.connectionString"),
                driver = conf.getString("database.driver")
            )
        } else {
            Database.connect(
                conf.getString("database.connectionString"),
                driver = conf.getString("database.driver"),
                user = conf.getString("database.username"),
                password = conf.getString("database.password")
            )
        }
    }

    fun resetTables() {
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.drop(Users)
            SchemaUtils.drop(Notes)
            SchemaUtils.drop(Titles)
            SchemaUtils.drop(SharedNotes)
            SchemaUtils.drop(CanvasObjects)
        }
        createTablesIfNotExist()
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
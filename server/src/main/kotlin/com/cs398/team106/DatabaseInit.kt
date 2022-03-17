package com.cs398.team106

import com.typesafe.config.ConfigFactory
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.vendors.currentDialect
import java.sql.DriverManager

object DatabaseInit {
    fun connect(testing: Boolean = false) {
        val conf = ConfigFactory.load("application")
        if (testing) {
            DriverManager.getConnection(conf.getString("database.connectionString"))
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
            if (currentDialect.name == "postgresql") {
                exec("DROP FUNCTION updatesearchindextrigger()")
            }
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
            if (currentDialect.name == "postgresql") {
                exec(
                    """CREATE OR REPLACE FUNCTION updateSearchIndexTrigger() RETURNS TRIGGER AS $$
                    begin
                        new.search_tokenized := setweight(to_tsvector('pg_catalog.english', new.title), 'A') ||
                            setweight(to_tsvector('pg_catalog.english', new.plaintext_content), 'B');
                        return new;
                    end
                    $$ LANGUAGE plpgsql;
                """.trimIndent()
                )
                exec("CREATE OR REPLACE TRIGGER update_search_tokenized BEFORE INSERT OR UPDATE ON Notes FOR EACH ROW EXECUTE PROCEDURE updateSearchIndexTrigger()")
            }
        }
    }
}
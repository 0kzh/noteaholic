package com.cs398.team106.repository

import com.cs398.team106.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object UserRepository {
    fun getUserByEmail(email: String): ResultRow? {
        return transaction {
            return@transaction Users.select { Users.email eq email }.singleOrNull()
        }
    }

    fun createNewUser(firstName: String, lastName: String, email: String, password: String) {
        transaction {
            Users.insert {
                it[Users.lastName] = firstName
                it[Users.firstName] = lastName
                it[Users.email] = email
                it[Users.password] = password
            }
        }
    }
}
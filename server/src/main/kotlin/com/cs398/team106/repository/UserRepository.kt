package com.cs398.team106.repository

import com.cs398.team106.DBUser
import com.cs398.team106.Users
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

object UserRepository {
    private const val numberOfSaltGenerationRounds = 12

    fun getUserByEmail(email: String): DBUser? {

        return transaction {
            val users = DBUser.find { Users.email eq email }
            return@transaction users.firstOrNull()
        }
    }

    fun createNewUser(firstName: String, lastName: String, email: String, password: String): DBUser {
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt(numberOfSaltGenerationRounds))

        return transaction {
            DBUser.new {
                this.firstName = firstName
                this.lastName = lastName
                this.email = email
                this.password = hashedPassword
                this.lastSignInDate = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            }
        }
    }

    fun login(dbUser: DBUser) {
        transaction {
            dbUser.lastSignInDate = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        }
    }
}
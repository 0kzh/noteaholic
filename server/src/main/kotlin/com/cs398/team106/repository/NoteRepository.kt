package com.cs398.team106.repository

import com.cs398.team106.DBNote
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.transactions.transaction

object NoteRepository {
    fun getNote(id: Int): DBNote? {
        return transaction {
            return@transaction DBNote.findById(id)
        }
    }

    fun createNote(title: String, plainTextContent: String, formattedContent: String, ownerID: Int): DBNote {
        return transaction {
            return@transaction DBNote.new {
                this.title = title
                this.plainTextContent = plainTextContent
                this.formattedContent = formattedContent
                this.createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                this.modifiedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                this.owner = ownerID
            }
        }
    }

    fun updateNote(id: Int, title: String?, plainTextContent: String?, formattedContent: String?, ownerID: Int?): DBNote? {
        return transaction {
            val note = DBNote.findById(id) ?: return@transaction null
            title?.let { note.title = it }
            plainTextContent?.let { note.plainTextContent = it }
            formattedContent?.let { note.formattedContent = it }
            ownerID?.let { note.owner = it }
            note.modifiedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            return@transaction note
        }
    }

    fun deleteNote(id: Int): Boolean {
        return transaction {
            val note = DBNote.findById(id) ?: return@transaction false
            note.delete()
            return@transaction true
        }
    }
}

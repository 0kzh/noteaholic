package com.cs398.team106.repository

import com.cs398.team106.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

object NOTE_ACCESS_LEVEL {
    const val WRITE = "WRITE"
    const val READ = "READ"
    const val NONE = "NONE"
}

object NoteRepository {
    fun getNote(id: Int): DBNote? {
        return transaction {
            return@transaction DBNote.findById(id)
        }
    }

    fun getNotes(userID: Int): List<DBNote> {
        return transaction {
            return@transaction DBNote.find {
                Notes.owner eq userID
            }.toList()
        }
    }

    fun createNote(
        data: CreateNoteData, ownerID: Int
    ): DBNote {
        val (title, positionX, positionY, plainTextContent, formattedContent) = data
        return transaction {
            return@transaction DBNote.new {
                this.title = title
                this.positionX = positionX
                this.positionY = positionY
                this.plainTextContent = plainTextContent
                this.formattedContent = formattedContent
                this.createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                this.modifiedAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
                this.owner = ownerID
            }
        }
    }

    fun getNoteAccessLevel(noteID: Int, userID: Int): String {
        return transaction {
            val dbNote = DBNote.findById(noteID) ?: return@transaction NOTE_ACCESS_LEVEL.NONE
            if (dbNote.owner == userID) {
                return@transaction NOTE_ACCESS_LEVEL.WRITE
            } else {
                val sharedNotes =
                    DBSharedNote.find { (SharedNotes.noteId eq noteID) and (SharedNotes.userId eq userID) }
                if (sharedNotes.count() != 0L) {
                    return@transaction NOTE_ACCESS_LEVEL.READ
                } else {
                    return@transaction NOTE_ACCESS_LEVEL.NONE
                }
            }
        }
    }

    fun searchInNotes(query: String, ownerId: Int, limit: Int?): List<DBNote> {
        val sqlExpression = (Notes.owner eq ownerId) and (Notes.noteSearchTokenized tsVector query)
        return transaction {
            val res = DBNote.find { sqlExpression }
            (if (limit != null) res.limit(limit) else res).toList()
        }
    }

    fun addSharedNotes(noteID: Int, userIDs: List<Int>): MutableList<DBSharedNote>? {
        return transaction {
            val sharedDbNotesOutput = mutableListOf<DBSharedNote>()
            val dbNotes = DBSharedNote.find { (SharedNotes.noteId eq noteID) and (SharedNotes.userId inList userIDs) }

            for (userID in userIDs) {
                val filteredList = dbNotes.filter { it.userId == userID }
                if (filteredList.isNotEmpty()) {
                    sharedDbNotesOutput.add(filteredList.first())
                } else {
                    // Note: this will throw error if note ID or user ID isn't valid (foreign key)
                    try {
                        val createdSharedNote = DBSharedNote.new {
                            this.noteId = noteID
                            this.userId = userID
                        }
                        sharedDbNotesOutput.add(createdSharedNote)
                    } catch (e: Throwable) {
                        return@transaction null
                    }
                }

            }
            return@transaction sharedDbNotesOutput
        }
    }

    fun updateNote(
        id: Int,
        data: UpdateNoteData,
    ): DBNote? {
        val (title, positionX, positionY, plainTextContent, formattedContent, ownerID) = data
        return transaction {
            val note = DBNote.findById(id) ?: return@transaction null
            title?.let { note.title = it }
            positionX?.let { note.positionX = it }
            positionY?.let { note.positionY = it }
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

package com.cs398.team106

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime


private const val nameLength = 128
private const val titleLength = 255
private const val emailLength = 255
private const val passwordLength = 512

object Users : IntIdTable() {
    val firstName = varchar("first_name", nameLength)
    val lastName = varchar("last_name", nameLength)
    val email = varchar("email", emailLength).uniqueIndex()
    val password = varchar("password", passwordLength)
    val lastSignInDate = datetime("last_sign_in_date")
}

object Notes : IntIdTable() {
    val title = varchar("title", titleLength)
    val plainTextContent = text("plaintext_content")
    val formattedContent = text("formatted_content")
    val createdAt = datetime("created_at")
    val modifiedAt = datetime("modified_at")
    val owner = integer("owner").references(Users.id)

}

object SharedNotes : Table("shared_notes") {
    val noteId = integer("note_id").references(Notes.id)
    val userId = integer("user_id").references(Users.id)
}

object Titles : IntIdTable() {
    val text = varchar("title", titleLength)
}

object CanvasObjects : IntIdTable("canvas_objects") {
    val positionX = integer("position_x")
    val positionY = integer("position_y")

    /* TODO: This is supposed to be an enumeration
             However this is currently a bit tricky to setup so for now it is a boolean
             In the future this should be changed to:
             val type which is an enum of 'note', 'title'
     */
    val isNote = bool("is_note")

    // TODO: This needs a one of is null constraint
    val noteId = integer("note_id").references(Notes.id).nullable()
    val titleId = integer("title_id").references(Titles.id).nullable()
}

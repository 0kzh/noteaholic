package com.cs398.team106

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.wrap
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import org.jetbrains.exposed.sql.vendors.currentDialect


object TSVector : ColumnType() {
    override fun sqlType(): String {
        return when (currentDialect.name.lowercase()) {
            "postgresql" -> {
                "tsvector"
            }
            else -> {
                "text"
            }
        }
    }
}

infix fun <T> ExpressionWithColumnType<T>.tsVector(t: T): Op<Boolean> = TsVectorOp(this, wrap(t))

class TsVectorOp(
    val expr1: Expression<*>,
    val expr2: Expression<*>,
    val tsQueryFunction: String = "websearch_to_tsquery"
) : Op<Boolean>(), ComplexExpression {
    override fun toQueryBuilder(queryBuilder: QueryBuilder): Unit = queryBuilder {
        if (expr1 is ComplexExpression) {
            append("(", expr1, ")")
        } else {
            append(expr1)
        }
        append(
            " @@ $tsQueryFunction("
        )
        append(expr2)
        append(")")
    }
}

fun Table.tsVector(name: String): Column<Any> = registerColumn(name, TSVector)

object DatabaseFieldLimits {
    const val nameLength = 128
    const val titleLength = 255
    const val emailLength = 255
    const val passwordLength = 60

    fun isInRange(input: String, maximumLength: Int): Boolean {
        return input.length <= maximumLength
    }
}

object Users : IntIdTable() {
    val firstName = varchar("first_name", DatabaseFieldLimits.nameLength)
    val lastName = varchar("last_name", DatabaseFieldLimits.nameLength)
    val email = varchar("email", DatabaseFieldLimits.emailLength).uniqueIndex()
    val password = varchar("password", DatabaseFieldLimits.passwordLength)
    val lastSignInDate = datetime("last_sign_in_date").nullable()
}

class DBUser(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DBUser>(Users)

    var firstName by Users.firstName
    var lastName by Users.lastName
    var email by Users.email
    var password by Users.password
    var lastSignInDate by Users.lastSignInDate

    fun toModel(): UserDTOOut {
        return UserDTOOut(id.value, firstName, lastName, email, lastSignInDate.toString())
    }
}

object Notes : IntIdTable() {
    val title = varchar("title", DatabaseFieldLimits.titleLength)
    val positionX = integer("position_x")
    val positionY = integer("position_y")
    val plainTextContent = text("plaintext_content")
    val formattedContent = text("formatted_content")
    val createdAt = datetime("created_at")
    val modifiedAt = datetime("modified_at")
    val owner = integer("owner").references(Users.id)
    val noteSearchTokenized = tsVector("search_tokenized").nullable()


}

class DBNote(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DBNote>(Notes)

    var title by Notes.title
    var positionX by Notes.positionX
    var positionY by Notes.positionY
    var plainTextContent by Notes.plainTextContent
    var formattedContent by Notes.formattedContent
    var createdAt by Notes.createdAt
    var modifiedAt by Notes.modifiedAt
    var owner by Notes.owner

    fun toModel(): NotesDTOOut {
        return NotesDTOOut(
            id.value,
            title,
            positionX,
            positionY,
            plainTextContent,
            formattedContent,
            createdAt.toString(),
            modifiedAt.toString(),
            owner
        )
    }
}

object SharedNotes : IntIdTable("shared_notes") {
    val noteId = integer("note_id").references(Notes.id)
    val userId = integer("user_id").references(Users.id)

    init {
        uniqueIndex("shared_note_unique_index", noteId, userId)
    }
}

class DBSharedNote(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<DBSharedNote>(SharedNotes)

    var noteId by SharedNotes.noteId
    var userId by SharedNotes.userId

    fun toModel(): SharedNotesDTOOut {
        return SharedNotesDTOOut(id.value, noteId, userId)
    }
}

object Titles : IntIdTable() {
    val text = varchar("title", DatabaseFieldLimits.titleLength)
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

import kotlinx.serialization.Serializable

object RESPONSE_ERRORS {
    const val ERR_EXISTS = "ERR_EXISTS"
    const val ERR_LENGTH = "ERR_LENGTH"
    const val ERR_EMPTY = "ERR_EMPTY"
    const val ERR_NOT_FOUND = "ERR_NOT_FOUND"
    const val ERR_MALFORMED = "ERR_MALFORMED"
}

@Serializable
data class ErrorResponse(val error: String, val errorMessage: String = "", val code: Int = 0)

interface UserLoginRequest {
    val email: String
    val password: String

    fun isValid(): Boolean
}

@Serializable
data class Login(
    override val email: String,
    override val password: String
) : UserLoginRequest {
    override fun isValid(): Boolean {
        return password.isNotBlank() && email.isNotBlank()
    }
}

@Serializable
data class CreateNoteData(
    val title: String,
    val plainTextContent: String,
    val formattedContent: String,
) {
    fun isValid(): Boolean {
        return title.isNotBlank()
    }
}

@Serializable
data class UpdateNoteData(
    val id: Int,
    val title: String? = null,
    val positionX: Int? = null,
    val positionY: Int? = null,
    val plainTextContent: String? = null,
    val formattedContent: String? = null,
    val ownerID: Int? = null,
) {}

@Serializable
data class User(
    val firstName: String,
    val lastName: String,
    override val email: String,
    override val password: String
) : UserLoginRequest {

    override fun isValid(): Boolean {
        return email.isNotBlank() && password.isNotBlank() && firstName.isNotBlank() && lastName.isNotBlank()
    }
}

@Serializable
data class UserDTOOut(
    val id: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val lastSignInDate: String
)

@Serializable
data class NotesDTOOut(
    var id: Int,
    var title: String,
    var positionX: Int,
    var positionY: Int,
    var plainTextContent: String,
    var formattedContent: String,
    val createdAt: String,
    val modifiedAt: String,
    var ownerID: Int,
)

@Serializable
data class TokenResponse(val token: String)
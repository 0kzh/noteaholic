import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import java.util.*
import java.util.prefs.Preferences

private const val JWT = "JWT"

@Serializable
data class ServerJwtData(val iss: String, val exp: Long, val userId: Int, val email: String, val name: String)

object PrivateJSONToken {
    private val userPreferences = Preferences.userRoot()
    var token = ""
    fun saveToAppData(token: String) {
        userPreferences.put(JWT, token)
        this.token = token
    }

    fun getUser(): String {
        println(this.token)
        val parts = this.token.split('.')
        val payloadBytes = Base64.getDecoder().decode(parts[1])
        val payload = String(payloadBytes)
        val jwtData = Json.decodeFromString<ServerJwtData>(result)
        println(jwtData)
        return jwtData.name
    }

    fun loadJWTFromAppData() {
        token = userPreferences.get(JWT, "")
    }
}
import java.util.prefs.Preferences

private const val JWT = "JWT"

object PrivateJSONToken {
    private val userPreferences = Preferences.userRoot()
    var token = ""
    fun saveToAppData(token: String) {
        userPreferences.put(JWT, token)
        this.token = token
    }

    fun loadJWTFromAppData() {
        token = userPreferences.get(JWT, "")
    }
}
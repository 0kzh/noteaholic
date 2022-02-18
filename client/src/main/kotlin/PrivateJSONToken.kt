import java.util.prefs.Preferences

object PrivateJSONToken {
    private val userPreferences = Preferences.userRoot()
    fun saveToAppData(token: String) {
        userPreferences.put("JWT", token)
    }

    fun loadJWTFromAppData(): String? {
        return userPreferences.get("JWT", null)
    }
}
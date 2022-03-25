import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.net.ConnectException
import io.ktor.client.features.auth.*
import io.ktor.http.*
import io.ktor.http.auth.*

object nHttpClient {
    lateinit var URL: String
    var onAuthFailure = {}

    val client = HttpClient(CIO) {
        expectSuccess = false
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }

        install(Auth) {
            customAuth {
                onJWTExpired {
                    onAuthFailure()
                }
            }
        }
    }

    suspend fun canConnectToServer(): Boolean {
        return try {
            client.get<HttpResponse>(URL) {}
            true
        } catch (error: ConnectException) {
            false
        }
    }
}

fun Auth.customAuth(block: CustomAuthConfig.() -> Unit) {
    with(CustomAuthConfig().apply(block)) {
        providers.add(CustomAuthProvider(PrivateJSONToken, _onJWTExpired, _sendWithoutRequest))
    }
}

class CustomAuthConfig {
    internal var _onJWTExpired: suspend (response: HttpResponse) -> Unit = { }
    internal var _sendWithoutRequest: (HttpRequestBuilder) -> Boolean = { true }

    fun onJWTExpired(block: suspend (response: HttpResponse) -> Unit) {
        _onJWTExpired = block
    }

    fun sendWithoutRequest(block: (HttpRequestBuilder) -> Boolean) {
        _sendWithoutRequest = block
    }
}


class CustomAuthProvider(
    private val JWTtoken: PrivateJSONToken,
    private val onError: suspend (response: HttpResponse) -> Unit,
    private val sendWithoutRequestCallback: (HttpRequestBuilder) -> Boolean = { true }
) : AuthProvider {

    override val sendWithoutRequest: Boolean
        get() = error("Deprecated")

    override fun sendWithoutRequest(request: HttpRequestBuilder): Boolean = sendWithoutRequestCallback(request)

    override suspend fun addRequestHeaders(request: HttpRequestBuilder) {
        request.headers {
            val tokenValue = "Bearer ${JWTtoken.token}"
            if (contains(HttpHeaders.Authorization)) {
                remove(HttpHeaders.Authorization)
            }
            append(HttpHeaders.Authorization, tokenValue)
        }
    }

    override fun isApplicable(auth: HttpAuthHeader): Boolean {
        return auth.authScheme == AuthScheme.Bearer
    }

    override suspend fun refreshToken(response: HttpResponse): Boolean {
        onError(response)
        return true
    }
}
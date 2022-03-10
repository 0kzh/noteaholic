import io.ktor.client.*
import io.ktor.client.engine.jetty.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import java.net.ConnectException

object nHttpClient {
    val URL = "http://localhost:8080"
    val client = HttpClient(Jetty) {
        expectSuccess = false
        install(JsonFeature) {
            serializer = KotlinxSerializer()
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
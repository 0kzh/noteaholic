// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import controllers.Authentication
import controllers.EditorController
import kotlinx.coroutines.runBlocking
import navcontroller.NavController
import navcontroller.NavigationHost
import navcontroller.composable
import navcontroller.rememberNavController
import screens.canvas.CanvasScreen
import screens.editor.EditorScreen
import screens.login.LoginScreen
import screens.login.SignUpScreen
import java.awt.Desktop
import java.io.File
import java.net.URI

val interFontFamily = FontFamily(
    Font(
        resource = "inter_bold.ttf",
        weight = FontWeight.Bold,
        style = FontStyle.Normal
    ),
    Font(
        resource = "inter_medium.ttf",
        weight = FontWeight.W500,
        style = FontStyle.Normal
    ),
    Font(
        resource = "inter_regular.ttf",
        weight = FontWeight.W400,
        style = FontStyle.Normal
    ),
)

@Composable
@Preview
fun App(authenticated: Boolean) {
    val screens = Screen.values().toList()
    val navController by rememberNavController(if (authenticated) Screen.CanvasScreen.name else Screen.LoginScreen.name)
    val currentScreen by remember {
        navController.currentScreen
    }

    MaterialTheme {
        Router(navController = navController)
    }
}

fun main() = application {
    PrivateJSONToken.loadJWTFromAppData()
    val jwt = PrivateJSONToken.token
    val isJWTValid = jwt.isNotBlank() &&
            runBlocking {
                Authentication.isJWTValid(jwt)
            }
    val isSupported = Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_URI)
    if (isSupported) {
        Desktop.getDesktop().setOpenURIHandler { event ->
            println("Got Open URI: " + event.uri)
            logURIDetails(event.uri)
        }
    } else {
        logURIDetails(URI.create("https://docs.oracle.com/javase/7/docs/api/java/net/URI.html"))
    }



    Window(
        title = ResString.appName,
        onCloseRequest = ::exitApplication
    ) {
        App(isJWTValid)
    }
}

private fun logURIDetails(uri: URI) {
    val eventDetails = """
                Got URI => $uri
                
                
                authority => ${uri.authority}
                fragment => ${uri.fragment}
                host => ${uri.host}
                path => ${uri.path}
                port => ${uri.port}
                query => ${uri.query}
                rawAuthority => ${uri.rawAuthority}
                rawFragment => ${uri.rawFragment}
                rawPath => ${uri.rawPath}
                rawQuery => ${uri.rawQuery}
                rawSchemeSpecificPart => ${uri.rawSchemeSpecificPart}
                rawUserInfo => ${uri.rawUserInfo}
                scheme => ${uri.scheme}
                schemeSpecificPart => ${uri.schemeSpecificPart}
                userInfo => ${uri.userInfo}
            """.trimIndent()

    File("/Users/advait/Downloads/cs398URIDetails.txt").writeText(eventDetails)
}

enum class Screen() {
    EditorScreen(),
    LoginScreen(),
    CanvasScreen(),
    SignUpScreen()
}

@Composable
fun Router(
    navController: NavController
) {
    NavigationHost(navController) {
        composable(Screen.LoginScreen.name) {
            LoginScreen(navController)
        }

        composable(Screen.EditorScreen.name) {
            EditorScreen(navController, EditorController())
        }

        composable(Screen.CanvasScreen.name) {
            CanvasScreen(navController)
        }

        composable(Screen.SignUpScreen.name) {
            SignUpScreen(navController)
        }
    }.build()
}
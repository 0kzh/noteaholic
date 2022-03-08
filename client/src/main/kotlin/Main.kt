// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.platform.Font
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import components.ConnectionError
import controllers.Authentication
import controllers.EditorController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import navcontroller.NavController
import navcontroller.NavigationHost
import navcontroller.composable
import navcontroller.rememberNavController
import screens.canvas.CanvasScreen
import screens.editor.EditorScreen
import screens.login.LoginScreen
import screens.login.SignUpScreen
import kotlin.time.Duration.Companion.seconds

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

    Router(navController = navController)
}

fun main() = application {
    PrivateJSONToken.loadJWTFromAppData()
    val jwt = PrivateJSONToken.token

    var canConnect by remember { mutableStateOf(false) }
    val connectivityChecker = connectionMonitor()
    val scope = rememberCoroutineScope()

    val isJWTValid = jwt.isNotBlank() &&
            runBlocking {
                canConnect = nHttpClient.canConnectToServer()
                canConnect && Authentication.isJWTValid(jwt)
            }



    scope.launch {
        connectivityChecker.collect { value ->
            canConnect = value
        }
    }

    Window(
        title = ResString.appName,
        onCloseRequest = ::exitApplication
    ) {
        MaterialTheme {
            if (canConnect) {
                App(isJWTValid)
            } else {
                ConnectionError()
            }
        }
    }
}

private fun connectionMonitor(): Flow<Boolean> {
    val connectivityChecker = flow {
        while (true) {
            emit(nHttpClient.canConnectToServer())
            delay(75.seconds.inWholeMilliseconds)
        }
    }
    return connectivityChecker
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
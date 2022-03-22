// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import navcontroller.NavController
import navcontroller.NavigationHost
import navcontroller.composable
import navcontroller.rememberNavController
import screens.canvas.CanvasContextProvider
import screens.canvas.CanvasScreen
import screens.editor.EditorScreen
import screens.login.LoginScreen
import screens.login.SignUpScreen
import java.awt.Desktop
import java.net.URI
import kotlin.time.Duration.Companion.seconds

@Serializable
data class Config(val url: String)

@Composable
@Preview
fun App(authenticated: Boolean, sharedNoteId: MutableState<Int>, navController: NavController) {
    println("Got SharedNoteId in App $sharedNoteId")
//    val sharedNoteId2 = 3
    val screens = Screen.values().toList()
//    val navController by rememberNavController(
//        if (authenticated) (
//                if (sharedNoteId != null) Screen.EditorScreen.name
//                else Screen.CanvasScreen.name)
//        else Screen.LoginScreen.name)
    val currentScreen by remember {
        navController.currentScreen
    }

    CanvasContextProvider(
        content = { Router(navController = navController) },
        currentScreen = currentScreen,
        sharedNoteId = sharedNoteId,
        navController = navController
    )
}

// https://stackoverflow.com/questions/42739807/how-to-read-a-text-file-from-resources-in-kotlin
fun getResourceAsText(path: String): String? =
    object {}.javaClass.getResource(path)?.readText()

fun getNoteIdFromURI(uri: URI): Int? =
    uri.query.split('&')
        .associate { it.split('=').let { splitData -> Pair(splitData[0], splitData[1]) } }["noteId"]?.toInt()

fun main(args: Array<String>) = application {
    val result = getResourceAsText("/config/config.json")
    nHttpClient.URL = if (result != null) Json.decodeFromString<Config>(result).url else "http://localhost:8080"

    PrivateJSONToken.loadJWTFromAppData()
    val jwt = PrivateJSONToken.token

    var canConnect by remember { mutableStateOf(false) }
    var sharedNoteId = remember { mutableStateOf<Int>(-1) }
    val connectivityChecker = connectionMonitor()
    val scope = rememberCoroutineScope()

    val isJWTValid = jwt.isNotBlank() &&
            runBlocking {
                canConnect = nHttpClient.canConnectToServer()
                canConnect && Authentication.isJWTValid()
            }

    val navController by rememberNavController(
        if (isJWTValid) (
                if (sharedNoteId.value != -1) Screen.EditorScreen.name
                else Screen.CanvasScreen.name)
        else Screen.LoginScreen.name
    )

    nHttpClient.onAuthFailure = {
        println("Called on authFailure")
        navController.navigate(Screen.LoginScreen.name)
    }

    val isSupported = Desktop.getDesktop().isSupported(Desktop.Action.APP_OPEN_URI)
    if (isSupported) {
        Desktop.getDesktop().setOpenURIHandler { event ->
            println("Got Open URI: " + event.uri)
//            logURIDetails(event.uri)
            sharedNoteId.value = getNoteIdFromURI(event.uri) ?: -1
            navController.navigate(Screen.EditorScreen.name)
        }
    } else if (args.size == 1) {
        sharedNoteId.value = getNoteIdFromURI(URI(args[0])) ?: -1
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
        MaterialTheme(typography = CustomTypography) {
            if (canConnect) {
                App(isJWTValid, sharedNoteId, navController)
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
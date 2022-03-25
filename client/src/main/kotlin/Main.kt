// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.dp
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
import screens.SearchPalette
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
@Preview
fun App(navController: NavController, sharedNoteId: MutableState<Int>, showPalette: MutableState<Boolean>) {
    println("Got SharedNoteId in App $sharedNoteId")
    val screens = Screen.values().toList()
    val currentScreen by remember {
        navController.currentScreen
    }

    CanvasContextProvider(
        content = {
            if (showPalette.value && navController.currentScreen.value in listOf(
                    Screen.EditorScreen.name,
                    Screen.CanvasScreen.name
                )
            ) {
                SearchPalette({ showPalette.value = false }, navController)
            }
            Box(modifier = Modifier.blur(if (showPalette.value) 7.dp else 0.dp)) {
                Router(navController = navController, showPalette = showPalette)
            }
        },
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

@OptIn(ExperimentalComposeUiApi::class)
fun main(args: Array<String>) = application {
    val result = getResourceAsText("/config/config.json")
    nHttpClient.URL = if (result != null) Json.decodeFromString<Config>(result).url else "http://localhost:8080"

    PrivateJSONToken.loadJWTFromAppData()
    val jwt = PrivateJSONToken.token

    var canConnect by remember { mutableStateOf(false) }
    val showPalette = remember { mutableStateOf(false) }

    val sharedNoteId = remember { mutableStateOf(-1) }
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
            sharedNoteId.value = getNoteIdFromURI(event.uri) ?: -1
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
        onCloseRequest = ::exitApplication,
        onKeyEvent = {
            if (it.isCtrlPressed && it.key == Key.P) {
                showPalette.value = !showPalette.value
                true
            } else {
                false
            }
        }
    ) {
        MaterialTheme(typography = CustomTypography) {

            if (canConnect) {
                App(navController, sharedNoteId, showPalette)
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
    navController: NavController,
    showPalette: MutableState<Boolean>
) {
    NavigationHost(navController) {
        composable(Screen.LoginScreen.name) {
            LoginScreen(navController)
        }

        composable(Screen.EditorScreen.name) {
            EditorScreen(navController, EditorController())
        }

        composable(Screen.CanvasScreen.name) {
            CanvasScreen(navController, showPalette)
        }

        composable(Screen.SignUpScreen.name) {
            SignUpScreen(navController)
        }
    }.build()
}
// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import navcontroller.NavController
import navcontroller.NavigationHost
import navcontroller.composable
import navcontroller.rememberNavController
import screens.canvas.CanvasScreen
import screens.editor.EditorScreen
import screens.login.LoginScreen

@Composable
@Preview
fun App() {
    val screens = Screen.values().toList()
    val navController by rememberNavController(Screen.LoginScreen.name)
    val currentScreen by remember {
        navController.currentScreen
    }

    MaterialTheme {
//        Button(onClick = {
//            text = "Hello, Desktop!"
//        }) {
//            Text(text)
//        }
        Router(navController = navController)
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

enum class Screen() {
    EditorScreen(),
    LoginScreen(),
    CanvasScreen(),
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
            EditorScreen(navController)
        }

        composable(Screen.CanvasScreen.name) {
            CanvasScreen(navController)
        }
    }.build()
}
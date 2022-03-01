package screens.login

import ResString
import Screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import controllers.Authentication
import navcontroller.NavController

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignUpScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        val showSpinner = remember { mutableStateOf(false) }
        val errorMessage = remember { mutableStateOf("") }

        BaseAuthPage(
            ResString.signup,
            ResString.haveAccount,
            ResString.login,
            errorMessage,
            linkActionNavigateTo = { navController.navigate(Screen.LoginScreen.name) },
            button = {
                ButtonWithLoading(ResString.signup, showSpinner) {
                    val res = Authentication.signup(firstName, lastName, email, password)
                    if (res) {
                        Authentication.login(email, password)
                        navController.navigate(Screen.CanvasScreen.name)
                    }
                }
            },
            children = {
                OutlinedTextField(
                    value = firstName,
                    singleLine = true,
                    label = { Text(ResString.firstName) },
                    onValueChange = { firstName = it },
                )

                OutlinedTextField(
                    value = lastName,
                    singleLine = true,
                    label = { Text(ResString.lastName) },
                    onValueChange = { lastName = it },
                )
                OutlinedTextField(
                    value = email,
                    singleLine = true,
                    label = { Text(ResString.email) },
                    onValueChange = { email = it },
                )
                OutlinedTextField(
                    value = password,
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(ResString.password) },
                    onValueChange = { password = it },
                )
            }
        )
    }
}
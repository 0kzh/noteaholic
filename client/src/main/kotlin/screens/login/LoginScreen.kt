package screens.login

import ResString
import Screen
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.text.input.PasswordVisualTransformation
import components.OutlinedTextFieldWithError
import controllers.Authentication
import navcontroller.NavController

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    val errorMessage = remember { mutableStateOf("") }
    val showSpinner = remember { mutableStateOf(false) }

    BaseAuthPage(
        ResString.login,
        ResString.noAccount,
        ResString.createOne,
        errorMessage,
        linkActionNavigateTo = { navController.navigate(Screen.SignUpScreen.name) },
        button = {
            ButtonWithLoading(ResString.login, showSpinner) {
                emailError = Authentication.validateEmail(email)
                if (emailError != null) {
                    showSpinner.value = false
                    return@ButtonWithLoading
                }
                val res = Authentication.login(email = email, password = password) { errorMessage.value = it }
                if (res) {
                    navController.navigate(Screen.CanvasScreen.name)
                }
            }
        },
        children = {
            OutlinedTextFieldWithError(
                singleLine = true,
                readOnly = showSpinner.value,
                value = email,
                errorText = emailError,
                label = { Text(ResString.email) },
                onValueChange = { email = it },
            )
            OutlinedTextFieldWithError(
                singleLine = true,
                readOnly = showSpinner.value,
                value = password,
                visualTransformation = PasswordVisualTransformation(),
                label = { Text(ResString.password) },
                onValueChange = { password = it },
            )
        }
    )
}

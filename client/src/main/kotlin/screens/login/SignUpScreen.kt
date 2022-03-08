package screens.login

import ResString
import Screen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import components.OutlinedTextFieldWithError
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

        var emailError by remember { mutableStateOf<String?>(null) }
        var passwordError by remember { mutableStateOf<String?>(null) }
        var firstNameError by remember { mutableStateOf<String?>(null) }
        var lastNameError by remember { mutableStateOf<String?>(null) }

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
                    emailError = Authentication.validateEmail(email)
                    passwordError = Authentication.validatePassword(password)
                    firstNameError = if (firstName.isBlank()) "${ResString.firstName} required" else null
                    lastNameError = if (lastName.isBlank()) "${ResString.lastName} required" else null

                    if (emailError != null || passwordError != null || firstNameError != null || lastNameError != null) {
                        showSpinner.value = false
                        return@ButtonWithLoading
                    }

                    val res = Authentication.signup(firstName, lastName, email, password) { errorMessage.value = it }
                    if (res) {
                        Authentication.login(email, password)
                        navController.navigate(Screen.CanvasScreen.name)
                    }
                }
            },
            children = {
                OutlinedTextFieldWithError(
                    value = firstName,
                    errorText = firstNameError,
                    singleLine = true,
                    label = { Text(ResString.firstName) },
                    onValueChange = { firstName = it },
                )

                OutlinedTextFieldWithError(
                    value = lastName,
                    errorText = lastNameError,
                    singleLine = true,
                    label = { Text(ResString.lastName) },
                    onValueChange = { lastName = it },
                )
                OutlinedTextFieldWithError(
                    value = email,
                    singleLine = true,
                    errorText = emailError,
                    label = { Text(ResString.email) },
                    onValueChange = { email = it },
                )
                OutlinedTextFieldWithError(
                    value = password,
                    singleLine = true,
                    errorText = passwordError,
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(ResString.password) },
                    onValueChange = { password = it },
                )
            }
        )
    }
}

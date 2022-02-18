package screens.login

import Screen
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import controllers.Authentication
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import navcontroller.NavController

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignUpScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val scope = rememberCoroutineScope()
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var underlined by remember { mutableStateOf(false) }
        var showSpinner by remember { mutableStateOf(false) }
        val annotatedText = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colors.primary
                )
            ) {
                append(ResString.login)
            }
        }
        Text(
            text = ResString.signup,
            style = MaterialTheme.typography.h4
        )
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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

            Button(
                modifier = Modifier.animateContentSize(),
                onClick = {
                    showSpinner = !showSpinner
                    scope.launch {
                        val res = Authentication.signup(firstName, lastName, email, password)
                        if (res) {
                            Authentication.login(email, password)
                            navController.navigate(Screen.CanvasScreen.name)
                        } else {
                            showSpinner = false
                        }
                    }
                }) {
                Text(ResString.signup)
                if (showSpinner) {
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row {
            Text(
                ResString.haveAccount + " " /* space at the end is needed to make it look like 1 sentence */,
                style = MaterialTheme.typography.body1
            )
            ClickableText(
                text = annotatedText,
                style = TextStyle(
                    fontSize = MaterialTheme.typography.body1.fontSize,
                    textDecoration = if (underlined) {
                        TextDecoration.Underline
                    } else {
                        TextDecoration.None
                    }
                ),
                onClick = { navController.navigate(Screen.LoginScreen.name) },
                modifier = Modifier.wrapContentSize(Alignment.Center)
                    .onPointerEvent(PointerEventType.Enter) { underlined = true }
                    .onPointerEvent(PointerEventType.Exit) { underlined = false }
            )
        }
    }
}
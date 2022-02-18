package screens.login

import ResString
import Screen
import androidx.compose.animation.animateContentSize
import androidx.compose.desktop.ui.tooling.preview.Preview
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
import components.OutlinedTextFieldWithError
import controllers.Authentication
import kotlinx.coroutines.launch
import navcontroller.NavController

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun LoginScreen(
    navController: NavController
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var underlined by remember { mutableStateOf(false) }
    var showSpinner by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val annotatedText = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colors.primary
                )
            ) {
                append(ResString.createOne)
            }
        }

        Text(
            text = ResString.login,
            style = MaterialTheme.typography.h4
        )
        Spacer(Modifier.height(16.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextFieldWithError(
                singleLine = true,
                readOnly = showSpinner,
                value = email,
                label = { Text(ResString.email) },
                onValueChange = { email = it },
            )
            OutlinedTextFieldWithError(
                singleLine = true,
                readOnly = showSpinner,
                value = password,
                visualTransformation = PasswordVisualTransformation(),
                label = { Text(ResString.password) },
                onValueChange = { password = it },
            )
            if (errorMessage.isNotBlank()) {
                Text(errorMessage, style = MaterialTheme.typography.caption, color = MaterialTheme.colors.error)
            }

            Button(
                enabled = !showSpinner,
                modifier = Modifier.animateContentSize(),
                onClick = {
                    showSpinner = !showSpinner
                    errorMessage = Authentication.validate(email, password)
                    if (errorMessage.isNotBlank()) {
                        showSpinner = false
                        return@Button
                    }
                    scope.launch {
                        val res = Authentication.login(email = email, password = password)
                        if (res) {
                            navController.navigate(Screen.CanvasScreen.name)
                        } else {
                            showSpinner = false
                        }
                    }
                }) {
                Text(ResString.login)
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
                ResString.noAccount + " " /* space at the end is needed to make it look like 1 sentence */,
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
                onClick = { navController.navigate(Screen.SignUpScreen.name) },
                modifier = Modifier.wrapContentSize(Alignment.Center)
                    .onPointerEvent(PointerEventType.Enter) { underlined = true }
                    .onPointerEvent(PointerEventType.Exit) { underlined = false }
            )
        }
    }
}
package screens.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import navcontroller.NavController

@Composable
fun LoginScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.h6
        )
        Button(
            onClick = {
                navController.navigate(Screen.CanvasScreen.name)
            }
        ) {
            Text(
                text = "Go to Home"
            )
        }
    }
}
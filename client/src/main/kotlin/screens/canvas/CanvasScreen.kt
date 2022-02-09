package screens.canvas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import navcontroller.NavController

@Composable
fun CanvasScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Canvas",
            style = MaterialTheme.typography.h6
        )
        Button(
            onClick = {
                navController.navigate(Screen.EditorScreen.name)
            }
        ) {
            Text(
                text = "Open note"
            )
        }
        Button(
            onClick = {
                navController.navigate(Screen.LoginScreen.name)
            }
        ) {
            Text(
                text = "Sign out"
            )
        }
    }
}
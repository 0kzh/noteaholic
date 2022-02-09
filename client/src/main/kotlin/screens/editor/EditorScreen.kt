package screens.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import navcontroller.NavController

@Composable
fun EditorScreen(
    navController: NavController
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Note Editor",
            style = MaterialTheme.typography.h6
        )
        Button(
            onClick = {
                navController.navigateBack()
            }
        ) {
            Text(
                text = "Close note"
            )
        }
    }
}
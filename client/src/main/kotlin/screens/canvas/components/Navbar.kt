package screens.canvas.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import components.Border
import components.border
import controllers.Authentication
import navcontroller.NavController
import screens.canvas.LocalCanvasContext

@Composable
fun Zoom(
) {
    val scale = LocalCanvasContext.current.scale
    Text(text = "Zoom", style = MaterialTheme.typography.h6)

}

@Composable
fun Navbar(navController: NavController) {
    val screenName = LocalCanvasContext.current.screenName

    Row(
        modifier = Modifier.fillMaxWidth().height(60.dp).background(Color.White)
            .border(bottom = Border(1.dp, Color.Gray.copy(alpha = 0.5f))),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
//        Box(modifier = Modifier.size(0.dp))
//        Zoom()
        Text(text = screenName.value, style = MaterialTheme.typography.h4)
//        Zoom()
        IconButton({
            Authentication.logout()
            navController.navigate(Screen.LoginScreen.name)
        }) { Icon(Icons.Filled.ExitToApp, "Log out") }
    }
}
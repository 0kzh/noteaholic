package components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionError() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(painterResource("icons/connection.svg"), "Connection Error", modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(32.dp))
        Text(
            "Connection Error",
            style = MaterialTheme.typography.h2,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Could not communicate with the Noteaholic servers",
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center
        )
    }
}

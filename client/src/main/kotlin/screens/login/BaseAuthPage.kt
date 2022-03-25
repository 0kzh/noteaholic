package screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BaseAuthPage(
    title: String,
    linkLabel: String,
    linkClickableText: String,
    errorMessage: MutableState<String>,
    linkActionNavigateTo: (Int) -> Unit,
    button: @Composable () -> Unit,
    children: @Composable () -> Unit,
    elementSpacing: Dp = 8.dp
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // image
        Image(painterResource("img/noteaholic.svg"), "logo", modifier = Modifier.width(275.dp).height(80.dp))
        Spacer(Modifier.height(elementSpacing * 2))
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            children()
            if (errorMessage.value.isNotBlank()) {
                Text(errorMessage.value, style = MaterialTheme.typography.caption, color = MaterialTheme.colors.error)
            }
            button()
        }

        Spacer(Modifier.height(elementSpacing * 2))
        ClickableHoverText(
            linkLabel,
            linkClickableText,
            onClick = linkActionNavigateTo

        )
    }
}

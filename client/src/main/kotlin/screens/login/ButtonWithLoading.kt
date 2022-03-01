package screens.login

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ButtonWithLoading(
    buttonText: String,
    showSpinner: MutableState<Boolean>,
    onClick: suspend () -> Unit,
) {
    val scope = rememberCoroutineScope()
    Button(
        enabled = !showSpinner.value,
        modifier = Modifier.animateContentSize(),
        onClick = {
            showSpinner.value = !showSpinner.value
            scope.launch {
                onClick()
                showSpinner.value = false
            }
        })
    {
        Text(buttonText)
        if (showSpinner.value) {
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(ButtonDefaults.IconSize),
                strokeWidth = 2.dp
            )
        }
    }
}
package screens.login

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ClickableHoverText(labelText: String, hoverText: String, onClick: (Int) -> Unit, initialState: Boolean = false) {
    var underlined by remember { mutableStateOf(initialState) }
    val annotatedText = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colors.primary
            )
        ) {
            append(hoverText)
        }
    }
    Row {
        Text(
            "$labelText " /* space at the end is needed to make it look like 1 sentence */,
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
            onClick = onClick,
            modifier = Modifier.wrapContentSize(Alignment.Center)
                .onPointerEvent(PointerEventType.Enter) { underlined = true }
                .onPointerEvent(PointerEventType.Exit) { underlined = false }
        )
    }
}
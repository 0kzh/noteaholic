package components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
fun Avatar(
    id: String,
    firstName: String,
    lastName: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    textStyle: TextStyle = MaterialTheme.typography.body2
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        val color = remember(id, firstName, lastName) {
            val name = listOf(firstName, lastName)
                .joinToString(separator = "")
                .uppercase()
//            Color("$id / $name".toHslColor())
              Color.Blue
        }
        val initials = firstName.take(1).uppercase()
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(SolidColor(color))
        }
        Text(text = initials, style = textStyle, color = Color.White)
    }
}
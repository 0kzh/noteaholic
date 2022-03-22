package screens.canvas.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchResult(matchingText: AnnotatedString, fromTitle: String, onClick: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth(), onClick = onClick, shape = RoundedCornerShape(5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp, 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(matchingText, overflow = TextOverflow.Ellipsis, maxLines = 1)
            Text(fromTitle, overflow = TextOverflow.Ellipsis, maxLines = 1)
        }
    }
}
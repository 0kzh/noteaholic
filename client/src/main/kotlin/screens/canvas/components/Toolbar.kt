package screens.canvas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

val SELECTED_COLOR = Color(COLOR_DEFAULT)

@Composable
fun Toolbar(
) {
    val canvasState = LocalCanvasContext.current.canvasState
    val setCanvasState = LocalCanvasContext.current.setCanvasState

    Box(
        modifier = Modifier.width(300.dp).height(60.dp).shadow(
            elevation = 16.dp, shape = RoundedCornerShape(24.dp)
        )
        // border stroke 1px #CFCFCF
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(60.dp).background(Color.White),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            IconButton(modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(
                if (canvasState.value == CanvasState.FOCUS_NOTE) SELECTED_COLOR else Color.Transparent
            ), onClick = { canvasState.value = CanvasState.FOCUS_NOTE }) {
                Image(painterResource("icons/cursor.svg"), "cursor icon", modifier = Modifier.size(24.dp))
            }
            IconButton(modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(
                if (canvasState.value == CanvasState.FOCUS_CANVAS) SELECTED_COLOR else Color.Transparent
            ), onClick = { canvasState.value = CanvasState.FOCUS_CANVAS }) {
                Image(painterResource("icons/hand.svg"), "hand icon", modifier = Modifier.size(24.dp))
            }
            IconButton(modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(
                if (canvasState.value == CanvasState.NEW_NOTE) SELECTED_COLOR else Color.Transparent
            ), onClick = { canvasState.value = CanvasState.NEW_NOTE }) {
                Image(
                    painterResource("icons/new_note.svg"), "new note icon", modifier = Modifier.size(24.dp)
                )
            }
            IconButton(modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(
                if (canvasState.value == CanvasState.NEW_TEXT) SELECTED_COLOR else Color.Transparent
            ), onClick = { canvasState.value = CanvasState.NEW_TEXT }) {
                Image(painterResource("icons/text.svg"), "new text", modifier = Modifier.size(24.dp))
            }
        }
    }

}
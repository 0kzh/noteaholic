package screens.canvas.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import navcontroller.NavController
import screens.canvas.LocalCanvasState
import screens.canvas.ToolbarState
import kotlin.math.roundToInt


val DEFAULT_NOTE_SIZE = 100.dp
const val DEFAULT_COLOR = 0xFFFCE183
const val DEFAULT_PREVIEW_COLOR = 0xFFFEF6D9

data class NoteData(val position: IntOffset = IntOffset.Zero, val text: String = "")

@Composable
fun Note(
    note: NoteData, navController: NavController
) {
    val scale = LocalCanvasState.current.scale
    val translate = LocalCanvasState.current.translate
    val toolbarState = LocalCanvasState.current.toolbarState
    val setToolbarState = LocalCanvasState.current.setToolbarState

    val translateX =
        translate.value.x.roundToInt()
    val translateY = translate.value.y.roundToInt()

    var positionX by remember { mutableStateOf(note.position.x) }
    var positionY by remember { mutableStateOf(note.position.y) }

    val gesturesEnabled = toolbarState.value != ToolbarState.NEW_NOTE

    if (gesturesEnabled) {
        @OptIn(ExperimentalFoundationApi::class) (Box(Modifier
            .offset {
                IntOffset(positionX + translateX, positionY + translateY)
            }.background(Color(DEFAULT_COLOR))
            .size(DEFAULT_NOTE_SIZE * scale.value)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consumeAllChanges()
                    positionX += dragAmount.x.roundToInt()
                    positionY += dragAmount.y.roundToInt()
                    setToolbarState(ToolbarState.FOCUS_NOTE)
                }
            }.combinedClickable(onClick = {}, onDoubleClick = {
                navController.navigate(Screen.EditorScreen.name)
            }), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${note.text}", style = MaterialTheme.typography.h6
            )
        })
    } else {
        @OptIn(ExperimentalFoundationApi::class) (Box(
            Modifier
                .offset {
                    IntOffset(positionX + translateX, positionY + translateY)
                }.background(Color(DEFAULT_COLOR)).size(DEFAULT_NOTE_SIZE * scale.value),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${note.text}", style = MaterialTheme.typography.h6
            )
        })
    }

}

@Composable
fun PreviewNote(
) {
    val scale = LocalCanvasState.current.scale
    val cursor = LocalCanvasState.current.cursor
    val translate = LocalCanvasState.current.translate
    val toolbarState = LocalCanvasState.current.toolbarState
    val size = DEFAULT_NOTE_SIZE * scale.value

    // Centers the position on the cursor
    val previewNoteSizePx = LocalDensity.current.run { (DEFAULT_NOTE_SIZE * scale.value).toPx() }
    val positionX = cursor.value.x - previewNoteSizePx / 2
    val positionY = cursor.value.y - previewNoteSizePx / 2
    val previewNotePosition = IntOffset(x = positionX.roundToInt(), y = positionY.roundToInt())

    if (toolbarState.value == ToolbarState.NEW_NOTE) {
        @OptIn(ExperimentalFoundationApi::class) (Box(
            Modifier.offset { previewNotePosition }.background(Color(DEFAULT_PREVIEW_COLOR)).size(size)
        ))
    }
}

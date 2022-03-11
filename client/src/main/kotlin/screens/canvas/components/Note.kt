package screens.canvas.components

import UpdateNoteData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import navcontroller.NavController
import screens.canvas.LocalCanvasContext
import screens.canvas.CanvasState
import screens.canvas.NoteData
import utils.debounce
import kotlin.math.roundToInt


val DEFAULT_NOTE_SIZE = 200.dp
const val DEFAULT_COLOR = 0xFFFCE183
const val DEFAULT_PREVIEW_COLOR = 0xFFFEF6D9

@Composable
fun Note(
    note: NoteData, navController: NavController
) {
    val scale = LocalCanvasContext.current.scale
    val translate = LocalCanvasContext.current.translate
    val canvasState = LocalCanvasContext.current.canvasState
    val setCanvasState = LocalCanvasContext.current.setCanvasState
    val selectedNote = LocalCanvasContext.current.selectedNote
    val updateNote = LocalCanvasContext.current.updateNote

    val size = DEFAULT_NOTE_SIZE * scale.value
//    val (id, title, positionX, positionY, plainTextContent, formattedContent, createdAt, modifiedAt, owner) = note

    // TODO: Modify note position directly instead
    val translateX = translate.value.x.roundToInt()
    val translateY = translate.value.y.roundToInt()

    val scope = rememberCoroutineScope()
    val debouncedUpdateNote = debounce(400L, scope, updateNote)

    var positionX by remember { mutableStateOf(note.positionX) }
    var positionY by remember { mutableStateOf(note.positionY) }

    val gesturesEnabled = canvasState.value != CanvasState.NEW_NOTE

    if (gesturesEnabled) {
        @OptIn(ExperimentalFoundationApi::class) (Box(Modifier.offset {
            IntOffset(positionX + translateX, positionY + translateY)
        }.background(Color(DEFAULT_COLOR)).size(size).pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consumeAllChanges()
                positionX += dragAmount.x.roundToInt()
                positionY += dragAmount.y.roundToInt()
                setCanvasState(CanvasState.FOCUS_NOTE)
                debouncedUpdateNote(UpdateNoteData(id = note.id, positionX = positionX, positionY = positionY))
            }
        }.combinedClickable(onClick = {}, onDoubleClick = {
            navController.navigate(Screen.EditorScreen.name)
            selectedNote.value = note
        }).padding(24.dp), contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = "${note.title}", style = MaterialTheme.typography.h3
            )
        })
    } else {
        @OptIn(ExperimentalFoundationApi::class) (Box(
            Modifier.offset {
                IntOffset(positionX + translateX, positionY + translateY)
            }.background(Color(DEFAULT_COLOR)).size(DEFAULT_NOTE_SIZE * scale.value).padding(24.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = "${note.title}", style = MaterialTheme.typography.h3
            )
        })
    }
}


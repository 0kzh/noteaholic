package screens.canvas.components

import UpdateNoteData
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import navcontroller.NavController
import screens.canvas.*
import utils.debounce
import kotlin.math.roundToInt


val DEFAULT_NOTE_SIZE = 200.dp
const val DEFAULT_BORDER_COLOR = 0xFF0f0f0f

@Composable
fun Note(
    note: NoteData, navController: NavController
) {
    val scale = LocalCanvasContext.current.scale
    val translate = LocalCanvasContext.current.translate
    val canvasState = LocalCanvasContext.current.canvasState
    val setCanvasState = LocalCanvasContext.current.setCanvasState
    val selectedNote = LocalCanvasContext.current.selectedNote
    val focusedNoteId = LocalCanvasContext.current.focusedNoteId
    val setFocusedNoteId = LocalCanvasContext.current.setFocusedNoteId
    val updateNote = LocalCanvasContext.current.updateNote
    val colorIdx = LocalCanvasContext.current.colorIdx

    val size = DEFAULT_NOTE_SIZE * scale.value

    // TODO: Modify note position directly instead
    val translateX = translate.value.x.roundToInt()
    val translateY = translate.value.y.roundToInt()

    val scope = rememberCoroutineScope()
    val debouncedUpdateNote = debounce(400L, scope, updateNote)

    val noteColor = note.colour.toLong()

    var positionX by remember { mutableStateOf(note.positionX) }
    var positionY by remember { mutableStateOf(note.positionY) }
    var color by remember { mutableStateOf(noteColor) }

    val gesturesEnabled = canvasState.value != CanvasState.NEW_NOTE && canvasState.value != CanvasState.FOCUS_CANVAS
    val isFocused = focusedNoteId.value == note.id

    println("Focused note: ${focusedNoteId.value}")

    // useEffect to change color
    LaunchedEffect(colorIdx.value) {
        if (isFocused) {
            color = NOTE_COLORS.get(colorIdx.value)
            debouncedUpdateNote(UpdateNoteData(id = note.id, colour = color.toString()))
        }
    }

    if (gesturesEnabled) {
        @OptIn(ExperimentalFoundationApi::class) (Box(Modifier.offset {
            IntOffset(positionX + translateX, positionY + translateY)
        }.background(Color(color)).size(size).combinedClickable(onClick = {
            setCanvasState(CanvasState.FOCUS_NOTE)
            setFocusedNoteId(note.id)
        }, onDoubleClick = {
            setCanvasState(CanvasState.EDITOR_SCREEN)
            navController.navigate(Screen.EditorScreen.name)
            setFocusedNoteId(note.id)
            selectedNote.value = note
        }).pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consumeAllChanges()
                positionX += dragAmount.x.roundToInt()
                positionY += dragAmount.y.roundToInt()
                setCanvasState(CanvasState.FOCUS_NOTE)
                setFocusedNoteId(note.id)
                debouncedUpdateNote(UpdateNoteData(id = note.id, positionX = positionX, positionY = positionY))
            }
        }.focusedBorder(isFocused).padding(24.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = "${note.title}", style = MaterialTheme.typography.h3
            )
        })
    } else {
        @OptIn(ExperimentalFoundationApi::class) (Box(
            Modifier.offset {
                IntOffset(positionX + translateX, positionY + translateY)
            }.background(Color(color)).size(DEFAULT_NOTE_SIZE * scale.value).padding(24.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = "${note.title}", style = MaterialTheme.typography.h3
            )
        })
    }
}

fun Modifier.focusedBorder(
    selected: Boolean
) = composed {
    if (selected) {
        this.border(BorderStroke(2.dp, Color(DEFAULT_BORDER_COLOR)))
    } else {
        Modifier.Companion
    }
}



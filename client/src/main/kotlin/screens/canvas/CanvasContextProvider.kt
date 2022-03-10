package screens.canvas

import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset

enum class CanvasState {
    FOCUS_NOTE, FOCUS_CANVAS, NEW_NOTE, CREATING_NOTE, NEW_TEXT
}

data class NoteData(val position: IntOffset = IntOffset.Zero, val title: String = "", val text: String = "")

data class CanvasContext(
    val scale: MutableState<Float>,
    val cursor: MutableState<Offset>,
    val translate: MutableState<Offset>,

    val canvasState: MutableState<CanvasState>,
    val setCanvasState: (CanvasState) -> Unit,

    val notes: MutableState<Array<NoteData>>,
    val uncreatedNote: MutableState<NoteData?>,

    val focusRequester: FocusRequester
)

val LocalCanvasContext = compositionLocalOf<CanvasContext> { error("No canvas state found!") }

val DEFAULT_NOTES = arrayOf<NoteData>(
    NoteData(position = IntOffset(500, 500), title = "Hello", text = ""),
    NoteData(position = IntOffset(100, 800), title = "Hello", text = ""),
    NoteData(position = IntOffset(200, 1200), title = "Hello", text = ""),
    NoteData(position = IntOffset(300, 1100), title = "Hello", text = ""),
    NoteData(position = IntOffset(500, 700), title = "Hello", text = ""),
    NoteData(position = IntOffset(700, 400), title = "Hello", text = ""),
    NoteData(position = IntOffset(900, 100), title = "Hello", text = ""),
)

@Composable
fun CanvasStateProvider(content: @Composable() () -> Unit) {
    val scale = remember { mutableStateOf(1f) }
    val cursor = remember { mutableStateOf(Offset.Zero) }
    val translate = remember { mutableStateOf(Offset.Zero) }
    val canvasState = remember { mutableStateOf(CanvasState.FOCUS_NOTE) }
    val setCanvasState =
        { newState: CanvasState -> if (canvasState.value != newState) canvasState.value = newState }

    val notes = remember { mutableStateOf(DEFAULT_NOTES) }
    val focusRequester = remember { FocusRequester() }


    LaunchedEffect(Unit) {

    }

    val uncreatedNote = remember { mutableStateOf<NoteData?>(null) }

    CompositionLocalProvider(
        LocalCanvasContext provides CanvasContext(
            scale,
            cursor,
            translate,
            canvasState,
            setCanvasState,
            notes,
            uncreatedNote,
            focusRequester
        ),
    ) {
        content()
    }
}
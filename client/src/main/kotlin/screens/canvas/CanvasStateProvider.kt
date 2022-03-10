package screens.canvas

import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import screens.canvas.components.NoteData

enum class ToolbarState {
    FOCUS_NOTE, FOCUS_CANVAS, NEW_NOTE, NEW_TEXT
}

data class CanvasState(
    val scale: MutableState<Float>,
    val cursor: MutableState<Offset>,
    val translate: MutableState<Offset>,
    val toolbarState: MutableState<ToolbarState>,
    val setToolbarState: (ToolbarState) -> Unit,
    val notes: MutableState<Array<NoteData>>,
)

val LocalCanvasState = compositionLocalOf<CanvasState> { error("No canvas state found!") }

val DEFAULT_NOTES = arrayOf<NoteData>(
    NoteData(position = IntOffset(500, 500), text = "Hello"),
    NoteData(position = IntOffset(100, 800), text = "Hello"),
    NoteData(position = IntOffset(200, 1200), text = "Hello"),
    NoteData(position = IntOffset(300, 1100), text = "Hello"),
    NoteData(position = IntOffset(500, 700), text = "Hello"),
    NoteData(position = IntOffset(700, 400), text = "Hello"),
    NoteData(position = IntOffset(900, 100), text = "Hello"),
)

@Composable
fun CanvasStateProvider(content: @Composable() () -> Unit) {
    val scale = remember { mutableStateOf(1f) }
    val cursor = remember { mutableStateOf(Offset.Zero) }
    val translate = remember { mutableStateOf(Offset.Zero) }
    val toolbarState = remember { mutableStateOf(ToolbarState.FOCUS_NOTE) }
    val setToolbarState =
        { newState: ToolbarState -> if (toolbarState.value != newState) toolbarState.value = newState }
    val notes = remember { mutableStateOf(DEFAULT_NOTES) }

    CompositionLocalProvider(
        LocalCanvasState provides CanvasState(scale, cursor, translate, toolbarState, setToolbarState, notes),
    ) {
        content()
    }
}
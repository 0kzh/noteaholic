package screens.canvas

import NotesDTOOut
import Screen
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import controllers.NoteRequests

enum class CanvasState {
    FOCUS_NOTE, FOCUS_CANVAS, NEW_NOTE, CREATING_NOTE, NEW_TEXT
}

typealias NoteData = NotesDTOOut

data class CanvasContext(
    val screenName: MutableState<String>,

    val scale: MutableState<Float>,
    val cursor: MutableState<Offset>,
    val translate: MutableState<Offset>,

    val canvasState: MutableState<CanvasState>,
    val setCanvasState: (CanvasState) -> Unit,

    val notes: MutableState<List<NoteData>>,
    val uncreatedNote: MutableState<NoteData?>,

    val focusRequester: FocusRequester
)

val LocalCanvasContext = compositionLocalOf<CanvasContext> { error("No canvas state found!") }

@Composable
fun CanvasContextProvider(content: @Composable() () -> Unit, currentScreen: String) {
    val screenName = remember { mutableStateOf("School Notes") }

    val scale = remember { mutableStateOf(1f) }
    val cursor = remember { mutableStateOf(Offset.Zero) }
    val translate = remember { mutableStateOf(Offset.Zero) }

    val canvasState = remember { mutableStateOf(CanvasState.FOCUS_NOTE) }
    val setCanvasState =
        { newState: CanvasState -> if (canvasState.value != newState) canvasState.value = newState }

    val notes = remember { mutableStateOf<List<NoteData>>(listOf()) }

    val didFetchNotes = remember { mutableStateOf(false) }

    LaunchedEffect(currentScreen) {
        // Fetch notes once more after logging in
        if (!didFetchNotes.value && currentScreen == Screen.CanvasScreen.name) {
            val res = NoteRequests.fetchNotes()
            res?.let { notes.value = it }
            didFetchNotes.value = true
        }
    }

    val uncreatedNote = remember { mutableStateOf<NoteData?>(null) }

    val focusRequester = remember { FocusRequester() }
    CompositionLocalProvider(
        LocalCanvasContext provides CanvasContext(
            screenName,
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
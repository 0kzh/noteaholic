package screens.canvas

import NotesDTOOut
import Screen
import UpdateNoteData
import androidx.compose.runtime.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import controllers.NoteRequests
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive
import navcontroller.NavController
import utils.debounce

enum class CanvasState {
    DEFAULT, FOCUS_NOTE, FOCUS_CANVAS, NEW_NOTE, CREATING_NOTE, EDITING_NOTE, CHANGE_COLOR, NEW_TEXT, EDITOR_SCREEN
}

val COLOR_DEFAULT = 0xFFFCE183
val COLOR_RED = 0xFFFF0000
val COLOR_BLUE = 0xFF00FF00
val COLOR_GREEN = 0xFF0000FF
val COLOR_PREVIEW = 0xFFFEF6D9

val NOTE_COLORS = arrayOf(COLOR_DEFAULT, COLOR_RED, COLOR_BLUE, COLOR_GREEN)

typealias NoteData = NotesDTOOut

data class CanvasContext(
    val screenName: MutableState<String>,

    val scale: MutableState<Float>,
    val cursor: MutableState<Offset>,
    val translate: MutableState<Offset>,

    val canvasState: MutableState<CanvasState>,
    val setCanvasState: (CanvasState) -> Unit,
    val debouncedResetCanvasState: (CanvasState) -> Unit,

    val notes: MutableState<List<NoteData>>,
    val uncreatedNote: MutableState<NoteData?>,
    val selectedNote: MutableState<NoteData?>,

    val focusedNoteId: MutableState<Number?>,
    val setFocusedNoteId: (Number?) -> Unit,

    val sharedNoteId: MutableState<Int>,
    val updateNote: (UpdateNoteData) -> Unit,

    val focusRequester: FocusRequester,

    val colorIdx: MutableState<Int>,
    val resetColor: () -> Unit,
)

val LocalCanvasContext = compositionLocalOf<CanvasContext> { error("No canvas state found!") }

@Composable
fun CanvasContextProvider(
    content: @Composable() () -> Unit,
    currentScreen: String,
    sharedNoteId: MutableState<Int>,
    navController: NavController
) {
    val screenName = remember { mutableStateOf("School Notes") }

    val scale = remember { mutableStateOf(1f) }
    val cursor = remember { mutableStateOf(Offset.Zero) }
    val translate = remember { mutableStateOf(Offset.Zero) }

    val canvasState = remember { mutableStateOf(CanvasState.DEFAULT) }
    val setCanvasState =
        { newState: CanvasState -> if (canvasState.value != newState) canvasState.value = newState }

    val canvasStateScope = rememberCoroutineScope()
    // Unused arg to satisfy type constraint
    val debouncedResetCanvasState =
        debounce(500L, canvasStateScope) { _: CanvasState -> setCanvasState(CanvasState.DEFAULT) }

    val notes = remember { mutableStateOf<List<NoteData>>(listOf()) }

    val didFetchNotes = remember { mutableStateOf(false) }

//    val sharedNoteId = remember { mutableStateOf(sharedNoteIdRaw) }

    val uncreatedNote = remember { mutableStateOf<NoteData?>(null) }
    val selectedNote = remember { mutableStateOf<NoteData?>(null) }

    val focusedNoteId = remember { mutableStateOf<Number?>(null) }
    val setFocusedNoteId =
        { id: Number? -> if (focusedNoteId.value != id) focusedNoteId.value = id }

    LaunchedEffect(currentScreen) {
        // Fetch notes once more after logging in
        if (!didFetchNotes.value && currentScreen == Screen.CanvasScreen.name) {
            val res = NoteRequests.fetchNotes()
            res?.let { notes.value = it }
            didFetchNotes.value = true
        }
    }

    LaunchedEffect(currentScreen) {
        // Fetch note after shared URI
        if (currentScreen == Screen.EditorScreen.name && sharedNoteId.value != -1) {
            val res = NoteRequests.fetchNote(sharedNoteId.value)
            res?.let { selectedNote.value = it }
        }
    }

//    LaunchedEffect(sharedNoteId.value) {
//        println("SWITCHING")
//        // Switch screens
//        if (sharedNoteId.value != -1) {
//            navController.navigate(Screen.EditorScreen.name)
//        }
//    }


    val updateNoteScope = rememberCoroutineScope()
    val updateNote: (UpdateNoteData) -> Unit = { updatedNote: UpdateNoteData ->
        notes.value = notes.value.map {
            val (id, title, positionX, positionY, plainTextContent, formattedContent, colour, ownerID) = updatedNote
            if (it.id == id) {
                var newNote = it.copy()
                title?.let { newNote.title = it }
                positionX?.let { newNote.positionX = it }
                positionY?.let { newNote.positionY = it }
                plainTextContent?.let { newNote.plainTextContent = it }
                formattedContent?.let { newNote.formattedContent = it }
                colour?.let { newNote.colour = it }
                ownerID?.let { newNote.ownerID = it }
                newNote
            } else {
                it
            }
        }
        // Local state should not sync with server state here bc we need state updates to be synchronous
        updateNoteScope.launch {
            NoteRequests.updateNote(updatedNote)
        }
        println("Updated Note: ${updatedNote}")
    }

    val focusRequester = remember { FocusRequester() }

    val colorIdx = remember { mutableStateOf(0) }
    val resetColor = { colorIdx.value = 0 }

    // Reset the color when a new note gets focus
    LaunchedEffect(focusedNoteId.value) {
        resetColor()
    }

    CompositionLocalProvider(
        LocalCanvasContext provides CanvasContext(
            screenName,

            scale,
            cursor,
            translate,

            canvasState,
            setCanvasState,
            debouncedResetCanvasState,

            notes,
            uncreatedNote,
            selectedNote,

            focusedNoteId,
            setFocusedNoteId,

            sharedNoteId,
            updateNote,

            focusRequester,

            colorIdx,
            resetColor,
        ),
    ) {
        content()
    }
}
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
    val selectedNote: MutableState<NoteData?>,
    val sharedNoteId: MutableState<Int>,
    val updateNote: (UpdateNoteData) -> Unit,

    val focusRequester: FocusRequester
)

val LocalCanvasContext = compositionLocalOf<CanvasContext> { error("No canvas state found!") }

@Composable
fun CanvasContextProvider(content: @Composable() () -> Unit, currentScreen: String, sharedNoteId: MutableState<Int>, navController: NavController) {
    val screenName = remember { mutableStateOf("School Notes") }

    val scale = remember { mutableStateOf(1f) }
    val cursor = remember { mutableStateOf(Offset.Zero) }
    val translate = remember { mutableStateOf(Offset.Zero) }

    val canvasState = remember { mutableStateOf(CanvasState.FOCUS_NOTE) }
    val setCanvasState =
        { newState: CanvasState -> if (canvasState.value != newState) canvasState.value = newState }

    val notes = remember { mutableStateOf<List<NoteData>>(listOf()) }

    val didFetchNotes = remember { mutableStateOf(false) }

//    val sharedNoteId = remember { mutableStateOf(sharedNoteIdRaw) }

    val uncreatedNote = remember { mutableStateOf<NoteData?>(null) }
    val selectedNote = remember { mutableStateOf<NoteData?>(null) }

    LaunchedEffect(currentScreen) {
        // Fetch notes once more after logging in
        if (!didFetchNotes.value && currentScreen == Screen.CanvasScreen.name) {
            val res = NoteRequests.fetchNotes()
            res?.let { notes.value = it }
            didFetchNotes.value = true
        }
    }

    LaunchedEffect(currentScreen) {
        println("FETCHCHINGGN")
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


    val scope = rememberCoroutineScope()
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
        scope.launch {
            NoteRequests.updateNote(updatedNote)
        }
        println("Updated Note: ${updatedNote}")
    }

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
            selectedNote,
            sharedNoteId,
            updateNote,
            focusRequester
        ),
    ) {
        content()
    }
}
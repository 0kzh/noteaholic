package screens.canvas.components

import UpdateNoteData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import controllers.NoteRequests
import kotlinx.coroutines.launch
import screens.canvas.LocalCanvasContext
import screens.canvas.CanvasState
import screens.canvas.NoteData
import kotlin.math.roundToInt

@Composable
fun CreateNote(
) {
    val canvasState = LocalCanvasContext.current.canvasState
    val uncreatedNote = LocalCanvasContext.current.uncreatedNote
    val notes = LocalCanvasContext.current.notes
    val updateNote = LocalCanvasContext.current.updateNote

    val scope = rememberCoroutineScope()

    // Important: createNote() should be passed to UncreatedNote instead of being defined in the function body
    //  because otherwise recomposition will cancel the coroutine
    //  Source: https://stackoverflow.com/a/66600970
    val createNote = { title: String, position: IntOffset ->
        println("CREATING NOTE: ${title} ${position}")

        // Publish uncreated note
        val newList = notes.value.toMutableList()
        val newNote = NoteData(
            id = -1,
            title = title,
            positionX = position.x,
            positionY = position.y,
            plainTextContent = "",
            formattedContent = "",
            colour = "FFFCE183",
            createdAt = "",
            modifiedAt = "",
            ownerID = -1
        )
        newList.add(newNote)
        notes.value = newList

        // Make the request to create note
        scope.launch {
            val res = NoteRequests.createNote(title, position)
            if (res != null) {
                // Update the note id after successfully creating the note
                notes.value = notes.value.map {
                    if (it.id == newNote.id) {
                        var createdNote = it.copy()
                        createdNote.id = res.id
                        createdNote
                    } else {
                        it
                    }
                }
            }
        }

        // Reset uncreated note
        uncreatedNote.value = null
    }

    if (canvasState.value == CanvasState.NEW_NOTE) {
        PreviewNote()
    }
    if (canvasState.value == CanvasState.CREATING_NOTE && uncreatedNote.value != null) {
        UncreatedNote(createNote)
    }
}

@Composable
fun PreviewNote(
) {
    val scale = LocalCanvasContext.current.scale
    val cursor = LocalCanvasContext.current.cursor
    val size = DEFAULT_NOTE_SIZE * scale.value

    // Centers the position on the cursor
    val previewNoteSizePx = LocalDensity.current.run { (DEFAULT_NOTE_SIZE * scale.value).toPx() }
    val positionX = cursor.value.x - previewNoteSizePx / 2
    val positionY = cursor.value.y - previewNoteSizePx / 2
    val previewNotePosition = IntOffset(x = positionX.roundToInt(), y = positionY.roundToInt())

    @OptIn(ExperimentalFoundationApi::class) Box(Modifier.offset { previewNotePosition }
        .background(Color(DEFAULT_PREVIEW_COLOR)).size(size).padding(24.dp))
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UncreatedNote(
    createNote: (String, IntOffset) -> Unit
) {
    val scale = LocalCanvasContext.current.scale
    val translate = LocalCanvasContext.current.translate
    val uncreatedNote = LocalCanvasContext.current.uncreatedNote
    val canvasState = LocalCanvasContext.current.canvasState
    val focusRequester = LocalCanvasContext.current.focusRequester

    val title = remember { mutableStateOf("") }
    val didLoseFocus = remember { mutableStateOf(false) }

    val size = DEFAULT_NOTE_SIZE * scale.value
    val positionX = uncreatedNote.value?.positionX ?: 0
    val positionY = uncreatedNote.value?.positionY ?: 0
    val translateX = translate.value.x.roundToInt()
    val translateY = translate.value.y.roundToInt()

    // If user has clicked to create a note, give focus to the preview note to edit the title
    LaunchedEffect(Unit) {
        println("Uncreated note requested focus")
        focusRequester.requestFocus()
    }

    @OptIn(ExperimentalFoundationApi::class) (Box(
        Modifier.offset {
            IntOffset(
                positionX + translateX,
                positionY + translateY
            )
        }.background(Color(DEFAULT_COLOR)).size(size).padding(24.dp)
    ) {
        BasicTextField(
            title.value,
            { title.value = it },
            textStyle = MaterialTheme.typography.h3,
            singleLine = true,
            maxLines = 1,
            modifier = Modifier.onKeyEvent {
                when (it.type) {
                    KeyEventType.KeyDown -> {
                        when (it.key) {
                            Key.Enter -> {
                                // Switch focus to canvas, so createNote() in onFocusChanged() will run
                                canvasState.value = CanvasState.FOCUS_CANVAS
                            }
                        }
                    }
                }
                false
            }.onFocusChanged { focusState ->
                if (!focusState.isFocused && !focusState.hasFocus && !focusState.isCaptured) {
                    // This component loses focus before having focus so we need to account for that, not sure why
                    if (!didLoseFocus.value) {
                        didLoseFocus.value = true
                    } else {
                        createNote(title.value, IntOffset(positionX, positionY))
                    }
                }
            }.focusRequester(focusRequester),
        )
    })
}
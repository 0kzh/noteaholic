package screens.canvas.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import controllers.NoteRequests
import kotlinx.coroutines.CoroutineScope
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

    val scope = rememberCoroutineScope()

    // Important: createNote() should be passed to UncreatedNote instead of being defined in the function body
    //  because otherwise recomposition will cancel the coroutine
    //  Source: https://stackoverflow.com/a/66600970
    val createNote = { title: String, position: IntOffset ->
        println("CREATING NOTE: ${title} ${position}")

        // Publish uncreated note
        val list: MutableList<NoteData> = notes.value.toMutableList()
        list.add(NoteData(position = position, title = title, text = ""))
        notes.value = list.toTypedArray()

        // Make the request to create note
        scope.launch {
            println("DATA: ${title} ${position}")
            val res = NoteRequests.createNote(title, position)
            println("Request res: ${res}")
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
fun UncreatedNote(createNote: (String, IntOffset) -> Unit
) {
    val scale = LocalCanvasContext.current.scale
    val translate = LocalCanvasContext.current.translate
    val uncreatedNote = LocalCanvasContext.current.uncreatedNote
    val canvasState = LocalCanvasContext.current.canvasState
    val focusRequester = LocalCanvasContext.current.focusRequester

    val title = remember { mutableStateOf("") }
    val didLoseFocus = remember { mutableStateOf(false) }

    val size = DEFAULT_NOTE_SIZE * scale.value
    val position = uncreatedNote.value?.position ?: IntOffset.Zero
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
                position.x + translateX,
                position.y + translateY
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
                        createNote(title.value, position)
                    }
                }
            }.focusRequester(focusRequester),
        )
    })
}
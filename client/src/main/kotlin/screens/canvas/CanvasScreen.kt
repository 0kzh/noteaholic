package screens.canvas

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.key.KeyEventType.Companion.KeyDown
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import navcontroller.NavController
import screens.canvas.components.*
import kotlin.math.roundToInt

val CELL_SIZE = 50f
val SCROLL_SENSITIVITY = 0.0005f

@Composable
fun CanvasScreen(
    navController: NavController
) {
    var screenName by remember { mutableStateOf("School Notes") }
    CanvasStateProvider() {
        CanvasBackground(navController = navController)
        Navbar(name = screenName)

        Box(
            modifier = Modifier.fillMaxSize().offset { IntOffset(0, -50) },
            Alignment.BottomCenter,
            // create box with 24px rounded corners and a slight drop shadow
        ) {
            Toolbar()
        }

    }
}

@Composable
fun CanvasBackground(navController: NavController) {
    val scale = LocalCanvasState.current.scale
    val notes = LocalCanvasState.current.notes

    Canvas(
        modifier = Modifier.fillMaxSize().background(Color.White)
            .createNote().scale().translate().updateCursor().keyboardShortcuts(),
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val rows = (canvasHeight / CELL_SIZE / scale.value).roundToInt()
        val columns = (canvasWidth / CELL_SIZE / scale.value).roundToInt()

        for (row in 1..rows) {
            for (col in 1..columns) {
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.3f), center = Offset(
                        x = col * CELL_SIZE * scale.value - (CELL_SIZE / 2),
                        y = row * CELL_SIZE * scale.value - (CELL_SIZE / 2)
                    ), radius = 3f
                )
            }
        }
    }
    for (note in notes.value) {
        Note(
            note = note, navController = navController
        )
    }
    PreviewNote()


}

/**
 * Scales all canvas contents
 */
fun Modifier.scale(): Modifier = composed {
    val scale = LocalCanvasState.current.scale
    val setToolbarState = LocalCanvasState.current.setToolbarState

    this.scrollable(
        orientation = Orientation.Vertical,
        state = rememberScrollableState { delta ->
            // Delta is negative when scrolling up
            if (delta > 0 && scale.value < 2.0f) {
                scale.value = Math.min(scale.value + delta * SCROLL_SENSITIVITY, 2.0f)
            }
            if (delta < 0 && scale.value > 0.8) {
                scale.value = Math.max(scale.value + delta * SCROLL_SENSITIVITY, 0.8f)
            }
            setToolbarState(ToolbarState.FOCUS_CANVAS)

            delta
        })
}

/**
 * Translates all canvas contents
 */
fun Modifier.translate(): Modifier = composed {
    val translate = LocalCanvasState.current.translate
    val setToolbarState = LocalCanvasState.current.setToolbarState

    this.pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            change.consumeAllChanges()

            val newTranslateX = translate.value.x + dragAmount.x
            val newTranslateY = translate.value.y + dragAmount.y
            translate.value = Offset(newTranslateX, newTranslateY)

            println("Translate")

            setToolbarState(ToolbarState.FOCUS_CANVAS)
        }
    }
}

/**
 * Updates the cursor position on canvas
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.updateCursor(): Modifier = composed {
    val cursor = LocalCanvasState.current.cursor

    this.onPointerEvent(PointerEventType.Move) {
        val position = it.changes.first().position
        cursor.value = position
    }
}

/**
 * Creates a note when the user clicks on canvas
 */
fun Modifier.createNote(): Modifier = composed {
    val notes = LocalCanvasState.current.notes
    val scale = LocalCanvasState.current.scale
    val cursor = LocalCanvasState.current.cursor
    val translate = LocalCanvasState.current.translate
    val toolbarState = LocalCanvasState.current.toolbarState
    val setToolbarState = LocalCanvasState.current.setToolbarState

    val previewNoteSizePx = LocalDensity.current.run { (DEFAULT_NOTE_SIZE * scale.value).toPx() }

    println("Translate: ${translate.value}")
    this.pointerInput(Unit) {
        detectTapGestures { _ ->
            println("ToolbarState: ${toolbarState}")
            if (toolbarState.value == ToolbarState.NEW_NOTE) {

                // Similar code to in PreviewNote()
                // Apply translations to the note's position relative to the canvas
                val positionX = cursor.value.x - translate.value.x - previewNoteSizePx / 2
                val positionY = cursor.value.y - translate.value.y - previewNoteSizePx / 2
                val previewNotePosition = IntOffset(x = positionX.roundToInt(), y = positionY.roundToInt())
                println("Cursor: ${cursor.value.x} ${cursor.value.y}")
                println("New position: ${previewNotePosition}")

                val list: MutableList<NoteData> = notes.value.toMutableList()
                list.add(NoteData(position = previewNotePosition, text = "Hello"))
                notes.value = list.toTypedArray()

                setToolbarState(ToolbarState.FOCUS_CANVAS)
            }
        }
    }
}

/**
 * Creates a note when the user clicks on canvas
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.keyboardShortcuts(): Modifier = composed {
    val setToolbarState = LocalCanvasState.current.setToolbarState

    // Don't ask what's going on with this FocusRequester logic
    //  source: https://medium.com/google-developer-experts/focus-in-jetpack-compose-6584252257fe
    val requester = FocusRequester()

    LaunchedEffect(Unit) {
        requester.requestFocus()
    }


    // Order matters here for onPreviewKeyEvent -> focusRequester() -> focusable()
    //  source: https://stackoverflow.com/questions/70015530/unable-to-focus-anything-other-than-textfield
    this.onKeyEvent {
        when (it.type) {
            KeyDown -> {
                when (it.key) {
                    Key.N -> {
                        setToolbarState(ToolbarState.NEW_NOTE)
                    }
                    Key.T -> {
                        setToolbarState(ToolbarState.NEW_TEXT)
                    }
                }
            }
        }
        false
    }.focusRequester(requester).focusable()
}
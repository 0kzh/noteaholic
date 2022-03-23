package screens.canvas

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
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
import utils.debounce
import kotlin.math.roundToInt

val CELL_SIZE = 50f

@Composable
fun CanvasScreen(
    navController: NavController
) {
    CanvasBackground(navController = navController)
    Navbar(navController)

    Box(
        modifier = Modifier.fillMaxSize().offset { IntOffset(0, -50) },
        Alignment.BottomCenter,
    ) {
        Toolbar()
    }
}

@Composable
fun CanvasBackground(navController: NavController) {
    val scale = LocalCanvasContext.current.scale
    val notes = LocalCanvasContext.current.notes

    Canvas(
        modifier = Modifier.fillMaxSize().background(Color.White).createNote().translate().updateCursor()
            .keyboardShortcuts(),
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
        // Optimize render performance using keys
        //  Source: https://pankaj-rai.medium.com/jetpack-compose-optimize-list-performance-with-key-1066567339f9
        key(note.id) {
            Note(
                note = note, navController = navController
            )
        }
    }
    CreateNote()
}

///**
// * Scales all canvas contents
// */
//fun Modifier.scale(): Modifier = composed {
//    val scale = LocalCanvasContext.current.scale
//    val setCanvasState = LocalCanvasContext.current.setCanvasState
//    val setFocusedNoteId = LocalCanvasContext.current.setFocusedNoteId
//
//    this.scrollable(orientation = Orientation.Horizontal, state = rememberScrollableState { delta ->
//        // Delta is negative when scrolling up
//        if (delta > 0 && scale.value < 2.0f) {
//            scale.value = Math.min(scale.value + delta * SCROLL_SENSITIVITY, 2.0f)
//        }
//        if (delta < 0 && scale.value > 0.8) {
//            scale.value = Math.max(scale.value + delta * SCROLL_SENSITIVITY, 0.8f)
//        }
//
//        println("Scroll")
//        setCanvasState(CanvasState.FOCUS_CANVAS)
//        setFocusedNoteId(null)
//
//        delta
//    })
//}

/**
 * Translates all canvas contents
 */
fun Modifier.translate(): Modifier = composed {
    val translate = LocalCanvasContext.current.translate
    val canvasState = LocalCanvasContext.current.canvasState

    this.pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
            if (canvasState.value == CanvasState.FOCUS_CANVAS) {
                change.consumeAllChanges()

                val newTranslateX = translate.value.x + dragAmount.x
                val newTranslateY = translate.value.y + dragAmount.y
                translate.value = Offset(newTranslateX, newTranslateY)

                println("Translate")
            }
        }
    }
}

/**
 * Updates the cursor position on canvas
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.updateCursor(): Modifier = composed {
    val cursor = LocalCanvasContext.current.cursor

    this.onPointerEvent(PointerEventType.Move) {
        val position = it.changes.first().position
        cursor.value = position
    }
}

/**
 * Creates a note when the user clicks on canvas
 */
fun Modifier.createNote(): Modifier = composed {
    val scale = LocalCanvasContext.current.scale
    val cursor = LocalCanvasContext.current.cursor
    val translate = LocalCanvasContext.current.translate
    val canvasState = LocalCanvasContext.current.canvasState
    val uncreatedNote = LocalCanvasContext.current.uncreatedNote

    val previewNoteSizePx = LocalDensity.current.run { (DEFAULT_NOTE_SIZE * scale.value).toPx() }

    this.pointerInput(Unit) {
        detectTapGestures { _ ->
            if (canvasState.value == CanvasState.NEW_NOTE) {

                // Similar code to in PreviewNote()
                // Apply translations to the note's position relative to the canvas
                val positionX = cursor.value.x - translate.value.x - previewNoteSizePx / 2
                val positionY = cursor.value.y - translate.value.y - previewNoteSizePx / 2
                val previewNotePosition = IntOffset(x = positionX.roundToInt(), y = positionY.roundToInt())

                canvasState.value = CanvasState.CREATING_NOTE
                uncreatedNote.value = NoteData(
                    id = -1,
                    title = "",
                    positionX = previewNotePosition.x,
                    positionY = previewNotePosition.y,
                    plainTextContent = "",
                    formattedContent = "",
                    colour = COLOR_DEFAULT.toString(),
                    createdAt = "",
                    modifiedAt = "",
                    ownerID = -1
                )
            }
        }
    }
}

/**
 * Creates a note when the user clicks on canvas
 */
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.keyboardShortcuts(): Modifier = composed {
    val canvasState = LocalCanvasContext.current.canvasState
    val setCanvasState = LocalCanvasContext.current.setCanvasState
    val focusRequester = LocalCanvasContext.current.focusRequester
    val setFocusedNoteId = LocalCanvasContext.current.setFocusedNoteId
    val debouncedResetCanvasState = LocalCanvasContext.current.debouncedResetCanvasState
    val colorIdx = LocalCanvasContext.current.colorIdx

    val changeColor = {
        colorIdx.value += 1
        if (colorIdx.value >= NOTE_COLORS.size) {
            colorIdx.value = 0
        }
    }

    val resetColor = {
        colorIdx.value = 0
    }
    // Resume focus onto Canvas when possible for keyboard shortcuts to work
    LaunchedEffect(canvasState.value) {
        if (canvasState.value != CanvasState.CREATING_NOTE && canvasState.value != CanvasState.FOCUS_NOTE) {
            println("canvas requested focus")
            focusRequester.requestFocus()
        }
    }

    println("Color: ${colorIdx.value}")

    // Order matters here for onPreviewKeyEvent -> focusRequester() -> focusable()
    //  source: https://stackoverflow.com/questions/70015530/unable-to-focus-anything-other-than-textfield
    this.onKeyEvent {
        when (it.type) {
            KeyDown -> {
                when (it.key) {
                    Key.C -> {
                        if (canvasState.value != CanvasState.CHANGE_COLOR) {
                            setCanvasState(CanvasState.CHANGE_COLOR)
                            resetColor()
                        }
                        changeColor()
                    }
                    Key.E -> {
                        setCanvasState(CanvasState.EDITING_NOTE)
                    }
                    Key.N -> {
                        setCanvasState(CanvasState.NEW_NOTE)
                    }
                    Key.T -> {
                        setCanvasState(CanvasState.NEW_TEXT)
                    }
                    Key.Spacebar -> {
                        setCanvasState(CanvasState.FOCUS_CANVAS)
                        setFocusedNoteId(null)
                        debouncedResetCanvasState(CanvasState.DEFAULT)
                    }
                    Key.Escape -> {
                        setCanvasState(CanvasState.DEFAULT)
                        setFocusedNoteId(null)
                    }
                }
            }
        }
        false
    }.focusRequester(focusRequester).focusable()
}
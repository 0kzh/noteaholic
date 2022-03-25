package screens.canvas.components

import Screen
import UpdateNoteData
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import navcontroller.NavController
import screens.canvas.CanvasState
import screens.canvas.LocalCanvasContext
import screens.canvas.NoteData
import utils.debounce
import kotlin.math.roundToInt


val DEFAULT_NOTE_SIZE = 200.dp
const val DEFAULT_BORDER_COLOR = 0xFF0f0f0f

@OptIn(ExperimentalFoundationApi::class)
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

    val size = DEFAULT_NOTE_SIZE * scale.value

    // TODO: Modify note position directly instead
    val translateX = translate.value.x.roundToInt()
    val translateY = translate.value.y.roundToInt()

    val scope = rememberCoroutineScope()
    val debouncedUpdateNote = debounce(400L, scope, updateNote)

    val noteColor = Color(note.colour.toLong())

    var positionX by remember { mutableStateOf(note.positionX) }
    var positionY by remember { mutableStateOf(note.positionY) }
    var color by remember { mutableStateOf(noteColor) }
    var showColorPicker by remember { mutableStateOf(false) }

    val gesturesEnabled = canvasState.value != CanvasState.NEW_NOTE && canvasState.value != CanvasState.FOCUS_CANVAS
    val isFocused = focusedNoteId.value == note.id

    fun updateColor() {
        debouncedUpdateNote(UpdateNoteData(id = note.id, colour = color.toArgb().toString()))
    }

    fun rejectColor() {
        color = noteColor
    }

    LaunchedEffect(isFocused) {
        if (!isFocused) {
            showColorPicker = false
            rejectColor()
        }
    }

    if (gesturesEnabled) {
        Box(Modifier.offset {
            IntOffset(positionX + translateX, positionY + translateY)
        }) {
            Box(Modifier.background(color).size(size).combinedClickable(onClick = {
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
            }.focusedBorder(isFocused).padding(24.dp), contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = "${note.title}", style = MaterialTheme.typography.h3
                )
            }
            if (isFocused) {
                Box(Modifier.align(Alignment.BottomEnd)) {
                    Row {
                        if (showColorPicker) {
                            IconButton({
                                updateColor()
                                showColorPicker = !showColorPicker
                            }) {
                                Icon(Icons.Filled.Done, "Accept Color Change")
                            }
                            IconButton({
                                rejectColor()
                                showColorPicker = !showColorPicker
                            }) {
                                Icon(Icons.Filled.Clear, "Reject Color Change")
                            }
                        } else {
                            IconButton({ showColorPicker = !showColorPicker }) {
                                Icon(painterResource("icons/palette.svg"), "Change Color")
                            }
                        }
                    }
                }
            }
            if (showColorPicker) {
                Popup(Alignment.TopEnd,
                    with(LocalDensity.current) { IntOffset(size.roundToPx() + 10.dp.roundToPx(), 0) },
                    onDismissRequest = {
                        rejectColor()
                    }) {
                    Column(Modifier.background(Color.White)) {
                        ClassicColorPicker(
                            Modifier.size(size),
                            showAlphaBar = false,
                            onColorChanged = { changedColor: HsvColor ->
                                color = changedColor.toColor()
                            })
                    }
                }
            }
        }
    } else {
        Box(
            Modifier.offset {
                IntOffset(positionX + translateX, positionY + translateY)
            }.background(color).size(DEFAULT_NOTE_SIZE * scale.value).padding(24.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = "${note.title}", style = MaterialTheme.typography.h3
            )
        }
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



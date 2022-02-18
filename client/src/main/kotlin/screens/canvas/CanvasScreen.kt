package screens.canvas

import Screen
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import components.Border
import components.border
import navcontroller.NavController
import kotlin.math.roundToInt

data class NoteData(val position: IntOffset = IntOffset.Zero, val text: String = "")

@Composable
fun Note(
    scale: Float, translateX: Int, translateY: Int, note: NoteData, navController: NavController
) {
    var positionX by remember { mutableStateOf(note.position.x) }
    var positionY by remember { mutableStateOf(note.position.y) }
    @OptIn(ExperimentalFoundationApi::class)
    Box(Modifier.offset { IntOffset(positionX + translateX, positionY + translateY) }
        .background(Color(0xFFFCE183)).size(100.dp * scale).pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consumeAllChanges()
                positionX += dragAmount.x.roundToInt()
                positionY += dragAmount.y.roundToInt()
            }
        }.combinedClickable(onClick = {}, onDoubleClick = {
            navController.navigate(Screen.EditorScreen.name)
        }), contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${note.text}", style = MaterialTheme.typography.h6
        )
    }
}

@Composable
fun Zoom(
) {
    val scale = LocalCanvasState.current.scale
    val onScaleChange = LocalCanvasStateMutators.current.setScale
    Text(text = "Zoom", style = MaterialTheme.typography.h6)

}

@Composable
fun Navbar(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth().height(60.dp).background(Color.White)
            .border(bottom = Border(1.dp, Color.Gray.copy(alpha = 0.5f))),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
//        Box(modifier = Modifier.size(0.dp))
        Zoom()
        Text(text = name, style = MaterialTheme.typography.h6)
        Zoom()
    }
}

@Composable
fun Toolbar(
) {

}

@Composable
fun CanvasBackground(navController: NavController) {
    val scale = LocalCanvasState.current.scale
    val onScaleChange = LocalCanvasStateMutators.current.setScale
    var offsetX by remember { mutableStateOf(0) }
    var offsetY by remember { mutableStateOf(0) }

//    val translateX = (offsetX * scale).roundToInt()
    val translateX = offsetX
//    val translateY = (offsetY * scale).roundToInt()
    val translateY = offsetY

    val CELL_SIZE = 50f
    val SCROLL_SENSITIVITY = 0.0005f

    val notes = arrayListOf<NoteData>(
        NoteData(position = IntOffset(500, 500), text = "Hello"),
        NoteData(position = IntOffset(100, 800), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(400, 300), text = "Hello")
    )

    Canvas(
        modifier = Modifier.fillMaxSize().background(Color.White)
            .scrollable(orientation = Orientation.Vertical,
                state = rememberScrollableState { delta ->
                    // Delta is negative when scrolling up
                    if (delta > 0) {
                        if (scale < 2.5f) {
                            onScaleChange(Math.min(scale + delta * SCROLL_SENSITIVITY, 2.5f))
                        }
                    } else {
                        if (scale > 0.5) {
                            onScaleChange(Math.max(scale + delta * SCROLL_SENSITIVITY, 0.5f))
                        }
                    }
                    delta
                }).pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consumeAllChanges()
                    offsetX += dragAmount.x.roundToInt()
                    offsetY += dragAmount.y.roundToInt()
                }
                detectTapGestures(
                    onDoubleTap = {
                        navController.navigate(Screen.EditorScreen.name)
                    }
                )
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val rows = (canvasHeight / CELL_SIZE / scale).roundToInt()
        val columns = (canvasWidth / CELL_SIZE / scale).roundToInt()


        for (row in 1..rows) {
            for (col in 1..columns) {
                drawCircle(
                    color = Color.Gray.copy(alpha = 0.3f), center = Offset(
                        x = col * CELL_SIZE * scale - (CELL_SIZE / 2), y = row * CELL_SIZE * scale - (CELL_SIZE / 2)
                    ), radius = 3f
                )
            }
        }
    }
    for (note in notes) {
        Note(
            scale = scale,
            translateX = translateX,
            translateY = translateY,
            note = note,
            navController = navController
        )
    }
}

@Composable
fun CanvasScreen(
    navController: NavController
) {
    var screenName by remember { mutableStateOf("School Notes") }
    CanvasStateProvider() {
        CanvasBackground(navController = navController)
        Toolbar()
        Navbar(name = screenName)

    }
}
package screens.canvas

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import navcontroller.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

data class NoteData(val position: IntOffset = IntOffset.Zero, val text: String = "")

@Composable
fun Note(
    scale: Float, translateX: Int, translateY: Int, note: NoteData
) {
    var positionX by remember { mutableStateOf(note.position.x) }
    var positionY by remember { mutableStateOf(note.position.y) }
    Box(Modifier.offset { IntOffset(positionX + translateX, positionY + translateY) }
        .background(MaterialTheme.colors.primary).size(100.dp * scale).pointerInput(Unit) {
            detectDragGestures { change, dragAmount ->
                change.consumeAllChanges()
                positionX += dragAmount.x.roundToInt()
                positionY += dragAmount.y.roundToInt()
            }
        }, contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${note.text}", style = MaterialTheme.typography.h6
        )
    }
}

@Composable
fun Toolbar(
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    onScaleChange: (Float) -> Unit,
    onOffsetXChange: (Float) -> Unit,
    onOffsetYChange: (Float) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(vertical = 50.dp), contentAlignment = Alignment.BottomCenter) {
        Column(
            modifier = Modifier.background(MaterialTheme.colors.secondary).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Zoom in: scale = ${scale}",
                modifier = Modifier.clickable { onScaleChange(Math.min(scale + 0.1f, 3f)) })
            Text(text = "Zoom out: scale = ${scale}",
                modifier = Modifier.clickable { onScaleChange(Math.max(scale - 0.1f, 0.3f)) })
            Text(text = "Translate right: offset = ${offsetX}",
                modifier = Modifier.clickable { onOffsetXChange(offsetX + 50) })
            Text(text = "Translate left: offset = ${offsetX}",
                modifier = Modifier.clickable { onOffsetXChange(offsetX - 50) })
            Text(text = "Translate up: offset = ${offsetY}",
                modifier = Modifier.clickable { onOffsetYChange(offsetY + 50) })
            Text(text = "Translate down: offset = ${offsetY}",
                modifier = Modifier.clickable { onOffsetYChange(offsetY - 50) })
        }
    }

}

@Composable
fun ResizableCanvas() {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val translateX = (-offsetX * scale).roundToInt()
    val translateY = (-offsetY * scale).roundToInt()

    val gridSize = 50f

    val notes = arrayListOf<NoteData>(
        NoteData(position = IntOffset(500, 500), text = "Hello"),
        NoteData(position = IntOffset(100, 800), text = "Hello"),
        NoteData(position = IntOffset(200, 1200), text = "Hello"),
        NoteData(position = IntOffset(400, 300), text = "Hello")
    )


    Canvas(
        modifier = Modifier.fillMaxSize().background(Color.Gray)
//            .scrollable(orientation = Orientation.Vertical,
//                state = rememberScrollableState { delta ->
//                    // Delta is negative when scrolling up
//                    if (delta < 0) {
//                        scale = Math.min(scale + delta, 3f)
//                    } else {
//                        scale = Math.min(scale + delta, 0.2f)
//                    }
//                    delta
//                })
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val rows = (canvasHeight / gridSize / scale).roundToInt()
        val columns = (canvasWidth / gridSize / scale).roundToInt()

        repeat(times = rows) {
            drawLine(
                start = Offset(x = 0f, y = (it + 1) * gridSize * scale),
                end = Offset(x = canvasWidth, y = (it + 1) * gridSize * scale),
                color = Color.Blue
            )
        }
        repeat(times = columns) {
            drawLine(
                start = Offset(x = (it + 1) * gridSize * scale, y = 0f),
                end = Offset(x = (it + 1) * gridSize * scale, y = canvasHeight),
                color = Color.Blue
            )
        }
    }
    for (note in notes) {
        Note(scale = scale, translateX = translateX, translateY = translateY, note = note)
    }
    Toolbar(scale = scale,
        offsetX = offsetX,
        offsetY = offsetY,
        onScaleChange = { scale = it },
        onOffsetXChange = { offsetX = it },
        onOffsetYChange = { offsetY = it })

}

@Composable
fun CanvasScreen(
    navController: NavController
) {
    ResizableCanvas()
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Canvas", style = MaterialTheme.typography.h6
        )
        Button(onClick = {
            navController.navigate(Screen.EditorScreen.name)
        }) {
            Text(
                text = "Open note"
            )
        }
        Button(onClick = {
            navController.navigate(Screen.LoginScreen.name)
        }) {
            Text(
                text = "Sign out"
            )
        }
    }
}
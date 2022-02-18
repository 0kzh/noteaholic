package screens.canvas

import androidx.compose.runtime.*

data class CanvasState(
    val scale: Float,
)
data class CanvasStateMutators(
    val setScale: (Float) -> Unit,
)
val LocalCanvasState = compositionLocalOf<CanvasState> { error("No canvas state found!") }
val LocalCanvasStateMutators = compositionLocalOf<CanvasStateMutators> { error("No canvas state mutator found!") }

@Composable
fun CanvasStateProvider(content: @Composable() () -> Unit) {
    val (scale, setScale) = remember { mutableStateOf(1f) }

    CompositionLocalProvider(
        LocalCanvasState provides CanvasState(scale),
        LocalCanvasStateMutators provides CanvasStateMutators(setScale),
    ) {
        content()
    }
}
package screens

import Screen
import SearchNoteDTOOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEvent
import androidx.compose.ui.draw.blur
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import controllers.NoteRequests.search
import kotlinx.coroutines.launch
import navcontroller.NavController
import screens.canvas.LocalCanvasContext
import screens.canvas.components.SearchResult
import java.awt.event.KeyEvent

// From https://stackoverflow.com/questions/66494838/android-compose-how-to-use-html-tags-in-a-text-view
fun htmlBoldToAnnotated(input: String): AnnotatedString {
    val parts = input.split("<b>", "</b>")
    println(parts)
    var isBold = false
    return buildAnnotatedString {
        for (part in parts) {
            if (isBold) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(part)
                }
            } else {
                append(part)
            }
            isBold = !isBold
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchPalette(onDismissRequest: () -> Unit, navController: NavController) {
    val scope = rememberCoroutineScope()
    val onClickOutDismiss = {}
    val loading = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val searchResults = remember { mutableStateOf(emptyList<SearchNoteDTOOut>()) }
    val selectedNote = LocalCanvasContext.current.selectedNote
    val enteredText = remember { mutableStateOf("") }



    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Popup(
        popupPositionProvider = object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset = IntOffset.Zero
        },
        focusable = true,
        onDismissRequest = onClickOutDismiss,
        onKeyEvent = {
            if (it.awtEvent.keyCode == KeyEvent.VK_ESCAPE) {
                onDismissRequest()
                true
            } else {
                false
            }
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize().background(SolidColor(Color.White), alpha = 0.4f)
                .pointerInput(onDismissRequest) {
                    detectTapGestures(onPress = {
                        onDismissRequest()
                    })
                }.padding(top = 80.dp), /* padding must be at end so that click event works on full background */
            contentAlignment = Alignment.TopCenter,
        ) {
            Surface(
                elevation = 24.dp,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(0.1.dp, Color.LightGray),
                modifier = Modifier.pointerInput(onDismissRequest) {
                    // Prevents the tap event from bubbling up and causing the thing to close.
                    detectTapGestures(onPress = { focusRequester.requestFocus() })
                },
            ) {
                Box(Modifier.width(500.dp)) {
                    if (loading.value) {
                        LinearProgressIndicator(Modifier.fillMaxWidth().height(3.dp), color = Color.LightGray)
                    }
                    Column(Modifier.padding(8.dp)) {
                        Row {
                            Box(
                                modifier = Modifier.defaultMinSize(48.dp, 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Search, "Search")
                            }
                            Box(
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp).fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            )
                            {
                                BasicTextField(
                                    enteredText.value,
                                    {
                                        enteredText.value = it
                                        loading.value = true
                                        scope.launch {
                                            searchResults.value = search(it)
                                            loading.value = false
                                        }
                                    },
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.h4,
                                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                                )
                            }
                        }

                        LazyColumn(modifier = Modifier.heightIn(0.dp, 300.dp)) {
                            items(searchResults.value) { item ->
                                SearchResult(
                                    htmlBoldToAnnotated(item.matchingBody.takeWhile { it != '\n' }),
                                    item.note.title
                                ) {
                                    onDismissRequest()
                                    selectedNote.value = item.note
                                    navController.navigate(Screen.EditorScreen.name)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
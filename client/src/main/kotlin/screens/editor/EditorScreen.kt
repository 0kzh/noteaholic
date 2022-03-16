package screens.editor

import PrivateJSONToken
import UpdateNoteData
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import components.Avatar
import components.Border
import components.OutlinedTextFieldWithError
import components.border
import controllers.EditorController
import controllers.NoteRequests
import kotlinx.coroutines.launch
import navcontroller.NavController
import screens.canvas.LocalCanvasContext
import utils.debounce
import utils.formatDateTime
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

typealias ComposableFun = @Composable (m: Modifier) -> Unit

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EditorScreen(
    navController: NavController,
    editorController: EditorController
) {
    val updateNote = LocalCanvasContext.current.updateNote
    val selectedNote = LocalCanvasContext.current.selectedNote
    val sharedNoteId = LocalCanvasContext.current.sharedNoteId


    var text by rememberSaveable { mutableStateOf("") }
    var createdAt by rememberSaveable { mutableStateOf(sharedNoteId.value.toString()) }
    var isEditingTitle by remember { mutableStateOf(false) }
    var currentTitle by remember { mutableStateOf("") }

    LaunchedEffect(selectedNote.value) {
        if (selectedNote.value != null) {
            text = selectedNote.value!!.plainTextContent
            currentTitle = selectedNote.value!!.title
            createdAt = selectedNote.value!!.createdAt
        }
    }

    val focusRequester = remember { FocusRequester() }
    val emails = remember { mutableStateOf("") }

    LaunchedEffect(isEditingTitle) {
        if (isEditingTitle) {
            focusRequester.requestFocus()
        }
    }

    val alertDialog = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val debouncedUpdateNote = debounce(400L, scope, updateNote)

    val tableData = listOf<Pair<String, ComposableFun>>(
        Pair(
            "Last Modified"
        ) { Text(formatDateTime(selectedNote.value!!.modifiedAt), it) },
        Pair("Created by") {
            Row(it, horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
                Avatar(
                    id = selectedNote.value!!.ownerID.toString(),
                    firstName = PrivateJSONToken.getNameOfUser().split(" ")[0],
                    lastName = PrivateJSONToken.getNameOfUser().split(" ")[1],
                    size = 24.dp
                )
                Text(PrivateJSONToken.getNameOfUser())
            }
        },
        Pair("Created at") { Text(formatDateTime(createdAt), it) }
    )

    Column(
        modifier = Modifier.fillMaxSize().border(bottom = Border(1.dp, Color.Gray.copy(alpha = 0.5f)))
    ) {
        TopAppBar(
            backgroundColor = Color.White,
            elevation = 0.dp,
            navigationIcon = {
                IconButton(onClick = {
                    navController.navigateBack()
                    selectedNote.value = null
                    sharedNoteId.value = -1
                }) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Navigate Back"
                    )
                }
            },
            actions = {
                IconButton(
                    onClick = { alertDialog.value = true },
                    enabled = sharedNoteId.value == -1
                ) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = Color.Black
                    )
                }
            },
            title = { Text("") }
        )

        Column(
            modifier = Modifier.fillMaxWidth().background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                // white background
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(0.dp, 600.dp)
            ) {
                Spacer(Modifier.height(2.dp))

                Column(Modifier.padding(16.dp, 0.dp)) {
                    // TODO: change from hardcoded values
                    BasicTextField(
                        currentTitle,
                        {
                            currentTitle = it
                            debouncedUpdateNote(
                                UpdateNoteData(
                                    id = selectedNote.value!!.id,
                                    title = currentTitle
                                )
                            )
                        },
                        singleLine = true,
                        maxLines = 1,
                        textStyle = TextStyle(
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        modifier = Modifier.focusRequester(focusRequester),
                        enabled = sharedNoteId.value == -1
                    )

                    // table of metadata
                    LazyColumn(Modifier.fillMaxWidth()) {
                        items(tableData) {
                            val (title, component) = it
                            Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                                Text(text = title, Modifier.weight(.25f), color = Color.Gray)
                                component(Modifier.weight(.75f))
//                                Text("asdf", Modifier.weight(.75f))
                            }
                        }
                    }
//                    Text("Tags: ")
                }

                if (alertDialog.value) {
                    ShareNoteDialog(emails, alertDialog, selectedNote.value!!.id)
                }

                Divider(color = Color.LightGray, thickness = 1.dp, modifier = Modifier.padding(16.dp))

                BasicTextField(
                    modifier = Modifier.padding(16.dp, 1.dp).fillMaxWidth().fillMaxHeight(),
                    value = text,
                    onValueChange = {
                        text = it
                        if (selectedNote.value != null) {
                            debouncedUpdateNote(
                                UpdateNoteData(
                                    id = selectedNote.value!!.id,
                                    formattedContent = it,
                                    plainTextContent = it,
                                )
                            )
                        }
                    },
                    maxLines = Int.MAX_VALUE,
                    textStyle = MaterialTheme.typography.h6,
                    visualTransformation = MarkdownTransform(editorController),
                    enabled = sharedNoteId.value == -1
                )
            }
        }

        Button(
            onClick = {
                navController.navigateBack()
            }
        ) {
            Text(
                text = "Close note"
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
fun ShareNoteDialog(
    emails: MutableState<String>,
    alertDialog: MutableState<Boolean>,
    selectedNoteId: Int,
) {
    val scope = rememberCoroutineScope()
    val bgColors = TextFieldDefaults.outlinedTextFieldColors().backgroundColor(true)
    val url = "noteaholic://?noteId=${LocalCanvasContext.current.selectedNote.value?.id}"
    AlertDialog(
        onDismissRequest = {},
        shape = MaterialTheme.shapes.large,
        title = {
            Text(text = "Share Note", style = MaterialTheme.typography.h4)
        },
        text = {
            Column {
                Row(Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = {},
                        enabled = false,
                        shape = MaterialTheme.shapes.small,
                        border = ButtonDefaults.outlinedBorder,
                        color = bgColors.value,
                        contentColor = contentColorFor(bgColors.value)
                    ) {
                        Row(
                            modifier = Modifier.defaultMinSize(
                                minWidth = ButtonDefaults.MinWidth,
                                minHeight = ButtonDefaults.MinHeight
                            ).padding(ButtonDefaults.ContentPadding)
                        ) {
                            BasicTextField(
                                url,
                                {},
                            )
                        }
                    }
                    OutlinedButton({
                        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(url), null)
                    }, modifier = Modifier.padding(start = 2.dp)) {
                        Text("Copy")
                    }
                }


                Spacer(Modifier.height(16.dp))
                Text(
                    "Enter comma-separated emails for collaborators (must be users in Noteaholic)",
                    Modifier.padding(bottom = 8.dp), style = MaterialTheme.typography.h6
                )
                OutlinedTextFieldWithError(
                    readOnly = false,
                    value = emails.value,
                    label = { Text("Emails") },
                    onValueChange = { emails.value = it },
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    alertDialog.value = false
                }) {
                Text("CANCEL", style = MaterialTheme.typography.h6)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {

                    println("IN ADD COLLAB")
                    scope.launch {
                        println(selectedNoteId)
                        println("IN SCOPE ADDCOLLABO")
                        val res = NoteRequests.addCollaborators(selectedNoteId, emails.value.split(","))
                        println("ADDCOLLAB $res")
                        alertDialog.value = false
                    }
                }) {
                Text("SHARE NOTE", style = MaterialTheme.typography.h6)
            }
        }
    )
}

//@Composable
//fun RowScope.TableCell(
//    text: Composable,
//    weight: Float,
//    color: Color = Color.Black,
//) {
//    text
////    Text(
////        text = text,
////        Modifier.weight(weight).padding(top = 4.dp, bottom = 4.dp),
////        color = color
////    )
//}

class MarkdownTransform(private val editorController: EditorController) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            buildAnnotatedStringWithFormatting(text.toString(), editorController),
            offsetMapping = OffsetMapping.Identity
        )
    }
}

fun buildAnnotatedStringWithFormatting(text: String, editorController: EditorController): AnnotatedString {
    val ret = editorController.parseMarkdown(text)
    println("ret '$ret' text: '$text'")
    return ret
}
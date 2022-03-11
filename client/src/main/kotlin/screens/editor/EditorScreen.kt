package screens.editor

import UpdateNoteData
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import components.OutlinedTextFieldWithError
import controllers.EditorController
import controllers.NoteRequests
import kotlinx.coroutines.launch
import navcontroller.NavController
import screens.canvas.LocalCanvasContext
import utils.debounce
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

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

    var edittingTitle by remember { mutableStateOf(currentTitle) }
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

    Column(
        modifier = Modifier.fillMaxSize()
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
            title = {
                if (!isEditingTitle) {
                    Text(text = currentTitle, style = MaterialTheme.typography.h4)
                    IconButton(
                        onClick = {
                            isEditingTitle = !isEditingTitle
                        },
                        enabled = sharedNoteId.value == -1
                    ) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Edit Title"
                        )
                    }
                } else {
                    BasicTextField(
                        edittingTitle,
                        { edittingTitle = it },
                        singleLine = true,
                        maxLines = 1,
                        textStyle = MaterialTheme.typography.h4,
                        modifier = Modifier.focusRequester(focusRequester),
                        enabled = sharedNoteId.value == -1
                    )
                    IconButton(onClick = {
                        currentTitle = edittingTitle
                        debouncedUpdateNote(UpdateNoteData(
                            id = selectedNote.value!!.id,
                            title = edittingTitle
                        ))
                        isEditingTitle = false
                        focusRequester.freeFocus()
                    }) {
                        Icon(
                            Icons.Filled.Done,
                            contentDescription = "Accept"
                        )
                    }
                    IconButton(onClick = {
                        isEditingTitle = false
                    }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Discard"
                        )
                    }

                }
            })
        Spacer(Modifier.height(2.dp))


        Column(Modifier.padding(16.dp, 0.dp)) {
            // TODO: change from hardcoded values
            Text("Created by: ${PrivateJSONToken.getNameOfUser()}")
            Text("Created at: ${createdAt}")
            Text("Tags: ")
        }

        if (alertDialog.value) {
            ShareNoteDialog(emails, alertDialog, selectedNote.value!!.id)
        }


        BasicTextField(
            modifier = Modifier.padding(16.dp, 1.dp).fillMaxWidth().fillMaxHeight(),
            value = text,
            onValueChange = {
                text = it
                if (selectedNote.value != null) {
                    debouncedUpdateNote(UpdateNoteData(
                        id = selectedNote.value!!.id,
                        formattedContent = it,
                        plainTextContent = it,
                    ))
                }
            },
            maxLines = Int.MAX_VALUE,
            textStyle = MaterialTheme.typography.h6,
            visualTransformation = MarkdownTransform(editorController),
            enabled = sharedNoteId.value == -1
        )

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
                    alertDialog.value = false
                    println("IN ADD COLLAB")
                    scope.launch {
                        println(selectedNoteId)
                        println("IN SCOPE ADDCOLLABO")
                        val res = NoteRequests.addCollaborators(selectedNoteId, emails.value.split(","))
                        println("ADDCOLLAB $res")
                    }
                }) {
                Text("SHARE NOTE", style = MaterialTheme.typography.h6)
            }
        }
    )
}

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
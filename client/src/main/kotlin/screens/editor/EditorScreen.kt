package screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun EditorScreen(
    navController: NavController,
    editorController: EditorController
) {
    var text by rememberSaveable { mutableStateOf("") }
    var isEditingTitle by remember { mutableStateOf(false) }
    var currentTitle by remember { mutableStateOf("Untitled") }
    var edittingTitle by remember { mutableStateOf(currentTitle) }
    val focusRequester = remember { FocusRequester() }
    val emails = remember { mutableStateOf("") }

    LaunchedEffect(isEditingTitle) {
        if (isEditingTitle) {
            focusRequester.requestFocus()
        }
    }

    val alertDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
       TopAppBar(
            backgroundColor = Color.White,
            elevation = 0.dp,
            navigationIcon = {
                IconButton(onClick = { navController.navigateBack() }) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Navigate Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { alertDialog.value = true }) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = Color.Black
                    )
                }
            },
            title = {
                if (!isEditingTitle) {
                    Text(text = currentTitle, style = MaterialTheme.typography.h6)
                    IconButton(
                        onClick = {
                            isEditingTitle = !isEditingTitle
                        }
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
                        textStyle = MaterialTheme.typography.h6,
                        modifier = Modifier.focusRequester(focusRequester)
                    )
                    IconButton(onClick = {
                        currentTitle = edittingTitle
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
            Text("Created by: Leon Fattakhov")
            Text("Created at: Jan 26, 2022 11:34")
            Text("Tags: ")
        }

        if (alertDialog.value) {
            ShareNoteDialog(emails, alertDialog)
        }


        BasicTextField(
            modifier = Modifier.padding(16.dp, 1.dp).fillMaxWidth().fillMaxHeight(),
            value = text,
            onValueChange = { text = it },
            maxLines = Int.MAX_VALUE,
            textStyle = MaterialTheme.typography.body1,
            visualTransformation = MarkdownTransform(editorController)
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


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ShareNoteDialog(
    emails: MutableState<String>,
    alertDialog: MutableState<Boolean>,
) {
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = {},
        shape = MaterialTheme.shapes.large,
        title = {
            Text(text = "Share Note", style = MaterialTheme.typography.h6)
        },
        text = {
            Column {
                Text(
                    "Enter comma-separated emails for collaborators (must be users in Noteaholic)",
                    Modifier.padding(bottom = 8.dp)
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
                Text("CANCEL")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    alertDialog.value = false
                    scope.launch {
                        val res = NoteRequests.addCollaborators(10, emails.value.split(","))
                        println(res)
                    }
                }) {
                Text("SHARE NOTE")
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
package screens.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import controllers.EditorController
import navcontroller.NavController

@Composable
fun EditorScreen(
    navController: NavController,
    editorController: EditorController
) {
    var text by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Note Editor",
            style = MaterialTheme.typography.h6
        )

        BasicTextField(
            value = text,
            onValueChange = { text = it },
            maxLines = Int.MAX_VALUE,
            visualTransformation = ColorsTransformation(editorController)
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

class ColorsTransformation(private val editorController: EditorController) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            buildAnnotatedStringWithColors(text.toString(), editorController),
            offsetMapping = OffsetMapping.Identity
        )
    }
}

fun buildAnnotatedStringWithColors(text: String, editorController: EditorController): AnnotatedString {
    return editorController.parseMarkdown(text)

//    val builder = AnnotatedString.Builder()
//    builder.append(text)
//    // turn text into annotated string
//
//    return builder.toAnnotatedString()
}
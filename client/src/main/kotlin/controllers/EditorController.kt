package controllers

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

class EditorController {
    private val flavour = CommonMarkFlavourDescriptor()

    fun parseMarkdown(src: String): AnnotatedString {
        val parsedTree: ASTNode = MarkdownParser(flavour).buildMarkdownTreeFromString(src)

        fun transformNode(node: ASTNode): AnnotatedString {
            val type = node.type
            val text = src.substring(node.startOffset, node.endOffset)
            println("type: $type, text: $text")
            // switch on type
            return when (type) {
                MarkdownElementTypes.ATX_1 -> {
                    val newText = if (text.startsWith("# ")) text.replace("# ", "") else text
                    AnnotatedString(newText + "\u200b\u200b", SpanStyle(fontSize = 38.sp))
                }
                MarkdownElementTypes.ATX_2 -> {
                    val newText = if (text.startsWith("## ")) text.replace("## ", "") else text
                    AnnotatedString(newText + "\u200b\u200b\u200b", SpanStyle(fontSize = 34.sp))
                }
                MarkdownElementTypes.ATX_3 -> {
                    val newText = if (text.startsWith("### ")) text.replace("### ", "") else text
                    AnnotatedString(newText + "\u200b\u200b\u200b\u200b", SpanStyle(fontSize = 31.sp))
                }
                MarkdownElementTypes.PARAGRAPH -> {
                    AnnotatedString(text)
                }
                MarkdownTokenTypes.EOL -> {
                    AnnotatedString(text)
                }
                MarkdownTokenTypes.WHITE_SPACE -> {
                    AnnotatedString(text)
                }
                else -> {
                    val children = node.children
                    if (children.isNotEmpty()) {
                        buildAnnotatedString { children.forEach { append(transformNode(it)) } }
                    } else {
                        AnnotatedString(text)
                    }
                }
            }
        }

        val res = transformNode(parsedTree)
        println(res)
        return res
    }
}

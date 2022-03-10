package controllers

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.commonmark.CommonMarkFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser


class EditorController {

    private val flavour = CommonMarkFlavourDescriptor()
    private val h1 = SpanStyle(color = Color.LightGray, fontSize = 40.sp)
    private val h2 = SpanStyle(color = Color.LightGray, fontSize = 32.sp)
    private val h3 = SpanStyle(color = Color.LightGray, fontSize = 28.sp)
    private val h4 = SpanStyle(color = Color.LightGray, fontSize = 24.sp)
    private val h5 = SpanStyle(color = Color.LightGray, fontSize = 20.sp)
    private val h6 = SpanStyle(color = Color.LightGray, fontSize = 18.sp)
    private val bold = SpanStyle(color = Color.LightGray, fontWeight = FontWeight.Bold)
    private val italics = SpanStyle(color = Color.LightGray, fontStyle = FontStyle.Italic)
    private val code = SpanStyle(background = Color.LightGray, fontFamily = FontFamily.Monospace, color = Color.Magenta)
    private val body = SpanStyle(fontSize = 16.sp)


    fun parseMarkdown(src: String): AnnotatedString {
        val parsedTree: ASTNode = MarkdownParser(flavour).buildMarkdownTreeFromString(src)

        fun transformNode(node: ASTNode): AnnotatedString {
            val type = node.type
            val text = src.substring(node.startOffset, node.endOffset)

            // HACK: There is a bug where '# ' produces no text ('') which causes an crash
            if (src == "# ") {
                return buildAnnotatedString {
                    append(AnnotatedString("#", h1))
                    append(AnnotatedString(" ", h1.withDefaultColor()))
                }
            }
            println("type: $type, text: '$text'")
            // switch on type
            return when (type) {
                MarkdownElementTypes.ATX_1 -> {

                    buildAnnotatedString {
                        append(AnnotatedString("#", h1))
                        append(AnnotatedString(text.trimStart('#'), h1.withDefaultColor()))
                    }
                }
                MarkdownElementTypes.ATX_2 -> {
                    buildAnnotatedString {
                        append(AnnotatedString("##", h2))
                        append(AnnotatedString(text.trimStart('#'), h2.withDefaultColor()))
                    }
                }
                MarkdownElementTypes.ATX_3 -> {
                    buildAnnotatedString {
                        append(AnnotatedString("###", h3))
                        append(AnnotatedString(text.trimStart('#'), h3.withDefaultColor()))
                    }
                }
                MarkdownElementTypes.ATX_4 -> {
                    buildAnnotatedString {
                        append(AnnotatedString("####", h4))
                        append(AnnotatedString(text.trimStart('#'), h4.withDefaultColor()))
                    }
                }
                MarkdownElementTypes.ATX_5 -> {
                    buildAnnotatedString {
                        append(AnnotatedString("#####", h5))
                        append(AnnotatedString(text.trimStart('#'), h5.withDefaultColor()))
                    }
                }
                MarkdownElementTypes.ATX_6 -> {
                    buildAnnotatedString {
                        append(AnnotatedString("######", h6))
                        append(AnnotatedString(text.trimStart('#'), h6.withDefaultColor()))
                    }
                }
                MarkdownElementTypes.EMPH -> {
                    // *text*
                    if (text.startsWith("*") && text.endsWith("*")) {
                        buildAnnotatedString {
                            append(AnnotatedString("*", italics))
                            append(AnnotatedString(text.trim('*'), italics.withDefaultColor()))
                            append(AnnotatedString("*", italics))
                        }
                    }
                    // _text_
                    else {
                        buildAnnotatedString {
                            append(AnnotatedString("_", italics))
                            append(AnnotatedString(text.trim('_'), italics.withDefaultColor()))
                            append(AnnotatedString("_", italics))
                        }
                    }
                }
                MarkdownElementTypes.STRONG -> {
                    // **text**
                    if (text.startsWith("**") && text.endsWith("**")) {
                        buildAnnotatedString {
                            append(AnnotatedString("**", bold))
                            append(AnnotatedString(text.trim('*'), bold.withDefaultColor()))
                            append(AnnotatedString("**", bold))
                        }
                    }
                    // __text__
                    else {
                        buildAnnotatedString {
                            append(AnnotatedString("__", bold))
                            append(AnnotatedString(text.trim('_'), bold.withDefaultColor()))
                            append(AnnotatedString("__", bold))
                        }
                    }
                }
                MarkdownTokenTypes.LIST_BULLET -> {
                    buildAnnotatedString {
                        append(AnnotatedString("•", bold))
                        append(AnnotatedString(text.trimStart('*').trimStart('-')))
                    }
                }

                // TODO: Make this look nicer
                MarkdownElementTypes.CODE_SPAN, MarkdownElementTypes.CODE_FENCE -> {
                    AnnotatedString(text, code)
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

private fun SpanStyle.withDefaultColor(): SpanStyle {
    return this.copy(color = Color.Black)
}

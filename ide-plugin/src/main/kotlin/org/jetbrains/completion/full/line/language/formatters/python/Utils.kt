package org.jetbrains.completion.full.line.language.formatters.python

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.parents
import com.jetbrains.python.psi.*

fun isNewLine(text: String): Boolean {
    return text.matches(Regex("\\n\\s*"))
}

fun spacesToOne(text: String): String {
    return text.replace(Regex(" +"), " ")
}

fun formatFloatOrInt(text: String): String {
    if (!text.contains('.')) {
        return formatInt(text)
    }

    val parts = text.split(".")
    val before = formatInt(parts[0]).reversed()
    val after = formatInt(parts[1])

    return "$before.$after"
}

fun formatInt(text: String): String {
    val s = text.replace("_", "")
    if (s.length <= 5) {
        return text
    }

    return StringBuilder().apply {
        var uCount = 0
        for (c in s.reversed()) {
            if ((length - uCount) % 3 == 0 && length != 0) {

                append('_')
                uCount++
            }
            append(c)
        }
    }.toString()
}

fun handlePyArgumentList2(arguments: Array<PyExpression>): String {
    val a = StringBuilder("(")
    for (argument in arguments) {
        val sb = StringBuilder()
        SyntaxTraverser.psiTraverser()
                .withRoot(argument)
                .onRange(argument.textRange)
                .filter { it is LeafPsiElement && it.parent !is PyStringElement }
                .forEach {
                    if (it is PyStringElement && it.prevSibling is PsiWhiteSpace && it.prevSibling.prevSibling is PyStringElement) {
                        sb.delete(sb.length - it.prevSibling!!.text.replace("    ", "\t").length, sb.length).append(" ")
                    }
                    when {
                        it is PyStringElement && !it.textContains('"') -> {
                            sb.append(it.prefix + "\"" + it.content + "\"")
                        }
                        it is PsiWhiteSpace && isNewLine(it.text) -> {
                            sb.append(it.text.replace("    ", "\t"))
                        }
                        it is PsiWhiteSpace && !isNewLine(it.text) -> {
                            sb.append(' ')
                        }
                        else -> {
                            sb.append(it.text)
                        }
                    }
                }
        a.append(sb).append(", ")
    }
    a.delete(a.length - 2, a.length).append(")")

    return a.toString()
}

fun handlePyArgumentList(arguments: Array<PyExpression>): String {
//    val code = StringBuilder("(")
//    val formatter = CodeFormatter.getInstance(Language.findLanguageByID("Python")!!)!!
//    for (argument in arguments) {
//        val formatted = formatter.format(argument, argument.textRange) { it, argumentCode ->
//            if (it is PyStringElement && it.prevSibling is PsiWhiteSpace && it.prevSibling.prevSibling is PyStringElement) {
//                argumentCode.delete(argumentCode.length - it.prevSibling!!.text.replace("    ", "\t").length, argumentCode.length).append(" ")
//            }
//        }
//        code.append(formatted).append(", ")
//    }
//    code.delete(code.length - 2, code.length).append(")")
    return handlePyArgumentList2(arguments)
}

fun fixEmptyLines(it: PsiWhiteSpace): String {
    val newLines = it.text.count { it == '\n' }
    var spaces = 0
    it.text.reversed().forEach {
        if (it == ' ') {
            spaces++
        } else {
            return@forEach
        }
    }

    val code = if (it.parents.filterIsInstance<PyClass>().toList().isEmpty()) {
        when (it.prevSibling) {
            is PyStringLiteralExpression -> {
                if ((it.prevSibling as PyStringLiteralExpression).isDocString) {
                    "\n\n\n"
                } else {
                    "\n".repeat(newLines)
                }
            }
            is PyClass -> {
                "\n\n\n"
            }
            is PyFunction -> {
                "\n\n\n"
            }
            is PsiComment -> {
                if (it.nextSibling is PyFunction) {
                    if (newLines > 1) {
                        "\n\n\n"
                    } else {
                        "\n"
                    }
                } else {
                    if (newLines > 1) {
                        "\n\n"
                    } else {
                        "\n"
                    }
                }
            }
            else -> {
                "\n".repeat(newLines)
            }
        }
    } else {

        when (it.prevSibling) {
            is PyClass -> {
                "\n\n"
            }
            is PyFunction -> {
                "\n\n"
            }
            is PsiComment -> {
                if (newLines > 1) {
                    "\n\n\n"
                } else {
                    "\n"
                }
            }
            is PyAssignmentStatement -> {
                "\n"
            }
            else -> {
                "\n".repeat(newLines)
            }
        }
    } + "\t".repeat(spaces / 4)

    return code
}

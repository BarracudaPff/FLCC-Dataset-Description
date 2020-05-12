package org.jetbrains.completion.full.line.language.formatters.python

import com.intellij.psi.PsiWhiteSpace
import com.jetbrains.python.psi.PyExpression

fun isNewLine(text: String): Boolean {
    return text.matches(Regex("\\n\\s*"))
}

fun spacesToOne(text: String): String {
    return text.replace(Regex(" +"), " ")
}

fun formatFloatOrInt(text: String, allowUnderscores: Boolean): String {
    if (!text.contains('.')) {
        return formatInt(text, allowUnderscores)
    }

    val parts = text.split(".")
    val before = formatInt(parts[0], allowUnderscores)
    val after = if (parts[1].isEmpty()) {
        "0"
    } else {
        formatInt(parts[1], allowUnderscores)
    }

    return "$before.$after"
}

fun formatInt(text: String, allowUnderscores: Boolean): String {
    if (!allowUnderscores) {
        return text
    }

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

fun handlePyArgumentList(arguments: Array<PyExpression>, lastComma: Boolean = false, closable: Boolean = true): String {
    val text = StringBuilder("(")
    arguments.forEach {
        text.append(it.text).append(", ")
    }
    if (!lastComma) {
        text.delete(text.length - 2, text.length)
    } else {
        text.delete(text.length - 1, text.length)
    }
    if (closable) {
        text.append(")")
    }
    return text.toString()
}

fun fixEmptyLines(element: PsiWhiteSpace): String {
    return "\n" + fixTabs(element.text)
}

fun fixTabs(text: String): String {
    var spaces = 0
    for (ch in text.reversed()) {
        if (ch == ' ') {
            spaces++
        } else {
            break
        }
    }

    return "\t".repeat(spaces / 4)
}

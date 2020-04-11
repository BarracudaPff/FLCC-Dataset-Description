package org.jetbrains.completion.full.line.language

import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.refactoring.suggested.startOffset
import com.jetbrains.python.PythonLanguage
import org.jetbrains.completion.full.line.language.formatters.PythonCodeFormatter

abstract class CodeFormatter(private val elementFormatters: Array<ElementFormatter>) {
    private val filters = elementFormatters.map { it::filter }

    fun format(element: PsiElement, range: TextRange): String {
        val code = StringBuilder()

        var skipUntil = 0
        SyntaxTraverser.psiTraverser()
                .withRoot(element)
                .onRange(range)
                .filter { el -> filters.mapNotNull { it(el) }.any { it } }
                .forEach lit@{
                    if (it.textOffset < skipUntil) {
                        return@lit
                    }

                    if (it.startOffset + it.text.length >= range.endOffset) {
                        val text = formatFinalElement(it, range)
                        code.append(text)
                        return code.toString()
                    }

                    var sk = false

                    elementFormatters.forEach { elementFormatter ->
                        if (!sk && elementFormatter.condition(it)) {
                            code.append(elementFormatter.format(it))
                            sk = true
                        }
                    }

                    skipUntil = it.textRange.endOffset
                }
        return code.toString()
    }

    abstract fun formatFinalElement(element: PsiElement, range: TextRange): String

    companion object {
        fun getInstance(language: Language): CodeFormatter? {
            return when (language) {
                PythonLanguage.getInstance() -> PythonCodeFormatter()
                else -> null
            }
        }
    }
}

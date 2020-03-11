package org.jetbrains.completion.full.line.language

import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.refactoring.suggested.startOffset
import org.jetbrains.completion.full.line.language.formatters.PythonCodeFormatter

abstract class CodeFormatter(
        private val elementFormatters: Array<ElementFormatter>
) {
    private val filters = elementFormatters.map { it::filter }

    fun format(element: PsiElement, range: TextRange, extra: ((PsiElement, StringBuilder) -> Unit)? = null): String {
        val code = StringBuilder()
        var skip = false

        var skipUntil = 0
        SyntaxTraverser.psiTraverser()
                .withRoot(element)
                .onRange(range)
                .filter { el -> filters.mapNotNull { it(el) }.any { it } }
                .forEach lit@{
                    if (it.textOffset < skipUntil || skip) {
                        return@lit
                    }

                    if (it.startOffset + it.text.length >= range.endOffset) {
                        code.append(it.text.slice(IntRange(0, (range.endOffset - it.startOffset-1))))
                        skip = true
                        return@lit
                    }

                    extra?.let { f -> f(it, code) }

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

    companion object {
        fun getInstance(language: Language): CodeFormatter? {
            return when (language) {
                Language.findLanguageByID("Python") -> PythonCodeFormatter()
                else -> null
            }
        }
    }
}

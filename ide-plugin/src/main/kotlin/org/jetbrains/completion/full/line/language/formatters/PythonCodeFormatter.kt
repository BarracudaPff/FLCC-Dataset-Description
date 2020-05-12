package org.jetbrains.completion.full.line.language.formatters

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.refactoring.suggested.startOffset
import org.jetbrains.completion.full.line.language.CodeFormatter
import org.jetbrains.completion.full.line.language.formatters.python.*

class PythonCodeFormatter : CodeFormatter(arrayOf(
        WhitespaceFormatter(),
        CommentFormatter(),
        NumericalFormatter(false),
        ImportFormatter(),
        ParenthesizedWithoutTuplesFormatter(),
        StringFormatter(),
        ListLiteralFormatter(),
        ArgumentListFormatter(),
        SequenceFormatter(),
        ParameterListFormatter(),
        DumpFormatter()
)) {
    override fun formatFinalElement(element: PsiElement, range: TextRange): String {
        val text = element.text.slice(IntRange(0, (range.endOffset - element.startOffset - 1)))
        return text + fixTabs(text)
    }
}

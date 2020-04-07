package org.jetbrains.completion.full.line.language.formatters.python

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import org.jetbrains.completion.full.line.language.ElementFormatter

class WhitespaceFormatter : ElementFormatter {
    private var skip = false

    override fun condition(element: PsiElement): Boolean = element is PsiWhiteSpace || skip

    override fun filter(element: PsiElement): Boolean? = null

    override fun format(element: PsiElement): String {
        if (skip) {
            skip = false
            return ""
        }

        if (element.prevSibling is PsiComment) {
            return ""
        }

        return if (isNewLine(element.text)) {
            fixEmptyLines(element as PsiWhiteSpace)
        } else {
            if ("\\" in element.text) {
                skip = true
            }
            " "
        }
    }
}

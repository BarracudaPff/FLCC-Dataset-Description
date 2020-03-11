package org.jetbrains.completion.full.line.language.formatters.python

import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyFromImportStatement
import org.jetbrains.completion.full.line.language.ElementFormatter

class ImportFormatter : ElementFormatter {
    override fun condition(element: PsiElement): Boolean = element is PyFromImportStatement

    override fun filter(element: PsiElement): Boolean? = element is PyFromImportStatement

    override fun format(element: PsiElement): String {
        element as PyFromImportStatement
        return if (element.rightParen != null) {
            element.text.replace(Regex("[()\n\\\\]"), "")
        } else {
            element.text
        }
    }
}

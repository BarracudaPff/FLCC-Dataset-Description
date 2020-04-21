package org.jetbrains.completion.full.line.language.formatters.python

import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyArgumentList
import com.jetbrains.python.psi.PyClass
import org.jetbrains.completion.full.line.language.ElementFormatter

class ArgumentListFormatter : ElementFormatter {
    override fun condition(element: PsiElement): Boolean = element is PyArgumentList

    override fun filter(element: PsiElement): Boolean? = element is PyArgumentList

    override fun format(element: PsiElement): String {
        element as PyArgumentList

        return if (element.arguments.isEmpty()) {
            if (element.parent is PyClass) {
                ""
            } else {
                element.text
            }
        } else {
            handlePyArgumentList(element.arguments, element.text.last() == ',', element.closingParen != null)
        }

    }
}

package org.jetbrains.completion.full.line.language.formatters.python

import com.intellij.psi.PsiElement
import com.jetbrains.python.psi.PyParameterList
import org.jetbrains.completion.full.line.language.ElementFormatter

class ParameterListFormatter : ElementFormatter {
    override fun condition(element: PsiElement): Boolean = element is PyParameterList

    override fun filter(element: PsiElement): Boolean? = element is PyParameterList

    override fun format(element: PsiElement): String {
        element as PyParameterList

        return element.getPresentableText(true)
    }
}

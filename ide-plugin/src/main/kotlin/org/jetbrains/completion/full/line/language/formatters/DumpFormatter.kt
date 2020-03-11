package org.jetbrains.completion.full.line.language.formatters

import com.intellij.psi.PsiElement
import org.jetbrains.completion.full.line.language.ElementFormatter

class DumpFormatter : ElementFormatter {
    override fun condition(element: PsiElement): Boolean = true

    override fun filter(element: PsiElement): Boolean? = null

    override fun format(element: PsiElement): String = element.text

}

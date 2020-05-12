package org.jetbrains.completion.full.line.language.formatters.python

import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import org.jetbrains.completion.full.line.language.ElementFormatter

class CommentFormatter : ElementFormatter {
    override fun condition(element: PsiElement): Boolean = element is PsiComment

    override fun filter(element: PsiElement): Boolean? = element is PsiComment

    override fun format(element: PsiElement): String = ""
}

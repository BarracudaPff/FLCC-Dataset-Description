package org.jetbrains.completion.full.line.language.formatters.python

import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.jetbrains.python.psi.PyStringElement
import org.jetbrains.completion.full.line.language.ElementFormatter

class StringFormatter : ElementFormatter {
    override fun condition(element: PsiElement): Boolean = element is PyStringElement

    override fun filter(element: PsiElement): Boolean? {
        return element is LeafPsiElement && element.parent !is PyStringElement || element is PyStringElement
    }

    override fun format(element: PsiElement): String {
        element as PyStringElement
        return if (element.isTripleQuoted) {
            element.text
        } else {
            element.prefix + "\"" + element.content + "\""
        }
    }

}

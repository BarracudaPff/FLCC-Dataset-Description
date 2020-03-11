package org.jetbrains.completion.full.line.language

import com.intellij.lang.Language
import com.intellij.psi.PsiElement
import org.jetbrains.completion.full.line.language.supporters.PythonSupporter

interface ElementFormatter {
    fun condition(element: PsiElement): Boolean

    fun filter(element: PsiElement): Boolean?

    fun format(element: PsiElement): String

    companion object {
        fun getInstance(language: Language): LanguageMLSupporter? {
            return when (language) {
                Language.findLanguageByID("Python") -> PythonSupporter()
                else -> null
            }
        }
    }
}

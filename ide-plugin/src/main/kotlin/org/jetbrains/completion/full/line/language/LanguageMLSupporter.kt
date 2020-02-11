package org.jetbrains.completion.full.line.language

import com.intellij.codeInsight.template.Template
import com.intellij.lang.Language
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement

interface LanguageMLSupporter {
    fun isComment(element: PsiElement): Boolean = element is PsiComment

    fun autoImportFix(element: PsiElement, from: Int, to: Int)

    fun getFirstToken(line: String): String?

    fun createStringTemplate(element: PsiElement, from: Int, to: Int): Template?

    fun getMissingBraces(line: String): List<Char>?

    companion object {
        fun getInstance(language: Language): LanguageMLSupporter? {
            return when (language) {
                Language.findLanguageByID("Python") -> PythonSupporter()
                else -> null
            }
        }
    }
}

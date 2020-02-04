package org.jetbrains.completion.full.line.language

import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.lang.Language
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile

interface LanguageMLSupporter {
    fun isComment(element: PsiElement): Boolean = element is PsiComment

    fun getFirstToken(line: String): String?

    fun getLastToken(line: String): String?

//    fun getFirstToken(file: PsiElement, offset: Int): String
//
//    fun getLastToken(file: PsiElement, offset: Int): String

    fun createStringTemplate(element: PsiElement, from: Int, to:Int): TemplateImpl?

    fun createStringTemplate(variant: String): TemplateImpl?

    fun getMissingBraces(line: String): List<Char>?

    fun getPrefix(line: String): String {
        return getLastToken(line) ?: ""
    }

    companion object {
        fun getInstance(language: Language): LanguageMLSupporter? {
            return when (language) {
                Language.findLanguageByID("Python") -> PythonSupporter()
                else -> null
            }
        }
    }
}

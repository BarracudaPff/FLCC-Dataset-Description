package org.jetbrains.completion.full.line.language

import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.lang.Language

interface LanguageMLSupporter {
    fun getFirstToken(line: String): String?

    fun getLastToken(line: String): String?

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

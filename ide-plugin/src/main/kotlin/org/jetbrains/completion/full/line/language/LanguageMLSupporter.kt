package org.jetbrains.completion.full.line.language

import com.intellij.lang.Language

interface LanguageMLSupporter {
    fun check(variant: String): Boolean

    fun getFirstToken(line: String): String?

    fun getLastToken(line: String): String?

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

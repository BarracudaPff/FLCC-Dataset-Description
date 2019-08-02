package org.jetbrains.completion.full.line

interface FullLineCompletionProvider {
    fun getVariants(context: String): List<String>
    fun description(): String
}
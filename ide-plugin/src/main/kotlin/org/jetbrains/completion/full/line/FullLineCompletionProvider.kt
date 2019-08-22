package org.jetbrains.completion.full.line

interface FullLineCompletionProvider {
    fun getVariants(context: String, filename: String): List<String>
    val description: String
}

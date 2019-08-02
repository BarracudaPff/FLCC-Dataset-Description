package org.jetbrains.completion.full.line

class DummyFullLineCompletionProvider : FullLineCompletionProvider {
    override fun description(): String = "dummy"

    override fun getVariants(context: String): List<String> = listOf("This is unbelievable!")
}
package org.jetbrains.completion.full.line.models

data class FullLineCompletionRequest(
    val code: String,
    val token: String,
    val offset: Int,
    val filename: String
)

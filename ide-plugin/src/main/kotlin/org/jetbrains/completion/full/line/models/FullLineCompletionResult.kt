package org.jetbrains.completion.full.line.models

data class FullLineCompletionResult(val completions: List<FullLineCompletion>)

data class FullLineCompletion(
        val score: Double,
        val suggestion: String
)

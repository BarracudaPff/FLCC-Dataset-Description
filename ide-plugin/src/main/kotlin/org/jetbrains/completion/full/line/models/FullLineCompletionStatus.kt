package org.jetbrains.completion.full.line.models

import com.google.gson.annotations.SerializedName

data class FullLineCompletionStatus(
        val status: String,

        @Suppress("ArrayInDataClass")
        @SerializedName("models_GPT")
        val models: Array<String>
)

package org.jetbrains.completion.full.line.models

import com.google.gson.annotations.SerializedName
import org.jetbrains.completion.full.line.settings.MLServerCompletionSettings

data class FullLineCompletionRequest(
        val code: String,
        val token: String,
        val offset: Int,
        val filename: String,
        val branches: Int,

        val tokens: Int,
        val mode: FullLineCompletionMode,
        val generator: FullLineCompletionAlgorithm,

        @SerializedName("beam_size")
        val beamSize: Int,

        @SerializedName("diversity_strength")
        val diversityStrength: Double,

        @SerializedName("top_n")
        val topN: Int,
        @SerializedName("use_top_n")
        val useTopN: Boolean

) {
    constructor(code: String, prefix: String, offset: Int, filename: String, settings: MLServerCompletionSettings)
            : this(code, prefix, offset, filename, settings.suggestions, settings.tokens, settings.mode,
            settings.algorithm, settings.beamSize, settings.diversityStrength, settings.topN, settings.useTopN)
}

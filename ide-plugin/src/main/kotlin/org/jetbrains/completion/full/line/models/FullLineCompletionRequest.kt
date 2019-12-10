package org.jetbrains.completion.full.line.models

import com.google.gson.annotations.SerializedName
import org.jetbrains.completion.full.line.settings.MLServerCompletionSettings

data class FullLineCompletionRequest(
        val code: String,
        val token: String,
        val offset: Int,
        val filename: String,
        @SerializedName("branches")
        val suggestions: Int,
        val tokens: Int,

        val mode: FullLineCompletionMode,
        @SerializedName("generator")
        val algorithm: FullLineCompletionAlgorithm,

        @SerializedName("num_iterations")
        val numIterations: Int,
        @SerializedName("beam_size")
        val beamSize: Int,
        @SerializedName("diversity_groups")
        val diversityGroups: Int,
        @SerializedName("diversity_strength")
        val diversityStrength: Double,

        @SerializedName("use_top_n")
        val useTopN: Boolean,
        @SerializedName("top_n")
        val topN: Int,
        @SerializedName("only_full_lines")
        val onlyFullLines: Boolean
) {
    constructor(code: String, prefix: String, offset: Int, filename: String, settings: MLServerCompletionSettings) : this(
            code,
            prefix,
            offset,
            filename,
            suggestions = settings.suggestions,
            tokens = settings.tokens,
            mode = settings.mode,
            algorithm = settings.algorithm,
            numIterations = settings.numIterations,
            beamSize = settings.beamSize,
            diversityGroups = settings.diversityGroups,
            diversityStrength = settings.diversityStrength,
            useTopN = settings.useTopN,
            topN = settings.topN,
            onlyFullLines = settings.onlyFullLines
    )
}


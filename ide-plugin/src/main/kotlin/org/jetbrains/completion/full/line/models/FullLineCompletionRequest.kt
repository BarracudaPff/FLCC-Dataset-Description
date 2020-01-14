package org.jetbrains.completion.full.line.models

import com.google.gson.annotations.SerializedName
import org.jetbrains.completion.full.line.settings.MLServerCompletionSettings

data class FullLineCompletionRequest(
        val code: String,
        val token: String,
        val offset: Int,
        val filename: String,

        val mode: FullLineCompletionMode,

        @SerializedName("num_iterations")
        val numIterations: Int,
        @SerializedName("beam_size")
        val beamSize: Int,
        @SerializedName("diversity_groups")
        val diversityGroups: Int,
        @SerializedName("diversity_strength")
        val diversityStrength: Double,

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
            settings.state.mode,
            settings.state.numIterations,
            settings.state.beamSize,
            settings.state.diversityGroups,
            settings.state.diversityStrength,
            settings.state.topN,
            settings.state.onlyFullLines
    )
}


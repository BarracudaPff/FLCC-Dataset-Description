package org.jetbrains.completion.full.line.models

import com.google.gson.annotations.SerializedName

data class Model(
        val lifetime: Int?,
        val name: String,
        @SerializedName("start_at")
        val startAt: String?,
        @SerializedName("time_left")
        val timeLeft: Int?,
        val type: ModelType
)

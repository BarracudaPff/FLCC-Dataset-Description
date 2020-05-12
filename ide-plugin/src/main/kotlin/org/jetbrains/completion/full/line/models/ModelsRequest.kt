package org.jetbrains.completion.full.line.models

import com.google.gson.annotations.SerializedName

data class ModelsRequest(
        @SerializedName("best_model")
        val bestModel: Model,
        val models: Array<Model>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModelsRequest

        if (bestModel != other.bestModel) return false
        if (!models.contentEquals(other.models)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bestModel.hashCode()
        result = 31 * result + models.contentHashCode()
        return result
    }
}

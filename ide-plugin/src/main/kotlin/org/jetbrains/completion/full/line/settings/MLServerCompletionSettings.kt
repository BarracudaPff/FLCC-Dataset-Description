package org.jetbrains.completion.full.line.settings

import com.intellij.openapi.components.*
import org.jetbrains.completion.full.line.models.FullLineCompletionMode

@State(name = "MLServerCompletionSettings", storages = [Storage("MLServerCompletionSettings.xml")])
class MLServerCompletionSettings : PersistentStateComponent<MLServerCompletionSettings.State> {
    private var state = State()

    fun isEnabled() = state.enable

    /**
     * Checks if template-like walking on string enabled.
     * It works just like Live Templates in IDEA
     */
    fun enableStringsWalking() = state.stringsWalking

    /**
     * Checks if completion can be called during auto popup
     */
    fun isAutoPopup() = state.autoPopup

    override fun getState(): State {
        val isTesting = System.getenv("flcc_evaluating")
        if (isTesting != null && isTesting.toBoolean()) {
            state.model             = System.getenv("flcc_model")                       ?: state.model
            state.numIterations     = System.getenv("flcc_numIterations")?.toInt()      ?: state.numIterations
            state.beamSize          = System.getenv("flcc_beamSize")?.toInt()           ?: state.beamSize
            state.diversityGroups   = System.getenv("flcc_diversityGroups")?.toInt()    ?: state.diversityGroups
            state.onlyFullLines     = System.getenv("flcc_onlyFullLines")?.toBoolean()  ?: state.onlyFullLines
        }
        return state
    }

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): MLServerCompletionSettings {
            return service()
        }
    }

    data class State(
            // General Server ML Completion
            var enable: Boolean = true,
            var autoPopup: Boolean = true,
            var stringsWalking: Boolean = true,
            var onlyFullLines: Boolean = true,

            var mode: FullLineCompletionMode = FullLineCompletionMode.FULL_LINE,
            // Beam search configuration
            var numIterations: Int = 10,
            var beamSize: Int = 6,
            var diversityGroups: Int = 5,
            var diversityStrength: Double = 0.3,
            var topN: Int = 5,
            var groupTopN: Int = 5,

            //Additional setting for correct usage
            var useTopN: Boolean = false,
            var useGroupTopN: Boolean = false,
            var model: String = "best",
            var groupAnswers: Boolean = false
    )
}

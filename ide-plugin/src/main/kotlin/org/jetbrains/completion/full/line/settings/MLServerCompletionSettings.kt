package org.jetbrains.completion.full.line.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.jetbrains.completion.full.line.models.FullLineCompletionMode

@State(name = "MLServerCompletionSettings", storages = [Storage("MLServerCompletionSettings.xml")])
class MLServerCompletionSettings : PersistentStateComponent<MLServerCompletionSettings.State> {
    private var state = State()

    fun isEnabled() = state.enable

    override fun getState(): State {
        return state
    }

    override fun loadState(state: State) {
        this.state = state
    }

    companion object {
        fun getInstance(): MLServerCompletionSettings {
            return ServiceManager.getService(MLServerCompletionSettings::class.java)
        }
    }

    data class State(
            // General Server ML Completion
            var enable: Boolean = true,
            var autoPopup: Boolean = true,
            var mode: FullLineCompletionMode = FullLineCompletionMode.FULL_LINE,
            // Beam search configuration
            var numIterations: Int = 10,
            var beamSize: Int = 6,
            var diversityGroups: Int = 5,
            var diversityStrength: Double = 0.3,
            var topN: Int = 5,
            var groupTopN: Int = 5
    )
}

package org.jetbrains.completion.full.line.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.jetbrains.completion.full.line.models.FullLineCompletionMode

@State(name = "MLServerCompletionSettings", storages = [Storage("MLServerCompletionSettings.xml")])
class MLServerCompletionSettings : PersistentStateComponent<MLServerCompletionSettings.State> {
    private val state = State()

    override fun getState(): State {
        return state
    }

    override fun loadState(state: State) {
        this.state.enable = state.enable
        this.state.autoPopup = state.autoPopup
        this.state.mode = state.mode

        this.state.numIterations = state.numIterations
        this.state.beamSize = state.beamSize
        this.state.diversityGroups = state.diversityGroups
        this.state.diversityStrength = state.diversityStrength
        this.state.topN = state.topN
        this.state.groupTopN = state.groupTopN
    }

    companion object {
        fun getInstance(): MLServerCompletionSettings {
            return ServiceManager.getService(MLServerCompletionSettings::class.java)
        }
    }

    class State {
        // General Server ML Completion
        var enable = true
        var autoPopup = true
        var mode = FullLineCompletionMode.FULL_LINE
        // Beam search configuration
        var numIterations = 10
        var beamSize = 6
        var diversityGroups = 5
        var diversityStrength = 0.3
        var topN = 5
        var groupTopN = 5
    }
}

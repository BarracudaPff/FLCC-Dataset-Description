package org.jetbrains.completion.full.line.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import org.jetbrains.completion.full.line.models.FullLineCompletionAlgorithm
import org.jetbrains.completion.full.line.models.FullLineCompletionMode

@State(name = "MLServerCompletionSettings", storages = [Storage("MLServerCompletionSettings.xml")])
class MLServerCompletionSettings : PersistentStateComponent<MLServerCompletionSettings.State> {
    private val state = State()

    override fun getState(): State {
        return state
    }

    override fun loadState(state: State) {
        this.enable = state.enable
        this.mode = state.mode
        this.algorithm = state.algorithm

        this.numIterations = state.numIterations
        this.beamSize = state.beamSize
        this.diversityGroups = state.diversityGroups
        this.diversityStrength = state.diversityStrength
        this.useTopN = state.useTopN
        this.topN = state.topN

        this.tokens = state.tokens
        this.suggestions = state.suggestions

        this.autoPopup = state.autoPopup
        this.filter = state.filter
        this.experimental = state.experimental
        this.enableTemplateWalking = state.enableTemplateWalking
        this.enableComments = state.enableComments
    }

    var tokens: Int
        get() = state.tokens
        set(value) {
            state.tokens = value
        }

    var diversityStrength: Double
        get() = state.diversityStrength
        set(value) {
            state.diversityStrength = value
        }
    var beamSize: Int
        get() = state.beamSize
        set(value) {
            state.beamSize = value
        }
    var suggestions: Int
        get() = state.suggestions
        set(value) {
            state.suggestions = value
        }
    var algorithm: FullLineCompletionAlgorithm
        get() = state.algorithm
        set(value) {
            state.algorithm = value
        }
    var mode: FullLineCompletionMode
        get() = state.mode
        set(value) {
            state.mode = value
        }
    var autoPopup: Boolean
        get() = state.autoPopup
        set(value) {
            state.autoPopup = value
        }
    var enable: Boolean
        get() = state.enable
        set(value) {
            state.enable = value
        }
    var filter: Boolean
        get() = state.filter
        set(value) {
            state.filter = value
        }
    var numIterations: Int
        get() = state.numIterations
        set(value) {
            state.numIterations = value
        }
    var diversityGroups: Int
        get() = state.diversityGroups
        set(value) {
            state.diversityGroups = value
        }
    var topN: Int
        get() = state.topN
        set(value) {
            state.topN = value
        }
    var useTopN: Boolean
        get() = state.useTopN
        set(value) {
            state.useTopN = value
        }
    var experimental: Boolean
        get() = state.experimental
        set(value) {
            state.experimental = value
        }
    var enableTemplateWalking: Boolean
        get() = state.enableTemplateWalking
        set(value) {
            state.enableTemplateWalking = value
        }
    var enableComments: Boolean
        get() = state.enableComments
        set(value) {
            state.enableComments = value
        }

    companion object {
        fun getInstance(): MLServerCompletionSettings {
            return ServiceManager.getService(MLServerCompletionSettings::class.java)
        }
    }


    class State {
        var enable = true
        var autoPopup = true
        var filter = true
        var useTopN = false
        var mode = FullLineCompletionMode.FULL_LINE
        var algorithm = FullLineCompletionAlgorithm.DEFAULT
        var numIterations = 50
        var diversityGroups = 5
        var tokens = 10
        var suggestions = 5
        var beamSize = 3
        var topN = 5
        var diversityStrength = 0.3

        var experimental = false
        var enableTemplateWalking = true
        var enableComments = true
    }
}

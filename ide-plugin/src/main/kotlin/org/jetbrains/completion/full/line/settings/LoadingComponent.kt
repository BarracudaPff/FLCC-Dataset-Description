package org.jetbrains.completion.full.line.settings

import com.intellij.ui.JBColor
import com.intellij.util.ui.AsyncProcessIcon
import javax.swing.JLabel

class LoadingComponent {
    val loadingIcon = AsyncProcessIcon("")
    val statusText = JLabel("")

    private val red = JBColor.RED
    private val green = JBColor(JBColor.GREEN.darker(), JBColor.GREEN.brighter())

    init {
        changeState(State.PROCESSED)
    }

    fun changeState(state: State, msg: String? = null) {
        when (state) {
            State.PROCESSED -> setProcessed(msg)
            State.LOADING -> setLoading(msg)
            State.SUCCESS -> setSuccess(msg)
            State.ERROR -> setError(msg)
        }
    }

    private fun setLoading(msg: String?) {
        loadingIcon.resume()
        loadingIcon.isVisible = true
        statusText.isVisible = false
        statusText.text = msg
    }

    private fun setSuccess(msg: String?) {
        statusText.text = msg
        statusText.foreground = green
    }

    private fun setError(msg: String?) {
        statusText.text = msg
        statusText.foreground = red
    }

    private fun setProcessed(msg: String?) {
        if (msg != null) {
            statusText.text = msg
        }

        loadingIcon.suspend()
        loadingIcon.isVisible = false
        statusText.isVisible = statusText.text.isNotEmpty()
    }

    enum class State {
        LOADING, SUCCESS, ERROR, PROCESSED
    }
}


package org.jetbrains.completion.full.line

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.util.messages.MessageBusConnection

class CompletionTabListener : Disposable, AnActionListener {
    var nextLevel = false
    private var busConnection: MessageBusConnection = ApplicationManager
            .getApplication()
            .messageBus
            .connect(this)

    init {
        busConnection.subscribe(AnActionListener.TOPIC, this)
    }

    override fun afterActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent) {
        if (action.templateText == ID_TO_CATCH) {
            if (nextLevel) {
                nextLevel = false
                ActionManager.getInstance()
                        .getAction(NEXT_LEVEL_ID)
                        .actionPerformed(event)
            }
        }
    }

    override fun dispose() {
        busConnection.disconnect()
        busConnection.dispose()
    }

    companion object {
        const val ID_TO_CATCH = "Choose Lookup Item Replace"
        const val NEXT_LEVEL_ID = "org.jetbrains.completion.full.line.actions.NextLevelCompletionAction"
    }
}

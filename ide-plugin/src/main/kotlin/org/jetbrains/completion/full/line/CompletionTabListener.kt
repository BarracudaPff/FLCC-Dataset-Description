package org.jetbrains.completion.full.line

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.ex.AnActionListener
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.ProjectManager


class CompletionTabListener : Disposable, AnActionListener {
    var nextLevel = false
    private val idToCatch = "Choose Lookup Item Replace"

    init {
        //val shortcut = KeyboardShortcut(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, KeyEvent.ALT_DOWN_MASK), null)
        //KeymapManager.getInstance().activeKeymap.addShortcut("Id here", shortcut)
        val busConnection = ApplicationManager.getApplication().messageBus.connect(this);
        busConnection.subscribe(AnActionListener.TOPIC, this);
    }

    override fun afterActionPerformed(action: AnAction, dataContext: DataContext, event: AnActionEvent) {
        if (action.templateText == idToCatch) {
            if (nextLevel) {
                doNextLevel()
            }
        }
    }

    private fun doNextLevel() {
        val project = ProjectManager.getInstance().openProjects[0]
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        CodeCompletionHandlerBase(CompletionType.BASIC, false, false, true)
            .invokeCompletion(project, editor)
        nextLevel = false
    }

    override fun dispose() {
    }
}

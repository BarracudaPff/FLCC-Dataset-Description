package org.jetbrains.completion.full.line.actions

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager

class NextLevelCompletionAction : AnAction() {
    private companion object {
        val LOG = Logger.getInstance(NextLevelCompletionAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        if (project != null) {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            if (editor != null) {
                ApplicationManager.getApplication().runReadAction {
                    CodeCompletionHandlerBase(CompletionType.BASIC, false, false, true)
                            .invokeCompletion(project, editor)
                }
            } else {
                LOG.error("Calling next completion without editor.")
            }
        } else {
            LOG.error("Calling next completion without project.")
        }
    }
}

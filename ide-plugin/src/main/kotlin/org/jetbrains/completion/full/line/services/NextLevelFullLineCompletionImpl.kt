package org.jetbrains.completion.full.line.services

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

class NextLevelFullLineCompletionImpl : NextLevelFullLineCompletion {
    override fun restartCompletion(project: Project, editor: Editor, line: String) {
        firstLine = line

        ApplicationManager.getApplication().invokeLater({
            CodeCompletionHandlerBase(CompletionType.BASIC, true, false, true)
                    .invokeCompletion(project, editor)
        }, ModalityState.defaultModalityState())
    }

    override var firstLine: String? = null
}

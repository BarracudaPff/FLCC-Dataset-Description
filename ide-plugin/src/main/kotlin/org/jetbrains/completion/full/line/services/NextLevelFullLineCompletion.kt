package org.jetbrains.completion.full.line.services

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

interface NextLevelFullLineCompletion {
    fun restartCompletion(project: Project, editor: Editor, line: String)
    var firstLine: String?
}

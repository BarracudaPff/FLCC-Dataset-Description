package org.jetbrains.completion.full.line

import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.util.text.StringUtil

class FullLineInsertHandler : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val ans = item.lookupString
        val indexes = StringUtil.getWordIndicesIn(ans)
        val offset = context.editor.caretModel.offset

        if (context.completionChar == '\t' && indexes.size > 1) {
            val token = ans.substring(0, indexes[1].startOffset)
            ApplicationManager.getApplication().runWriteAction {
                context.editor.document.replaceString(offset - ans.length, offset, token)
            }

            ApplicationManager.getApplication().invokeLater({
                CodeCompletionHandlerBase(CompletionType.BASIC, false, false, true)
                    .invokeCompletion(context.project, context.editor)
            }, ModalityState.defaultModalityState())
        }
    }
}

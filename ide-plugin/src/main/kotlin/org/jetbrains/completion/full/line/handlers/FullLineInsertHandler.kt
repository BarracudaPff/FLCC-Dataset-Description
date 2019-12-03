package org.jetbrains.completion.full.line.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.completion.full.line.services.NextLevelFullLineCompletion

class FullLineInsertHandler : InsertHandler<LookupElement> {
    private val nextLevelService: NextLevelFullLineCompletion = ServiceManager.getService(NextLevelFullLineCompletion::class.java)

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        val ans = item.lookupString
        val indexes = StringUtil.getWordIndicesIn(ans)

        if (context.completionChar == '\t' && indexes.size > 1) {
            val offset = context.editor.caretModel.offset
            val token = ans.substring(0, indexes[1].startOffset)
            val line = ans.substring(indexes[1].startOffset, ans.length)

            ApplicationManager.getApplication().runWriteAction {
                context.editor.document.replaceString(offset - ans.length, offset, token)
            }

            nextLevelService.restartCompletion(context.project, context.editor, line)
        }
    }
}

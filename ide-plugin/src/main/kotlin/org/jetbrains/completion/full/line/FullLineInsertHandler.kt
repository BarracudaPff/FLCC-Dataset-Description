package org.jetbrains.completion.full.line

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.openapi.util.text.StringUtil

class FullLineInsertHandler(
    private val altHandler: CompletionTabListener = CompletionTabListener()
) : InsertHandler<LookupElement> {
    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        println(context.completionChar)
        val ans = item.lookupString
        val indexes = StringUtil.getWordIndicesIn(ans)
        val offset = context.editor.caretModel.offset
        if (indexes.size > 1) {
            val token = ans.substring(0, indexes[1].startOffset)
            context.editor.document.replaceString(offset - ans.length, offset, token)
        }
        if (context.completionChar != '\t') {
            altHandler.nextLevel = true
        }

    }

}

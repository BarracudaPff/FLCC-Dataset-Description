package org.jetbrains.completion.full.line.handlers

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.impl.LiveTemplateLookupElementImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.completion.full.line.language.LanguageMLSupporter
import org.jetbrains.completion.full.line.language.PythonSupporter
import org.jetbrains.completion.full.line.services.NextLevelFullLineCompletion

class FullLineInsertHandler(private val supporter: LanguageMLSupporter) : InsertHandler<LookupElement> {
    private val nextLevelService: NextLevelFullLineCompletion = ServiceManager.getService(NextLevelFullLineCompletion::class.java)

    override fun handleInsert(context: InsertionContext, item: LookupElement) {

        ApplicationManager.getApplication().runWriteAction {
            val ans = item.lookupString
            val document = context.document
            val offset = context.tailOffset

            val startCompletion = context.startOffset
            val indexes = StringUtil.getWordIndicesIn(ans)

            if (context.completionChar == '\t' && indexes.size > 1) {
                val token = ans.substring(0, indexes[1].startOffset)
                val lineWithoutToken = ans.substring(indexes[1].startOffset, ans.length)

                document.replaceString(startCompletion, offset, token)

                nextLevelService.restartCompletion(context.project, context.editor, lineWithoutToken)
            } else {
                val endLine = document.getLineEndOffset(document.getLineNumber(offset))

                val remove = removeOverwritingChars(
                        document.getText(TextRange.create(startCompletion, offset)),
                        document.getText(TextRange.create(offset, endLine))
                )

                document.deleteString(offset, offset + remove)

                val missingBracesAmount = supporter.getMissingBraces(ans)?.joinToString()?.let { it ->
                    document.insertString(offset, it)
                    return@let it.length
                } ?: 0

                val newText = document.getText(TextRange(startCompletion, offset + missingBracesAmount))

                val template = supporter.createTemplate(newText) ?: return@runWriteAction
                LiveTemplateLookupElementImpl.startTemplate(context, template)
            }
        }
    }

    fun removeOverwritingChars(completion: String, line: String): Int {
        var remove = 0
        for (ch in line) {
            var found = false
            for (chC in completion.drop(remove)) {
                if (chC == ch) {
                    found = true
                    break
                }
            }
            if (!found) {
                break
            } else {
                remove++
            }
        }

        return remove
    }
}

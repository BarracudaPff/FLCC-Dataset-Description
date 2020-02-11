package org.jetbrains.completion.full.line

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.impl.LiveTemplateLookupElementImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.SyntaxTraverser
import com.jetbrains.python.codeInsight.imports.PythonImportUtils
import com.jetbrains.python.psi.PyElement
import org.jetbrains.completion.full.line.language.LanguageMLSupporter
import org.jetbrains.completion.full.line.services.NextLevelFullLineCompletion
import org.jetbrains.completion.full.line.settings.MLServerCompletionSettings

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

                val missingBracesAmount = supporter.getMissingBraces(ans)
                        ?.joinToString()
                        ?.let { it ->
                            document.insertString(offset, it)
                            it.length
                        } ?: 0

                if (MLServerCompletionSettings.getInstance().enableStringsWalking()) {
                    val fixedStart = context.file
                            .findElementAt(startCompletion)
                            ?.textRange
                            ?.startOffset
                            ?.apply { document.deleteString(this, startCompletion) }
                            ?: startCompletion

                    val template = supporter.createStringTemplate(context.file, fixedStart, offset + missingBracesAmount)
                            ?: return@runWriteAction
                    LiveTemplateLookupElementImpl.startTemplate(context, template)
                }
            }

            supporter.autoImportFix(context.file, context.startOffset, context.selectionEndOffset)
        }
    }

    private fun removeOverwritingChars(completion: String, line: String): Int {
        var amount = 0

        for (char in line) {
            var found = false

            for (charWithOffset in completion.drop(amount)) {
                if (!charWithOffset.isLetterOrWhitespace() && charWithOffset == char) {
                    found = true
                    break
                }
            }
            if (found) {
                amount++
            } else {
                break
            }
        }

        return amount
    }
}

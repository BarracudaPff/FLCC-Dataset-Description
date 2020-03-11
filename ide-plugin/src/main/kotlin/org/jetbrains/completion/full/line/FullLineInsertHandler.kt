package org.jetbrains.completion.full.line

import com.intellij.codeInsight.completion.InsertHandler
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.template.impl.LiveTemplateLookupElementImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.SyntaxTraverser
import com.intellij.refactoring.suggested.startOffset
import com.jetbrains.python.psi.PyStringElement
import org.jetbrains.completion.full.line.language.LanguageMLSupporter
import org.jetbrains.completion.full.line.services.NextLevelFullLineCompletion
import org.jetbrains.completion.full.line.settings.MLServerCompletionSettings

class FullLineInsertHandler(private val supporter: LanguageMLSupporter) : InsertHandler<LookupElement> {
    private val nextLevelService: NextLevelFullLineCompletion = service()

    override fun handleInsert(context: InsertionContext, item: LookupElement) {
        ApplicationManager.getApplication().runWriteAction {
            val ans = item.lookupString
            val document = context.document
            val offset = context.tailOffset

            val startCompletion = context.startOffset
            val endLine = document.getLineEndOffset(document.getLineNumber(offset))
            val prefix = item.getUserData(key)!!

            if (context.completionChar == '\t') {
                val token = getNextToken(context.file, startCompletion + prefix.length + 1, endLine)

                val end = if (token.startsWith(prefix)) {
                    token
                } else {
                    prefix + token
                }

                val lineWithoutToken = document.getText(TextRange(startCompletion + end.length, endLine))
                document.replaceString(startCompletion, endLine, end)

                nextLevelService.restartCompletion(context.project, context.editor, lineWithoutToken, token)
            } else {
                val remove = removeOverwritingChars(
                        document.getText(TextRange.create(startCompletion, offset)),
                        document.getText(TextRange.create(offset, endLine))
                )
                document.deleteString(offset, offset + remove)
                if (context.file.findElementAt(offset) !is PyStringElement) {

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
                        if (template != null) {
                            LiveTemplateLookupElementImpl.startTemplate(context, template)
                        }

                    }
                }
                try {
                    supporter.autoImportFix(context.file, context.startOffset, context.selectionEndOffset)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getNextToken(element: PsiElement, from: Int, to: Int): String {
        val token = StringBuilder()
        var found = false
        SyntaxTraverser.psiTraverser()
                .withRoot(element)
                .onRange(TextRange(from, to))
                .filter { it.firstChild == null && it.text.isNotEmpty() }
                .forEach {
                    if (found) {
                        if (it is PsiWhiteSpace) {
                            token.append(it.text)
                        }
                        return token.toString().trimStart()
                    }
                    if (it.startOffset < from) {
                        token.append(it.text.slice(IntRange(from - it.startOffset - 1, it.text.length - 1)))
                    } else {
                        token.append(it.text)
                    }
                    if (it !is PsiWhiteSpace) {
                        found = true
                    }
                }
        return ""
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

package org.jetbrains.completion.full.line

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiFile
import icons.PythonIcons
import org.jetbrains.completion.full.line.language.CodeFormatter
import org.jetbrains.completion.full.line.language.LanguageMLSupporter
import org.jetbrains.completion.full.line.models.FullLineCompletion
import org.jetbrains.completion.full.line.models.FullLineCompletionMode
import org.jetbrains.completion.full.line.services.NextLevelFullLineCompletion
import org.jetbrains.completion.full.line.settings.MLServerCompletionSettings
import java.math.RoundingMode
import java.text.DecimalFormat
import javax.swing.Icon

class FullLineContributor : CompletionContributor() {
    private val provider = GPTCompletionProvider()
    private val settings = MLServerCompletionSettings.getInstance()
    private val service: NextLevelFullLineCompletion = service()

    private val scoreFormatter = DecimalFormat("#.####").apply { roundingMode = RoundingMode.DOWN }

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        if (!settings.isEnabled() || (parameters.isAutoPopup && !settings.isAutoPopup()))
            return

        val supporter = LanguageMLSupporter.getInstance(parameters.originalFile.language) ?: return
        val formatter = CodeFormatter.getInstance(parameters.originalFile.language) ?: return

        if (supporter.isComment(parameters.position) || insideLine(parameters.editor.document, parameters.offset)) {
            return
        }

        val context = if (settings.state.model.contains("gpt") || settings.state.model == "best") {
            formatter.format(parameters.originalFile, TextRange(0, parameters.offset))
        } else {
            parameters.originalFile.text
        }

        val filename = getFilePath(parameters.originalFile)
        val offset = context.length
        val prefix = CompletionUtil.findJavaIdentifierPrefix(parameters)

        LOG.debug("Completion data:\n\tfilename: $filename\n\toffset: $offset\n\tprefix: $prefix")

        handleFirstLine(result, supporter, prefix)

        for (variant in provider.getVariants(context, filename, prefix, offset)) {
            val element = getLookupElementBuilder(supporter, variant, prefix) ?: continue
            element.putUserData(key, prefix)
            result.addElement(PrioritizedLookupElement.withPriority(element, variant.score * 1000000))
        }
    }

    private fun handleFirstLine(result: CompletionResultSet, supporter: LanguageMLSupporter, prefix: String) {
        if (service.firstLine != null) {
            LOG.debug("First line: ${service.firstLine}")
            val element = LookupElementBuilder.create(service.firstLine!!)
                    .withTypeText(FULL_LINE_TAIL_TEXT)
                    .withInsertHandler(FullLineInsertHandler(supporter))
                    .withIcon(GPT_ICON)

            element.putUserData(key, prefix)
            result.addElement(PrioritizedLookupElement.withPriority(element, 1000000.0))
            service.firstLine = null
        }
    }

    private fun getLookupElementBuilder(supporter: LanguageMLSupporter, variant: FullLineCompletion, prefix: String): LookupElementBuilder? {
        val tail = if (settings.state.showScore) " ${scoreFormatter.format(variant.score * 100)}%" else ""

        return if (settings.state.mode == FullLineCompletionMode.ONE_TOKEN) {
            val token = supporter.getFirstToken(variant.suggestion) ?: return null
            if (token == prefix) {
                return null
            }

            LookupElementBuilder.create(token)
        } else {
            LookupElementBuilder.create(variant.suggestion)
                    .withTypeText(FULL_LINE_TAIL_TEXT)
                    .withInsertHandler(FullLineInsertHandler(supporter))
        }.withTailText(tail, true).withIcon(GPT_ICON)
    }

    private fun insideLine(document: Document, offset: Int): Boolean {
        val endLine = document.getLineEndOffset(document.getLineNumber(offset))
        val rightText = document.getText(TextRange(offset, endLine))

        return rightText.isNotBlank()
    }

    private fun getFilePath(file: PsiFile): String {
        val projectPath = file.project.basePath ?: "/"
        val filePath = file.virtualFile.path

        return filePath.drop(projectPath.length + 1)
    }

    companion object {
        const val FULL_LINE_TAIL_TEXT = "full-line"
        val GPT_ICON: Icon = PythonIcons.Python.Python

        val LOG = Logger.getInstance(FullLineContributor::class.java)
    }
}

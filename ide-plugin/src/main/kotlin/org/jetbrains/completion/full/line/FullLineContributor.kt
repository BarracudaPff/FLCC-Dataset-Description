package org.jetbrains.completion.full.line

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.registry.Registry
import icons.PythonIcons
import org.jetbrains.completion.full.line.settings.MLServerCompletionSettings

class FullLineContributor : CompletionContributor() {
    companion object {
        const val FULL_LINE_TAIL_TEXT = "full-line"
        private val settings = MLServerCompletionSettings.getInstance().state

        val LOG = Logger.getInstance(FullLineContributor::class.java)

        val INSERT_HANDLER = FullLineInsertHandler()
    }

    private val provider = GPTCompletionProvider()

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        if (!settings.enable)
            return

        val context = parameters.originalFile.text.substring(0, parameters.offset)
        val filename = parameters.originalFile.name

        for (variant in provider.getVariants(context, filename)) {
            val lookupElementBuilder = LookupElementBuilder.create(variant)
                    .withTailText(provider.description, true)
                    .withTypeText(FULL_LINE_TAIL_TEXT)
                    .withIcon(PythonIcons.Python.Python)
                    .withInsertHandler(INSERT_HANDLER)
            result.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, 200000.0))
        }
    }
}

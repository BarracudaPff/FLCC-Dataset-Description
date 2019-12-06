package org.jetbrains.completion.full.line

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.registry.Registry
import icons.PythonIcons

class FullLineContributor : CompletionContributor() {
    companion object {
        const val FULL_LINE_TAIL_TEXT = "full-line"

        val LOG = Logger.getInstance(FullLineContributor::class.java)

        val INSERT_HANDLER = FullLineInsertHandler()
    }

    private val providers: List<FullLineCompletionProvider> = listOf(
            NetworkCompletionProvider("gpt")
            //NetworkCompletionProvider("char-rnn", "${completionServerUrl()}/v1/complete/charrnn")
    )

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        if (!Registry.`is`("full.line.completion.enable"))
            return

        val context = parameters.originalFile.text.substring(0, parameters.offset)
        val filename = parameters.originalFile.name

        for (provider in providers) {
            for (variant in provider.getVariants(context, filename)) {
                val lookupElementBuilder = LookupElementBuilder.create(variant)
                        .withTailText("  ${provider.description}", true)
                        .withTypeText(FULL_LINE_TAIL_TEXT)
                        .withIcon(PythonIcons.Python.Python)
                        .withInsertHandler(INSERT_HANDLER)
                result.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, 200000.0))
            }
        }
    }
}

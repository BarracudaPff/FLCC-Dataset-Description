package org.jetbrains.completion.full.line

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.registry.Registry
import icons.PythonIcons

class FullLineContributor : CompletionContributor() {
    companion object {
        const val FULL_LINE_TAIL_TEXT = "full-line"

        private fun completionServerUrl(): String {
            val url = Registry.get("full.line.completion.server.url")
            val port = Registry.get("full.line.completion.server.port")
            return "https://$url:$port"
        }
    }

    private val providers: List<FullLineCompletionProvider> = listOf(
//        NetworkCompletionProvider("gpt", TODO("add gpt handler address here")),
        NetworkCompletionProvider("char-rnn", "${completionServerUrl()}/completion/python3")
    )

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        if (!Registry.`is`("full.line.completion.enable")) return
        val context = parameters.originalFile.text.substring(0, parameters.offset)
        for (provider in providers) {
            for (variant in provider.getVariants(context)) {
                val lookupElementBuilder = LookupElementBuilder.create(variant)
                    .withTailText("  ${provider.description}", true)
                    .withTypeText(FULL_LINE_TAIL_TEXT)
                    .withIcon(PythonIcons.Python.Python)
                    .withInsertHandler(FullLineInsertHandler())
                result.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, 200000.0))
            }
        }
    }
}

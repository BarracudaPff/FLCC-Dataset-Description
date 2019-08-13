package org.jetbrains.completion.full.line

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import icons.PythonIcons

class FullLineContributor : CompletionContributor() {
    companion object {
        const val FULL_LINE_TAIL_TEXT = "full-line"
        private const val URL = "localhost"
    }

    private val contributors = listOf(
        GPTCompletionProvider(URL, 5000)
    )

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val context = parameters.originalFile.text.substring(0, parameters.offset)
        for (contributor in contributors) {
            for (variant in contributor.getVariants(context)) {
                val lookupElementBuilder = LookupElementBuilder.create(variant)
                    .withTailText("  ${contributor.description()}", true)
                    .withTypeText(FULL_LINE_TAIL_TEXT)
                    .withIcon(PythonIcons.Python.Python)
                    .withInsertHandler(MyInsertHandler())
                result.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, 200000.0))
            }
        }
    }
}

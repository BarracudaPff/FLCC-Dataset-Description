package org.jetbrains.completion.full.line

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.components.service
import icons.PythonIcons
import org.jetbrains.completion.full.line.language.LanguageMLSupporter
import org.jetbrains.completion.full.line.models.FullLineCompletionMode
import org.jetbrains.completion.full.line.services.NextLevelFullLineCompletion
import org.jetbrains.completion.full.line.settings.MLServerCompletionSettings
import javax.swing.Icon
import javax.swing.plaf.metal.MetalCheckBoxIcon

class FullLineContributor : CompletionContributor() {
    private val provider = GPTCompletionProvider()
    private val settings = MLServerCompletionSettings.getInstance()
    private val service: NextLevelFullLineCompletion = service()

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val t1 = System.currentTimeMillis()
        if (!settings.isEnabled() || (parameters.isAutoPopup && !settings.isAutoPopup()))
            return

        val supporter = LanguageMLSupporter.getInstance(parameters.originalFile.language) ?: return

        if (!settings.isCommentsEnabled() && supporter.isComment(parameters.position)) {
            return
        }
        handleFirstLine(result, supporter)

        val context = parameters.originalFile.text
        val filename = parameters.originalFile.name
        val offset = parameters.offset

        val prefix = supporter.getPrefix(context.substring(0, parameters.offset))

        var variants = provider.getVariants(context, filename, prefix, offset)

        println(variants)

        for (variant in variants) {
            val element = getLookupElementBuilder(supporter, variant) ?: continue
            result.addElement(PrioritizedLookupElement.withPriority(element, 100000.0))
        }

        println("Time to complete: ${(System.currentTimeMillis() - t1)/1000.0}")

//        val element3 = getLookupElementBuilder(supporter, "response = requests.get('data') ")?.withIcon(MetalCheckBoxIcon())
//        result.addElement(PrioritizedLookupElement.withPriority(element3, 50000.0))
    }

    private fun handleFirstLine(result: CompletionResultSet, supporter: LanguageMLSupporter) {
        if (service.firstLine != null) {
            val element = LookupElementBuilder.create(service.firstLine!!)
                    .withTypeText(FULL_LINE_TAIL_TEXT)
                    .withInsertHandler(FullLineInsertHandler(supporter))
                    .withTailText(provider.description, true)
                    .withIcon(GPT_ICON)

            result.addElement(PrioritizedLookupElement.withPriority(element, 1000000.0))
            service.firstLine = null
        }
    }

    private fun getLookupElementBuilder(supporter: LanguageMLSupporter, variant: String): LookupElementBuilder? {
        return if (settings.state.mode == FullLineCompletionMode.ONE_TOKEN) {
            val token = supporter.getFirstToken(variant) ?: return null
            LookupElementBuilder.create(token)
        } else {
            LookupElementBuilder.create(variant)
                    .withTypeText(FULL_LINE_TAIL_TEXT)
                    .withInsertHandler(FullLineInsertHandler(supporter))
        }.withTailText(provider.description, true).withIcon(GPT_ICON)
    }

    companion object {
        const val FULL_LINE_TAIL_TEXT = "full-line"
        val GPT_ICON: Icon = PythonIcons.Python.Python
    }
}

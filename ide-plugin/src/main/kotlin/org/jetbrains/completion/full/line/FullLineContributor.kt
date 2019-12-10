package org.jetbrains.completion.full.line

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.PrioritizedLookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.util.elementType
import com.jetbrains.python.PyTokenTypes
import icons.PythonIcons
import org.jetbrains.completion.full.line.handlers.FullLineInsertHandler
import org.jetbrains.completion.full.line.handlers.OneTokenHandler
import org.jetbrains.completion.full.line.language.LanguageMLSupporter
import org.jetbrains.completion.full.line.models.FullLineCompletionMode
import org.jetbrains.completion.full.line.services.NextLevelFullLineCompletion
import org.jetbrains.completion.full.line.settings.MLServerCompletionSettings
import javax.swing.Icon

class FullLineContributor : CompletionContributor() {
    private val provider = GPTCompletionProvider(host, port)
    private val settings = MLServerCompletionSettings.getInstance()
    private val service: NextLevelFullLineCompletion = ServiceManager.getService(NextLevelFullLineCompletion::class.java)

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        if (!settings.enable
                || (parameters.isAutoPopup && !settings.autoPopup)
                || parameters.position.elementType == PyTokenTypes.END_OF_LINE_COMMENT)
            return

        val supporter = LanguageMLSupporter.getInstance(parameters.originalFile.language) ?: return

        handleFirstLine(result, supporter)

        val context = parameters.originalFile.text.substring(0, parameters.offset)
        val filename = parameters.originalFile.name
        val prefix = supporter.getPrefix(context)

        for (variant in provider.getVariants(context, filename, prefix)) {
            if (settings.filter && !supporter.isCorrect(variant)) {
                continue
            }

            val lookupElementBuilder = getLookupElementBuilder(supporter, variant) ?: continue
            result.addElement(PrioritizedLookupElement.withPriority(lookupElementBuilder, 100000.0))
        }
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
        return if (settings.mode == FullLineCompletionMode.ONE_TOKEN) {
            val token = supporter.getFirstToken(variant) ?: return null
            LookupElementBuilder.create(token)
                    .withInsertHandler(OneTokenHandler())
        } else {
            LookupElementBuilder.create(variant)
                    .withTypeText(FULL_LINE_TAIL_TEXT)
                    .withInsertHandler(FullLineInsertHandler(supporter))
        }.withTailText(provider.description, true).withIcon(GPT_ICON)
    }

    companion object {
        val LOG = Logger.getInstance(FullLineContributor::class.java)

        val host = Registry.get("full.line.completion.server.url").asString()
        val port = Registry.get("full.line.completion.server.port").asInteger()

        const val FULL_LINE_TAIL_TEXT = "full-line"
        val GPT_ICON: Icon = PythonIcons.Python.Python
    }
}

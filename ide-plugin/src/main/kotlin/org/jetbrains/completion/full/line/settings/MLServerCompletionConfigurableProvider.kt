package org.jetbrains.completion.full.line.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import org.jetbrains.completion.full.line.GPTCompletionProvider

class ServerCompletionConfigurableProvider : ConfigurableProvider() {
    override fun createConfigurable(): Configurable? {
        return MLServerCompletionConfigurable(GPTCompletionProvider.getModels()) {
            GPTCompletionProvider.getStatusCallback()
        }
    }
}

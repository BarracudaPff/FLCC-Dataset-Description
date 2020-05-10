package org.jetbrains.completion.full.line.settings

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableProvider
import org.jetbrains.completion.full.line.GPTCompletionProvider
import java.net.SocketTimeoutException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeoutException

class ServerCompletionConfigurableProvider : ConfigurableProvider() {
    override fun createConfigurable(): Configurable? {
        return MLServerCompletionConfigurable(
                this::exceptionMessage,
                { GPTCompletionProvider.getStatus() },
                { GPTCompletionProvider.getModels() }
        )
    }

    private fun exceptionMessage(it: Throwable): String {
        return when (it) {
            is SocketTimeoutException, is TimeoutException -> MLServerCompletionBundle.message("ml.server.completion.gpt.connection.timeout")
            is ExecutionException -> it.cause?.localizedMessage ?: it.localizedMessage
            else -> it.localizedMessage
        }
    }
}

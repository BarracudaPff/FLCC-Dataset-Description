package org.jetbrains.completion.full.line

import com.intellij.util.io.HttpRequests
import org.jetbrains.completion.full.line.FullLineContributor.Companion.LOG
import org.jetbrains.completion.full.line.utils.GPTServerUtils
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.ExecutionException

class GPTCompletionProvider {
    fun getVariants(context: String, filename: String, prefix: String): List<String> {
        return try {
            val start = System.currentTimeMillis()
            val completions = GPTServerUtils.getCompletions(context, filename, prefix)
            LOG.debug("Time to predict completions is ${(System.currentTimeMillis() - start) / 1000f}")
            completions

        } catch (e: Exception) {
            val message = when (e) {
                is SocketTimeoutException -> "Timeout. Probably IP is wrong"
                is ConnectException -> "Network is down. Can't reach server"
                is HttpRequests.HttpStatusException -> "Something wrong with completion server"
                is ExecutionException, is IllegalStateException -> "Error while getting completions from server"
                else -> "Some other error occurred"
            }
            return logError(message, e)
        }
    }

    private fun logError(msg: String, error: Exception): List<String> {
        LOG.debug(msg, error)
        return emptyList()
    }
}

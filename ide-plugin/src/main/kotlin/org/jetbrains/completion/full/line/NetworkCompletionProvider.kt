package org.jetbrains.completion.full.line

import com.google.gson.Gson
import com.intellij.openapi.application.ex.ApplicationUtil
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.util.io.HttpRequests
import org.jetbrains.completion.full.line.FullLineContributor.Companion.LOG
import org.jetbrains.completion.full.line.models.FullLineCompletionRequest
import org.jetbrains.completion.full.line.models.FullLineCompletionResult
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException

class NetworkCompletionProvider(override val description: String, private val url: String) :
        FullLineCompletionProvider {
    override fun getVariants(context: String, filename: String): List<String> {
        return try {
            val start = System.currentTimeMillis()
            val completions = ApplicationUtil.runWithCheckCanceled(Callable {
                return@Callable HttpRequests.post(url, "application/json").gzip(true)
                        .connect { r ->
                            val offset = context.length
                            val token = TextUtils.getLastToken(context)

                            val request = FullLineCompletionRequest(context, token, offset, filename)
                            r.write(Gson().toJson(request))

                            Gson().fromJson(r.reader, FullLineCompletionResult::class.java).completions
                        }
            }, EmptyProgressIndicator())
            LOG.debug("Time to predict completions is ${(System.currentTimeMillis() - start) / 1000}")
            completions

        } catch (e: Exception) {
            val message = when (e) {
                is SocketTimeoutException                       -> "Timeout. Probably IP is wrong"
                is ConnectException                             -> "Network is down. Can't reach server"
                is HttpRequests.HttpStatusException             -> "Something wrong with completion server"
                is ExecutionException, is IllegalStateException -> "Error while getting completions from server"
                else                                            -> "Some other error occurred"
            }
            return logError(message, e)
        }
    }

    private fun logError(msg: String, error: Exception): List<String> {
        LOG.error(msg, error)
        return emptyList()
    }
}

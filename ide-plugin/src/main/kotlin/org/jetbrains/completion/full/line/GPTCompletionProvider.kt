package org.jetbrains.completion.full.line

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.util.ActionCallback
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.HttpRequests
import org.jetbrains.completion.full.line.models.FullLineCompletionRequest
import org.jetbrains.completion.full.line.models.FullLineCompletionResult
import org.jetbrains.completion.full.line.settings.MLServerCompletionBundle
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException

class GPTCompletionProvider {
    val description = "  gpt"

    fun getVariants(context: String, filename: String): List<String> {
        val start = System.currentTimeMillis()
        val future = executor.submit(Callable<List<String>> {
            try {
                HttpRequests.post("http://$host:$port/v1/complete/gpt", "application/json")
                        .connect { r ->
                            val offset = context.length
                            val token = TextUtils.getLastToken(context)

                            val request = FullLineCompletionRequest(context, token, offset, filename)
                            r.write(Gson().toJson(request))

                            Gson().fromJson(r.reader, FullLineCompletionResult::class.java).completions
                        }
            } catch (e: Exception) {
                val message = when (e) {
                    is ConnectException                             -> return@Callable emptyList()
                    is SocketTimeoutException                       -> "Timeout. Probably IP is wrong"
                    is HttpRequests.HttpStatusException             -> "Something wrong with completion server"
                    is ExecutionException, is IllegalStateException -> "Error while getting completions from server"
                    else                                            -> "Some other error occurred"
                }
                logError(message, e)
            }
        })

        ProgressIndicatorUtils.awaitWithCheckCanceled(future)

        LOG.debug("Time to predict completions is ${(System.currentTimeMillis() - start) / 1000.0}")
        return future.get()
    }

    private fun logError(msg: String, error: Exception): List<String> {
        LOG.debug(msg, error)
        return emptyList()
    }

    companion object {
        private val LOG = Logger.getInstance(FullLineContributor::class.java)

        private val host = Registry.get("ml.server.completion.host").asString()
        private val port = Registry.get("ml.server.completion.port").asInteger()

        fun getStatusCallback(): ActionCallback {
            val callback = ActionCallback()
            executor.submit {
                try {
                    if (HttpRequests.request("http://$host:$port/v1/status").tryConnect() == 200) {
                        callback.setDone()
                    } else {
                        callback.reject(MLServerCompletionBundle.message("ml.server.completion.gpt.connection.error"))
                    }
                } catch (e: SocketTimeoutException) {
                    callback.reject(MLServerCompletionBundle.message("ml.server.completion.gpt.connection.timeout"))
                } catch (e: Exception) {
                    callback.reject(e.localizedMessage)
                }

            }
            return callback
        }

        //Completion server cancels all threads besides the last one
        //We need at least 2 threads, the second one must (almost) instantly cancel the first one, otherwise, UI will freeze
        private val executor = AppExecutorUtil.createBoundedApplicationPoolExecutor("ML Server Completion", 2)
    }
}

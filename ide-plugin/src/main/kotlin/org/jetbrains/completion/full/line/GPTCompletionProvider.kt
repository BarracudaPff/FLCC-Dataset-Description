package org.jetbrains.completion.full.line

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.util.concurrency.SequentialTaskExecutor
import com.intellij.util.io.HttpRequests
import org.jetbrains.completion.full.line.models.FullLineCompletionRequest
import org.jetbrains.completion.full.line.models.FullLineCompletionResult
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.ExecutionException
import java.util.concurrent.locks.ReentrantLock

class GPTCompletionProvider(private val host: String, private val port: Int) {
    val description = "  gpt"
    private val lock = ReentrantLock()

    fun getVariants(context: String, filename: String): List<String> {
        val start = System.currentTimeMillis()
        val future = SequentialTaskExecutor.createSequentialApplicationPoolExecutor(javaClass.simpleName)
                .submit<List<String>> {
                    lock.withListLock {
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
                                is ConnectException                             -> return@submit emptyList()
                                is SocketTimeoutException                       -> "Timeout. Probably IP is wrong"
                                is HttpRequests.HttpStatusException             -> "Something wrong with completion server"
                                is ExecutionException, is IllegalStateException -> "Error while getting completions from server"
                                else                                            -> "Some other error occurred"
                            }
                            logError(message, e)
                        }
                    }
                }

        ProgressIndicatorUtils.awaitWithCheckCanceled(future)

        LOG.debug("Time to predict completions is ${(System.currentTimeMillis() - start) / 1000.0}")
        return future.get()
    }

    private fun logError(msg: String, error: Exception): List<String> {
        LOG.debug(msg, error)
        return emptyList()
    }

    private inline fun <T> ReentrantLock.withListLock(action: () -> List<T>): List<T> {
        if (isLocked) {
            return emptyList()
        }

        lock()
        try {
            return action()
        } finally {
            unlock()
        }
    }

    companion object {
        private val LOG = Logger.getInstance(FullLineContributor::class.java)
    }
}

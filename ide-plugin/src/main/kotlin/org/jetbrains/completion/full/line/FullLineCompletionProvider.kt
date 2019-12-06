package org.jetbrains.completion.full.line

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.util.io.HttpRequests
import org.jetbrains.completion.full.line.FullLineContributor.Companion.LOG
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.ExecutionException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

abstract class FullLineCompletionProvider(open val description: String) {
    private val communicationLock = ReentrantLock()

    abstract fun completionServerUrl(): String

    abstract fun sendAndReceiveRequest(r: HttpRequests.Request, context: String, filename: String): List<String>

    fun getVariants(context: String, filename: String): List<String> = communicationLock.withLock {
        val start = System.currentTimeMillis()

        val future = ApplicationManager.getApplication().executeOnPooledThread<List<String>> {
            try {
                HttpRequests.post(completionServerUrl(), "application/json").gzip(true)
                        .connect { r ->
                            sendAndReceiveRequest(r, context, filename)
                        }

            } catch (e: Exception) {
                val message = when (e) {
                    is ConnectException -> return@executeOnPooledThread emptyList()
                    is SocketTimeoutException                       -> "Timeout. Probably IP is wrong"
                    is HttpRequests.HttpStatusException             -> "Something wrong with completion server"
                    is ExecutionException, is IllegalStateException -> "Error while getting completions from server"
                    else                                            -> "Some other error occurred"
                }
                logError(message, e)
            }
        }
        ProgressIndicatorUtils.awaitWithCheckCanceled(future)

        LOG.debug("Time to predict completions is ${(System.currentTimeMillis() - start) / 1000}")
        return future.get()
    }


    fun logError(msg: String, error: Exception): List<String> {
        LOG.error(msg, error)
        return emptyList()
    }
}

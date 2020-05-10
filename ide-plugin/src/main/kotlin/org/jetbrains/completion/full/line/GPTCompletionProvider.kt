package org.jetbrains.completion.full.line

import com.google.gson.Gson
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.HttpRequests
import org.jetbrains.completion.full.line.models.*
import org.jetbrains.completion.full.line.settings.MLServerCompletionSettings
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class GPTCompletionProvider {
    @Suppress("RemoveExplicitTypeArguments")
    fun getVariants(context: String, filename: String, prefix: String, offset: Int): List<FullLineCompletion> {
        val start = System.currentTimeMillis()

        val future = executor.submit(Callable<List<FullLineCompletion>> {
            try {
                HttpRequests.post("http://$host:$port/v1/complete/gpt", "application/json")
                        .connect { r ->
                            val request = FullLineCompletionRequest(
                                    context,
                                    prefix,
                                    offset,
                                    filename,
                                    MLServerCompletionSettings.getInstance())

                            r.write(GSON.toJson(request))

                            val raw = r.reader.readText()

                            GSON.fromJson(raw, FullLineCompletionResult::class.java)?.completions
                                    ?: logError(GSON.fromJson(raw, RequestError::class.java))
                        }
            } catch (e: Exception) {
                val message = when (e) {
                    is ConnectException -> return@Callable emptyList()
                    is SocketTimeoutException -> "Timeout. Probably IP is wrong"
                    is HttpRequests.HttpStatusException -> "Something wrong with completion server"
                    is ExecutionException, is IllegalStateException -> "Error while getting completions from server"
                    else -> "Some other error occurred"
                }
                logError(message, e)
            }
        })

        awaitWithCheckCanceled(future)

        LOG.debug("Time to predict completions is ${(System.currentTimeMillis() - start) / 1000.0}")
        return try {
            future.get(1, TimeUnit.SECONDS)
        } catch (e: Exception) {
            logError(e)
        }
    }

    private fun logError(msg: String, throwable: Throwable): List<FullLineCompletion> {
        LOG.debug(msg, throwable)
        return emptyList()
    }

    private fun logError(error: RequestError): List<FullLineCompletion> = logError("Server bad response", error)

    private fun logError(error: Throwable): List<FullLineCompletion> = logError("Server request", error)

    companion object {
        private val LOG = logger<FullLineContributor>()
        private val GSON = Gson()

        private const val timeout = 3L

        private val host = Registry.get("ml.server.completion.host").asString()
        private val port = Registry.get("ml.server.completion.port").asInteger()

        fun getModels(): List<String> {
            LOG.info("Getting models")
            val future = executor.submit(Callable {
                HttpRequests.request("http://$host:$port/v1/models").connect { r ->
                    val res = GSON.fromJson(r.reader, ModelsRequest::class.java)

                    listOf("best (${res.bestModel.name})") + res.models.map { model -> model.name }
                }
            })
            ProgressManager.checkCanceled()

            return future.get(timeout, TimeUnit.SECONDS)
        }

        fun getStatus(): Boolean {
            LOG.info("Getting status")
            val future = executor.submit(Callable {
                HttpRequests.request("http://$host:$port/v1/status").tryConnect() == 200
            })
            ProgressManager.checkCanceled()
            return future.get(timeout, TimeUnit.SECONDS)
        }

        //Completion server cancels all threads besides the last one
        //We need at least 2 threads, the second one must (almost) instantly cancel the first one, otherwise, UI will freeze
        val executor = AppExecutorUtil.createBoundedApplicationPoolExecutor("ML Server Completion", 2)
    }
}

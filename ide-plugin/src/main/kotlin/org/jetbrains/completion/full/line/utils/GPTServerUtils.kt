package org.jetbrains.completion.full.line.utils

import com.google.gson.Gson
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ex.ApplicationUtil
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.util.ActionCallback
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.io.HttpRequests
import org.jetbrains.completion.full.line.models.FullLineCompletionRequest
import org.jetbrains.completion.full.line.models.FullLineCompletionResult
import org.jetbrains.completion.full.line.settings.MLServerCompletionBundle
import org.jetbrains.completion.full.line.settings.MLServerCompletionSettings
import java.net.SocketTimeoutException
import java.util.concurrent.Callable

object GPTServerUtils {
    private val host = Registry.get("ml.server.completion.host").asString()
    private val port = Registry.get("ml.server.completion.port").asInteger()

    private val completionURL = "http://$host:$port/v1/complete/gpt"
    private val statusURL = "http://$host:$port/v1/complete/gpt"

    fun getCompletions(context: String, filename: String, prefix: String): List<String> {
        return ApplicationUtil.runWithCheckCanceled(Callable {
            return@Callable HttpRequests.post(statusURL, "application/json")
                    .connect { r ->
                        val request = FullLineCompletionRequest(
                                context,
                                prefix,
                                context.length,
                                filename,
                                MLServerCompletionSettings.getInstance())

                        r.write(Gson().toJson(request))

                        Gson().fromJson(r.reader, FullLineCompletionResult::class.java).completions
                    }
        }, EmptyProgressIndicator())
    }


    fun getStatus(): ActionCallback {
        val callback = ActionCallback()
        ApplicationManager.getApplication().executeOnPooledThread {
            doUpdateAndShowResult(callback)
        }
        return callback
    }

    private fun doUpdateAndShowResult(callback: ActionCallback) {
        try {
            if (HttpRequests.request(completionURL).tryConnect() == 200) {
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
}

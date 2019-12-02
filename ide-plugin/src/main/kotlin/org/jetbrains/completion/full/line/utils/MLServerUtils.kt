package org.jetbrains.completion.full.line.utils

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.ActionCallback
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.io.HttpRequests
import org.jetbrains.completion.full.line.settings.MLServerCompletionBundle
import java.net.SocketTimeoutException

class MLServerUtils {
    companion object {
        private val host = Registry.get("ml.server.completion.host").asString()
        private val port = Registry.get("ml.server.completion.port").asInteger()

        fun getStatus(): ActionCallback {
            val callback = ActionCallback()
            ApplicationManager.getApplication().executeOnPooledThread {
                doUpdateAndShowResult(callback)
            }
            return callback
        }

        private fun doUpdateAndShowResult(callback: ActionCallback) {
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
    }
}

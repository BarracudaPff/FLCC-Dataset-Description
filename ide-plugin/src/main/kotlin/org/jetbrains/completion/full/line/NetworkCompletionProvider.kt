package org.jetbrains.completion.full.line

import com.google.gson.Gson
import com.intellij.openapi.application.ex.ApplicationUtil
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.io.HttpRequests
import org.jetbrains.completion.full.line.FullLineContributor.Companion.LOG
import org.jetbrains.completion.full.line.models.FullLineCompletionRequest
import org.jetbrains.completion.full.line.models.FullLineCompletionResult
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException

class NetworkCompletionProvider(override val description: String, private val url: String) :
    FullLineCompletionProvider {
    override fun getVariants(context: String, filename: String): List<String> {
        return try {
            val start = System.currentTimeMillis()
            val completions = ApplicationManager.getApplication().executeOnPooledThread(Callable {
                return@Callable HttpRequests.post(url, "application/json").gzip(true)
                    .connect { r ->
                        val offset = context.length
                        val token = StringUtil.getWordsIn(context).last()
                        val request = FullLineCompletionRequest(context, token, offset, filename)

                        r.write(Gson().toJson(request))

                        Gson().fromJson(r.reader, FullLineCompletionResult::class.java).completions
                    }
            }, EmptyProgressIndicator())
            LOG.debug("Time to predict completions is ${System.currentTimeMillis() - start}")
            completions
        } catch (e: HttpRequests.HttpStatusException) {
            logError("Something wrong with completion server. $e", e)
        } catch (e: ExecutionException) {
            logError("Error while getting completions from server", e)
        }
    }

    private fun logError(msg: String, error: Exception): List<String> {
        LOG.error(msg, error)
        return emptyList()
    }
}

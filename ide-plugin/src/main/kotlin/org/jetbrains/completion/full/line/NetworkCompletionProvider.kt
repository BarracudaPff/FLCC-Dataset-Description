package org.jetbrains.completion.full.line

import com.google.gson.Gson
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.io.HttpRequests
import org.jetbrains.completion.full.line.models.FullLineCompletionRequest
import org.jetbrains.completion.full.line.models.FullLineCompletionResult
import java.util.concurrent.Callable

class NetworkCompletionProvider(override val description: String, private val url: String) :
    FullLineCompletionProvider {
    override fun getVariants(context: String): List<String> {
        return ApplicationManager.getApplication().executeOnPooledThread(Callable {
            return@Callable HttpRequests.post(url, "application/json").gzip(true)
                .connect { r ->
                    val offset = context.length
                    val token = StringUtil.getWordsIn(context).last()
                    val request = FullLineCompletionRequest(StringUtil.escapeStringCharacters(context), token, offset)

                    r.write(Gson().toJson(request))

                    Gson().fromJson(r.reader, FullLineCompletionResult::class.java).completions
                }
        }).get()
    }
}
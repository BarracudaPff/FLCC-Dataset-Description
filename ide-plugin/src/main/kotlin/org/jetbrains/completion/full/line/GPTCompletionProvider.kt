package org.jetbrains.completion.full.line

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.io.HttpRequests
import java.util.concurrent.Callable

class GPTCompletionProvider(private val url: String, private val port: Int) : FullLineCompletionProvider {
    private companion object {
        val RESULT_TYPE = object : TypeReference<List<String>>() {}
    }

    override fun description(): String = "gpt"

    override fun getVariants(context: String): List<String> {
        return ApplicationManager.getApplication().executeOnPooledThread(Callable {
            return@Callable HttpRequests.post("http://$url:$port/complete", "application/json").gzip(true)
                .connect { r ->
                    r.write("{ \"context\": \"${StringUtil.escapeStringCharacters(context)}\"}")
                    ObjectMapper().readValue(r.inputStream, RESULT_TYPE) as List<String>
                }
        }).get()
    }
}
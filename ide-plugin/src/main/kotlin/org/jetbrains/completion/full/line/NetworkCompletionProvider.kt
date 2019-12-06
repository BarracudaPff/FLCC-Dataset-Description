package org.jetbrains.completion.full.line

import com.google.gson.Gson
import com.intellij.openapi.util.registry.Registry
import com.intellij.util.io.HttpRequests
import org.jetbrains.completion.full.line.models.FullLineCompletionRequest
import org.jetbrains.completion.full.line.models.FullLineCompletionResult

class NetworkCompletionProvider(override val description: String) :
        FullLineCompletionProvider(description) {

    override fun completionServerUrl(): String {
        val url = Registry.get("full.line.completion.server.url").asString()
        val port = Registry.get("full.line.completion.server.port").asInteger()
        return "http://$url:$port/v1/complete/gpt"
    }

    override fun sendAndReceiveRequest(r: HttpRequests.Request, context: String, filename: String): List<String> {
        val offset = context.length
        val token = TextUtils.getLastToken(context)

        val request = FullLineCompletionRequest(context, token, offset, filename)
        r.write(Gson().toJson(request))

        return Gson().fromJson(r.reader, FullLineCompletionResult::class.java).completions
    }
}

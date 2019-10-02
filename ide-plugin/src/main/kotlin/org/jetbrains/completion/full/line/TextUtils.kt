package org.jetbrains.completion.full.line

import com.intellij.openapi.util.text.StringUtil

object TextUtils {

    fun getLastToken(text: String): String {
        val inds = StringUtil.getWordIndicesIn(text)
        return if (inds.isNotEmpty()) {
            if (inds.last().endOffset != text.length) {
                ""
            } else {
                text.substring(inds.last().startOffset, text.length)
            }
        } else {
            ""
        }
    }

}

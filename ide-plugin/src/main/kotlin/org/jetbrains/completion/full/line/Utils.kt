package org.jetbrains.completion.full.line

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.util.ProgressIndicatorUtils
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.util.ExceptionUtil
import java.util.concurrent.CancellationException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun Char.isLetterOrWhitespace(): Boolean {
    return isWhitespace() || isLetter()
}

fun TextRange.toIntRangeWithOffsets(contentOffset: Int, quote: Int): IntRange {
    return IntRange(startOffset + contentOffset + quote, endOffset + contentOffset - quote - 1)
}

fun IntRange.length(): Int {
    return last - first
}

val key = Key<String>("flcc-prefix")

fun <T> awaitWithCheckCanceled(future: Future<T>): T {
    val indicator = ProgressManager.getInstance().progressIndicator
    while (true) {
        ProgressIndicatorUtils.checkCancelledEvenWithPCEDisabled(indicator)
        try {
            return future[10, TimeUnit.MILLISECONDS]
        } catch (ignore: TimeoutException) {
        } catch (e: Throwable) {
            val cause = e.cause
            if (cause is ProcessCanceledException) {
                throw (cause as ProcessCanceledException?)!!
            }
            if (cause is CancellationException) {
                throw ProcessCanceledException(cause)
            }
            ExceptionUtil.rethrowUnchecked(e)
            throw RuntimeException(e)
        }
    }
}

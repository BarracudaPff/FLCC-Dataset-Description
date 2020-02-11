package org.jetbrains.completion.full.line

import com.intellij.openapi.util.TextRange

fun Char.isLetterOrWhitespace(): Boolean {
    return isWhitespace() || isLetter()
}

fun TextRange.toIntRangeWithOffsets(contentOffset: Int, quote: Int): IntRange {
    return IntRange(startOffset + contentOffset + quote, endOffset + contentOffset - quote - 1)
}

fun IntRange.length(): Int {
    return last - first
}

package org.jetbrains.completion.full.line.language

import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TextExpression
import org.jetbrains.completion.full.line.settings.MLServerCompletionSettings
import java.util.*

class PythonSupporter : LanguageMLSupporter {
    override fun isCorrect(variant: String): Boolean {
        //temporary implementation
        val f = variant.indexOf("#", 0, true)
        if (f != -1) {
            if (f != 0) {
                return false
            } else if (!MLServerCompletionSettings.getInstance().enableComments) {
                return false
            }
        }

        if (variant.last() == '_') {
            return false
        }

        if (Regex("'").findAll(variant).count() % 2 != 0) {
            return false
        }

        if (Regex("\"").findAll(variant).count() % 2 != 0) {
            return false
        }


        return true
    }

    override fun getFirstToken(line: String): String? {
        var curToken = ""
        var offset = 0
        for (ch in line) {
            if (ch == '_' || ch.isLetter() || ch.isDigit()) {
                curToken += ch
            } else if (curToken.isNotEmpty()) {
                return line.substring(0, curToken.length)
            } else {
                return null
            }
            offset++
        }
        if (curToken.isNotEmpty())
            return line.substring(0, curToken.length)
        return null
    }

    override fun getLastToken(line: String): String? {
        var curToken = ""
        var offset = line.length
        for (ch in line.reversed()) {
            if (ch == '_' || ch.isLetter() || ch.isDigit()) {
                curToken = ch + curToken
            } else if (curToken.isNotEmpty()) {
                return line.substring(offset, line.length)
            } else {
                return null
            }
            offset--
        }
        if (curToken.isNotEmpty())
            return line.substring(offset, line.length)
        return null
    }

    override fun createTemplate(variant: String): TemplateImpl? {
        val pair = prepareDataForTemplate(variant)
        if (pair.second.isEmpty()) {
            return null
        }

        val template = TemplateImpl("fakeKey", pair.first, "ml server completion")

        pair.second.forEach { variable -> template.addVariable(TextExpression(variable), true) }
        template.parseSegments()
        template.addEndVariable()
        return template
    }

    override fun getMissingBraces(line: String): List<Char>? {
        val stack = Stack<Char>()
        for (c in line.toCharArray()) {

            if (OPENERS.contains(c)) {
                stack.push(c)
            } else if (CLOSE_TO_OPEN.containsKey(c)) {
                try {
                    val opener = stack.pop()
                    if (!CLOSE_TO_OPEN[c]?.equals(opener)!!) {
                        return null
                    }
                } catch (ignore: EmptyStackException) {
                    return null
                }
            }
        }

        if (!stack.isEmpty()) {
            return stack.reversed().map { OPEN_TO_CLOSE[it]!! }
        }
        return null
    }


    fun prepareDataForTemplate(variant: String): Pair<String, MutableList<String>> {
        var i = 0
        val variants = mutableListOf<String>()

        return variant.replace(Regex("(\"(?:[^\"\\\\]|\\\\.)*\")|('(?:[^'\\\\]|\\\\.)*')")) { match ->
            val value = match.value
            variants.add(value.substring(1, value.length - 1))
            value.first() + "\$__Variable${i++}\$" + value.last()
        } to variants
    }

    companion object {
        private var CLOSE_TO_OPEN = hashMapOf(
                ')' to '(',
                ']' to '[',
                '}' to '{'
                //,
                //'\'' to '\'',
                //'\"' to '\"'
        )

        private var OPEN_TO_CLOSE = hashMapOf(
                '(' to ')',
                '[' to ']',
                '{' to '}'
                //,
                //'\'' to '\'',
                //'\"' to '\"'
        )

        private var OPENERS = OPEN_TO_CLOSE.keys
    }
}

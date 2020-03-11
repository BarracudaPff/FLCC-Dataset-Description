package org.jetbrains.completion.full.line.language.supporters

import com.intellij.codeInsight.template.Template
import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.SyntaxTraverser
import com.jetbrains.python.codeInsight.imports.PythonImportUtils
import com.jetbrains.python.psi.PyElement
import com.jetbrains.python.psi.PyNumericLiteralExpression
import com.jetbrains.python.psi.PyPlainStringElement
import com.jetbrains.python.psi.PyStringElement
import org.jetbrains.completion.full.line.language.LanguageMLSupporter
import org.jetbrains.completion.full.line.length
import org.jetbrains.completion.full.line.toIntRangeWithOffsets
import java.util.*
import kotlin.math.abs

class PythonSupporter : LanguageMLSupporter {
    override fun autoImportFix(element: PsiElement, from: Int, to: Int) {
        SyntaxTraverser.psiTraverser()
                .withRoot(element)
                .onRange(TextRange(from, to))
                .filter { it.parent.reference != null && it.parent is PyElement }
                .forEach {
                    PythonImportUtils.proposeImportFix(it.parent as PyElement, it.parent.reference)?.applyFix()
                }
    }

    override fun isComment(element: PsiElement): Boolean {
        return (element is PsiComment || (element is PyPlainStringElement && element.isTripleQuoted))
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

    override fun createStringTemplate(element: PsiElement, from: Int, to: Int): Template? {
        var id = 0
        var content = element.text.substring(from, to)
        var contentOffset = 0

        return SyntaxTraverser.psiTraverser()
                .withRoot(element)
                .onRange(TextRange(from, to))
                .filter { it is PyStringElement }
                .asIterable()
                .map {
                    if (it is PyStringElement) {
                        var range = it.textRange.toIntRangeWithOffsets(contentOffset - from, it.quote.length)
                        val name = "\$__Variable${id++}\$"

                        contentOffset += name.length - range.length() - 1
                        if (range.last<0) {
                            range = IntRange(abs(range.last), abs(range.first))
                        }

                        content = content.replaceRange(range, name)
                        it.content
                    } else {
                        it as PyNumericLiteralExpression
                        ""
                    }
                }.let {
                    createTemplate(content, it)
                }
    }

    private fun createTemplate(content: String, variables: List<String>): Template? {
        return if (variables.isEmpty()) {
            null
        } else {
            TemplateImpl("fakeKey", content, "ml server completion").apply {
                variables.forEach { variable -> addVariable(TextExpression(variable), true) }
            }
        }
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

    companion object {
        private var CLOSE_TO_OPEN = hashMapOf(
                ')' to '(',
                ']' to '[',
                '}' to '{'
        )

        private var OPEN_TO_CLOSE = hashMapOf(
                '(' to ')',
                '[' to ']',
                '{' to '}'
        )

        private var OPENERS = OPEN_TO_CLOSE.keys
    }
}

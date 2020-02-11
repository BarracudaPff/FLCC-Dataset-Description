package org.jetbrains.completion.full.line.language

import com.intellij.codeInsight.template.impl.TemplateImpl
import com.intellij.codeInsight.template.impl.TextExpression
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.SyntaxTraverser
import com.jetbrains.python.codeInsight.imports.PythonImportUtils
import com.jetbrains.python.psi.PyElement
import java.util.*

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

    override fun createStringTemplate(element: PsiElement, from: Int, to: Int): TemplateImpl? {
//        val line = element.text.substring(IntRange(from, to))
////        val template = TemplateImpl("fakeKey", pair.first, "ml server completion")
//        println(line)
//        val a = SyntaxTraverser.psiTraverser()
//                .withRoot(element)
//                .onRange(TextRange(from, to))
//                .filter { it.firstChild == null && !it.text.isBlank() && it is PyStringElement }
//                .forEach {
//                    it is PyStringElement
//                    println(it.textRangeInParent.toString() + " " + (it is PyStringElement))
//                }
////                .map {
////
//////                    println(it.content)
//////                    val range = it.contentRange
//////                    template.addVariable(TextExpression(variable), true)
////                    it
////                }

        println("!!")

        return null
    }

    override fun createStringTemplate(variant: String): TemplateImpl? {
        val pair = prepareDataForTemplate(variant) ?: return null
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


    fun prepareDataForTemplate(variant: String): Pair<String, MutableList<String>>? {
        var i = 0
        val variants = mutableListOf<String>()

        val a = variant.findAnyOf(listOf("'''", "\"\"\""))
        if (a != null) {
            println("no")
            return null
        } else {
            println(a)
        }

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
        )

        private var OPEN_TO_CLOSE = hashMapOf(
                '(' to ')',
                '[' to ']',
                '{' to '}'
        )

        private var OPENERS = OPEN_TO_CLOSE.keys
    }
}


//package org.jetbrains.completion.full.line.language
//
//import com.intellij.codeInsight.template.impl.TemplateImpl
//import com.intellij.codeInsight.template.impl.TextExpression
//import com.intellij.openapi.util.TextRange
//import com.intellij.psi.PsiElement
//import com.intellij.psi.PsiFile
//import com.intellij.psi.SyntaxTraverser
//import com.jetbrains.python.psi.PyPlainStringElement
//import com.jetbrains.python.psi.PyStringElement
//import java.util.*
//import kotlin.math.max
//
//class PythonSupporter : LanguageMLSupporter {
//    override fun isComment(element: PsiElement): Boolean {
//        return (super.isComment(element) || element is PyPlainStringElement)
//    }
//
//    override fun getFirstToken(line: String): String? {
//        var curToken = ""
//        var offset = 0
//        for (ch in line) {
//            if (ch == '_' || ch.isLetter() || ch.isDigit()) {
//                curToken += ch
//            } else if (curToken.isNotEmpty()) {
//                return line.substring(0, curToken.length)
//            } else {
//                return null
//            }
//            offset++
//        }
//        if (curToken.isNotEmpty())
//            return line.substring(0, curToken.length)
//        return null
//    }
//
//    override fun getFirstToken(file: PsiElement, offset: Int): String {
//        val element = getTraverser(file, offset)?.last() ?: return ""
//        return fixToken(element, offset)
//    }
//
//    override fun getLastToken(file: PsiElement, offset: Int): String {
//        val element = getTraverser(file, offset)?.first() ?: return ""
//        return fixToken(element, offset)
//    }
//
//    override fun createStringTemplate(variant: String): TemplateImpl? {
//        val pair = prepareDataForTemplate(variant)
//        if (pair.second.isEmpty()) {
//            return null
//        }
//
//        val template = TemplateImpl("fakeKey", pair.first, "ml server completion")
//
//        pair.second.forEach { variable -> template.addVariable(TextExpression(variable), true) }
//        template.parseSegments()
//        template.addEndVariable()
//        return template
//    }
//
//    override fun getMissingBraces(line: String): List<Char>? {
//        val stack = Stack<Char>()
//        for (c in line.toCharArray()) {
//
//            if (OPENERS.contains(c)) {
//                stack.push(c)
//            } else if (CLOSE_TO_OPEN.containsKey(c)) {
//                try {
//                    val opener = stack.pop()
//                    if (!CLOSE_TO_OPEN[c]?.equals(opener)!!) {
//                        return null
//                    }
//                } catch (ignore: EmptyStackException) {
//                    return null
//                }
//            }
//        }
//
//        if (!stack.isEmpty()) {
//            return stack.reversed().map { OPEN_TO_CLOSE[it]!! }
//        }
//        return null
//    }
//
//    fun prepareDataForTemplate(variant: String): Pair<String, MutableList<String>> {
//        var i = 0
//        val variants = mutableListOf<String>()
//
//        return variant.replace(Regex("(\"(?:[^\"\\\\]|\\\\.)*\")|('(?:[^'\\\\]|\\\\.)*')")) { match ->
//            val value = match.value
//            variants.add(value.substring(1, value.length - 1))
//            value.first() + "\$__Variable${i++}\$" + value.last()
//        } to variants
//    }
//
//    private fun getTraverser(file: PsiElement, offset: Int): SyntaxTraverser<PsiElement>? {
//        val element = SyntaxTraverser.psiTraverser()
//                .withRoot(file)
//                .onRange(TextRange(0, max(0, offset - 1)))
//                .filter { it.firstChild == null && !it.text.isBlank() }
//
//        println("===")
//        element.forEach { println(it.text) }
//        println("===")
//
//        return if (!element.iterator().hasNext()) {
//            null
//        } else {
//            element
//        }
//    }
//
//    //0, startOffsetInParent, offset
//    private fun fixToken(element: PsiElement, offset: Int): String {
//        val token = getTextFromPsiElement(element)
//        println("token: $token")
//        println("offset: $offset")
//        println("element.startOffsetInParent: ${element.textOffset}")
//        println("ans: ${offset - element.textOffset}")
//        return if (token.isNotEmpty()) {
//            if (token.last().isCorrectLetterOrDigit()) {
//                token.substring(0,offset - element.textOffset)
//            } else {
//                ""
//            }
//        } else {
//            ""
//        }
//    }
//
//    private fun getTextFromPsiElement(element: PsiElement): String {
//        return if (element is PyStringElement) {
//            println("STRING")
//            element.content
//            element.
//        } else {
//            element.text
//        }
//    }
//
//    companion object {
//        private var CLOSE_TO_OPEN = hashMapOf(
//                ')' to '(',
//                ']' to '[',
//                '}' to '{'
//        )
//
//        private var OPEN_TO_CLOSE = hashMapOf(
//                '(' to ')',
//                '[' to ']',
//                '{' to '}'
//        )
//
//        private var OPENERS = OPEN_TO_CLOSE.keys
//    }
//}
//
//private fun Char.isCorrectLetterOrDigit(): Boolean = this == '_' || isLetterOrDigit()

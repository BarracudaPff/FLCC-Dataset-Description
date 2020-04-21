package org.jetbrains.completion.full.line.language.formatters.python

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.util.containers.ContainerUtil
import com.jetbrains.python.psi.PyNamedParameter
import com.jetbrains.python.psi.PyParameterList
import com.jetbrains.python.psi.PyStringLiteralUtil
import com.jetbrains.python.psi.types.PyCallableParameter
import com.jetbrains.python.psi.types.PyCallableParameterImpl
import org.jetbrains.completion.full.line.language.ElementFormatter

class ParameterListFormatter : ElementFormatter {
    override fun condition(element: PsiElement): Boolean = element is PyParameterList

    override fun filter(element: PsiElement): Boolean? = element is PyParameterList

    override fun format(element: PsiElement): String {
        element as PyParameterList
        val closing = if (element.text.last() == ')') ")" else ""
        val params = ContainerUtil.map(element.parameters, PyCallableParameterImpl::psi)
        return "(" + a(params) + closing
    }

    fun a(params: List<PyCallableParameter>): String {
        return params.joinToString(separator = ", ") {
            if (it.hasDefaultValue()) {
                it.getPresentableText(false) + ((it.parameter as PyNamedParameter).annotation?.text
                        ?: "") + includeDefaultValue(it.defaultValueText!!)
            } else {
                it.getPresentableText(true) + ((it.parameter as PyNamedParameter).annotation?.text ?: "")
            }
        }
    }

    fun includeDefaultValue(defaultValue: String): String {
        val sb = StringBuilder()
        val quotes = PyStringLiteralUtil.getQuotes(defaultValue)

        sb.append("=")
        if (quotes != null) {
            val value: String = defaultValue.substring(quotes.getFirst().length, defaultValue.length - quotes.getSecond().length)
            sb.append(quotes.getFirst())
            StringUtil.escapeStringCharacters(value.length, value, sb)
            sb.append(quotes.getSecond())
        } else {
            sb.append(defaultValue)
        }

        return sb.toString()
    }
}

package org.jetbrains.completion.full.line.language

class PythonSupporter : LanguageMLSupporter {
    override fun check(variant: String): Boolean {
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
}

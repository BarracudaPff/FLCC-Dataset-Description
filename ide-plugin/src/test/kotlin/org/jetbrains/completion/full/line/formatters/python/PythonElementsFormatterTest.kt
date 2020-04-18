package org.jetbrains.completion.full.line.formatters.python

import org.junit.jupiter.api.Test


class PythonElementsFormatterTest : PythonCodeFormatterTest() {
    @Test
    fun `format numerical`() {
        testFile("test-elements/numerical.py")
    }

    @Test
    fun `format strings`() {
        testFile("test-elements/strings.py")
    }

    @Test
    fun `format imports`() {
        testFile("test-elements/imports.py")
    }
}

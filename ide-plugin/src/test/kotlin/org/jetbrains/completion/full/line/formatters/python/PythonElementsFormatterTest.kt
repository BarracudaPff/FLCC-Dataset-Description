package org.jetbrains.completion.full.line.formatters.python

import org.junit.jupiter.api.Test


class PythonElementsFormatterTest : PythonCodeFormatterTest() {
    @Test
    fun numerical() {
        testFile("test-elements/numerical.py")
    }

    @Test
    fun `parameter list without closed bracket`() {
        testCodeFragment("def nginx(_config ","def nginx(_config ", 18)
    }

    @Test
    fun `function params without type`() {
        testCodeFragment("def checkout_branch(branch):\n    return None", "def checkout_branch(branch):\n\treturn None")
    }

    @Test
    fun `function params with type`() {
        testCodeFragment("def checkout_branch(branch: str):\n    return None", "def checkout_branch(branch: str):\n\treturn None")
    }

    @Test
    fun `function params with type and default value`() {
        testCodeFragment("def checkout_branch(branch: str=\"master\"):\n    return None", "def checkout_branch(branch: str=\"master\"):\n\treturn None")
    }

    @Test
    fun `not fully filled import`() {
        testCodeFragment("import ", "import ")
    }

    @Test
    fun `not fully filled from import`() {
        testCodeFragment("from tqdm import ", "from tqdm import ")
    }

    @Test
    fun `inside not fully filled from import`() {
        testCodeFragment("from  import tqdm", "from ", 5)
    }

    @Test
    fun strings() {
        testFile("test-elements/strings.py")
    }

    @Test
    fun imports() {
        testFile("test-elements/imports.py")
    }
}

package org.jetbrains.completion.full.line.formatters.python

import com.intellij.openapi.util.TextRange
import com.jetbrains.python.PythonFileType
import org.jetbrains.completion.full.line.FilesTest.Companion.TEMP
import org.jetbrains.completion.full.line.formatters.CodeFormatterTest
import org.jetbrains.completion.full.line.language.formatters.PythonCodeFormatter
import org.junit.Assert
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.test.assertFalse

open class PythonCodeFormatterTest : CodeFormatterTest(PythonCodeFormatter()) {

    protected fun testFile(filename: String, trimEnd: Boolean = true) {
        invokeTestRunnable(Runnable {
            val file = fixture.configureByText(PythonFileType.INSTANCE, readFile(filename))

            val ideaContent = formatter.format(file, TextRange(0, file.textLength))
            val blackContent = formatFile(filename).replace(Regex("[\\n]+"), "\n")

            if (trimEnd) {
                Assert.assertEquals(blackContent.trimEnd(), ideaContent.trimEnd())
            } else {
                Assert.assertEquals(blackContent, ideaContent)
            }
        })
    }

    private fun formatFileWithCommand(formatterCommand: String, filename: String? = null) {
        val file = if (filename != null) {
            createTempFile(filename)
        } else {
            fullPath(TEMP)
        }

        val process = Runtime.getRuntime().exec(formatterCommand + file)
        val error = readerContent(BufferedReader(InputStreamReader(process.errorStream)))

        assertFalse(error) { error.startsWith("error: ") }
    }

    private fun formatFile(filename: String): String {
        formatFileWithCommand("python3 src/test/resources/black.py --line-length 5000 ", filename)
        formatFileWithCommand("python3 src/test/resources/comments.py ")
        formatFileWithCommand("python3 src/test/resources/black.py --use-tabs --line-length 5000 ")
        return readTempFile()
    }
}

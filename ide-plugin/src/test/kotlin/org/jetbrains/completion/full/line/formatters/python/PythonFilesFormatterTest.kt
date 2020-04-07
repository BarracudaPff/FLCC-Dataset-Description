package org.jetbrains.completion.full.line.formatters.python

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


class PythonFilesFormatterTest : PythonCodeFormatterTest() {
    @ParameterizedTest(name = "formatting file {index}")
    @MethodSource("data")
    fun `format data`(filename: String) {
        testFile(filename)
    }


    companion object {

        @Suppress("unused")
        @JvmStatic
        private fun data(): Stream<Arguments> =
                Stream.of(
                        Arguments.of("test-files/data1.py"),
                        Arguments.of("test-files/data2.py"),
                        Arguments.of("test-files/data3.py"),
                        Arguments.of("test-files/data4.py"),
                        Arguments.of("test-files/data5.py"),
                        Arguments.of("test-files/fp16util.py")
                )
    }
}

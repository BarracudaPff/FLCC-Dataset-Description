package org.jetbrains.completion.full.line

import com.intellij.lang.Language
import com.intellij.openapi.util.TextRange
import com.intellij.testFramework.LightPlatformTestCase.initApplication
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import com.intellij.testFramework.runInEdtAndWait
import com.jetbrains.python.PythonFileType
import org.jetbrains.completion.full.line.language.CodeFormatter
import org.junit.Assert
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.BufferedReader
import java.io.FileReader
import java.io.InputStreamReader
import java.util.stream.Stream


class PythonFormatterTest {
    private val blackPath = "python3 src/test/resources/black.py --diff --use-tabs --line-length 5000"
    private val formatter = CodeFormatter.getInstance(Language.findLanguageByID("Python")!!)!!

    @ParameterizedTest
    @MethodSource("data")
    fun `format data`(filename: String) {
        invokeTestRunnable(Runnable {
            val file = myFixture.configureByText(PythonFileType.INSTANCE, readFile(filename))

            val ideaContent = formatter.format(file, TextRange(0, file.textLength))
            val blackContent = getBlackOutput(filename)

            Assert.assertEquals(ideaContent.trimEnd(), blackContent.trimEnd())
        })
    }

    private fun invokeTestRunnable(runnable: Runnable) {
        if (runInDispatchThread()) {
            runInEdtAndWait {
                runnable.run()
            }
        } else {
            runnable.run()
        }
    }

    private fun runInDispatchThread(): Boolean {
        return IdeaTestExecutionPolicy.current()?.runInDispatchThread() ?: true
    }


    private fun getBlackOutput(filename: String): String {
        val file = fullPath(filename)
        val pr = Runtime.getRuntime().exec("$blackPath  $file")
        val reader = BufferedReader(InputStreamReader(pr.inputStream))

        return readFile(reader)
    }

    private fun readFile(reader: BufferedReader): String {
        return StringBuilder().apply {
            reader.lineSequence().forEach {
                append(it).append('\n')
            }
        }.toString()
    }

    private fun readFile(filename: String): String {
        val file = fullPath(filename)

        return readFile(BufferedReader(FileReader(file)))
    }

    private fun fullPath(filename: String): String {
        return javaClass.classLoader.getResource(filename)!!.path
    }


    companion object {
        lateinit var myFixture: CodeInsightTestFixture

        @JvmStatic
        private fun data(): Stream<Arguments> =
                Stream.of(
                        Arguments.of("data1.py"),
                        Arguments.of("data2.py")
                )


        @BeforeAll
        @JvmStatic
        fun setUp() {
            initApplication()
            val fixture = IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder().fixture
            myFixture = IdeaTestFixtureFactory.getFixtureFactory()
                    .createCodeInsightFixture(fixture, LightTempDirTestFixtureImpl())
            myFixture.setUp()
        }

        @AfterAll
        @JvmStatic
        fun tearDown() {
        }

    }
}

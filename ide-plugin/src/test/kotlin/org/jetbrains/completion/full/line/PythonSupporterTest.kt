package org.jetbrains.completion.full.line

import org.jetbrains.completion.full.line.language.PythonSupporter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream


class PythonSupporterTest {

    @ParameterizedTest
    @MethodSource("firstTokenData")
    fun `test first token`(line: String, expectedToken: String?) {
        val supporter = PythonSupporter()
        assertEquals(expectedToken, supporter.getFirstToken(line))
    }

//    @ParameterizedTest
//    @MethodSource("lastTokenData")
//    fun `test last token`(line: String, expectedToken: String?) {
//        val supporter = PythonSupporter()
//        assertEquals(expectedToken, supporter.getLastToken(line))
//    }

    @ParameterizedTest
    @MethodSource("getMatchesData")
    fun `test quotes`(line: String, expectedLine: String?, values: List<String>) {
        val supporter = PythonSupporter()
        val matches =  supporter.prepareDataForTemplate(line)
        assertEquals(expectedLine, matches.first)
        assertEquals(values, matches.second)
    }

//    @ParameterizedTest
//    @MethodSource("lastTokenData")
//    fun `test missing braces`(line: String, expectedToken: String?) {
//        val supporter = PythonSupporter()
//        assertEquals(expectedToken, supporter.getLastToken(line))
//    }

    @Suppress("unused")
    companion object {
        @JvmStatic
        private fun getMatchesData(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of("with open(\"hi.py\") as f:",
                            "with open(\"\$__Variable0\$\") as f:",
                            listOf("hi.py")),

                    Arguments.of("with open(\"'hi.py\") as f:",
                            "with open(\"\$__Variable0\$\") as f:",
                            listOf("'hi.py")),

                    Arguments.of("with open(\"'hi.py'\") as f:",
                            "with open(\"\$__Variable0\$\") as f:",
                            listOf("'hi.py'")),

                    Arguments.of("with open(\"\\\"hi.py\") as f:",
                            "with open(\"\$__Variable0\$\") as f:",
                            listOf("\\\"hi.py")),

                    Arguments.of("with open(\"\\\"hi.py\\\"\") as f:",
                            "with open(\"\$__Variable0\$\") as f:",
                            listOf("\\\"hi.py\\\"")),

                    Arguments.of("with open('hi.py') as f:",
                            "with open('\$__Variable0\$') as f:",
                            listOf("hi.py")),

                    Arguments.of("with open('\"hi.py') as f:",
                            "with open('\$__Variable0\$') as f:",
                            listOf("\"hi.py")),

                    Arguments.of("with open('\"hi.py\"') as f:",
                            "with open('\$__Variable0\$') as f:",
                            listOf("\"hi.py\"")),

                    Arguments.of("with open('\\'hi.py') as f:",
                            "with open('\$__Variable0\$') as f:",
                            listOf("\\'hi.py")),

                    Arguments.of("with open('\\'hi.py\\'') as f:",
                            "with open('\$__Variable0\$') as f:",
                            listOf("\\'hi.py\\'")),

                    Arguments.of("with open('hi.py', 'hello.py') as f:",
                            "with open('\$__Variable0\$', '\$__Variable1\$') as f:",
                            listOf("hi.py", "hello.py")),

                    Arguments.of("with open('hi.py', 'hello.py', 'hey.py') as f:",
                            "with open('\$__Variable0\$', '\$__Variable1\$', '\$__Variable2\$') as f:",
                            listOf("hi.py", "hello.py", "hey.py")),

                    Arguments.of("with open('hi.py', \"hello.py\", 'hey.py') as f:",
                            "with open('\$__Variable0\$', \"\$__Variable1\$\", '\$__Variable2\$') as f:",
                            listOf("hi.py", "hello.py", "hey.py")),

                    Arguments.of("with open('hi.py', \"hello.py\", 'hey.py\") as f:",
                            "with open('\$__Variable0\$', \"\$__Variable1\$\", 'hey.py\") as f:",
                            listOf("hi.py", "hello.py"))
            )
        }

        @JvmStatic
        private fun firstTokenData(): Stream<Arguments> =
                Stream.of(
                        Arguments.of("app.register_blueprint(api, url_prefix='/v1')", "app"),
                        Arguments.of(".register_blueprint(api, url_prefix='/v1')", null),
                        Arguments.of("register_blueprint(api, url_prefix='/v1')", "register_blueprint"),
                        Arguments.of("_blueprint(api, url_prefix='/v1')", "_blueprint"),
                        Arguments.of("(api, url_prefix='/v1')", null),
                        Arguments.of("api, url_prefix='/v1')", "api"),
                        Arguments.of(", url_prefix='/v1')", null),
                        Arguments.of("url_prefix='/v1')", "url_prefix"),
                        Arguments.of("='/v1')", null),
                        Arguments.of("'/v1')", null),
                        Arguments.of("/v1')", null),
                        Arguments.of("v1')", "v1"),
                        Arguments.of("1')", "1"),
                        Arguments.of("')", null),
                        Arguments.of("", null)
                )

        @JvmStatic
        private fun lastTokenData(): Stream<Arguments> =
                Stream.of(
                        Arguments.of("app.register_blueprint(api, url_prefix='/v1')", null),
                        Arguments.of("app.register_blueprint(api, url_prefix='/v1", "v1"),
                        Arguments.of("app.register_blueprint(api, url_prefix='/", null),
                        Arguments.of("app.register_blueprint(api, url_prefix='", null),
                        Arguments.of("app.register_blueprint(api, url_prefix=", null),
                        Arguments.of("app.register_blueprint(api, url_prefix", "url_prefix"),
                        Arguments.of("app.register_blueprint(api, ", null),
                        Arguments.of("app.register_blueprint(api,", null),
                        Arguments.of("app.register_blueprint(api", "api"),
                        Arguments.of("app.register_blueprint(", null),
                        Arguments.of("app.register_blueprint", "register_blueprint"),
                        Arguments.of("app.register_", "register_"),
                        Arguments.of("app.", null),
                        Arguments.of("app", "app"),
                        Arguments.of("", null)
                )
    }
}

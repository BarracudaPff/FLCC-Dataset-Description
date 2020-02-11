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


    @Suppress("unused")
    companion object {
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
    }
}

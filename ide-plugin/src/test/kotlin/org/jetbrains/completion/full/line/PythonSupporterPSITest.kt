//package org.jetbrains.completion.full.line
//
//import com.intellij.testFramework.LightPlatformTestCase.initApplication
//import com.intellij.testFramework.fixtures.CodeInsightTestFixture
//import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
//import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
//import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
//import com.intellij.testFramework.runInEdtAndWait
//import com.jetbrains.python.PythonFileType
//import com.jetbrains.python.sdk.name
//import junit.framework.TestCase.assertEquals
//import org.jetbrains.completion.full.line.language.PythonSupporter
//import org.junit.jupiter.api.AfterAll
//import org.junit.jupiter.api.BeforeAll
//import org.junit.jupiter.params.ParameterizedTest
//import org.junit.jupiter.params.provider.Arguments
//import org.junit.jupiter.params.provider.MethodSource
//import java.util.stream.Stream
//
//class PythonSupporterPSITest {
//    var supporter = PythonSupporter()
//    private val line = "app.register_blueprint(api, url_prefix='/v1')"
//
//    @ParameterizedTest
//    @MethodSource("prefixData")
//    fun `first token`(text: String, expectedToken: String?) {
//        invokeTestRunnable(Runnable {
//            println(text)
//            val file = myFixture.configureByText(PythonFileType.INSTANCE, text)
//
//            assertEquals(expectedToken ?: "", supporter.getFirstToken(file, text.length))
//        })
//    }
//
//    @ParameterizedTest
//    @MethodSource("prefixData")
//    fun `first token for full line`(text: String, expectedToken: String?) {
//        invokeTestRunnable(Runnable {
//            println(text)
//            val file = myFixture.configureByText(PythonFileType.INSTANCE, line)
//
//            assertEquals(expectedToken ?: "", supporter.getFirstToken(file, text.length))
//        })
//    }
//
////    @ParameterizedTest
////    @MethodSource("postfixData")
////    fun b(text: String, expectedToken: String?) {
////        invokeTestRunnable(Runnable {
////            val file = myFixture.configureByText(PythonFileType.INSTANCE, text)
////
////            assertEquals(expectedToken ?: "", supporter.getLastToken(file, text.length))
////        })
////    }
//
//    private fun invokeTestRunnable(runnable: Runnable) {
//        if (runInDispatchThread()) {
//            runInEdtAndWait {
//                runnable.run()
//            }
//        } else {
//            runnable.run()
//        }
//    }
//
//    private fun runInDispatchThread(): Boolean {
//        return IdeaTestExecutionPolicy.current()?.runInDispatchThread() ?: true
//    }
//
//    companion object {
//        lateinit var myFixture: CodeInsightTestFixture
//
//        @JvmStatic
//        private fun prefixData(): Stream<Arguments> =
//                Stream.of(
//                        Arguments.of("app.register_blueprint(api, url_prefix='/v1')", null),
//                        Arguments.of("app.register_blueprint(api, url_prefix='/v1", "/v1"),
//                        Arguments.of("app.register_blueprint(api, url_prefix='/", null),
//                        Arguments.of("app.register_blueprint(api, url_prefix='", null),
//                        Arguments.of("app.register_blueprint(api, url_prefix=", null),
//                        Arguments.of("app.register_blueprint(api, url_prefix", "url_prefix"),
//                        Arguments.of("app.register_blueprint(api, ", null),
//                        Arguments.of("app.register_blueprint(api,", null),
//                        Arguments.of("app.register_blueprint(api", "api"),
//                        Arguments.of("app.register_blueprint(", null),
//                        Arguments.of("app.register_blueprint", "register_blueprint"),
//                        Arguments.of("app.register_", "register_"),
//                        Arguments.of("app.", null),
//                        Arguments.of("app", "app"),
//                        Arguments.of("", null)
//                )
//
//        @JvmStatic
//        private fun postfixData(): Stream<Arguments> =
//                Stream.of(
//                        Arguments.of("app.register_blueprint(api, url_prefix='/v1')", "app"),
//                        Arguments.of(".register_blueprint(api, url_prefix='/v1')", null),
//                        Arguments.of("register_blueprint(api, url_prefix='/v1')", "register_blueprint"),
//                        Arguments.of("_blueprint(api, url_prefix='/v1')", "_blueprint"),
//                        Arguments.of("(api, url_prefix='/v1')", null),
//                        Arguments.of("api, url_prefix='/v1')", "api"),
//                        Arguments.of(", url_prefix='/v1')", null),
//                        Arguments.of("url_prefix='/v1')", "url_prefix"),
//                        Arguments.of("='/v1')", null),
//                        Arguments.of("'/v1')", null),
//                        Arguments.of("/v1')", null),
//                        Arguments.of("v1')", "v1"),
//                        Arguments.of("1')", "1"),
//                        Arguments.of("')", null),
//                        Arguments.of("", null)
//                )
//
//
//        @BeforeAll
//        @JvmStatic
//        fun setUp() {
//            initApplication()
//            val fixture = IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder().fixture
//            myFixture = IdeaTestFixtureFactory.getFixtureFactory()
//                    .createCodeInsightFixture(fixture, LightTempDirTestFixtureImpl())
//            myFixture.setUp()
//        }
//
//        @AfterAll
//        @JvmStatic
//        fun tearDown() {
//            println("fsgd")
//        }
//
//    }
//}

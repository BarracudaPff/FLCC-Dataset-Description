package org.jetbrains.completion.full.line

import com.intellij.testFramework.LightPlatformTestCase
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.IdeaTestExecutionPolicy
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import com.intellij.testFramework.fixtures.impl.LightTempDirTestFixtureImpl
import com.intellij.testFramework.runInEdtAndWait
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class IdeaTest {
    lateinit var fixture: CodeInsightTestFixture

    fun invokeTestRunnable(runnable: Runnable) {
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

    @BeforeAll
    fun setUp() {
        LightPlatformTestCase.initApplication()
        val lightFixture = IdeaTestFixtureFactory.getFixtureFactory().createLightFixtureBuilder().fixture
        fixture = IdeaTestFixtureFactory.getFixtureFactory()
                .createCodeInsightFixture(lightFixture, LightTempDirTestFixtureImpl())
        fixture.setUp()
    }

    @AfterAll
    fun tearDown() {
        fixture.tearDown()
    }
}

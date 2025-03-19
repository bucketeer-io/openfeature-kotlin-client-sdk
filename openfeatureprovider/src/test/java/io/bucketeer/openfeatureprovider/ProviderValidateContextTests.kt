package io.bucketeer.openfeatureprovider

import android.app.Activity
import dev.openfeature.sdk.EvaluationContext
import dev.openfeature.sdk.ImmutableContext
import dev.openfeature.sdk.Value
import dev.openfeature.sdk.events.OpenFeatureEvents
import io.bucketeer.openfeatureprovider.mock.MockBKTClientResolver
import io.bucketeer.openfeatureprovider.mock.MockBKTClientResolverFactory
import io.bucketeer.sdk.android.BKTConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ProviderValidateContextTests {
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity
    private val mockBKTClientResolver: MockBKTClientResolver = MockBKTClientResolver()
    private val mockBKTClientResolverFactory: MockBKTClientResolverFactory =
        MockBKTClientResolverFactory(mockBKTClientResolver)
    private lateinit var config: BKTConfig
    private lateinit var provider: BucketeerProvider
    private lateinit var initContext: EvaluationContext
    private val testScope = TestScope()

    @Before
    fun setUp() {
        activityController = Robolectric.buildActivity(Activity::class.java).setup()
        activity = activityController.get()
        config =
            BKTConfig
                .builder()
                .apiEndpoint("https://api.bucketeer.io")
                .apiKey("api_key_value")
                .featureTag("feature_tag_value")
                .appVersion("1.2.3")
                .build()
        provider = BucketeerProvider(mockBKTClientResolverFactory, activity, config, testScope)
        initContext =
            ImmutableContext(
                targetingKey = "user1",
                attributes = mapOf("attr1" to Value.String("value1")),
            )
    }

    @After
    fun tearDown() {
    }

    private suspend fun requiredInitSuccess() {
        val evaluationContext = initContext
        val eventDeferred =
            testScope.async {
                provider.observe().take(1).first()
            }

        provider.initialize(evaluationContext)
        val expectedEvent = eventDeferred.await()
        assertTrue(expectedEvent is OpenFeatureEvents.ProviderReady)
    }

    @Test
    fun onNewContextIsInvalidMissingTargetingKey() =
        testScope.runTest(timeout = 1.minutes) {
            requiredInitSuccess()
            val evaluationContext =
                ImmutableContext(
                    targetingKey = "",
                    attributes = mapOf("attr1" to Value.String("value1")),
                )
            val eventDeferred =
                async {
                    provider.observe().take(1).first()
                }

            provider.onContextSet(initContext, evaluationContext)
            val expectedEvent = eventDeferred.await()

            assertTrue(expectedEvent is OpenFeatureEvents.ProviderError)
            assertEquals(
                "missing targeting key",
                (expectedEvent as OpenFeatureEvents.ProviderError).error.message,
            )

            advanceUntilIdle()
        }

    @Test
    fun onNewContextIsChangeUserIdShouldFail() =
        testScope.runTest(timeout = 1.minutes) {
            requiredInitSuccess()
            val evaluationContext =
                ImmutableContext(
                    targetingKey = "1",
                    attributes = mapOf("attr1" to Value.String("value1")),
                )
            val eventDeferred =
                async {
                    provider.observe().take(1).first()
                }

            provider.onContextSet(initContext, evaluationContext)
            val expectedEvent = eventDeferred.await()

            assertTrue(expectedEvent is OpenFeatureEvents.ProviderError)
            assertEquals(
                "Changing the targeting_id after initialization is not supported, please reinitialize the provider",
                (expectedEvent as OpenFeatureEvents.ProviderError).error.message,
            )

            advanceUntilIdle()
        }

    @Test
    fun onNewContextChangeAttributesShouldSuccess() =
        testScope.runTest {
            requiredInitSuccess()
            val evaluationContext =
                ImmutableContext(
                    targetingKey = "user1",
                    attributes =
                        mapOf(
                            "attr1" to Value.String("value_test"),
                            "attr12" to Value.Double(3.2),
                        ),
                )

            provider.onContextSet(initContext, evaluationContext)

            val userAttributes = mockBKTClientResolver.userAttributes
            val expectedUserAttributes = evaluationContext.toBKTUser().attributes
            assertEquals(userAttributes, expectedUserAttributes)

            val status = provider.getProviderStatus()
            assertTrue(status is OpenFeatureEvents.ProviderReady)

            advanceUntilIdle()
        }
}

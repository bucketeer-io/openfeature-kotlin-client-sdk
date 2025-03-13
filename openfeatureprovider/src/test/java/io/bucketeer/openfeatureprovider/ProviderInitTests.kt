package io.bucketeer.openfeatureprovider

import android.app.Activity
import dev.openfeature.sdk.ImmutableContext
import dev.openfeature.sdk.Value
import dev.openfeature.sdk.events.OpenFeatureEvents
import dev.openfeature.sdk.exceptions.OpenFeatureError
import io.bucketeer.openfeatureprovider.mock.MockBKTClientResolver
import io.bucketeer.openfeatureprovider.mock.MockBKTClientResolverFactory
import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.BKTException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ProviderInitTests {
    private lateinit var activityController: ActivityController<Activity>
    private lateinit var activity: Activity
    private val mockBKTClientResolver: MockBKTClientResolver = MockBKTClientResolver()
    private val mockBKTClientResolverFactory: MockBKTClientResolverFactory =
        MockBKTClientResolverFactory(mockBKTClientResolver)
    private lateinit var config: BKTConfig

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
    }

    @After
    fun tearDown() {
    }

    @Test
    fun initializeFailWithNilContext() = runTest(timeout = 500.milliseconds) {
        val provider = BucketeerProvider(mockBKTClientResolverFactory, activity, config, this)

        val eventDeferred = async {
            provider.observe().take(1).first()
        }

        provider.initialize(null)
        val expectedEvent = eventDeferred.await()

        advanceUntilIdle()

        assertTrue(expectedEvent is OpenFeatureEvents.ProviderError)
        assertEquals("missing targeting key", (expectedEvent as OpenFeatureEvents.ProviderError).error.message)
    }

    @Test
    fun initializeFailMissingTargetingKey() = runTest(timeout = 500.milliseconds) {
        val provider = BucketeerProvider(mockBKTClientResolverFactory, activity, config, this)
        val evaluationContext =
            ImmutableContext(
                attributes = mapOf("attr1" to Value.String("value1")),
            )
        val eventDeferred = async {
            provider.observe().take(1).first()
        }

        provider.initialize(null)
        val expectedEvent = eventDeferred.await()

        advanceUntilIdle()

        assertTrue(expectedEvent is OpenFeatureEvents.ProviderError)
        assertEquals("missing targeting key", (expectedEvent as OpenFeatureEvents.ProviderError).error.message)
    }

    @Test
    fun initializeFailWithError() = runTest(timeout = 500.milliseconds) {
        mockBKTClientResolverFactory.onInitializeError = BKTException.ForbiddenException("ForbiddenException")
        val provider = BucketeerProvider(mockBKTClientResolverFactory, activity, config, this)
        val evaluationContext =
            ImmutableContext(
                targetingKey = "user1",
                attributes = mapOf("attr1" to Value.String("value1")),
            )
        val eventDeferred = async {
            provider.observe().take(1).first()
        }

        provider.initialize(evaluationContext)
        val expectedEvent = eventDeferred.await()

        advanceUntilIdle()

        assertTrue(expectedEvent is OpenFeatureEvents.ProviderError)
        assertEquals("ForbiddenException", (expectedEvent as OpenFeatureEvents.ProviderError).error.message)
    }

    @Test
    fun notReadyProviderStatus() = runTest(timeout = 500.milliseconds) {
        val provider = BucketeerProvider(mockBKTClientResolverFactory, activity, config, this)
        try {
            provider.getDoubleEvaluation("feature_id", 0.0, null)
            fail("Provider should throw an exception")
        } catch (e: Exception) {
            assertTrue(e is OpenFeatureError.ProviderNotReadyError)
            assertEquals("BKTClientResolver is not initialized", e.message)
        }
    }

    @Test
    fun initializeSuccess () = runTest(timeout = 500.milliseconds) {
        val provider = BucketeerProvider(mockBKTClientResolverFactory, activity, config, this)
        val evaluationContext =
            ImmutableContext(
                targetingKey = "user1",
                attributes = mapOf("attr1" to Value.String("value1")),
            )
        val eventDeferred = async {
            provider.observe().take(1).first()
        }

        provider.initialize(evaluationContext)
        val expectedEvent = eventDeferred.await()

        advanceUntilIdle()

        assertTrue(expectedEvent is OpenFeatureEvents.ProviderReady)
    }

    @Test
    fun initializeSuccessButReceivedTimeoutError() = runTest(timeout = 500.milliseconds) {
        mockBKTClientResolverFactory.onInitializeError = BKTException.TimeoutException("TimeoutException", null, 1000L)
        val provider = BucketeerProvider(mockBKTClientResolverFactory, activity, config, this)
        val evaluationContext =
            ImmutableContext(
                targetingKey = "user1",
                attributes = mapOf("attr1" to Value.String("value1")),
            )
        val eventDeferred = async {
            provider.observe().take(1).first()
        }

        provider.initialize(evaluationContext)
        val expectedEvent = eventDeferred.await()

        advanceUntilIdle()

        assertTrue(expectedEvent is OpenFeatureEvents.ProviderReady)
    }
}

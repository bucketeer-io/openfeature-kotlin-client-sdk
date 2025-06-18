package io.bucketeer.openfeatureprovider

import android.app.Activity
import dev.openfeature.sdk.EvaluationContext
import dev.openfeature.sdk.EvaluationMetadata
import dev.openfeature.sdk.ImmutableContext
import dev.openfeature.sdk.Value
import dev.openfeature.sdk.events.OpenFeatureEvents
import io.bucketeer.openfeatureprovider.mock.MockBKTClientResolver
import io.bucketeer.openfeatureprovider.mock.MockBKTClientResolverFactory
import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.BKTEvaluationDetails
import io.bucketeer.sdk.android.BKTValue
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import java.util.Date

@RunWith(RobolectricTestRunner::class)
internal class ProviderGetEvaluationTests {
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
        provider =
            BucketeerProvider(
                mockBKTClientResolverFactory,
                activity,
                config,
                testScope,
                StandardTestDispatcher(testScope.testScheduler),
            )
        initContext =
            ImmutableContext(
                targetingKey = "user1",
                attributes = mapOf("attr1" to Value.String("value1")),
            )
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

    @After
    fun tearDown() {
    }

    @Test
    fun getStringEvaluationReturnsCorrectValue() =
        testScope.runTest {
            requiredInitSuccess()
            mockBKTClientResolver.mockStringValue =
                BKTEvaluationDetails(
                    featureId = "feature1",
                    featureVersion = 1,
                    userId = "user1",
                    variationId = "variation1",
                    variationName = "variation1",
                    variationValue = "value1",
                    reason = BKTEvaluationDetails.Reason.CLIENT,
                )

            val providerEvaluation = provider.getStringEvaluation("feature1", "default", null)
            assertTrue(providerEvaluation.value == "value1")
            assertTrue(providerEvaluation.variant == "variation1")
            assertTrue(providerEvaluation.reason == "CLIENT")
            assertTrue(providerEvaluation.errorCode == null)
            assertTrue(providerEvaluation.errorMessage == null)
            assertTrue(providerEvaluation.metadata == EvaluationMetadata.EMPTY)
        }

    @Test
    fun getBooleanEvaluationReturnsCorrectValue() =
        testScope.runTest {
            requiredInitSuccess()
            mockBKTClientResolver.mockBoolValue =
                BKTEvaluationDetails(
                    featureId = "feature1",
                    featureVersion = 1,
                    userId = "user1",
                    variationId = "variation1",
                    variationName = "variation1",
                    variationValue = true,
                    reason = BKTEvaluationDetails.Reason.DEFAULT,
                )

            val providerEvaluation = provider.getBooleanEvaluation("feature1", false, null)
            assertTrue(providerEvaluation.value)
            assertTrue(providerEvaluation.variant == "variation1")
            assertTrue(providerEvaluation.reason == "DEFAULT")
            assertTrue(providerEvaluation.errorCode == null)
            assertTrue(providerEvaluation.errorMessage == null)
            assertTrue(providerEvaluation.metadata == EvaluationMetadata.EMPTY)
        }

    @Test
    fun getIntEvaluationReturnsCorrectValue() =
        testScope.runTest {
            requiredInitSuccess()
            mockBKTClientResolver.mockIntValue =
                BKTEvaluationDetails(
                    featureId = "feature1",
                    featureVersion = 1,
                    userId = "user1",
                    variationId = "variation1",
                    variationName = "variation1",
                    variationValue = 123,
                    reason = BKTEvaluationDetails.Reason.OFF_VARIATION,
                )

            val providerEvaluation = provider.getIntegerEvaluation("feature1", 0, null)
            assertTrue(providerEvaluation.value == 123)
            assertTrue(providerEvaluation.variant == "variation1")
            assertTrue(providerEvaluation.reason == "OFF_VARIATION")
            assertTrue(providerEvaluation.errorCode == null)
            assertTrue(providerEvaluation.errorMessage == null)
            assertTrue(providerEvaluation.metadata == EvaluationMetadata.EMPTY)
        }

    @Test
    fun getDoubleEvaluationReturnsCorrectValue() =
        testScope.runTest {
            requiredInitSuccess()
            mockBKTClientResolver.mockDoubleValue =
                BKTEvaluationDetails(
                    featureId = "feature1",
                    featureVersion = 1,
                    userId = "user1",
                    variationId = "variation1",
                    variationName = "variation1",
                    variationValue = 123.45,
                    reason = BKTEvaluationDetails.Reason.PREREQUISITE,
                )

            val providerEvaluation = provider.getDoubleEvaluation("feature1", 0.0, null)
            assertTrue(providerEvaluation.value == 123.45)
            assertTrue(providerEvaluation.variant == "variation1")
            assertTrue(providerEvaluation.reason == "PREREQUISITE")
            assertTrue(providerEvaluation.errorCode == null)
            assertTrue(providerEvaluation.errorMessage == null)
            assertTrue(providerEvaluation.metadata == EvaluationMetadata.EMPTY)
        }

    @Test
    fun getObjectEvaluationReturnsCorrectValue() =
        testScope.runTest {
            requiredInitSuccess()
            mockBKTClientResolver.mockObjectValue =
                BKTEvaluationDetails(
                    featureId = "feature1",
                    featureVersion = 1,
                    userId = "user1",
                    variationId = "variation1",
                    variationName = "variation1",
                    variationValue =
                        BKTValue.Structure(
                            mapOf(
                                "key1" to BKTValue.String("value1"),
                                "key2" to BKTValue.Number(42.0),
                            ),
                        ),
                    reason = BKTEvaluationDetails.Reason.PREREQUISITE,
                )

            val providerEvaluation =
                provider.getObjectEvaluation("feature1", Value.String("default"), null)
            assertTrue(
                providerEvaluation.value ==
                    Value.Structure(
                        mapOf(
                            "key1" to Value.String("value1"),
                            "key2" to Value.Double(42.0),
                        ),
                    ),
            )
            assertTrue(providerEvaluation.variant == "variation1")
            assertTrue(providerEvaluation.reason == "PREREQUISITE")
            assertTrue(providerEvaluation.errorCode == null)
            assertTrue(providerEvaluation.errorMessage == null)
            assertTrue(providerEvaluation.metadata == EvaluationMetadata.EMPTY)
        }

    @Test
    fun getObjectEvaluationForUnsupportedValueTypeDate() =
        testScope.runTest {
            requiredInitSuccess()
            val unsupportedValues =
                listOf(
                    Pair(Value.Date(Date(0)), BKTValue.Number(0.0)),
                    Pair(Value.Integer(0), BKTValue.Number(0.0)),
                )

            for ((defaultValue, defaultBKTValue) in unsupportedValues) {
                // Emulate the feature flag not found in the BKT client
                mockBKTClientResolver.mockObjectValue =
                    BKTEvaluationDetails(
                        featureId = "feature1",
                        featureVersion = 1,
                        userId = "user1",
                        variationId = "variation1",
                        variationName = "variation1",
                        variationValue = defaultBKTValue,
                        reason = BKTEvaluationDetails.Reason.CLIENT,
                    )

                val providerEvaluation =
                    provider.getObjectEvaluation("feature1", defaultValue, null)
                // The default value should be returned if the feature flag was not found
                // But it should Value.Date or Value.Integer
                // It should not be Value.Double
                assertFalse(providerEvaluation.value is Value.Double)
                assertTrue(providerEvaluation.value == defaultValue)
                assertTrue(providerEvaluation.variant == "variation1")
                assertTrue(providerEvaluation.reason == "CLIENT")
                assertTrue(providerEvaluation.errorCode == null)
                assertTrue(providerEvaluation.errorMessage == null)
                assertTrue(providerEvaluation.metadata == EvaluationMetadata.EMPTY)
            }
        }
}

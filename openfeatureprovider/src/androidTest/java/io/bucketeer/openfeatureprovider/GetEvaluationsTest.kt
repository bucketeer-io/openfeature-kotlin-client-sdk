package io.bucketeer.openfeatureprovider

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.openfeature.sdk.EvaluationContext
import dev.openfeature.sdk.FlagEvaluationDetails
import dev.openfeature.sdk.ImmutableContext
import dev.openfeature.sdk.OpenFeatureAPI
import dev.openfeature.sdk.Value
import dev.openfeature.sdk.events.OpenFeatureEvents
import io.bucketeer.sdk.android.BKTConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class GetEvaluationsTest {
    private var provider: BucketeerProvider? = null
    private lateinit var initContext: EvaluationContext
    private lateinit var config: BKTConfig
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        config =
            BKTConfig
                .builder()
                .apiKey(BuildConfig.API_KEY)
                .apiEndpoint(BuildConfig.API_ENDPOINT)
                .featureTag(FEATURE_TAG)
                .appVersion("1.2.3")
                .build()
        initContext =
            ImmutableContext(
                targetingKey = "user1",
                attributes = mapOf("attr1" to Value.String("value1")),
            )

        runBlocking {
            val provider = BucketeerProvider(context, config, CoroutineScope(Dispatchers.Main))
            val eventDeferred =
                async {
                    val event = provider.observe().take(1).first()
                    return@async event
                }
            OpenFeatureAPI.setProviderAndWait(provider, Dispatchers.Main, initContext)
            val expectedEvent = eventDeferred.await()
            Assert.assertEquals(expectedEvent, OpenFeatureEvents.ProviderReady)
            Assert.assertEquals("BucketeerProvider", OpenFeatureAPI.getProvider().metadata.name)
            this@GetEvaluationsTest.provider = provider
        }
    }

    @After
    fun tearDown() {
        try {
            provider = null
            OpenFeatureAPI.shutdown()
            OpenFeatureAPI.clearProvider()
            context.cleanDatabase()
        } catch (e: Exception) {
            Assert.fail(e.message)
        }
    }

    @Test
    fun initializeSuccessWithCorrectUserData() {
        val currentUser = provider!!.clientResolver?.currentUser()
        Assert.assertEquals("user1", currentUser?.id)
        Assert.assertEquals(mapOf("attr1" to "value1"), currentUser?.attributes)
    }

    @Test
    fun shouldUpdateTheUserAttributeWhenContextChange() {
        val newContext =
            ImmutableContext(
                targetingKey = "user1",
                attributes = mapOf("attr2" to Value.String("value2")),
            )
        OpenFeatureAPI.setEvaluationContext(newContext)
        val currentUser = provider!!.clientResolver?.currentUser()
        Assert.assertEquals("user1", currentUser?.id)
        Assert.assertEquals(mapOf("attr2" to "value2"), currentUser?.attributes)
    }

    @Test
    fun shouldNotUpdateTheUserAttributeWhenContextChangeWithNewTargetingKey() {
        // Note: This test will fail if we directly get the provider status from the provider
        // because the event is broadcast on a different dispatcher.
        // Therefore, the correct approach is to use async to get the event from the flow.
        runBlocking {
            try {
                val eventDeferred =
                    async {
                        val event = provider!!.observe().take(1).first()
                        return@async event
                    }
                val newContext =
                    ImmutableContext(
                        targetingKey = "user2",
                        attributes = mapOf("attr2" to Value.String("value2")),
                    )
                OpenFeatureAPI.setEvaluationContext(newContext)

                val expectedEvent = eventDeferred.await()
                Assert.assertTrue(expectedEvent is OpenFeatureEvents.ProviderError)
                Assert.assertEquals(
                    "Changing the targeting_id after initialization is not supported, please reinitialize the provider",
                    (expectedEvent as OpenFeatureEvents.ProviderError).error.message,
                )
            } catch (e: Exception) {
                Assert.assertTrue(e is IllegalStateException)
            }
        }
    }

    @Test
    fun getBooleanEvaluation() {
        val featureId = FEATURE_ID_BOOLEAN
        val defaultValue = false
        val client = OpenFeatureAPI.getClient()
        val evaluation = client.getBooleanValue(featureId, defaultValue)
        Assert.assertTrue(evaluation)

        val evaluationDetails = client.getBooleanDetails(featureId, defaultValue)
        val expectedEvaluationDetails =
            FlagEvaluationDetails(
                value = true,
                flagKey = featureId,
                reason = "DEFAULT",
                variant = "variation true",
            )

        Assert.assertEquals(expectedEvaluationDetails, evaluationDetails)
    }

    @Test
    fun getIntegerEvaluation() {
        val featureId = FEATURE_ID_INT
        val defaultValue = 0
        val client = OpenFeatureAPI.getClient()
        val evaluation = client.getIntegerValue(featureId, defaultValue)
        Assert.assertEquals(10, evaluation)

        val evaluationDetails = client.getIntegerDetails(featureId, defaultValue)
        val expectedEvaluationDetails =
            FlagEvaluationDetails(
                value = 10,
                flagKey = featureId,
                reason = "DEFAULT",
                variant = "variation 10",
            )

        Assert.assertEquals(expectedEvaluationDetails, evaluationDetails)
    }

    @Test
    fun getDoubleEvaluation() {
        val featureId = FEATURE_ID_DOUBLE
        val defaultValue = 0.0
        val client = OpenFeatureAPI.getClient()
        val evaluation = client.getDoubleValue(featureId, defaultValue)
        Assert.assertEquals(2.1, evaluation, 0.0)

        val evaluationDetails = client.getDoubleDetails(featureId, defaultValue)
        val expectedEvaluationDetails =
            FlagEvaluationDetails(
                value = 2.1,
                flagKey = featureId,
                reason = "DEFAULT",
                variant = "variation 2.1",
            )

        Assert.assertEquals(expectedEvaluationDetails, evaluationDetails)
    }

    @Test
    fun getStringEvaluation() {
        val featureId = FEATURE_ID_STRING
        val defaultValue = "default"
        val client = OpenFeatureAPI.getClient()
        val evaluation = client.getStringValue(featureId, defaultValue)
        Assert.assertEquals("value-1", evaluation)

        val evaluationDetails = client.getStringDetails(featureId, defaultValue)
        val expectedEvaluationDetails =
            FlagEvaluationDetails(
                value = "value-1",
                flagKey = featureId,
                reason = "DEFAULT",
                variant = "variation 1",
            )

        Assert.assertEquals(expectedEvaluationDetails, evaluationDetails)
    }

    @Test
    fun getObjectEvaluation() {
        val featureId = FEATURE_ID_JSON
        val defaultValue = Value.String("default")
        val client = OpenFeatureAPI.getClient()
        val evaluation = client.getObjectValue(featureId, defaultValue)
        Assert.assertEquals(Value.Structure(mapOf("key" to Value.String("value-1"))), evaluation)

        val evaluationDetails = client.getObjectDetails(featureId, defaultValue)
        val expectedEvaluationDetails =
            FlagEvaluationDetails(
                value = Value.Structure(mapOf("key" to Value.String("value-1"))),
                flagKey = featureId,
                reason = "DEFAULT",
                variant = "variation 1",
            )

        Assert.assertEquals(expectedEvaluationDetails, evaluationDetails)
    }
}

package io.bucketeer.openfeatureprovider

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.openfeature.sdk.EvaluationContext
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
    lateinit var initContext: EvaluationContext
    lateinit var config: BKTConfig
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

    private fun requireInitProviderSuccess() {
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

    @Test
    fun initializeSuccessWithCorrectUserData() {
        requireInitProviderSuccess()
        val currentUser = provider!!.clientResolver?.currentUser()
        Assert.assertEquals("user1", currentUser?.id)
        Assert.assertEquals(mapOf("attr1" to "value1"), currentUser?.attributes)
    }

    @Test
    fun shouldUpdateTheUserAttributeWhenContextChange() {
        requireInitProviderSuccess()
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
        requireInitProviderSuccess()
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
}

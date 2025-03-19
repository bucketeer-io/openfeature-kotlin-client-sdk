package io.bucketeer.openfeatureprovider

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.openfeature.sdk.EvaluationContext
import dev.openfeature.sdk.ImmutableContext
import dev.openfeature.sdk.OpenFeatureAPI
import dev.openfeature.sdk.Value
import dev.openfeature.sdk.events.OpenFeatureEvents
import io.bucketeer.sdk.android.BKTClient
import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.BKTException
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
class DestroyAndReinit {
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

    private fun shouldShutdownProvider() {
        requireInitProviderSuccess()
        OpenFeatureAPI.shutdown()
        OpenFeatureAPI.clearProvider()
        Assert.assertEquals("No-op provider", OpenFeatureAPI.getProvider().metadata.name)
        Assert.assertNull(provider!!.clientResolver)
        try {
            BKTClient.getInstance()
            Assert.fail("BKTClient should be null")
        } catch (e: Exception) {
            Assert.assertTrue(e is BKTException.IllegalArgumentException)
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
            this@DestroyAndReinit.provider = provider
        }
    }

    @Test
    fun destroyAndReinitialize() {
        runBlocking {
            shouldShutdownProvider()
            shouldShutdownProvider()
        }
    }
}

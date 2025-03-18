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
            OpenFeatureAPI.setProviderAndWait(provider, Dispatchers.Main, initContext)
            Assert.assertEquals("BucketeerProvider", OpenFeatureAPI.getProvider().metadata.name)
            Assert.assertEquals(OpenFeatureAPI.getProvider().getProviderStatus(), OpenFeatureEvents.ProviderReady)
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
        val newContext = ImmutableContext(targetingKey = "user1", attributes = mapOf("attr2" to Value.String("value2")))
        OpenFeatureAPI.setEvaluationContext(newContext)
        val currentUser = provider!!.clientResolver?.currentUser()
        Assert.assertEquals("user1", currentUser?.id)
        Assert.assertEquals(mapOf("attr2" to "value2"), currentUser?.attributes)
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun shouldNotUpdateTheUserAttributeWhenContextChangeWithNewTargetingKey() {
////        requireInitProviderSuccess()
//        runBlocking {
//            val provider = BucketeerProvider(context, config, CoroutineScope(Dispatchers.Main))
//            OpenFeatureAPI.setProviderAndWait(provider, Dispatchers.Main, initContext)
//            Assert.assertEquals("BucketeerProvider", OpenFeatureAPI.getProvider().metadata.name)
//            Assert.assertEquals(OpenFeatureAPI.getProvider().getProviderStatus(), OpenFeatureEvents.ProviderReady)
//            this@GetEvaluationsTest.provider = provider
//            try {
//               val eventDeferred =
//                   async {
//                       OpenFeatureAPI.getProvider().observe().collect{
//                           println("event: $it")
//                       }
//                       val event = OpenFeatureAPI.getProvider().observe().take(1).first()
//                       return@async event
//                   }
//                val newContext = ImmutableContext(targetingKey = "user2", attributes = mapOf("attr2" to Value.String("value2")))
//                OpenFeatureAPI.setEvaluationContext(newContext)
////                delay(1L)
//               val expectedEvent = if (eventDeferred.isCompleted) eventDeferred.getCompleted() else eventDeferred.await()
//                Assert.assertTrue(expectedEvent is OpenFeatureEvents.ProviderError)
//                val status = OpenFeatureAPI.getProvider().getProviderStatus()
//                Assert.assertTrue(status is OpenFeatureEvents.ProviderError)
//            } catch (e: Exception) {
//                Assert.assertTrue(e is IllegalStateException)
//            }
//        }
//    }
}

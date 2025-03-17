package io.bucketeer.openfeatureprovider

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.openfeature.sdk.EvaluationContext
import dev.openfeature.sdk.ImmutableContext
import dev.openfeature.sdk.OpenFeatureAPI
import dev.openfeature.sdk.Value
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
    lateinit var provider: BucketeerProvider
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
        context.cleanDatabase()
    }

    @Test
    fun initializeBucketeerProvider() {
        runBlocking {
            provider = BucketeerProvider(context, config, CoroutineScope(Dispatchers.Main))
            OpenFeatureAPI.setProviderAndWait(provider, Dispatchers.Main, initContext)
            Assert.assertEquals("BucketeerProvider", provider.metadata.name)
        }
    }

}

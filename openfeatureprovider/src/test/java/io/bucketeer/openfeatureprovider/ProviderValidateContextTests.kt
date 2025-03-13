package io.bucketeer.openfeatureprovider

import android.app.Activity
import io.bucketeer.openfeatureprovider.mock.MockBKTClientResolver
import io.bucketeer.openfeatureprovider.mock.MockBKTClientResolverFactory
import io.bucketeer.sdk.android.BKTConfig
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import kotlin.time.Duration.Companion.milliseconds

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
    }

    @After
    fun tearDown() {
    }

    fun requiredInitSuccess () = testScope.runTest(timeout = 500.milliseconds) {

    }

    @Test
    fun onNewContextIsInvalidMissingTargetingKey() = testScope.runTest(timeout = 500.milliseconds) {

    }

    @Test
    fun onNewContextIsChangeUserIdShouldFail() = testScope.runTest(timeout = 500.milliseconds) {

    }

    @Test
    fun onNewContextChangeAttributesShouldSuccess() = testScope.runTest(timeout = 500.milliseconds) {

    }
}
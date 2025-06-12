package io.bucketeer.openfeatureprovider

import io.bucketeer.sdk.android.BKTConfig
import org.junit.Assert.assertEquals
import org.junit.Test

class BKTConfigTest {
    @Test
    fun `copyToSetSourceIdAndVersion should copy config and set sourceId and version`() {
        // Arrange
        val originalConfig =
            BKTConfig
                .builder()
                .apiKey("test-api-key")
                .apiEndpoint("https://api.example.com")
                .featureTag("test-feature-tag")
                .eventsFlushInterval(1000)
                .eventsMaxQueueSize(50)
                .pollingInterval(2000)
                .backgroundPollingInterval(3000)
                .appVersion("1.0.0")
                .logger(null)
                .build()

        // Act
        val overriddenConfig = originalConfig.overrideWithProviderData()

        // Assert
        assertEquals(originalConfig.apiKey, overriddenConfig.apiKey)
        assertEquals(originalConfig.apiEndpoint, overriddenConfig.apiEndpoint)
        assertEquals(originalConfig.featureTag, overriddenConfig.featureTag)
        assertEquals(originalConfig.eventsFlushInterval, overriddenConfig.eventsFlushInterval)
        assertEquals(originalConfig.eventsMaxBatchQueueCount, overriddenConfig.eventsMaxBatchQueueCount)
        assertEquals(originalConfig.pollingInterval, overriddenConfig.pollingInterval)
        assertEquals(originalConfig.backgroundPollingInterval, overriddenConfig.backgroundPollingInterval)
        assertEquals(originalConfig.appVersion, overriddenConfig.appVersion)
        assertEquals(originalConfig.logger, overriddenConfig.logger)
        assertEquals(BuildConfig.SDK_VERSION, overriddenConfig.sdkVersion)
        assertEquals(OPEN_FEATURE_KOTLIN_SOURCE_ID, overriddenConfig.sourceIdValue)
    }
}

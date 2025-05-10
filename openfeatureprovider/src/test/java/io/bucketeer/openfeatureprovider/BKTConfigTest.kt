package io.bucketeer.openfeatureprovider

import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.internal.model.SourceID
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
        val copiedConfig = originalConfig.copyToSetSourceIdAndVersion()

        // Assert
        assertEquals(originalConfig.apiKey, copiedConfig.apiKey)
        assertEquals(originalConfig.apiEndpoint, copiedConfig.apiEndpoint)
        assertEquals(originalConfig.featureTag, copiedConfig.featureTag)
        assertEquals(originalConfig.eventsFlushInterval, copiedConfig.eventsFlushInterval)
        assertEquals(originalConfig.eventsMaxBatchQueueCount, copiedConfig.eventsMaxBatchQueueCount)
        assertEquals(originalConfig.pollingInterval, copiedConfig.pollingInterval)
        assertEquals(originalConfig.backgroundPollingInterval, copiedConfig.backgroundPollingInterval)
        assertEquals(originalConfig.appVersion, copiedConfig.appVersion)
        assertEquals(originalConfig.logger, copiedConfig.logger)
        assertEquals(BuildConfig.SDK_VERSION, copiedConfig.sdkVersion)
        assertEquals(SourceID.OPEN_FEATURE_KOTLIN, copiedConfig.sourceId)
    }
}

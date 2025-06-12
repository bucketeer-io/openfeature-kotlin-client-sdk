package io.bucketeer.openfeatureprovider

import android.content.Context
import dev.openfeature.sdk.exceptions.OpenFeatureError
import io.bucketeer.sdk.android.BKTClient
import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.BKTEvaluationDetails
import io.bucketeer.sdk.android.BKTException
import io.bucketeer.sdk.android.BKTUser
import io.bucketeer.sdk.android.BKTValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// BKTClientResolver a sealed interface that provides methods to get feature variations from the BKTClient.
internal interface BKTClientResolver {
    fun boolVariationDetails(
        featureId: String,
        defaultValue: Boolean,
    ): BKTEvaluationDetails<Boolean>

    fun intVariationDetails(
        featureId: String,
        defaultValue: Int,
    ): BKTEvaluationDetails<Int>

    fun doubleVariationDetails(
        featureId: String,
        defaultValue: Double,
    ): BKTEvaluationDetails<Double>

    fun stringVariationDetails(
        featureId: String,
        defaultValue: String,
    ): BKTEvaluationDetails<String>

    fun objectVariationDetails(
        featureId: String,
        defaultValue: BKTValue,
    ): BKTEvaluationDetails<BKTValue>

    fun currentUser(): BKTUser

    fun updateUserAttributes(attributes: Map<String, String>)
}

internal interface BKTClientResolverFactory {
    fun getClientResolver(): BKTClientResolver

    suspend fun initialize(
        context: Context,
        config: BKTConfig,
        user: BKTUser,
        timeoutMillis: Long = 5000,
    ): BKTException?

    fun destroy()
}

internal class DefaultBKTClientResolverFactory : BKTClientResolverFactory {
    private var clientResolver: BKTClientResolver? = null

    override fun getClientResolver(): BKTClientResolver {
        val clientResolver =
            clientResolver
                ?: throw OpenFeatureError.ProviderNotReadyError("BKTClientResolver is not initialized")
        return clientResolver
    }

    override suspend fun initialize(
        context: Context,
        config: BKTConfig,
        user: BKTUser,
        timeoutMillis: Long,
    ): BKTException? {
        val future = BKTClient.initialize(context, config, user)
        val result =
            withContext(Dispatchers.IO) {
                future.get()
            }
        if (result == null || result is BKTException.TimeoutException) {
            val bktClient = BKTClient.getInstance()
            clientResolver = DefaultBKTClientResolver(bktClient)
        }
        return result
    }

    override fun destroy() {
        BKTClient.destroy()
        clientResolver = null
    }
}

@JvmInline
internal value class DefaultBKTClientResolver(
    private val client: BKTClient,
) : BKTClientResolver {
    override fun boolVariationDetails(
        featureId: String,
        defaultValue: Boolean,
    ): BKTEvaluationDetails<Boolean> = client.boolVariationDetails(featureId, defaultValue)

    override fun intVariationDetails(
        featureId: String,
        defaultValue: Int,
    ): BKTEvaluationDetails<Int> = client.intVariationDetails(featureId, defaultValue)

    override fun doubleVariationDetails(
        featureId: String,
        defaultValue: Double,
    ): BKTEvaluationDetails<Double> = client.doubleVariationDetails(featureId, defaultValue)

    override fun stringVariationDetails(
        featureId: String,
        defaultValue: String,
    ): BKTEvaluationDetails<String> = client.stringVariationDetails(featureId, defaultValue)

    override fun objectVariationDetails(
        featureId: String,
        defaultValue: BKTValue,
    ): BKTEvaluationDetails<BKTValue> = client.objectVariationDetails(featureId, defaultValue)

    override fun currentUser(): BKTUser = client.currentUser()

    override fun updateUserAttributes(attributes: Map<String, String>) = client.updateUserAttributes(attributes)
}

internal const val OPEN_FEATURE_KOTLIN_SOURCE_ID = 100

internal fun BKTConfig.overrideWithProviderData(): BKTConfig =
    BKTConfig
        .builder()
        .apiKey(this.apiKey)
        .apiEndpoint(this.apiEndpoint)
        .featureTag(this.featureTag)
        .eventsFlushInterval(this.eventsFlushInterval)
        .eventsMaxQueueSize(this.eventsMaxBatchQueueCount)
        .pollingInterval(this.pollingInterval)
        .backgroundPollingInterval(this.backgroundPollingInterval)
        .appVersion(this.appVersion)
        .logger(this.logger)
        .wrapperSdkVersion(BuildConfig.SDK_VERSION)
        .wrapperSdkSourceId(OPEN_FEATURE_KOTLIN_SOURCE_ID)
        .build()

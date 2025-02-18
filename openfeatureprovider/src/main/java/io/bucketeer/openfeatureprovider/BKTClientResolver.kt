package io.bucketeer.openfeatureprovider

import android.content.Context
import dev.openfeature.sdk.exceptions.OpenFeatureError
import io.bucketeer.sdk.android.BKTClient
import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.BKTEvaluationDetails
import io.bucketeer.sdk.android.BKTException
import io.bucketeer.sdk.android.BKTUser
import io.bucketeer.sdk.android.BKTValue
import java.util.concurrent.Future

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
}

internal interface BKTClientResolverFactory {
    fun getClientResolver(): BKTClientResolver

    fun initialize(
        context: Context,
        config: BKTConfig,
        user: BKTUser,
        timeoutMillis: Long = 5000,
    ): Future<BKTException?>

    fun destroy()
}

internal class DefaultBKTClientResolverFactory : BKTClientResolverFactory {
    private var client: BKTClient? = null

    private var clientResolver: BKTClientResolver? = null

    override fun getClientResolver(): BKTClientResolver {
        if (clientResolver == null) {
            throw OpenFeatureError.ProviderNotReadyError("BKTClientResolver is not initialized")
        }
        return clientResolver!!
    }

    override fun initialize(
        context: Context,
        config: BKTConfig,
        user: BKTUser,
        timeoutMillis: Long,
    ): Future<BKTException?> {
        val future = BKTClient.initialize(context, config, user)
        val bktClient = BKTClient.getInstance()
        client = bktClient
        clientResolver = DefaultBKTClientResolver(bktClient)
        return future
    }

    override fun destroy() {
        client = null
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
}

package io.bucketeer.openfeatureprovider

import android.content.Context
import android.util.Log
import dev.openfeature.sdk.EvaluationContext
import dev.openfeature.sdk.FeatureProvider
import dev.openfeature.sdk.Hook
import dev.openfeature.sdk.ImmutableContext
import dev.openfeature.sdk.ProviderEvaluation
import dev.openfeature.sdk.ProviderMetadata
import dev.openfeature.sdk.Value
import dev.openfeature.sdk.events.EventHandler
import dev.openfeature.sdk.events.OpenFeatureEvents
import dev.openfeature.sdk.exceptions.OpenFeatureError
import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.BKTException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BucketeerProvider(
    private val context: Context,
    private val config: BKTConfig,
    private val coroutineScope: CoroutineScope,
) : FeatureProvider {
    private val eventHandler = EventHandler(Dispatchers.IO)
    private var clientResolver: BKTClientResolver? = null
    private lateinit var clientResolverFactory: BKTClientResolverFactory

    // For testing purposes
    internal constructor(
        clientResolverFactory: BKTClientResolverFactory,
        context: Context,
        config: BKTConfig,
        coroutineScope: CoroutineScope,
    ) : this(context, config, coroutineScope) {
        this.clientResolverFactory = clientResolverFactory
    }

    init {
        if (::clientResolverFactory.isInitialized.not()) {
            clientResolverFactory = DefaultBKTClientResolverFactory()
        }
    }

    override val hooks: List<Hook<*>> = emptyList()

    override val metadata: ProviderMetadata =
        object : ProviderMetadata {
            override val name: String = "BucketeerProvider"
        }

    override fun getBooleanEvaluation(
        key: String,
        defaultValue: Boolean,
        context: EvaluationContext?,
    ): ProviderEvaluation<Boolean> {
        val client = requiredClientResolver()
        val evaluation = client.boolVariationDetails(key, defaultValue).toProviderEvaluation()
        return evaluation
    }

    override fun getDoubleEvaluation(
        key: String,
        defaultValue: Double,
        context: EvaluationContext?,
    ): ProviderEvaluation<Double> {
        val client = requiredClientResolver()
        val evaluation = client.doubleVariationDetails(key, defaultValue).toProviderEvaluation()
        return evaluation
    }

    override fun getIntegerEvaluation(
        key: String,
        defaultValue: Int,
        context: EvaluationContext?,
    ): ProviderEvaluation<Int> {
        val client = requiredClientResolver()
        val evaluation = client.intVariationDetails(key, defaultValue).toProviderEvaluation()
        return evaluation
    }

    override fun getObjectEvaluation(
        key: String,
        defaultValue: Value,
        context: EvaluationContext?,
    ): ProviderEvaluation<Value> {
        val client = requiredClientResolver()
        val defaultBKTValue = defaultValue.toBKTValue()
        val bktEvaluation = client.objectVariationDetails(key, defaultBKTValue)
        // If the value is the same as the default value, it indicates that the feature flag was not found,
        // and the default value was used. In this case, we should return the default value to OpenFeature.
        // There are differences between BKTValue and OpenFeatureValue.
        // BKTValue does not support Value.date types, so we need to convert the BKTValue to Value.
        // However, for the default value, we can directly return it without additional conversion.
        val openfeatureEvaluation = bktEvaluation.toProviderEvaluationValue()
        return if (bktEvaluation.variationValue == defaultBKTValue) {
            return openfeatureEvaluation.copy(value = defaultValue)
        } else {
            openfeatureEvaluation
        }
    }

    override fun getStringEvaluation(
        key: String,
        defaultValue: String,
        context: EvaluationContext?,
    ): ProviderEvaluation<String> {
        val client = requiredClientResolver()
        val evaluation = client.stringVariationDetails(key, defaultValue).toProviderEvaluation()
        return evaluation
    }

    override fun initialize(initialContext: EvaluationContext?) {
        coroutineScope.launch {
            try {
                val bktUser = (initialContext ?: ImmutableContext()).toBKTUser()
                val result = clientResolverFactory.initialize(context, config, user = bktUser)
                if (result == null || result is BKTException.TimeoutException) {
                    // The BKTClient SDK has been initialized
                    clientResolver = clientResolverFactory.getClientResolver()
                    config.logger?.log(
                        Log.INFO,
                        { "Initialize successful with result: $result" },
                        result,
                    )
                    eventHandler.publish(OpenFeatureEvents.ProviderReady)
                } else {
                    throw result
                }
            } catch (e: Exception) {
                val errorMessage = "Initialize failed with error $e"
                config.logger?.log(Log.ERROR, { errorMessage }, e)
                eventHandler.publish(OpenFeatureEvents.ProviderError(e))
            }
        }
    }

    override fun getProviderStatus(): OpenFeatureEvents = eventHandler.getProviderStatus()

    override fun observe(): Flow<OpenFeatureEvents> = eventHandler.observe()

    override fun onContextSet(
        oldContext: EvaluationContext?,
        newContext: EvaluationContext,
    ) {
        // Not support changing the targeting_id after initialization
        // Need to reinitialize the provider
        try {
            val requiredClientResolver = requiredClientResolver()
            val bktUser = newContext.toBKTUser()
            val currentUser = requiredClientResolver.currentUser()
            if (bktUser.id != currentUser.id) {
                throw OpenFeatureError.InvalidContextError(
                    "Changing the targeting_id after initialization is not supported, please reinitialize the provider",
                )
            } else {
                requiredClientResolver.updateUserAttributes(bktUser.attributes)
            }
        } catch (e: Exception) {
            val errorMessage = "onContextSet failed with error $e"
            config.logger?.log(Log.ERROR, { errorMessage }, e)
            eventHandler.publish(OpenFeatureEvents.ProviderError(e))
        }
    }

    override fun shutdown() {
        if (::clientResolverFactory.isInitialized) {
            try {
                clientResolver = null
                clientResolverFactory.destroy()
            } catch (e: Exception) {
                val errorMessage = "shutdown failed with error $e"
                config.logger?.log(Log.ERROR, { errorMessage }, e)
            }
        }
    }

    private fun requiredClientResolver(): BKTClientResolver =
        clientResolver
            ?: throw OpenFeatureError.ProviderNotReadyError("BKTClientResolver is not initialized")
}

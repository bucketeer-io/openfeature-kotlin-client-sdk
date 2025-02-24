package io.bucketeer.openfeatureprovider

import dev.openfeature.sdk.EvaluationContext
import dev.openfeature.sdk.FeatureProvider
import dev.openfeature.sdk.Hook
import dev.openfeature.sdk.ProviderEvaluation
import dev.openfeature.sdk.ProviderMetadata
import dev.openfeature.sdk.Value
import dev.openfeature.sdk.events.EventHandler
import dev.openfeature.sdk.events.OpenFeatureEvents
import dev.openfeature.sdk.exceptions.OpenFeatureError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class BucketeerProvider() : FeatureProvider {
    private val eventHandler = EventHandler(Dispatchers.IO)
    private var clientResolver: BKTClientResolver? = null
    private lateinit var clientResolverFactory: BKTClientResolverFactory

    // For testing purposes
    internal constructor(clientResolverFactory: BKTClientResolverFactory) : this() {
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
    }

    override fun getProviderStatus(): OpenFeatureEvents = eventHandler.getProviderStatus()

    override fun observe(): Flow<OpenFeatureEvents> = eventHandler.observe()

    override fun onContextSet(
        oldContext: EvaluationContext?,
        newContext: EvaluationContext,
    ) {
        // Not support changing the targeting_id after initialization
        // Need to reinitialize the provider
    }

    override fun shutdown() {
        if (::clientResolverFactory.isInitialized) {
            clientResolverFactory.destroy()
        }
    }

    private fun requiredClientResolver(): BKTClientResolver {
        return clientResolver
            ?: throw OpenFeatureError.ProviderNotReadyError("BKTClientResolver is not initialized")
    }
}

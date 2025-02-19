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
        val evaluation = client.objectVariationDetails(key, defaultValue.toBKTValue()).toProviderEvaluationValue()
        return evaluation
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
    }

    override fun shutdown() {
        if (::clientResolverFactory.isInitialized) {
            clientResolverFactory.destroy()
        }
    }

    internal fun requiredClientResolver(): BKTClientResolver {
        if (clientResolver == null) {
            throw OpenFeatureError.ProviderNotReadyError("BKTClientResolver is not initialized")
        }
        return clientResolver!!
    }
}

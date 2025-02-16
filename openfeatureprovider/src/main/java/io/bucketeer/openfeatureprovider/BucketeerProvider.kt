package io.bucketeer.openfeatureprovider

import dev.openfeature.sdk.EvaluationContext
import dev.openfeature.sdk.FeatureProvider
import dev.openfeature.sdk.Hook
import dev.openfeature.sdk.ProviderEvaluation
import dev.openfeature.sdk.ProviderMetadata
import dev.openfeature.sdk.Value
import dev.openfeature.sdk.events.OpenFeatureEvents
import kotlinx.coroutines.flow.Flow

class BucketeerProvider : FeatureProvider {
    override val hooks: List<Hook<*>>
        get() = TODO("Not yet implemented")
    override val metadata: ProviderMetadata
        get() = TODO("Not yet implemented")

    override fun getBooleanEvaluation(
        key: String,
        defaultValue: Boolean,
        context: EvaluationContext?,
    ): ProviderEvaluation<Boolean> {
        TODO("Not yet implemented")
    }

    override fun getDoubleEvaluation(
        key: String,
        defaultValue: Double,
        context: EvaluationContext?,
    ): ProviderEvaluation<Double> {
        TODO("Not yet implemented")
    }

    override fun getIntegerEvaluation(
        key: String,
        defaultValue: Int,
        context: EvaluationContext?,
    ): ProviderEvaluation<Int> {
        TODO("Not yet implemented")
    }

    override fun getObjectEvaluation(
        key: String,
        defaultValue: Value,
        context: EvaluationContext?,
    ): ProviderEvaluation<Value> {
        TODO("Not yet implemented")
    }

    override fun getProviderStatus(): OpenFeatureEvents {
        TODO("Not yet implemented")
    }

    override fun getStringEvaluation(
        key: String,
        defaultValue: String,
        context: EvaluationContext?,
    ): ProviderEvaluation<String> {
        TODO("Not yet implemented")
    }

    override fun initialize(initialContext: EvaluationContext?) {
        TODO("Not yet implemented")
    }

    override fun observe(): Flow<OpenFeatureEvents> {
        TODO("Not yet implemented")
    }

    override fun onContextSet(
        oldContext: EvaluationContext?,
        newContext: EvaluationContext,
    ) {
        TODO("Not yet implemented")
    }

    override fun shutdown() {
        TODO("Not yet implemented")
    }
}

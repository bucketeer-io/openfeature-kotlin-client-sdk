package io.bucketeer.openfeatureprovider

import dev.openfeature.sdk.ProviderEvaluation
import dev.openfeature.sdk.Value
import io.bucketeer.sdk.android.BKTEvaluationDetails
import io.bucketeer.sdk.android.BKTValue

internal fun <T> BKTEvaluationDetails<T>.toProviderEvaluation(): ProviderEvaluation<T> =
    ProviderEvaluation(
        value = variationValue,
        variant = variationName,
        reason = reason.name,
    )

internal fun BKTEvaluationDetails<BKTValue>.toProviderEvaluationValue(): ProviderEvaluation<Value> =
    ProviderEvaluation(
        value = variationValue.toValue(),
        variant = variationName,
        reason = reason.name,
    )

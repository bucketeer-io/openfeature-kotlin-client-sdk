package io.bucketeer.openfeatureprovider

import dev.openfeature.sdk.EvaluationContext
import dev.openfeature.sdk.exceptions.OpenFeatureError
import io.bucketeer.sdk.android.BKTUser

fun EvaluationContext.toBKTUser(): BKTUser {
    try {
        val userId = this.getTargetingKey()
        val attributes =
            this
                .asMap()
                .mapNotNull { (key, value) -> value.asString()?.let { key to it } }
                .toMap()

        return BKTUser
            .builder()
            .id(userId)
            .customAttributes(attributes)
            .build()
    } catch (e: Exception) {
        throw OpenFeatureError.TargetingKeyMissingError("missing targeting key")
    }
}

package io.bucketeer.openfeatureprovider.mock

import io.bucketeer.openfeatureprovider.BKTClientResolver
import io.bucketeer.sdk.android.BKTEvaluationDetails
import io.bucketeer.sdk.android.BKTUser
import io.bucketeer.sdk.android.BKTValue

class MockBKTClientResolver : BKTClientResolver {
    var mockBoolValue: BKTEvaluationDetails<Boolean>? = null
    var mockIntValue: BKTEvaluationDetails<Int>? = null
    var mockDoubleValue: BKTEvaluationDetails<Double>? = null
    var mockStringValue: BKTEvaluationDetails<String>? = null
    var mockObjectValue: BKTEvaluationDetails<BKTValue>? = null
    var mockCurrentUser: BKTUser? = null
    var userAttributes: Map<String, String>? = null

    override fun boolVariationDetails(
        featureId: String,
        defaultValue: Boolean,
    ): BKTEvaluationDetails<Boolean> = mockBoolValue!!

    override fun intVariationDetails(
        featureId: String,
        defaultValue: Int,
    ): BKTEvaluationDetails<Int> = mockIntValue!!

    override fun doubleVariationDetails(
        featureId: String,
        defaultValue: Double,
    ): BKTEvaluationDetails<Double> = mockDoubleValue!!

    override fun stringVariationDetails(
        featureId: String,
        defaultValue: String,
    ): BKTEvaluationDetails<String> = mockStringValue!!

    override fun objectVariationDetails(
        featureId: String,
        defaultValue: BKTValue,
    ): BKTEvaluationDetails<BKTValue> = mockObjectValue!!

    override fun currentUser(): BKTUser = mockCurrentUser!!

    override fun updateUserAttributes(attributes: Map<String, String>) {
        userAttributes = attributes
    }
}

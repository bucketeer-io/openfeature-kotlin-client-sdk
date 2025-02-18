package io.bucketeer.openfeatureprovider

import dev.openfeature.sdk.Value
import io.bucketeer.sdk.android.BKTEvaluationDetails
import io.bucketeer.sdk.android.BKTValue
import org.junit.Assert.assertEquals
import org.junit.Test

class BKTEvaluationDetailsExtTests {
    @Test
    fun toProviderEvaluation_withIntValue_returnsCorrectProviderEvaluation() {
        val bktEvaluationDetails =
            BKTEvaluationDetails(
                featureId = "feature1",
                featureVersion = 1,
                userId = "user1",
                variationId = "var1",
                variationName = "Variation 1",
                variationValue = 42,
                reason = BKTEvaluationDetails.Reason.TARGET,
            )

        val providerEvaluation = bktEvaluationDetails.toProviderEvaluation()

        assertEquals(42, providerEvaluation.value)
        assertEquals("Variation 1", providerEvaluation.variant)
        assertEquals("TARGET", providerEvaluation.reason)
    }

    @Test
    fun toProviderEvaluation_withDoubleValue_returnsCorrectProviderEvaluation() {
        val bktEvaluationDetails =
            BKTEvaluationDetails(
                featureId = "feature2",
                featureVersion = 2,
                userId = "user2",
                variationId = "var2",
                variationName = "Variation 2",
                variationValue = 42.1,
                reason = BKTEvaluationDetails.Reason.RULE,
            )

        val providerEvaluation = bktEvaluationDetails.toProviderEvaluation()

        assertEquals(42.1, providerEvaluation.value, 0.0)
        assertEquals("Variation 2", providerEvaluation.variant)
        assertEquals("RULE", providerEvaluation.reason)
    }

    @Test
    fun toProviderEvaluation_withStringValue_returnsCorrectProviderEvaluation() {
        val bktEvaluationDetails =
            BKTEvaluationDetails(
                featureId = "feature3",
                featureVersion = 3,
                userId = "user3",
                variationId = "var3",
                variationName = "Variation 3",
                variationValue = "Value 3",
                reason = BKTEvaluationDetails.Reason.DEFAULT,
            )

        val providerEvaluation = bktEvaluationDetails.toProviderEvaluation()

        assertEquals("Value 3", providerEvaluation.value)
        assertEquals("Variation 3", providerEvaluation.variant)
        assertEquals("DEFAULT", providerEvaluation.reason)
    }

    @Test
    fun toProviderEvaluation_withBKTValue_returnsCorrectProviderEvaluation() {
        val bktEvaluationDetails: BKTEvaluationDetails<BKTValue> =
            BKTEvaluationDetails(
                featureId = "feature4",
                featureVersion = 4,
                userId = "user4",
                variationId = "var4",
                variationName = "Variation 4",
                variationValue = BKTValue.String("BKT Value"),
                reason = BKTEvaluationDetails.Reason.CLIENT,
            )

        val providerEvaluation = bktEvaluationDetails.toProviderEvaluationValue()

        assertEquals(Value.String("BKT Value"), providerEvaluation.value)
        assertEquals("Variation 4", providerEvaluation.variant)
        assertEquals("CLIENT", providerEvaluation.reason)
    }
}

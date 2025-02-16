package io.bucketeer.openfeatureprovider

import dev.openfeature.sdk.Value
import io.bucketeer.sdk.android.BKTValue
import org.junit.Assert.assertEquals
import org.junit.Test

class BKTValueExtTests {
    @Test
    fun test_BKTValueList_toValue_withMultipleElements() {
        val bktValue =
            BKTValue.List(
                listOf(
                    BKTValue.String("test1"),
                    BKTValue.Number(42.0),
                    BKTValue.Boolean(true),
                ),
            )
        val value = bktValue.toValue()
        assertEquals(
            Value.List(
                listOf(
                    Value.String("test1"),
                    Value.Double(42.0),
                    Value.Boolean(true),
                ),
            ),
            value,
        )
    }

    @Test
    fun test_BKTValueList_toValue_withEmptyList() {
        val bktValue = BKTValue.List(emptyList())
        val value = bktValue.toValue()
        assertEquals(Value.List(emptyList()), value)
    }

    @Test
    fun test_BKTValueStructure_toValue_withMultipleEntries() {
        val bktValue =
            BKTValue.Structure(
                mapOf(
                    "key1" to BKTValue.String("value1"),
                    "key2" to BKTValue.Number(42.0),
                ),
            )
        val value = bktValue.toValue()
        assertEquals(
            Value.Structure(
                mapOf(
                    "key1" to Value.String("value1"),
                    "key2" to Value.Double(42.0),
                ),
            ),
            value,
        )
    }

    @Test
    fun test_BKTValueStructure_toValue_withEmptyStructure() {
        val bktValue = BKTValue.Structure(emptyMap())
        val value = bktValue.toValue()
        assertEquals(Value.Structure(emptyMap()), value)
    }
}

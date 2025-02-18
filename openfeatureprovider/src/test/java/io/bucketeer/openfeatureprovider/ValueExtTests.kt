package io.bucketeer.openfeatureprovider

import dev.openfeature.sdk.Value
import io.bucketeer.sdk.android.BKTValue
import org.junit.Assert.assertEquals
import org.junit.Test

class ValueExtTests {
    @Test
    fun test_ValueBoolean_toBKTValue() {
        val value = Value.Boolean(true)
        val bktValue = value.toBKTValue()
        assertEquals(BKTValue.Boolean(true), bktValue)
    }

    @Test
    fun test_ValueList_toBKTValue_withMultipleElements() {
        val value =
            Value.List(listOf(Value.String("test1"), Value.Double(42.0), Value.Boolean(true)))
        val bktValue = value.toBKTValue()
        assertEquals(
            BKTValue.List(
                listOf(
                    BKTValue.String("test1"),
                    BKTValue.Number(42.0),
                    BKTValue.Boolean(true),
                ),
            ),
            bktValue,
        )
    }

    @Test
    fun test_ValueList_toBKTValue_withEmptyList() {
        val value = Value.List(emptyList())
        val bktValue = value.toBKTValue()
        assertEquals(BKTValue.List(emptyList()), bktValue)
    }

    @Test
    fun test_ValueNull_toBKTValue() {
        val value = Value.Null
        val bktValue = value.toBKTValue()
        assertEquals(BKTValue.Null, bktValue)
    }

    @Test
    fun test_ValueNumber_Double_toBKTValue() {
        val value = Value.Double(42.1)
        val bktValue = value.toBKTValue()
        assertEquals(BKTValue.Number(42.1), bktValue)
    }

    @Test
    fun test_ValueNumber_Integer_toBKTValue() {
        val value = Value.Integer(42)
        val bktValue = value.toBKTValue()
        assertEquals(BKTValue.Number(42.0), bktValue)
    }

    @Test
    fun test_ValueString_toBKTValue() {
        val value = Value.String("test")
        val bktValue = value.toBKTValue()
        assertEquals(BKTValue.String("test"), bktValue)
    }

    @Test
    fun test_ValueStructure_toBKTValue_withMultipleEntries() {
        val value =
            Value.Structure(mapOf("key1" to Value.String("value1"), "key2" to Value.Double(42.0)))
        val bktValue = value.toBKTValue()
        assertEquals(
            BKTValue.Structure(
                mapOf(
                    "key1" to BKTValue.String("value1"),
                    "key2" to BKTValue.Number(42.0),
                ),
            ),
            bktValue,
        )
    }

    @Test
    fun test_ValueStructure_toBKTValue_withEmptyStructure() {
        val value = Value.Structure(emptyMap())
        val bktValue = value.toBKTValue()
        assertEquals(BKTValue.Structure(emptyMap()), bktValue)
    }

    @Test
    fun test_ValueDate_toBKTValue() {
        val date = java.util.Date()
        val value = Value.Date(date)
        val bktValue = value.toBKTValue()
        assertEquals(BKTValue.Number(date.time.toDouble()), bktValue)
    }
}

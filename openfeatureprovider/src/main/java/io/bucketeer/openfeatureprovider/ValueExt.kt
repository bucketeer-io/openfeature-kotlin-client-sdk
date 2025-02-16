package io.bucketeer.openfeatureprovider

import dev.openfeature.sdk.Value
import io.bucketeer.sdk.android.BKTValue

fun Value.toBKTValue(): BKTValue =
    when (this) {
        is Value.Boolean -> BKTValue.Boolean(this.boolean)
        is Value.List -> BKTValue.List(this.list.map { it.toBKTValue() })
        Value.Null -> BKTValue.Null
        is Value.Double -> BKTValue.Number(this.double)
        is Value.Integer -> BKTValue.Number(this.integer.toDouble())
        is Value.String -> BKTValue.String(this.string)
        is Value.Structure -> BKTValue.Structure(this.structure.mapValues { it.value.toBKTValue() })
        is Value.Date -> BKTValue.Number(this.date.time.toDouble())
    }

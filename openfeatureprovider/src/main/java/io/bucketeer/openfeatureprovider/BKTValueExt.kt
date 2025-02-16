package io.bucketeer.openfeatureprovider

import dev.openfeature.sdk.Value
import io.bucketeer.sdk.android.BKTValue

fun BKTValue.toValue(): Value =
    when (this) {
        is BKTValue.Boolean -> Value.Boolean(this.boolean)
        is BKTValue.List -> Value.List(this.list.map { it.toValue() })
        BKTValue.Null -> Value.Null
        is BKTValue.Number -> Value.Double(this.number)
        is BKTValue.String -> Value.String(this.string)
        is BKTValue.Structure -> Value.Structure(this.structure.mapValues { it.value.toValue() })
    }

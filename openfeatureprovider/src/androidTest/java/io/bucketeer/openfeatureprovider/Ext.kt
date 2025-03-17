package io.bucketeer.openfeatureprovider

import android.content.Context

fun Context.cleanDatabase() {
    deleteDatabase("bucketeer.db")
    getSharedPreferences("bucketeer", Context.MODE_PRIVATE)
        .edit()
        .clear()
        .commit()
}
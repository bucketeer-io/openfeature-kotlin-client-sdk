package io.bucketeer.openfeatureprovider.mock

import android.content.Context
import io.bucketeer.openfeatureprovider.BKTClientResolver
import io.bucketeer.openfeatureprovider.BKTClientResolverFactory
import io.bucketeer.sdk.android.BKTConfig
import io.bucketeer.sdk.android.BKTException
import io.bucketeer.sdk.android.BKTUser

internal class MockBKTClientResolverFactory(
    private val client: MockBKTClientResolver,
) : BKTClientResolverFactory {
    var user: BKTUser? = null
    var config: BKTConfig? = null
    var onDestroy: (() -> Unit)? = null
    var onInitializeError: BKTException? = null

    override fun getClientResolver(): BKTClientResolver = client

    override suspend fun initialize(
        context: Context,
        config: BKTConfig,
        user: BKTUser,
        timeoutMillis: Long,
    ): BKTException? {
        this.config = config
        this.user = user
        client.userAttributes = user.attributes
        client.mockCurrentUser = user
        if (onInitializeError != null) {
            return onInitializeError
        }
        return null
    }

    override fun destroy() {
        onDestroy?.invoke()
    }
}

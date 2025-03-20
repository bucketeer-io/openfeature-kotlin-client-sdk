# Bucketeer - OpenFeature Kotlin provider for Android clients

This is the official Kotlin OpenFeature provider for accessing your feature flags with [Bucketeer](https://bucketeer.io/).

[Bucketeer](https://bucketeer.io) is an open-source platform created by [CyberAgent](https://www.cyberagent.co.jp/en/) to help teams make better decisions, reduce deployment lead time and release risk through feature flags. Bucketeer offers advanced features like dark launches and staged rollouts that perform limited releases based on user attributes, devices, and other segments.

In conjunction with the [OpenFeature SDK](https://openfeature.dev/docs/reference/concepts/provider) you will be able to evaluate your feature flags in your **Android** applications.

> [!WARNING]
> This is a beta version. Breaking changes may be introduced before general release.

For documentation related to flags management in Bucketeer, refer to the [Bucketeer documentation website](https://docs.bucketeer.io/sdk/client-side/android).

## Supported Android sdk versions

This version of the SDK is built for the following targets:

* Android 5.0 (API level 21) and above.

## Installation

### Gradle

dependencies {
    implementation 'io.bucketeer:openfeature-kotlin-client-sdk:LATEST_VERSION'
}

## Usage

### Initialize the provider

Bucketeer provider needs to be created and then set in the global OpenFeatureAPI.

```kotlin
import dev.openfeature.sdk.*
import io.bucketeer.openfeatureprovider.BucketeerProvider
import io.bucketeer.sdk.android.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

try {
    val featureId = "featureId"
    val featureTag = "android"
    val apiKey = "API_KEY"
    val apiEndpoint = "API_ENDPOINT"

    val config = BKTConfig.builder()
        .apiKey(apiKey)
        .apiEndpoint(apiEndpoint)
        .featureTag(featureTag)
        .appVersion(BuildConfig.VERSION_NAME)
        .eventsMaxQueueSize(10)
        .pollingInterval(TimeUnit.SECONDS.toMillis(20))
        .backgroundPollingInterval(TimeUnit.SECONDS.toMillis(60))
        .eventsFlushInterval(TimeUnit.SECONDS.toMillis(20))
        .build()

    val initContext = ImmutableContext(
        targetingKey = "USER_ID",
        attributes = mapOf("attr1" to Value.String("value1"))
    )

    val provider = BucketeerProvider(this, config, lifecycleScope)

    lifecycleScope.launch {
        OpenFeatureAPI.setProviderAndWait(provider, Dispatchers.IO, initContext)
        val flag = OpenFeatureAPI.getClient().getBooleanValue(featureId, defaultValue = false)

        if (flag) {
            // show new feature
        } else {
            // show old feature
        }
    }
} catch (e: Exception) {
    // handle error
}
```

Note: `lifecycleScope` is a `CoroutineScope` that will be use when initializing the provider. It should be the activity, fragment lifecycle scope or a MainScope.

See our [documentation](https://docs.bucketeer.io/sdk/client-side/android) for more SDK configuration.

The evaluation context allows the client to specify contextual data that Bucketeer uses to evaluate the feature flags.

The `targetingKey` is the user ID (Unique ID) and cannot be empty.

### Update the Evaluation Context

You can update the evaluation context with the new attributes if the user attributes change.

```kotlin
val newContext = ImmutableContext(
    targetingKey = "USER_ID",
    attributes = mapOf("attr2" to Value.String("value2"))
)
OpenFeatureAPI.setEvaluationContext(newContext)
```

> [!WARNING]
> Changing the `targetingKey` is not supported in the current implementation of the BucketeerProvider.

To change the user ID, the BucketeerProvider must be removed and reinitialized.

```kotlin
// Shut down the provider first
OpenFeatureAPI.shutdown()
// Remove the provider
OpenFeatureAPI.clearProvider()
// Reinitialize the provider with new targetingKey
```

### Evaluate a feature flag

After the provider is set and no error is thrown, you can evaluate a feature flag using OpenFeatureAPI.

```kotlin
val client = OpenFeatureAPI.getClient()

// Bool
client.getBooleanValue("my-flag", defaultValue: false)

// String
client.getStringValue("my-flag", defaultValue: "default")

// Integer
client.getIntegerValue("my-flag", defaultValue: 1)

// Double
client.getDoubleValue("my-flag", defaultValue: 1.1)

// Object
client.getObjectValue("my-flag", defaultValue: Value.Structure(mapOf("key" to Value.String("value-1"))))
```

> [!WARNING]
> Value.date is not supported in the current implementation of the BucketeerProvider.

## Contributing

We would ❤️ for you to contribute to Bucketeer and help improve it! Anyone can use and enjoy it!

Please follow our contribution guide [here](https://docs.bucketeer.io/contribution-guide/).

## Development

## Setup

Install prerequisite tools.

- Android Studio Ladybug Feature Drop | 2024.2.2
- Java 11

Then, you need to create `local.properties`.

```
# build
sdk.dir=<SDK_DIR_PATH> # e.g. /Users/<USER_NAME>/Library/Android/sdk

# test
api_key=<API_KEY>
api_endpoint=<API_ENDPOINT> # e.g. api.example.jp
```

## License

Apache License 2.0, see [LICENSE](https://github.com/bucketeer-io/ios-client-sdk/blob/main/LICENSE).
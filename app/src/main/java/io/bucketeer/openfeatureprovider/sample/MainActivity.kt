package io.bucketeer.openfeatureprovider.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import dev.openfeature.sdk.ImmutableContext
import dev.openfeature.sdk.OpenFeatureAPI
import dev.openfeature.sdk.Value
import io.bucketeer.openfeatureprovider.BucketeerProvider
import io.bucketeer.sdk.android.BKTConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.textView)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initBucketeer()
    }

    @SuppressLint("SetTextI18n")
    fun initBucketeer() {
        // Initialize Bucketeer Provider
        try {
            val featureId = "feature-android-e2e-boolean"
            val featureTag = "android"
            val apiKey = "API_KEY"
            val apiEndpoint = "API_ENDPOINT"
            val config =
                BKTConfig
                    .builder()
                    .apiKey(apiKey)
                    .apiEndpoint(apiEndpoint)
                    .featureTag(featureTag)
                    .appVersion(BuildConfig.VERSION_NAME)
                    .eventsMaxQueueSize(10)
                    .pollingInterval(TimeUnit.SECONDS.toMillis(20))
                    .backgroundPollingInterval(TimeUnit.SECONDS.toMillis(60))
                    .eventsFlushInterval(TimeUnit.SECONDS.toMillis(20))
                    .build()
            val initContext =
                ImmutableContext(
                    targetingKey = "USER_ID",
                    attributes = mapOf("attr1" to Value.String("value1")),
                )
            val provider = BucketeerProvider(this, config, lifecycleScope)
            lifecycleScope.launch {
                OpenFeatureAPI.setProviderAndWait(provider, Dispatchers.IO, initContext)
                val flag = OpenFeatureAPI.getClient().getBooleanValue(featureId, defaultValue = false)
                if (flag) {
                    textView.text = "Should show new feature"
                } else {
                    textView.text = "Should not show new feature"
                }
            }
        } catch (e: Exception) {
            textView.text = "Error: ${e.message}"
        }
    }

    private fun shutdownProvider() {
        OpenFeatureAPI.shutdown()
        OpenFeatureAPI.clearProvider()
    }

    override fun onDestroy() {
        super.onDestroy()
        shutdownProvider()
    }
}

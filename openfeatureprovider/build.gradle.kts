import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.publish)
    alias(libs.plugins.kotlinter)
}

val customProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    customProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "io.bucketeer.openfeatureprovider"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        val apiKey = customProperties.getProperty("api_key") ?: System.getenv("API_KEY")
        val apiEndpoint = customProperties.getProperty("api_endpoint") ?: System.getenv("API_ENDPOINT")
        val versionName = project.properties["VERSION_NAME"].toString()
        buildConfigField("String", "API_KEY", "\"${apiKey}\"")
        buildConfigField("String", "API_ENDPOINT", "\"${apiEndpoint}\"")
        buildConfigField("String", "SDK_VERSION", "\"${versionName}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.lifecycle.runtime)
    androidTestImplementation(libs.androidx.lifecycle.process)

    // Available to this module and any module that depends on it
    api(libs.openfeature.android.sdk)
    api(libs.bucketeer.android.sdk)
}

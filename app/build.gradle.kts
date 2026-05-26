plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.chaquo.python")
}
android {
    namespace = "com.eternal.ai"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.eternal.ai"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        ndk { abiFilters += "arm64-v8a" }
    }
    buildTypes { release { isMinifyEnabled = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
    aaptOptions { noCompress += "onnx" }
}
chaquopy {
    defaultConfig {
        pip {
            install("numpy")          // numpy 在 Chaquopy 里是可用的
            install("tokenizers")     // 我们将用 Chaquopy 做 tokenizer
        }
    }
}
dependencies {
    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("androidx.activity:activity-compose:1.7.2")
    // ONNX Runtime for Android (原生)
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.18.0")
}

import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val localProps = Properties()
val localPropsFile = rootProject.file("local.properties")
if (localPropsFile.exists()) {
    localProps.load(localPropsFile.inputStream())
}
val defaultGeminiKey: String = localProps.getProperty("GEMINI_API_KEY")
    ?: System.getenv("GEMINI_API_KEY")
    ?: (project.findProperty("GEMINI_API_KEY") as? String)
    ?: ""

val appVersionName: String = (project.findProperty("versionName") as? String)
    ?: System.getenv("APP_VERSION_NAME")
    ?: "1.0.11"

val appVersionCode: Int = (project.findProperty("versionCode") as? String)?.toIntOrNull()
    ?: (System.getenv("APP_VERSION_CODE")?.toIntOrNull())
    ?: 111

android {
    namespace = "com.weather24h.app"
    compileSdk = 34

    signingConfigs {
        create("release") {
            val keystoreFile = rootProject.file("keystore/release.jks")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = "weather24h"
                keyAlias = "weather24h"
                keyPassword = "weather24h"
            } else {
                val debugConfig = signingConfigs.getByName("debug")
                storeFile = debugConfig.storeFile
                storePassword = debugConfig.storePassword
                keyAlias = debugConfig.keyAlias
                keyPassword = debugConfig.keyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.weather24h.app"
        minSdk = 26
        targetSdk = 34
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "DEFAULT_GEMINI_API_KEY", "\"$defaultGeminiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX & Activity / Fragment Compose
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Jetpack Compose BOM & UI
    val composeBom = platform("androidx.compose:compose-bom:2024.02.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Networking (OkHttp) & JSON (Gson)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Background Tasks (WorkManager for Periodic Weather Status)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Play Services Location (GPS)
    implementation("com.google.android.gms:play-services-location:21.1.0")

    // Debugging
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

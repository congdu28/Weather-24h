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

val defaultWeatherApiKey: String = localProps.getProperty("WEATHER_API_KEY")?.takeIf { it.isNotBlank() }
    ?: System.getenv("WEATHER_API_KEY")?.takeIf { it.isNotBlank() }
    ?: (project.findProperty("WEATHER_API_KEY") as? String)?.takeIf { it.isNotBlank() }
    ?: ""

val defaultGoogleWeatherKey: String = localProps.getProperty("GOOGLE_WEATHER_KEY")?.takeIf { it.isNotBlank() }
    ?: System.getenv("GOOGLE_WEATHER_KEY")?.takeIf { it.isNotBlank() }
    ?: (project.findProperty("GOOGLE_WEATHER_KEY") as? String)?.takeIf { it.isNotBlank() }
    ?: ""

/**
 * Mã hóa XOR + Hex khóa API ngay tại thời điểm biên dịch (Build-time Obfuscation)
 * để không tồn tại bất kỳ chuỗi khóa dạng văn bản thuần (plaintext) nào trong BuildConfig.java hay classes.dex.
 */
fun encodeSecretForBuildConfig(raw: String, seed: Int): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return ""
    val bytes = trimmed.toByteArray(Charsets.UTF_8)
    val sb = StringBuilder(bytes.size * 2)
    for (i in bytes.indices) {
        val b = bytes[i].toInt() and 0xFF
        val k = ((seed ushr ((i % 4) * 8)) xor (i * 31 + 0x5A)) and 0xFF
        val enc = b xor k
        sb.append(String.format("%02x", enc))
    }
    return sb.toString()
}

val maskSeed = 0x5A3C9E71
val encodedGeminiKey = encodeSecretForBuildConfig(defaultGeminiKey, maskSeed)
val encodedWeatherApiKey = encodeSecretForBuildConfig(defaultWeatherApiKey, maskSeed xor 0x13572468)
val encodedGoogleWeatherKey = encodeSecretForBuildConfig(defaultGoogleWeatherKey, maskSeed xor 0x24681357)

val appVersionName: String = (project.findProperty("versionName") as? String)
    ?: System.getenv("APP_VERSION_NAME")
    ?: "1.0.56"

val appVersionCode: Int = (project.findProperty("versionCode") as? String)?.toIntOrNull()
    ?: (System.getenv("APP_VERSION_CODE")?.toIntOrNull())
    ?: 156

android {
    namespace = "com.weather24h.app"
    compileSdk = 34

    signingConfigs {
        create("release") {
            val keystoreFile = rootProject.file("keystore/release.jks")
            if (keystoreFile.exists()) {
                val ksPass = localProps.getProperty("KEYSTORE_PASSWORD")
                    ?: System.getenv("KEYSTORE_PASSWORD")
                    ?: "weather24h"
                val kAlias = localProps.getProperty("KEY_ALIAS")
                    ?: System.getenv("KEY_ALIAS")
                    ?: "weather24h"
                val kPass = localProps.getProperty("KEY_PASSWORD")
                    ?: System.getenv("KEY_PASSWORD")
                    ?: "weather24h"
                storeFile = keystoreFile
                storePassword = ksPass
                keyAlias = kAlias
                keyPassword = kPass
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

        buildConfigField("String", "DEFAULT_GEMINI_API_KEY_ENC", "\"$encodedGeminiKey\"")
        buildConfigField("String", "DEFAULT_GOOGLE_WEATHER_KEY_ENC", "\"$encodedGoogleWeatherKey\"")
        buildConfigField("String", "DEFAULT_WEATHER_API_KEY_ENC", "\"$encodedWeatherApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
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
        freeCompilerArgs = freeCompilerArgs + listOf(
            "-Xno-param-assertions",
            "-Xno-call-assertions",
            "-Xno-receiver-assertions"
        )
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
            excludes += "/META-INF/*.kotlin_module"
            excludes += "/META-INF/version-control-info.textproto"
            excludes += "/DebugProbesKt.bin"
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
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

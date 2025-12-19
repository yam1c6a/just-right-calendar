plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.yam1c6a.justrightcalendar"

    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.yam1c6a.justrightcalendar"
        minSdk = 34
        targetSdk = 36

        versionCode = 1
        versionName = "0.9.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

/**
 * APKファイル名をリネームしたコピーを作る（AGPの内部APIに依存しない安全版）
 *
 * 生成先:
 *  - debug  : app/build/outputs/apk/debug/just-right-calendar-v0.9.0-debug.apk
 *  - release: app/build/outputs/apk/release/just-right-calendar-v0.9.0-release.apk
 */
val appName = "just-right-calendar"
val verName = android.defaultConfig.versionName ?: "unknown"

tasks.register<Copy>("copyRenamedDebugApk") {
    dependsOn("assembleDebug")

    val fromApk = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk")
    val outDir = layout.buildDirectory.dir("outputs/apk/debug")

    from(fromApk)
    into(outDir)
    rename { "${appName}-v${verName}-debug.apk" }
}

tasks.register<Copy>("copyRenamedReleaseApk") {
    dependsOn("assembleRelease")

    val fromApk = layout.buildDirectory.file("outputs/apk/release/app-release.apk")
    val outDir = layout.buildDirectory.dir("outputs/apk/release")

    from(fromApk)
    into(outDir)
    rename { "${appName}-v${verName}-release.apk" }
}

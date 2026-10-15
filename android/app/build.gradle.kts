plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// versionName comes from CI (-PappVersionName=1.2.0); versionCode is derived from it.
val appVersionName = (findProperty("appVersionName") as String?) ?: "0.1.0-dev"
val appVersionCode = appVersionName.substringBefore('-').split('.').map { it.toInt() }
    .let { (major, minor, patch) -> major * 10_000 + minor * 100 + patch }
    .coerceAtLeast(1)

android {
    namespace = "online.codyuhi.desksetup"
    compileSdk = 37

    defaultConfig {
        applicationId = "online.codyuhi.desksetup"
        minSdk = 33
        targetSdk = 37
        versionName = appVersionName
        versionCode = appVersionCode
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Filament ships native code per ABI; both phones (and Apple-silicon emulators) are arm64.
        ndk { abiFilters += "arm64-v8a" }
    }

    // Release signing comes from the homelab key in Vault, injected by CI as env vars.
    signingConfigs {
        create("release") {
            val ks = System.getenv("ANDROID_KEYSTORE_PATH")
            if (ks != null) {
                storeFile = file(ks)
                storePassword = System.getenv("ANDROID_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("ANDROID_KEY_ALIAS")
                keyPassword = System.getenv("ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        // One app, one icon: debug shares the release application ID (create-android-app rule 8).
        debug {
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release").takeIf { it.storeFile != null }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = true
        warningsAsErrors = true
        checkDependencies = false
        // Version-bump nags would fail every build as releases land; versions are reviewed by hand.
        disable += setOf("GradleDependency", "NewerVersionAvailable", "AndroidGradlePluginVersion", "OldTargetApi")
        // Sideloaded onto arm64 Galaxy phones only; ChromeOS x86_64 isn't a target.
        disable += "ChromeOsAbiSupport"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3.adaptive)
    implementation(libs.androidx.material3.navigation.suite)
    implementation(libs.androidx.window)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.sceneview)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

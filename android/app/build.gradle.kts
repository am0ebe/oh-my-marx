import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// ⭐ th 1 version number · bump b4 bin/ship.sh · bin/lib.sh reads this line
val appVersion = "0.5.1"

android {
    namespace = "de.strix.marxversion"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.strix.marxversion"
        minSdk = 26
        targetSdk = 36
        // versionCode = minutes since 2026-01-01 ➡ evy build is "newer" than th last (dev + ship share 1 counter:
        // no downgrade error when th fon jumps between th dev channel and a release)
        versionCode = (System.currentTimeMillis() / 60_000L - 29_453_760L).toInt()
        // -Pdev (bin/deploy.sh) ➡ 0.5.1-dev.1103.0215 = a new version 4 Obtainium evy minute
        versionName = if (project.hasProperty("dev"))
            appVersion + "-dev." + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMdd.HHmm"))
        else appVersion
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // sideload 2 friends: debug-signed is fine 4 now (no Play Store)
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true  // BuildConfig.VERSION_NAME ➡ About screen
    }
}

kotlin {
    // must match compileOptions ⬆ (Java + Kotlin bytecode target)
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.work.runtime.ktx)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}

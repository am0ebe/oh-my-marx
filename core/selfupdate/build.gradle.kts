// 🆕 appkit :selfupdate — APK self-update 4 apps outside th stores (README.md § :selfupdate)
//   used by th app's settings.gradle.kts: include(":selfupdate") + projectDir ➡ this dir · versions = th app's libs catalog
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "appkit.selfupdate"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    testOptions { unitTests.isReturnDefaultValues = true }
}

kotlin {
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    api(libs.work.runtime.ktx)            // th app subclasses SelfUpdateWorker
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}

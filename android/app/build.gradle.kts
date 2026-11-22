plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// ⭐ th 1 version number = th last shipped (o about-to-ship) release · `bin/ship.sh minor|patch|major` bumps it
// dev builds (bin/deploy.sh) show th NEXT one: 0.8.0 shipped ➡ 0.9.0-dev.MMDD.HHMM (-PdevBase) · bin/lib.sh reads this line
val appVersion = "0.13.0"
// 🔢 versionCode (minutes) · DEV name (-Pdev -PdevBase) · 1st-entry APK stamp ➡ appkit (../appkit/README.md · D-079)
extra["appVersion"] = appVersion
extra["appStampKey"] = "ommBuild"
apply(from = rootProject.file("../appkit/tool/app-version.gradle.kts"))

// 💬 Matrix registration token (git-ignored file, see .gitignore) ➡ BuildConfig · missing ➡ comments off
val ommRegToken: String = rootProject.file("omm.properties").takeIf { it.exists() }
    ?.readLines()?.firstOrNull { it.startsWith("omm.regToken=") }?.substringAfter("=")?.trim() ?: ""

android {
    namespace = "de.strix.marxversion"
    compileSdk = 36

    defaultConfig {
        applicationId = "de.strix.marxversion"
        buildConfigField("String", "OMM_REG_TOKEN", "\"$ommRegToken\"")
        minSdk = 26
        targetSdk = 36
        versionCode = project.extra["appVersionCode"] as Int        // appkit: minutes since 2026-01-01, only goes up
        versionName = project.extra["appVersionName"] as String     // appkit: appVersion · -Pdev ➡ <devBase>-dev.MMDD.HHMM
    }

    buildTypes {
        // 🧪 dev builds (bin/deploy.sh) = a 2nd app "Oh My Marx DEV" next 2 th stable one:
        // own package ➡ own data + own Obtainium entry · label/shortcuts overridden i src/debug/res
        debug {
            applicationIdSuffix = ".dev"
        }
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
    // 🧹 CI gate (am0/ci app.yml `android`, @1108): errs known at onboarding live i th baseline ➡ only NEW ones fail
    //   fix one ➡ regen: ./gradlew updateLintBaseline · list ➡ todo_omm.md
    lint {
        baseline = file("lint-baseline.xml")
    }
    // 📸 headless screenshots (QA r4 @1113, no KVM ➡ no emulator): Robolectric native graphics renders th real app
    //   ./gradlew testDebugUnitTest --tests '*ShotsTest*' -Pshots ➡ app/build/shots/*.png · wo -Pshots th test skips
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { t ->
            t.maxHeapSize = "512m"   // D-030: Robolectric fork · CI 3g cap = gradle 1280m + this
            t.maxParallelForks = 1
            t.systemProperty("shots", if (project.hasProperty("shots")) "1" else "")
            t.systemProperty("shotsDir", layout.buildDirectory.dir("shots").get().asFile.path)
            // 💬 MatrixLiveTest = th real UI vs LIVE Synapse (real accounts) ➡ only via `bin/matrix_e2e.py --app` (-Plive)
            t.systemProperty("live", if (project.hasProperty("live")) "1" else "")
            t.systemProperty("liveDir", rootProject.file("../priv").path)   // state per run: matrix_e2e_app_<MMDD-hhmm>.json
        }
    }
}

// 🔢 Obtainium trap fix (@1107, 1st-entry stamp "ommBuild=…") = appkit app-version.gradle.kts (applied ⬆)

kotlin {
    // must match compileOptions ⬆ (Java + Kotlin bytecode target)
    compilerOptions { jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17) }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":selfupdate"))   // 🧩 appkit
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
    testImplementation(libs.org.json)  // android.jar stubs throw on JVM ➡ real org.json for MatrixTest
    // 📸 ShotsTest (see testOptions)
    testImplementation(libs.robolectric)
    testImplementation(platform(libs.compose.bom))
    testImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}

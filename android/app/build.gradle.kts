plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// ⭐ th 1 version number = th last shipped (o about-to-ship) release · `bin/ship.sh minor|patch|major` bumps it
// dev builds (bin/deploy.sh) show th NEXT one: 0.8.0 shipped ➡ 0.9.0-dev.MMDD.HHMM (-PdevBase) · bin/lib.sh reads this line
val appVersion = "0.14.0"
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
            // 🔍 CI log = only place a runner-only failure shows ➡ full message (dflt = "AssertionError at X.kt:95", msg dropped)
            t.testLogging { exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL }
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

// 🔒 night F12: exact resolved versions ➡ app/gradle.lockfile (ops' osv-scanner reads it, can't read libs.versions.toml)
//   holds :selfupdate's deps too (app's classpaths include them) ➡ no lockfile i th vendored kit
//   dep bump ➡ `./gradlew :app:dependencies --write-locks` + commit th lockfile (a drifted lock = build fails)
dependencyLocking {
    lockAllConfigurations()
    // Kotlin 2.x: stdlib-common = empty compat jar, resolved i/out o runtime classpaths by task order ➡ flaky lock (kotlin-stdlib stays locked)
    ignoredDependencies.add("org.jetbrains.kotlin:kotlin-stdlib-common")
}

// 🔒 D-086 (= cali D-068, ops build_vulns.md #346): indirect libraries o th build + tst tools w a known hole ➡ pinned 2 a fixed version
//   none o them is i th apk (Robolectric · Room's code generator · th Android plugin's device-tst runner),
//   but th push guard + th CI scan read th whole lock file · a tool bump th brings a fixed version itself ➡ drop its line
//   new finding ➡ raise th version here, then re-write th lock (cmd above)
val pinned = mapOf(
    "com.google.guava:guava" to "33.8.0-jre",             // CVE-2026-102554 (fixed 33.7.2)
    "org.bouncycastle:bcprov-jdk18on" to "1.87",          // CVE-2025-14813 · CVE-2026-8763 · -13506 · -5598 (fixed 1.85)
    "com.google.protobuf:protobuf-java" to "3.25.9",      // CVE-2024-7254 (fixed 3.25.5)
    "com.google.protobuf:protobuf-java-util" to "3.25.9", // same release train as protobuf-java
    "com.google.protobuf:protobuf-kotlin" to "3.25.9",
    "org.json:json" to "20260719",                        // GHSA-ghc9-756g-vrcv (Robolectric's 20240303 · omm only, cali hs no org.json)
)
configurations.configureEach {
    resolutionStrategy.eachDependency {
        val v = requested.version.orEmpty()
        pinned["${requested.group}:${requested.name}"]?.let { useVersion(it); because("known hole in the older version (D-086)") }
        // netty 4.1.x: findings across codec · http · http2 · handler (fixed up to 4.1.137) · netty-tcnative hs its own numbers
        if (requested.group == "io.netty" && v.startsWith("4.1.")) { useVersion("4.1.141.Final"); because("known holes in the older version (D-086)") }
    }
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

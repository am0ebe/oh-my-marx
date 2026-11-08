import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

// ⭐ th 1 version number = th last shipped (o about-to-ship) release · `bin/ship.sh minor|patch|major` bumps it
// dev builds (bin/deploy.sh) show th NEXT one: 0.8.0 shipped ➡ 0.9.0-dev.MMDD.HHMM (-PdevBase) · bin/lib.sh reads this line
val appVersion = "0.9.0"

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
        // versionCode = minutes since 2026-01-01 ➡ evy build is "newer" than th last (dev + ship share 1 counter:
        // no downgrade error when th fon jumps between th dev channel and a release)
        versionCode = (System.currentTimeMillis() / 60_000L - 29_453_760L).toInt()
        // -Pdev (bin/deploy.sh) ➡ 0.5.1-dev.1103.0215 = a new version 4 Obtainium evy minute
        versionName = if (project.hasProperty("dev"))
            (project.findProperty("devBase") as String? ?: appVersion) + "-dev." + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMdd.HHmm"))
        else appVersion
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
}

// 🔢 Obtainium trap fix (@1107): an Obtainium entry on DEFAULTS (URL pasted by hand, no version regex) "versions" th
//   APK by a hash o its 1st 1024 bytes only (Obtainium lib/providers/apps_provider.dart checkPartialDownloadHash) =
//   1st zip entry META-INF/…/app-metadata.properties + start o th library dex ➡ identical evy build ➡ nvr an update.
//   stamp versionName + versionCode into that 1st entry ➡ its CRC i th zip header changes per build ➡ new hash ➡
//   any entry updates (➕ link w regex o not) · check: bin/deploy.sh compares th 1st 1 KiB vs th live APK
val ommStamp = "${android.defaultConfig.versionName} ${android.defaultConfig.versionCode}"
tasks.matching { it.name.matches(Regex("write.+AppMetadata")) }.configureEach {
    inputs.property("ommStamp", ommStamp)   // new stamp ➡ task reruns (not UP-TO-DATE / fr cache)
    doLast {
        outputs.files.asFileTree.filter { it.name == "app-metadata.properties" }
            .forEach { it.appendText("ommBuild=$ommStamp\n") }
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

// 🔢 appkit app-version — 1 version number, a versionCode th only goes up, DEV names, a per-build APK stamp
//   apply i th app module, b4 android {}:
//     val appVersion = "1.2.0"                       // th LAST SHIPPED release · apptool ship bumps this exact line
//     extra["appVersion"] = appVersion
//     apply(from = rootProject.file("../appkit/tool/app-version.gradle.kts"))
//     android { defaultConfig { versionCode = project.extra["appVersionCode"] as Int; versionName = project.extra["appVersionName"] as String } }
//       (project.extra: inside defaultConfig {} a bare `extra` = th config's own, not th project's)
//   in  : extra appVersion · optional extra appStampKey (dflt "appBuild") · -Pdev -PdevBase=X.Y.Z (apptool deploy)
//   out : extra appVersionCode · appVersionName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val kitExtra = extensions.extraProperties
val kitBase = kitExtra["appVersion"] as String

// versionCode = minutes since 2026-01-01 ➡ evy build is "newer" than th last (dev + ship share 1 counter:
//   no downgrade error when a fon jumps between th dev channel and a release)
val kitCode = (System.currentTimeMillis() / 60_000L - 29_453_760L).toInt()
// -Pdev (apptool deploy) ➡ 0.5.1-dev.1103.0215 = a new version evy minute · devBase = th NEXT release (apptool picks it)
val kitName = if (project.hasProperty("dev"))
    (project.findProperty("devBase") as String? ?: kitBase) + "-dev." + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMdd.HHmm"))
else kitBase
kitExtra["appVersionCode"] = kitCode
kitExtra["appVersionName"] = kitName

// 🔢 Obtainium trap fix: an Obtainium entry on DEFAULTS (URL pasted by hand, no version regex) "versions" th APK by a
//   hash o its 1st 1024 bytes only (Obtainium lib/providers/apps_provider.dart checkPartialDownloadHash) = 1st zip entry
//   META-INF/…/app-metadata.properties + start o th library dex ➡ identical evy build ➡ nvr an update.
//   stamp versionName + versionCode into that 1st entry ➡ its CRC i th zip header changes per build ➡ new hash ➡
//   any entry updates (➕ link w regex o not) · check: apptool deploy compares th 1st 1 KiB vs th live APK
val kitStampKey = if (kitExtra.has("appStampKey")) kitExtra["appStampKey"] as String else "appBuild"
val kitStamp = "$kitName $kitCode"
tasks.matching { it.name.matches(Regex("write.+AppMetadata")) }.configureEach {
    inputs.property(kitStampKey, kitStamp)   // new stamp ➡ task reruns (not UP-TO-DATE / fr cache)
    doLast {
        outputs.files.asFileTree.filter { it.name == "app-metadata.properties" }
            .forEach { it.appendText("$kitStampKey=$kitStamp\n") }
    }
}

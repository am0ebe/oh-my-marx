# {{APP}}

<!-- 📄 appkit README template · copy 2 th app root, fill evy {{…}}, drop sections u don't need
     order (🦠 #327 @1122): install = APK + in-app update 1st · Obtainium 2nd "also works" · build it yourself -->

{{ONE_LINE: what it is, for whom}}

## Install

Same steps as the download page **<{{WEB}}/>**, in the same order:

**1. Download the APK** (recommended: the app updates itself)

1. On your Android phone open **<{{WEB}}/>** ➡ tap **Download**
2. Allow installing from this source (once) ➡ **Install**
3. Play Protect may warn about an "unknown app" (normal outside the Play Store) ➡ **Install anyway**
4. Updates: the app tells you when a new version is out (notification + banner) ➡ **Install** ➡
   confirm, your data stays. ⚙️ **Update automatically** = new versions install without asking (Android 12+)

**2. Also works: Obtainium** (if you already use it to update apps)

1. Get [Obtainium](https://github.com/ImranR98/Obtainium/releases/latest) (free, open source app updater)
2. In Obtainium tap ＋ and paste `{{WEB}}/` ➡ **Add** ➡ **Install**
   (or tap **➕ Add to Obtainium** on the download page)

**3. Or build it yourself** from this repo: see [below](#build-it-yourself).

Every version stays downloadable under [Releases]({{SRC}}/releases), with its notes.

## Specs

| | |
|---|---|
| **version** | single source: `appVersion` in [`android/app/build.gradle.kts`](android/app/build.gradle.kts) · release notes in-app and on each release |
| **platform** | Android 8.0+ (`minSdk 26`) |
| **stack** | Kotlin · Jetpack Compose · {{MORE}} |
| **package** | `{{PKG}}` · dev builds `….dev` install side by side |
| **license** | {{LICENSE}} |

## Build it yourself

**Easiest: Android Studio.** It brings its own JDK and Android SDK.

1. Install [Android Studio](https://developer.android.com/studio) and start it once (it downloads the SDK)
2. `git clone {{SRC}}`
3. Android Studio ➡ **Open** ➡ pick the `android/` folder ➡ wait for the Gradle sync
4. Phone: Settings ➡ About ➡ tap *Build number* 7× ➡ Developer options ➡ **USB debugging** on ➡ plug in
5. Press ▶ **Run**. The app lands on your phone.

### Versions and releases

One version number: `val appVersion = "x.y.z"` in `android/app/build.gradle.kts`, the last shipped release.
Nobody edits it by hand: `apptool ship minor|patch|major` bumps it. Two channels, two apps on the phone:

| | command | version | goes to | phone |
|---|---|---|---|---|
| 🧪 dev | `bin/deploy.sh` (`-a` = also install over wireless adb) | next release + `-dev.MMDD.HHMM` | `{{WEB}}/dev/` | "{{APP}} DEV" (`….dev`), updates itself |
| 📦 stable | `bin/ship.sh minor` | `x.y.z` | `{{WEB}}/` + git tag + GitHub release | "{{APP}}" |

`versionCode` = minutes since 2026-01-01, so every build counts as newer and switching channels never needs a
downgrade. The app's own updater checks `version.json` next to the download page on start + daily: a notification
and a one-tap install, silent on Android 12+ with ⚙️ "Update automatically". Stores and Obtainium keep their own job.

Release cycle: code ➡ `bin/deploy.sh` ➡ QA on the phone ➡ release notes for the new version ➡ commit + push ➡
`bin/ship.sh minor` (refuses a dirty tree, an unpushed HEAD, missing notes or an already-shipped version).

## bin

| script | what |
|---|---|
| `deploy.sh` | 🧪 dev build ➡ `{{WEB}}/dev/` (= `appkit/tool/apptool deploy`) |
| `ship.sh` | 📦 release ➡ page + tag + GitHub release (= `appkit/tool/apptool ship`) |

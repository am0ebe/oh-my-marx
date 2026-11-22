# 🧰 appkit — 1 kit 4 all our Android apps

**What:** everything general fr building our apps, so app #3 starts fr th kit, not fr a copy o app #1. An app = **config** (package · name · page url · key path), no app names inside th kit.
**4 shelves** (pr/appkit D-001): 🔧 [tool/](tool/) ship + deploy · 📄 [templates/](templates/) README + web page · 🪞 [lessons/](lessons/) wh we L'd · 🧩 [core/](core/) shared code (updater now · chat nxt)
**Status:** built inside omm (`pr/omm/appkit/`, #327 @1122, omm D-079) · omm consumes it, tsts green · lifts 2 `pr/appkit/` ➡ § lift-over

## ⚡ adopt it i a new app (5 lines)
1. vendor th kit at `<app>/appkit/`: `git subtree add --prefix appkit <appkit repo> main --squash` (= plain files ➡ public clone + CI build wo extra steps)
2. `android/settings.gradle.kts`: `include(":selfupdate")` + `project(":selfupdate").projectDir = file("../appkit/core/selfupdate")` · catalog: `android-library` plugin (same AGP) · app: `implementation(project(":selfupdate"))`
3. `android/app/build.gradle.kts`: `val appVersion = "0.1.0"` + `extra["appVersion"] = appVersion` + `apply(from = rootProject.file("../appkit/tool/app-version.gradle.kts"))` ➡ `versionCode/versionName = project.extra["appVersionCode"/"appVersionName"]` · `debug { applicationIdSuffix = ".dev" }`
4. Kotlin: `class UpdateWorker(c, p) : SelfUpdateWorker(c, p)` · `Update.init(UpdateConfig(web, versionCode, …))` + `scheduleUpdate(this)` i `Application.onCreate` · show `Update.check()` as a banner ➡ ur own dialog (download ➡ `installSession`) · FileProvider `${applicationId}.files` w `<cache-path name="update" path="update/"/>`
5. `bin/lib.sh` = config (`APP FILE PKG WEB DIRS SRC` + optional, § tool) · `bin/deploy.sh` / `bin/ship.sh` = `exec appkit/tool/apptool deploy|ship "$@"` · page + README fr [templates/](templates/) (o ur own `page()`)

Reference app: omm (`bin/lib.sh` · `android/…/notify/UpdateWorker.kt` = its whole config · `ui/UpdateDialog.kt` = its own skin).

---

## 🧩 core/selfupdate — APK self-update (Android lib module `:selfupdate`, pkg `appkit.selfupdate`)
- **wh:** stable app ➡ `<web>/version.json` · DEV app ➡ `<web>/dev/version.json` · on app start + daily (WorkManager) · by installer: browser/adb ➡ notification ➡ th app's dialog ➡ download (sha256) ➡ PackageInstaller (1st update 1 confirm, then silent on 12+ if `auto`) · Obtainium ➡ `obtainium://refresh` · store ➡ nothing
- **app gives** `UpdateConfig`: `web` · `versionCode/Name` · `dev` · `appName` · `icon` · `worker` (own subclass = own class name, WorkManager keeps finding th job) · `openUpdate/openApp` intents · `de` / `auto` (read th app's settings) · `onReplaced` hook · `prefs/work/sessionName` (🔒 keep once shipped)
- **kit gives** `Update` (check · download · installSession · via · silentOk · devLatest · installError) · `checkAndNotify` · `scheduleUpdate` · `InstallReceiver` + `UpdatedReceiver` + perms (manifest merge)
- **app keeps** th dialog / banner (its design system) · tsts: kit `UpdateTest` (pure JVM) · app `UpdateWorkerTest` (Robolectric, its config)

## 🔧 tool — `apptool` (ship · deploy · page · oblink · version)
- `tool/apptool` = CLI (run fr th app root, `-h` evy cmd) · `tool/apptool.sh` = lib (`version_json` · `oblink` · `publish` · `app_version` · `enc` · JAVA_HOME/ANDROID_HOME dflts) · `tool/app-version.gradle.kts` = version scheme (last-shipped `appVersion` · minutes `versionCode` · `-Pdev -PdevBase` names · 1st-entry stamp 4 Obtainium's 1 KiB hash)
- **app's `bin/lib.sh`** (sourced): must `APP FILE PKG WEB DIRS SRC` · optional `AUTHOR` · `NOTES` (cmd: notes 4 a version, dflt `bin/notes.py`) · `SHIP_GUARD` (cmd run b4 a bump) · `GH_REPO` (dflt fr SRC) · `GH_REMOTE` (gh) · `MAIN_ACTIVITY` · `ANDROID_DIR` (android) · `MODULE` (app) · `FAVICON` · `TAGLINE FGJ MAIL` (tpl page)
- **hooks** (optional fns): `page OUT VER APK [dev]` (must call `version_json`, apptool checks) · `publish_dir OUT SUB DIR` · `publish_done OUT SUB` · `tools` (help footer)

## 📄 templates
- [page.sh](templates/page.sh) = dflt download page (`page()`): APK + in-app update 1st · Obtainium 2nd "also works" · build it yourself · what's new · DEV page
- [README.md](templates/README.md) = app README skeleton, same order · fill `{{…}}`

## 🪞 lessons
- [lessons.md](lessons/lessons.md) = 🔝 8 principles · 🔄 do differently · 📥 inbox (refl ➡ distill: seen i a 2nd app ➡ promote) · [fon_debug.md](lessons/fon_debug.md) = cable-free adb

---

## 📋 wh else is general — in / later / no
| item | shelf | verdict | y |
|---|---|---|---|
| `:selfupdate` (Update · worker · receivers) | 🧩 | ✔️ in | 2nd app (cali) wants it · 🦠 #327: evy app ships w th updater |
| `apptool` deploy · ship · page · oblink | 🔧 | ✔️ in | cali copied it @1122 (#326) = 2nd copy |
| `app-version.gradle.kts` | 🔧 | ✔️ in | same copy |
| page tpl · README tpl | 📄 | ✔️ in | 🦠 D-001 · install order = 1 decision 4 all apps |
| lessons (omm app_lessons + cali lessons/fon_debug) | 🪞 | ✔️ in | 🦠 D-001 |
| update dialog UI (Compose) | 🧩 | ⏳ later | needs a skin slot (each app's design system) · logic alr i th kit |
| 💬 **chat (Matrix)** | 🧩 | ⏳ later | big, own step · sketch ⬇ |
| `:testrig` (Robolectric shots harness · prefs reset · design-guard ratchet) | 🧩 | ⏳ later | cali hs its own ShotTest ➡ extract on th 2nd real copy |
| i18n kit (`tr()` + translate `--only ui` + check_langs + hand_fix) | 🧩🔧 | ⏳ later | cali = DE + EN only v1 ➡ no 2nd user yet |
| release notes (`Changelog` `Release(…)` + `notes.py`) | 🔧 | ⏳ later | apptool takes any `NOTES` cmd now · fmt shared when cali ships notes |
| cali's apptool extras: `-n` dry-run · release key (`-k`) · tow page (`deploy -p`) · `-s` status · `-w -y` 1st-public gate | 🔧 | ⏳ when cali adopts | cali's copy grew them @1122 ➡ fold into apptool i cali's adopt order (#328), omm gains them too |
| CI (`am0/ci` `app.yml`) | — | ✔️ alr shared | lives i ops |
| matrix_e2e live tst · content pipeline (fetch/split/align/score) · Hooked mode | — | ❌ no | omm-only |

**💬 chat sketch (later, don't extract yet):**
1. `core/matrix` lib module ⬅ omm `data/Matrix.kt` (register w reg token · access token · send/redact/paginate · deactivate + redact-all D-076) + `KreisWorker` poll · app = config (homeserver · reg token · room alias prefix · display-name rules)
2. app keeps UI (Comments sheet · Kreis screen) like th update dialog · tsts: omm `MatrixTest` moves w it, `MatrixLiveTest` stays app (live server)
3. trigger = 2nd app wants chat · open Qs 1st: 1 homeserver 4 all apps? room naming per app · report/mod gate (omm D-073)

## 🚚 lift-over 2 `pr/appkit/` (ready cmds · M8 / appkit crew runs them, not omm)
```sh
cd ~/gopro/go/pr/omm && git subtree split -P appkit -b appkit-split        # kit-only history
cd ~/gopro/go/pr/appkit && mv README.md README_scaffold.md && git init -b main
git pull ~/gopro/go/pr/omm appkit-split                                    # kit files at th root (core/ tool/ templates/ lessons/ README.md)
# fold README_scaffold.md (🤖 Agents · places) into README.md by hand ➡ rm README_scaffold.md
git add -A && git commit -m "appkit: scaffold + kit fr omm #327"            # adr · notes · ref · todo join
# ops: Forgejo repo am0/appkit (private til 🦠 says public) ➡ git remote add origin … && git push -u origin main
cd ~/gopro/go/pr/omm && git rm -rq appkit && git commit -m "appkit ➡ pr/appkit"
git subtree add --prefix appkit <forgejo url o ~/gopro/go/pr/appkit> main --squash   # same paths ➡ 0 code change
(cd android && ./gradlew testDebugUnitTest lintDebug) && python3 -m unittest bin/test_bin.py   # green ➡ push
# later updates i any app: git subtree pull --prefix appkit <url> main --squash
```

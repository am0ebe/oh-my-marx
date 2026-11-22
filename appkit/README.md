# 🧰 appkit — 1 kit 4 all our Android apps

**What:** everything general fr building our apps, so app #3 starts fr th kit, not fr a copy o app #1. An app = **config** (package · name · page url · key path), no app names inside th kit.
**4 shelves** (pr/appkit D-001): 🔧 [tool/](tool/) ship + deploy · 📄 [templates/](templates/) README + web page · 🪞 [lessons/](lessons/) wh we L'd · 🧩 [core/](core/) shared code (updater now · chat nxt)
**Status:** active · own git repo `pr/appkit/` since @1122 (#329, lifted fr omm, history kept) · Forgejo remote = ops order, private til 🦠 says public · apps vendor it at `<app>/appkit/` (git subtree) ➡ omm ✔️ · cali nxt (#328)
**Stack:** bash (apptool) · Gradle Kotlin DSL · Android lib module (Kotlin, WorkManager, PackageInstaller) · runs on tow (build + upload) + th fon (updater)

## ⚡ adopt it i a new app (5 lines)
1. vendor th kit at `<app>/appkit/`: `git subtree add --prefix appkit ~/gopro/go/pr/appkit main --squash` (= plain files ➡ public clone + CI build wo extra steps · § 🔄)
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

## ➕ wh a new extra goes (rule 4 evy app, cali 1st)
_🎯 1 copy o evy general thing · an extra nvr changes wh oth apps do unless they opt in_
- **general** (any app cd want it) ➡ th kit: a flag o subcmd i `tool/apptool` (`-x, --long` on 1 help line) · behaviour behind a `bin/lib.sh` config var whose **dflt = today's behaviour** ➡ apps th don't set it see no change
- **app-only** ➡ th app's `bin/lib.sh`: a hook (`page` · `publish_dir` · `publish_done` · `tools`) · need a new hook point? add `declare -F <hook> >/dev/null && <hook> …` i apptool (no-op 4 oth apps)
- **shared code** ➡ `core/<name>/` lib module, app = 1 config object (like `UpdateConfig`) · app keeps its UI skin
- **lesson** ➡ 1 line i `lessons/lessons.md` § 📥 inbox w src
- cali's extras (#328) ➡ where:
  | extra | goes 2 | config / flag (dflt = no change 4 omm) |
  |---|---|---|
  | dry-run | `apptool ship` + `deploy` | `-n, --dry-run` = build + render, nothing leaves th box |
  | release key | `apptool ship` + app gradle `signingConfig` | `RELEASE_KEY=<jks path>` · empty = debug key (omm today) · `-k, --key` = show/ck it |
  | tow page (tailnet, no adb) | `apptool deploy` | `-p, --publish` ➡ `LOCAL_DIRS` / `LOCAL_WEB` (unset = flag errors) |
  | status | new subcmd `apptool status` (`-s` alias i deploy) | read-only: fon · live versions · local build |
  | public gate (1st upload) | `publish()` | `PUBLIC_GATE=1` ➡ a new web dir needs `-y, --yes-public` once · unset = omm today |
- 🧪 evy kit change: kit tsts (`./gradlew :selfupdate:testDebugUnitTest` fr an app) + th changing app's full tsts green ➡ push kit ➡ oth apps catch up on their nxt `subtree pull`

## 🔄 kit ⬄ apps (git subtree, src = `~/gopro/go/pr/appkit` til th Forgejo remote exists)
```sh
git subtree add  --prefix appkit ~/gopro/go/pr/appkit main --squash       # adopt (once, clean tree)
git subtree pull --prefix appkit ~/gopro/go/pr/appkit main --squash       # get kit updates
git subtree push --prefix appkit ~/gopro/go/pr/appkit from-<app>          # fix made inside <app>/appkit/ ➡ a branch (main is checked out)
cd ~/gopro/go/pr/appkit && git merge from-<app> && git branch -d from-<app>   # ➡ kit main
```
- prefer fixing i `pr/appkit/` 1st, then `subtree pull` i each app · lift fr omm done @1122 (#329: `git subtree split -P appkit` ➡ `git pull` here ➡ omm `git rm -r appkit` + `subtree add`)

## 🤖 Agents
- **Access:** local only (tow) · no remote yet (Forgejo = ops, private til 🦠 says public) · `apptool` uploads via `ssh vps` (th app's `DIRS`)
- **Rules:** 0 app names inside th kit (`grep -ri "<app>" core tool templates` = 0) · a change must keep evy vendoring app green · storage names i `UpdateConfig` (prefs · work · worker class) nvr change once an app shipped · push = 🦠 ok-tier like any repo · public = 🦠 1st
- **Truth:** `git log` here · apps: `git log --oneline -- appkit` (last subtree pull) · `appkit/tool/apptool -V` · lookup ➡ [ref.md](ref.md)

## Important places
| what | where |
|------|-------|
| `README.md` | this page — entry point · adopt recipe · extras rule |
| `adr.md` | intent + decisions (D-001 four shelves · D-002 where it lives) |
| `todo_appkit.md` | status board |
| `notes.md` · `ref.md` | working notes · lookup |
| `core/` `tool/` `templates/` `lessons/` | th 4 shelves |
| apps | omm `pr/omm/appkit/` (ref app) · cali (#328) |

## Maintenance
- nothing runs by itself · on us: evy kit change ➡ `subtree pull` i each app + its tsts · lessons inbox ➡ promote when an item shows up i a 2nd app

## Secrets
- none i th kit · signing keys + reg tokens stay i each app's git-ignored files (`~/.android/*.jks` · `android/*.properties`), th kit only reads a path fr config

## More
Part of a personal knowledge base (`go/pr/appkit`). Notes use a compressed shorthand — symbols/abbreviations are explained in `go/pr/kb/abbrev.md`.

---
tags: [wiki, reference]
---
# ☭ omm — wiki entry
_Oh My Marx: Kapital Bd.1 as bible-style Android app · Kotlin + Compose · @1106_
_entry page = click-thru start · details live i th linked files · design + decisions ➡ [🔗 notes.md](notes.md) · board ➡ [🔗 todo_omm.md](todo_omm.md) · public (EN) ➡ [🔗 README.md](README.md)_

## 🧩 big picture
- **offline app**: whole book i th APK (`android/app/src/main/assets/content/*.json`) · built by th Python pipeline `bin/` + local ollama
- **2 apps on th fon**, side by side, own data each:
  - **Oh My Marx** (`de.strix.marxversion`) = stable · friends
  - **Oh My Marx DEV** (`….dev`) = evy dev build · QA
- **2 channels on th VPS** (`/srv/www/oh-my-marx`):
  - `scagent.de/oh-my-marx/` = download page + apk + `version.json` · short link `scagent.de/omm`
  - `…/dev/` = bare Obtainium source 4 DEV (no real page)
- **online bits**: update banner (reads `version.json`) · omm_api counter · Matrix (comments + 👥 Kreise) ➡ server side = ops ([🔗 server/SERVICE.md](server/SERVICE.md))

## 🔢 versions — how th number works
- **1 number**: `val appVersion = "x.y.z"` i `android/app/build.gradle.kts` = th **last shipped** release
- 🚫 nvr bump by hand: `bin/ship.sh minor` (feats) · `patch` (fixes) · `major` ➡ bumps fr th last tag + commits + pushes + ships
- dev build = **next** release + `-dev.MMDD.HHMM`, automatic (`deploy.sh` ➡ `-PdevBase`)
  - 0.8.0 shipped ➡ dev = `0.9.0-dev.1107.1430` = "test build o th coming 0.9.0"
- `versionCode` = minutes since 2026-01-01 ➡ evy build counts as newer · dev ⬄ stable nvr a downgrade err
- 🆕 in-app update banner = only 4 hand-installed APKs · Obtainium / F-Droid / store installs + DEV app ➡ nvr (`data/Update.kt`)

## 🌊 release wflo
1. code ➡ `bin/deploy.sh` (`-a` = + wireless adb now) ➡ fon: Obtainium ➡ pull 2 refresh ➡ tap **Update** on th DEV entry
2. QA on th fon ➡ [🔗 notes.md § 🧪 QA checklist](notes.md) ➡ fix ➡ back 2 step 1
3. new/changed UI strings? ➡ `bin/translate.py --only ui` (~1 min)
4. `Release("0.9.0", …)` on top o `ui/Changelog.kt` (= in-app + GitHub + page notes via `bin/notes.py`)
5. commit + push ➡ `bin/ship.sh minor` ➡ bump + tag + page + GitHub release
6. 📣 friends: `scagent.de/omm`

## 🌐 download page (`bin/lib.sh` `page()`)
- Obtainium 1st · APK tabs **Stable | 🧪 Dev** (Dev tab reads `dev/version.json` live) · build-it-urself tabs GitHub | Forgejo (brand icons, `bin/icons/` = Simple Icons CC0, inline)
- 📣 share = 1 btn ➡ panel (`<dialog>`, fon + PC): Fediverse (Share₂Fedi) · Bluesky · Telegram · Reddit · mail · WhatsApp (last) · copy link · QR (`qrencode` at render) · "More apps…" = native sheet, touch only (Signal/Matrix go thru it, no web share URL)
- what's new = newest version open + "Older versions" fold · no emoji · same i th app (⚙️)
- 🃏 link preview = `bin/og.png` (1200×630, OG + Twitter tags) ➡ th card Signal / Telegram / Mastodon show = th real viral lever
- 🤖 agents: `llms.txt` + JSON-LD `MobileApplication` + `version.json` · static, no JS needed 4 content
- `/dev/` = small page + Obtainium source 4 th DEV app (1 apk link)
- ⚠️ **Obtainium trap** (@1107): pasting th bare URL ➡ HTML source w/o version regex ➡ "version" = hash o th APK's 1st bytes ➡ **identical across our builds** (checked: 1st 1 MB o 2 dev APKs byte-equal) ➡ nvr sees an update, Install greyed
  - fix: version regex `OhMyMarx-(.+)\.apk` (+ match group `$1`) i th app's Obtainium settings · th page's ➕ buttons carry it (`oblink` i `bin/lib.sh`, + `APKLinkHash` fallback)

## ⭐ most important parts
| part | Y it matters | doc |
|---|---|---|
| signing key (`~/.android/debug.keystore`) | lose it ➡ friends can't update (uninstall = data gone) | [🔗 notes.md § 🔑 keystore](notes.md) |
| `appVersion` + Changelog.kt | Obtainium + banner only see a NEW version | ⬆ § 🔢 |
| content pipeline `bin/` | th book itself · re-run order matters | [🔗 notes.md § 🔄 pipeline](notes.md) |
| `ship.sh` guards | tag = exactly th shipped code | `bin/ship.sh -h` |

## 🔑 passwords · tokens
- 🔐 **hu-held**: keystore ➡ symlink ➡ `~/dotfiles/secrets/android/omm.keystore` (ST, git-ignored) + key-mg copy · `gh` auth (GitHub releases)
- 🤖 **box-minted**: Matrix registration token ➡ `android/omm.properties` (git-ignored) ➡ BuildConfig · missing = comments off

## ⌨️ most important cmds
```sh
bin/deploy.sh -a                          # 🧪 dev build ➡ /dev + install over wireless adb
bin/ship.sh                               # 📦 release (clean + pushed tree, new version)
bin/ship.sh -p                            # re-render only th download page
bin/translate.py --only ui                # new UI strings ➡ it es pt zh ar
bin/translate_full.py --stats             # whole-book translation coverage
bin/translate_full.py --export            # ➡ assets full_<lang>.json (aft any re-translation)
(cd android && ./gradlew testDebugUnitTest)
```

## 🔧 scripts
- full list w 1 line each ➡ `bin/deploy.sh -h` (bottom) · pipeline order ➡ [🔗 notes.md § 🔄 pipeline](notes.md)

## 🧹 maintenance
- auto: `deploy.sh`/`ship.sh` keep th newest 3 apks per dir on th VPS · GitHub releases keep evy version
- on u: QA b4 ship · triage feedback (in-app ➡ priv inbox · `omm@scagent.de` · GitHub issues) ➡ [🔗 notes.md § feedback](notes.md)

## 🛠️ install · restore
- build fr src ➡ [🔗 README § Build it yourself](README.md#build-it-yourself)
- new box: JDK 21 at `~/.local/opt/jdk21` · Android SDK `~/Android/Sdk` (paths i `bin/lib.sh`) · keystore symlink (⬆ 🔑) · `ssh vps` works · `gh auth login`

## 📚 deep docs
| wh | file |
|---|---|
| concept · decisions · research | [🔗 notes.md](notes.md) |
| raw idea | [🔗 plan.md](plan.md) |
| chapter map (MEW pages) | [🔗 kapital_toc.md](kapital_toc.md) |
| server side | [🔗 server/README.md](server/README.md) · [🔗 server/SERVICE.md](server/SERVICE.md) |

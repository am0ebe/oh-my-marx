# 📱 app lessons — wh omm taught us 4 th nxt app
_@1121 (M8 #297) · src = [adr.md](adr.md) D-001..072 · refl log `pr/sys/refl/log*.md` (~30 omm blocks @1104-1121) · git (259 commits, 11 tags i 3 wk) · 1st reader: cali P1 · cali's own pick fr this ➡ [../cali/lessons.md](../cali/lessons.md) (stack + UX, @1120) · th file = general + process_

## 🔝 th 8 th wd hv saved most ti
1. 🔑 **prove th fix on th target, don't trust a proxy**: "compiles ✔️" claimed 2× off a grep while gradle died on JAVA_HOME · Obtainium "fixed" 3× on guessed causes ➡ only th measured cause (1st KiB hash, D-008) worked
2. 🏛️ **stable ids + versioned content fr day 1**: users' marks keyed by verse id + plan days by index ➡ 2 retrofits (D-028 id moves · D-042 plan archive), both lock-in forever
3. 🆕 **distribution = day-1 infra**: 2 app ids (stable + `.dev`) · version stamp · updater by installer ➡ omm spent @1104-1114 + 4 Ds (D-008/021/023/038) getting updates 2 just arrive
4. 🛡️ **evy claim th can lie gets a guard script**: changelog said "whole book" 4 ur/hi/bn wo files ➡ `check_langs.py` · design drift ➡ `DesignGuardTest` ratchet · a caveman "o" leaked into a DE UI string
5. 🎨 **design system + gesture grammar b4 screens**: tap/turn rules went D-022 ➡ 044 ➡ 057 ➡ 065 (4 rounds) · 21 text sizes + ~60 hard colours b4 tokens (D-029)
6. 📸 **headless shots early, but shot input = th app's call path**: `.take(30)` titles masked th bug th shot was built 2 show · `-Pshots` missing = silent skip + old PNGs stay
7. 📱 **Robolectric ≠ device ➡ name th device items**: Orden popup froze th app (0.8.0) · silent install · gestures · widget = only th fon proves them ➡ `🦠🧪` list per DEV build
8. 🐢 **LLM batch jobs = bg + guarded**: translate ran 56 min i th foreground (600 s cap) · short GPU job queued behind th night job · tok/s meter counted only successes ➡ looked like a slow GPU 4 days

---

## ✔️ reuse as-is
| wh | file(s) | note |
|---|---|---|
| stack | `android/` Kotlin + Compose, offline, whole content i assets (D-003) | server optional, degrade gracefully |
| DEV + stable side by side | `android/app/build.gradle.kts` (`applicationIdSuffix = ".dev"` · `-PdevBase`) | own data each · DEV = nxt version + `-dev.MMDD.HHMM` |
| version scheme | same file: `val appVersion` = last shipped · `versionCode` = minutes since 2026-01-01 | evy build newer · nvr bump by hand · [ref.md § 🔢](ref.md) |
| Obtainium | `ommStamp` task (build.gradle.kts) + `oblink` i `bin/lib.sh` + 1st-KiB guard i `bin/deploy.sh` | trap: Obtainium hashes th APK's 1st 1 KiB ➡ stamp it · [ref.md § 🌐](ref.md) |
| self-updater | `data/Update.kt` · `notify/UpdateWorker.kt` · `ui/UpdateDialog.kt` · `UpdatedReceiver` (manifest) | by installer o record: Obtainium = poke `obtainium://refresh` · self = PackageInstaller, silent on 12+ · store = nothing (D-023/038) |
| dev loop | `bin/deploy.sh` (`-a` adb · `-q` no launch · `--qr`) | `-q` = don't hijack th fon screen while 🦠 uses it |
| release | `bin/ship.sh` (bump + tag + APK + page + GH release) · `bin/notes.py` ⬅ `ui/Changelog.kt` | 1 notes src 4 app · GitHub · page · refuses dirty tree / shipped version |
| download page | `bin/lib.sh` `page()` · `bin/og.png` · `bin/icons/` | Obtainium 1st · OG card = th share lever · `llms.txt` + JSON-LD |
| CI | `.forgejo/workflows/ci.yml` ➡ `am0/ci` `app.yml@v1` (`android:` input) · `app/lint-baseline.xml` | lint = only NEW errs · ⚠️ 3g memcg ➡ keep th `-Xmx1280m --max-workers=2` override (ops D-030) |
| screenshot tsts | `test/…/ShotsTest.kt` (Robolectric native graphics, `-Pshots` ➡ `app/build/shots/`) (D-019) | no KVM on tow ➡ no emulator · ck PNG timestamps b4 eyeballing |
| design guard | `test/…/DesignGuardTest.kt` + [design_system.md](design_system.md) + `ui/ds/` | ratchet both ways: count down ➡ baseline down i th same commit |
| live e2e | `bin/matrix_e2e.py` (`--app` = `MatrixLiveTest` real UI vs live server, `-Plive`) (D-070) | by hand, not CI · state per run i `priv/` + `--cleanup` |
| i18n | `T("de","en")` / `tr()` inline (D-010) ➡ `bin/translate.py --only ui` ➡ `bin/check_langs.py` (ship guard) · `data/i18n/hand_fix.json` (D-069) | ⚠️ no `$var` inside `tr()` · fix LLM junk i th table, not th cache |
| QA loop | `qa_MMDD.md` (🦠 ticks on th fon) | 1 doc per DEV build · `[x]` + 1 word |

## 🔄 wh we'd do differently
- 🏛️ **ids**: key user state by a content hash / stable id fr commit 1 · plan-like content = versioned (cut archive) b4 th 1st friend installs
- 🆕 **updater + stamp i P1**, not aftr 3 ships · tst it end 2 end on th fon once (D-023 + D-038 = "phone test pending" 4 days)
- 🎨 **mockup decides look + gestures**, code follows · 72 Ds i 3 wk, 10 superseded o amended (D-022/033/044/051/059 …) = churn paid i code
- 🛠️ **env i 1 place**: gradle needs `JAVA_HOME` (only `bin/lib.sh` sets it) ➡ bare `./gradlew` failed i ≥5 seshs ➡ nxt app: a `bin/gw` wrapper o `org.gradle.java.home` i `gradle.properties` fr day 1
- 🧪 **glue tsts early**: `Update.kt` · `notes.py` · `page()` had 0 tsts @1105 ➡ page bug caught by curl only · later `test_bin.py` (55) + `UpdateWorkerTest` covered it
- 🧪 **Robolectric hygiene**: prefs + statics leak betw methods ➡ 1 red = 3 more red · reset prefs i `@Before` fr th start
- 🚦 **"QA b4 ship" needs an override phrase**: 0.12.0 shipped on a "ship" order b4 🦠's fon ck o 0.11 lanes
- 🔥 **hot files** (`Changelog.kt` · `strings.xml` · i18n json · big screens) = 1 owner per lane, fan-in only (adr § arch) · i18n json = top-churn files (~47 commits each)
- 📝 **user-facing strings = plain words**, nvr caveman ("Lesezeichen o Notiz" ➡ extra LLM pass)
- 🤖 **GPU jobs**: run short ones b4 handing over th long night job · `at` + `bin/gpu_night.sh` · log per-call duration

## 🧩 shared-code candidates (named, not extracted)
_🔵 i [todo_omm.md](todo_omm.md) · extract when app #2 (cali) needs it, not b4 — copy 1st, extract on th 2nd copy_
- `apptool` (sh, multi-cmd per sys D-037): `deploy` · `ship` · `page` · `oblink` · `publish` ⬅ `bin/lib.sh` + `bin/deploy.sh` + `bin/ship.sh` · per-app config = pkg · name · web dir
- `:selfupdate` (Android lib module): `Update.kt` + `UpdateWorker.kt` + `UpdateDialog.kt` + `UpdatedReceiver` · input = channel URL
- `app-version.gradle.kts` (convention snippet): `appVersion` line · minutes `versionCode` · `devBase` · `.dev` suffix · 1st-entry stamp · `-Pshots` / `-Plive` props
- `:testrig` (test lib): Shots harness (native graphics · `shotsDir`) · `DesignGuardTest` ratchet · prefs reset rule
- i18n kit: `I18n.kt` `T()/tr()` + `translate.py --only ui` + `check_langs.py` + `hand_fix.json`
- release notes: `Changelog.kt` `Release(…)` fmt + `notes.py` (md · `--html` · `-p/--plain`)
- ✔️ alr shared: CI (`am0/ci` `app.yml`) · only th gradle JVM override still lives here (move 2 `app.yml` dflt = ops)

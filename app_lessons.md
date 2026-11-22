# 📱 app lessons — wh omm taught us 4 th nxt app
_@1121 (M8 #297) · @1122 #327: th general part (🔝 8 principles · 🔄 do differently · 🧩 shared-code list) moved 2 th kit ➡ **[appkit/lessons/lessons.md](appkit/lessons/lessons.md)** + [appkit/README.md § in / later / no](appkit/README.md) · th file keeps only omm's own file map (where 2 look i omm 4 each pattern)_

## ✔️ reuse as-is — omm's file map
| wh | file(s) | note |
|---|---|---|
| stack | `android/` Kotlin + Compose, offline, whole content i assets (D-003) | server optional, degrade gracefully |
| DEV + stable side by side | `android/app/build.gradle.kts` (`applicationIdSuffix = ".dev"`) | own data each · DEV = nxt version + `-dev.MMDD.HHMM` |
| version scheme | 🧰 `appkit/tool/app-version.gradle.kts` (omm: `val appVersion` + `apply(from …)`) | evy build newer · nvr bump by hand · [ref.md § 🔢](ref.md) |
| Obtainium | 🧰 stamp i `app-version.gradle.kts` + `oblink` i `appkit/tool/apptool.sh` + 1st-KiB guard i `apptool deploy` | trap: Obtainium hashes th APK's 1st 1 KiB ➡ stamp it · [ref.md § 🌐](ref.md) |
| self-updater | 🧰 `appkit/core/selfupdate` · omm config `notify/UpdateWorker.kt` · omm skin `ui/UpdateDialog.kt` | by installer o record: Obtainium = poke `obtainium://refresh` · self = PackageInstaller, silent on 12+ · store = nothing (D-023/038/079) |
| dev loop | `bin/deploy.sh` = 🧰 `apptool deploy` (`-a` adb · `-q` no launch · `-r` QR) | `-q` = don't hijack th fon screen while 🦠 uses it |
| release | `bin/ship.sh` = 🧰 `apptool ship` · `bin/notes.py` ⬅ `ui/Changelog.kt` | 1 notes src 4 app · GitHub · page · refuses dirty tree / shipped version |
| download page | `bin/lib.sh` `page()` (omm's own look) · `bin/og.png` · `bin/icons/` · 🧰 plain version = `appkit/templates/page.sh` | APK + in-app update 1st, Obtainium 2nd (#327) · OG card = th share lever · `llms.txt` + JSON-LD |
| CI | `.forgejo/workflows/ci.yml` ➡ `am0/ci` `app.yml@v1` (`android:` input) · `app/lint-baseline.xml` | lint = only NEW errs · ⚠️ 3g memcg ➡ keep th `-Xmx1280m --max-workers=2` override (ops D-030) |
| screenshot tsts | `test/…/ShotsTest.kt` (Robolectric native graphics, `-Pshots` ➡ `app/build/shots/`) (D-019) | no KVM on tow ➡ no emulator · ck PNG timestamps b4 eyeballing |
| design guard | `test/…/DesignGuardTest.kt` + [design_system.md](design_system.md) + `ui/ds/` | ratchet both ways: count down ➡ baseline down i th same commit |
| live e2e | `bin/matrix_e2e.py` (`--app` = `MatrixLiveTest` real UI vs live server, `-Plive`) (D-070) | by hand, not CI · state per run i `priv/` + `--cleanup` |
| i18n | `T("de","en")` / `tr()` inline (D-010) ➡ `bin/translate.py --only ui` ➡ `bin/check_langs.py` (ship guard = `SHIP_GUARD`) · `data/i18n/hand_fix.json` (D-069) | ⚠️ no `$var` inside `tr()` · fix LLM junk i th table, not th cache |
| chat (Matrix) | `data/Matrix.kt` · `notify/KreisWorker.kt` · `ui/Comments.kt` · `ui/KreisScreen.kt` | 🧰 kit "later" ➡ sketch i [appkit/README.md](appkit/README.md) |
| QA loop | `qa_MMDD.md` (🦠 ticks on th fon) | 1 doc per DEV build · `[x]` + 1 word |

# 🪞 app lessons — wh building our apps taught us (read b4 app #3)
_appkit shelf 🪞 (D-001) · @1122 lifted fr omm `app_lessons.md` (#297 @1121) + cali `lessons.md` § avoid/own (@1120-21) · src tag = where we L'd it (`omm D-NNN` = that app's adr)_
_🔄 grows like refl ➡ distill: new lesson ➡ 1 line i § 📥 inbox w src · seen i a 2nd app (o costs > 1 sesh) ➡ promote 2 § 🔝 o § 🔄 · stale ➡ ✂️ flag, nvr rm wo 🦠_

## 🔝 principles (most ti saved)
1. 🔑 **prove th fix on th target, don't trust a proxy**: "compiles ✔️" off a grep while gradle died on JAVA_HOME · Obtainium "fixed" 3× on guessed causes ➡ only th measured cause (1st KiB hash) worked `omm D-008`
2. 🏛️ **stable ids + versioned content fr day 1**: user marks keyed by content id + plan days by index ➡ 2 retrofits, both lock-in forever `omm D-028 · D-042`
3. 🆕 **distribution = day-1 infra**: 2 app ids (stable + `.dev`) · version stamp · in-app updater ➡ = appkit `core/selfupdate` + `tool/` now · omm spent 10 days + 4 Ds on it `omm D-008/021/023/038`
4. 🛡️ **evy claim th can lie gets a guard script**: changelog said "whole book" 4 langs wo files ➡ ship guard · design drift ➡ ratchet tst · page wo version.json ➡ apptool `render` guard `omm check_langs · DesignGuardTest`
5. 🎨 **design system + gesture grammar b4 screens**: tap/turn rules took 4 rounds · 21 text sizes + ~60 hard colours b4 tokens · mockup decides look, code follows `omm D-022→065 · D-029 · cali § avoid`
6. 📸 **headless shots early, shot input = th app's real call path**: a `.take(30)` i th shot masked th bug it was built 2 show · `-Pshots` missing = silent skip + old PNGs stay · a shot = th eye th assertions lack (21 green tsts, 3 faults i 2 PNGs) `omm D-019 · cali P1`
7. 📱 **Robolectric ≠ device ➡ name th device items**: popup froze th app · silent install · gestures · widget = only th fon proves them ➡ `🦠🧪` list per DEV build `omm 0.8.0 · cali § avoid`
8. 🐢 **LLM batch jobs = bg + guarded**: 56 min i th foreground (600 s cap) · short GPU job queued behind th night job · tok/s meter counted only successes `omm translate`

## 🔄 do differently next time
- 🆕 **updater i P1 + fon-tst it once end 2 end** (omm: "phone test pending" 4 days, 1st real browser-APK update only @1122) · 🦠 #327: in-app update = main path, Obtainium 2nd
- 🛠️ **env i 1 place**: bare `./gradlew` failed ≥ 5 seshs on JAVA_HOME ➡ appkit `tool/apptool.sh` exports it · also set `org.gradle.java.home` o a `bin/gw` wrapper fr day 1
- 🧪 **glue tsts early**: updater · notes · page() had 0 tsts at first ➡ a page bug caught by curl only `omm test_bin.py · UpdateWorkerTest`
- 🧪 **Robolectric hygiene**: prefs + statics leak betw methods ➡ 1 red = 3 more red · reset prefs i `@Before` fr th start
- 🚦 **"QA b4 ship" needs an override phrase**: a release went out on a "ship" order b4 🦠's fon ck `omm 0.12.0`
- 🔥 **hot files = 1 owner per lane**, fan-in only: changelog · strings · i18n json · big screens (i18n json = top-churn, ~47 commits each)
- 📝 **user-facing strings = plain words**, nvr caveman · ⚠️ no `$var` inside `tr()` (lookup misses)
- 🎮 **1 progress currency**: stamps + ranks + stars = "2much" (🦠) ➡ XP ➡ lvl + streak `cali § avoid`
- 🧪 **DEV panel: 1 btn per celebration/anim fr day 1**, not an "unlock all" state hack `cali § avoid`
- 🎨 **contrast = a tst over th real tokens**, not th doc's claim (doc said pass, measured 3.85-4.38) `cali D-037`
- 🔗 **coverage tst = glue betw spec + code**: a rule row wo tst (o reverse) = red · found 4 stale rows day 1 `cali AeCoverageTest`
- 🧨 **falsify a guard once**: break th rule on purpose ➡ tsts go red ➡ revert `cali P1`
- 🐌 **read real signatures b4 writing against an API** (`grep "fun \|class "`, 1 call) · 4 screens vs a remembered API = 6 compile errs `cali P1`
- ⚠️ **Android 2027**: certified fons block apps fr unregistered devs, incl sideload/Obtainium ➡ all our apps on 1 dev registration · decide b4 strangers `cali § avoid`
- 🔒 **new repos private by dflt** til 🦠 says public · secrets nvr i th public repo (reg tokens ➡ git-ignored properties)

## 📥 inbox (1 line + src, promote later)
- 🧩 kit extraction: WorkManager stores th worker's **class name** ➡ moving a worker 2 a lib = keep a thin subclass w th old name (else th periodic job dies silently) · keep prefs / work names too `omm #327 D-079`
- 📦 a lib module needs its own `POST_NOTIFICATIONS` i its manifest 4 lint, even if th app declares it `omm #327`
- 🔧 inside `android { defaultConfig {} }` a bare `extra` = th config's extras, not th project's ➡ `project.extra` `omm #327`
- 📱 cable-free fon debug ➡ [fon_debug.md](fon_debug.md) `cali @1122`

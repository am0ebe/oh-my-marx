# arch — omm ☭ 🧩 (living · hu + agents read it)
_SDD: code says **wh** · th file says **y** · drift = a bug · fmt ➡ `pr/vibe++/arch_tpl.md` · backfilled @1112 fr notes.md "decided" · todo ✔️ · git log · y unknown = "❓ am0"_

## 🎯 intent
- 4 strix + few friends: actually get thru Kapital Bd.1 — habit, not willpower · Hooked L hands-on · L Android (Kotlin + Compose) · fun, honest about its own hooks (notes.md § 🎯 goals)
- good = daily ≤2 min verse + Andacht, streak wo anxiety, whole book readable offline i 13 langs

## 📐 req + constraints
- must: offline-first (whole book i APK) · sideload (no store) · DE + EN UI evywhere, rest via translate.py
- must-not: ❌ PII · keys i public repo (`priv/` git-ignored) · ❌ bump/ship/deploy wo am0's fon QA · ❌ edit VPS/Caddy (ops-owned)
- limits: APK ~30 MB · local LLM 4 translation (ollama) · lone dev + AI crew

## 🧩 arch (current)
- app `android/` (pkg `de.strix.marxversion`): `logic/` (Daily · Feast · Orden · Voice) · `data/` (Prefs · Content · Update · Community · Matrix) · `ui/` (Compose screens) · `notify/` (Reminder · Widget · KreisWorker)
- content pipeline `bin/*.py` (fetch ➡ split ➡ align ➡ score ➡ translate) ➡ `assets/content/*.json` · cache `data/i18n/<lang>.jsonl`
- server (VPS, ops-owned): `server/omm_api.py` counter + Synapse `omm-matrix` (comments · Kreise) · contract `server/SERVICE.md`
- release: `bin/deploy.sh` (DEV, Obtainium) · `bin/ship.sh` (bump + tag + page + GH release) · map ➡ [ref.md](ref.md)
- 🔥 hot files (1 owner at a ti — fan-out lanes nvr split these): `assets/content/i18n_*.json` + `data/i18n/*.jsonl` (translate pass = fan-in only) · `android/app/build.gradle.kts` (version) · `ui/Changelog.kt` · `res/values*/strings.xml` · `TodayScreen.kt` · `MineScreen.kt` · `Share.kt`

## 🧾 decisions (newest on top)
### D-023 @1113 · updater by installer: Obtainium ➡ poke it · self ➡ silent aft 1st · amends D-021 · ✔️ (fon tst pending)
- **ctx**: am0: Obtainium bg updates were on but he always beat its 6 h interval (deploy ➡ reload ➡ update by hand = same friction as th website) · "use Obtainium if it's there, else th website apk" · 6 h delay ok 4 users
- **dc**: `Update.Via` fr installer o record: OBTAINIUM ➡ notify only aft `GRACE_H` 8 h o th same version, tap = `obtainium://refresh?id=<pkg>` (Obtainium wiki deep link; no link can INSTALL) · SELF ➡ PackageInstaller session (not ACTION_VIEW) so WE become installer o record ➡ Android 12+ + `UPDATE_PACKAGES_WITHOUT_USER_ACTION` ➡ worker installs silently, `UpdatedReceiver` posts "✔️ X installed" · 1st self-update / < 12 ➡ 1 confirm · STORE ➡ nothing
- **y**: 0 taps 4 friends fr th 2nd update on · Obtainium keeps its job (we don't install over it: it declares ENFORCE_UPDATE_OWNERSHIP ➡ extra prompts)
- **alt**: install over Obtainium installs ourselves (❌ ownership prompt + 2 updaters fighting) · Shizuku (❌ setup 4 friends) · daily nag 4 Obtainium too (❌ noise, its bg wins mostly)
- **ck**: `UpdateWorkerTest` 8 (per installer · grace · silent · fallback) · `UpdateTest` via/silentRule · DEV ⚙️ "🧪 bg update check now · installer: X"
### D-022 @1113 · reader gestures: double-tap = turn page · 📐 dog-ear = turn · 1st chapters peek + hint · ✔️
- **ctx**: QA r5: am0 found page turn by accident ("NICE") ➡ needs discovery · "double tap = turn page, drop bookmark-via-double-tap"
- **dc**: `LocalPageTurn` (ChapterScreen) ⬅ VerseView `onDoubleClick` + paper `detectTapGestures` · `DogEar` bottom-right (tap = turn) · until 3 own turns (`omm_ui` SharedPrefs "turns"): page lifts 2× + pill "swipe or double-tap" ~6 s · 🔖 = verse menu only
- **y**: 3 cues (motion · object · words) w/o a tutorial screen · outside th book (Today) no double-tap ➡ tap reacts at once
- **alt**: HorizontalPager w dots (❌ loses th spine-hinged turn) · persistent ‹ › btns (❌ clutter on "pure Marx" page)
- **ck**: `ui/ReadScreen.kt` TURN_HINT · DogEar · grep `onDoubleClick` VerseView = turnPage
### D-021 @1113 · self-update 4 hand-installed APKs: daily bg check ➡ notification ➡ download + install dialog · ✔️
- **ctx**: QA r5 🔑: friends install th APK directly ➡ must nvr sit on a stale version · audit: only `Update.check` on app start ➡ Today banner ➡ opened th web page (manual download) · DEV: Changelog line "➡ Obtainium" only · no bg, no notification
- **dc**: `notify/UpdateWorker.kt` WorkManager 1 d (network) ➡ `Update.check` on th app's own channel (`.dev` ➡ `<web>/dev/version.json`, else `<web>/version.json`) ➡ notification "🆕 X is out" (new version at once, same one again evy 2 d) ➡ tap / Today banner ➡ `UpdateDialog`: download `apk` fr version.json (plain name only, sha256 checked) ➡ `ACTION_VIEW` apk via FileProvider (`REQUEST_INSTALL_PACKAGES`; 1st time ➡ "allow this source" page, resumes by itself) · fail ➡ web page
- **y**: same debug key ➡ installs over, data stays · installer = Obtainium/F-Droid/Play ➡ skip (they update themselves) · lib.sh alr writes apk + sha256
- **alt**: PackageInstaller session API (❌ more code, same system dialog) · in-app silent install (❌ impossible wo device owner) · Obtainium 4 all (❌ friends won't)
- **ck**: `UpdateTest` (parse · channel · nag · sha · installer) + `UpdateWorkerTest` (Robolectric, fake version.json ➡ notification, no repeat, older = silent) · manifest REQUEST_INSTALL_PACKAGES
### D-020 @1113 · Mitlesen always on (toggle gone) · name field only i ⚙️, label "Name" · supersedes D-005 switch + D-016 label · ✔️
- **ctx**: QA r5: "rm the Mitlesen/read-along option + its txt (always on)" · "label just 'Name'" · sweep #8 name field on ⚙️ + Kreis (+ comments sheet)
- **dc**: `Settings.community = true` (old stored off ignored) · ⚙️ Name field (D-016 prefill + blur-save stay) · Kreis + comments: `WritesAs` line "you write as X · change: ⚙️"
- **y**: am0 · 1 place 2 change identity · still anonymous daily token (D-005), UI says nothing about it now
- **alt**: keep switch (❌ am0) · name field evywhere (❌ 3 fields drift)
- **ck**: grep `setCommunity` i ui/ = 0 · `nameLabel()` = "Name"
- **note D-017**: QA r5 amends th note editor: no ★ (only app-wide close elsewhere) · ANY short tap while open = close (save if changed, blank = gone) + th tap is eaten (can't toggle th action row) · 👥 chip exempt · look = `NoteBubble` (side ruler · ✍️ icon · blue tint · sans)
### D-019 @1113 · headless screenshots = Robolectric native graphics (`ShotsTest`, -Pshots) · ✔️
- **ctx**: am0 r4: "design keeps slipping thru ➡ compare screens side by side" · no /dev/kvm on tow ➡ emulator ❌ · fon screencap = private (memory)
- **dc**: `test/…/ShotsTest.kt` drives th real MainActivity (tabs · chapter · verse menu · note · settings · In book) + cardBitmap variants ➡ `app/build/shots/*.png` · skipped i normal `testDebugUnitTest`
- **y**: real app, real Compose, 20 s, no device · catches overlaps/stale copy before am0's fon
- **alt**: emulator (no KVM) · Paparazzi (lib modules only) · Roborazzi (extra plugin, same engine)
- **ck**: `./gradlew testDebugUnitTest --tests '*ShotsTest*' -Pshots` · ⚠️ Robolectric fonts: newer emoji (🪝) = tofu, ignore
### D-018 @1113 · no share i th reader: verse menu = 🔊 · 🔖 · ✍️ · 📋 · page share pills gone · ✔️
- **ctx**: QA r1 "2 boxes i each other look weird · drop sharing here?" · r4 "rm share from th Read page" · r4 "Listen on th LEFT"
- **dc**: rm verse-menu Teilen (also on Today's plan verses = same menu) + r3's 2 pills at page end · `CardDialog` + `LosungPopup` deleted (dead) · 🔊 1st i evy action row
- **y**: Read = pure Marx · sharing lives on th Today card (Karte teilen · App teilen)
- **alt**: keep verse share on Today plan only (❌ 2 dif menus) · 💬 am0 may want it back
- **ck**: grep `CardDialog` = 0 · VerseView Action order
### D-017 @1113 · writing on th book = 1 pattern: tap passage ➡ ✍️ Notiz (inline) · 👥 chip = also public · ✔️
- **ctx**: r4: upper "Notiz" + lower "comment on this page" = same function, 2 looks ➡ merge · note editor: tap outside = save, empty = cancel, small X, no Save/Cancel words
- **dc**: rm page-end "Mitreden" btn · inline editor: tap outside (root watcher `tapOutsideNote`) = commit · ★ (CloseButton, app-wide close) = discard · blank = no note/note gone · Mitlesen on ➡ chip "👥 auch im Lesesaal posten — als <name>" posts "¶ <id> — txt" 2 th page's room · reading oth's = 💬 N i th running head ➡ sheet
- **y**: 1 entry, 1 composer, privacy = 1 toggle · comment keeps its passage ref
- **alt**: 2 btns side by side i th menu (Notiz · Kommentar) · Matrix field 4 verse id (later, if threads per passage wanted)
- **ck**: VerseView `NoteFocus` · ReadScreen has no OutlinedButton · ★ not ✕ = r1 rule "red star closes" — 💬 am0 said "small X"
### D-016 @1112 · public name = "Lesesaal-Name", prefilled random comrade name, saves on blur · ✔️ (label 💬 am0 may swap)
- **ctx**: QA r3: "Your name" label dull · empty field = friction · Save btn = extra step
- **dc**: label "Lesesaal-Name / Reading-room name" · prefill = socialist thinker 1st name + movement year (Rosa 1871 …), 1 per app run · ⚙️ field saves on blur/Done only if changed
- **y**: am0 r3 ask · label fits D-014 reading-room theme · no Lenin/Stalin/Mao (D-014: lens, not regime)
- **alt**: Genoss:in (gendered/party-ish) · Kampfname (militant) · Name / Username (generic) ➡ am0 picks if he prefers
- **ck**: `nameLabel()` + `comradeName()` i `ui/Comments.kt` · `NameTest.kt`
### D-015 @1112 · streak = Amen (ez 1 tap), placed under th reading snippet · ✔️ (not built) · amended @1112
- **ctx**: streak counted quote Amen; plan "done" looked meagre next 2 it (Mi) · options a any day · b plan days · c hybrid
- **dc**: Amen = th streak action, stays ez (1 tap, no read-gate / timer) · 1 place: bottom o th reading snippet (rm card + hover dup) ➡ th investment (Hooked/IKEA) = actually reading a bit
- **y**: am0 @1112: "its not the quote … its the actual book" + "Amen counts 4 th streak, keep it an ez action · investment = reading a bit"
- **alt**: a/b/c chip (❌) · gate Amen on scroll/ti read (❌ friction)
- **ck**: (aft build) Amen only i read snippet · Amen btn i `Cards.kt` = 0 · streak tst
### D-014 @1112 · Orden ➡ Abzeichen (badges), reading-room blue/gold · ✔️
- **ctx**: Mi feedback: Orden = too real-socialism/authoritarian, ☭ evywhere
- **dc**: rename + re-theme (reading-room look), positive reframes ("missed a day" tone out), stamp gift = ☭, Marx = lens not utopia
- **y**: user feedback (Mi) via am0 · **alt**: keep Orden (❌ alienates non-hardcore users)
- **ck**: `grep -rn "Orden" ui/` = only enum/code names, UI text says Abzeichen/badges · `OrdenTest.kt`
- **amend @1112 (am0)**: tone = positive + friendly, **nudging allowed** (miss ➡ kind nudge, e.g. sad Marx) · ❌ guilt / shame / loss threats · ck: nudge strings carry no blame ("verpasst" / "verloren")
### D-013 @1112 · Krisenfest = 15 shakes · ✔️
- **ctx**: crisis medal too easy · **dc**: `KRISE_SHAKES = 15` · **y**: Mi: "needs more shakes" · **ck**: `logic/Orden.kt` KRISE_SHAKES
### D-012 @1112 · 1 sober voice only · ✔️
- **ctx**: holy voices (Gottvater, Gott, Pfarrer…) cringe · **dc**: `Persona.picker = [NORMAL]`, rest hidden i code, not deleted · old stored dflt ➡ sober
- **y**: Mi + am0 agree · **alt**: keep as opt-in (❌ clutter) · **ck**: `Persona.picker` i `ui/Share.kt` · `VoiceTest.kt`
### D-011 @1112 · reading plan: continuous, no repeat, evy1 i a default plan · ✔️
- **ctx**: real-user 🐛 same page 2× · plan didn't train regular reading
- **dc**: `planIndex` continuity, finished plan doesn't repeat, default plan 4 all · **y**: Mi feedback · **ck**: `PlanTest.kt`
### D-010 @1031 · UI strings inline DE+EN i code, rest via translate.py · ✔️
- **ctx**: 13 langs, lone dev · **dc**: `T("de","en")` / `tr(..)` pairs i Kotlin (key = EN) ➡ `bin/translate.py --only ui` ➡ i18n_xx.json · ❌ translate_full 4 UI · 1 pass at fan-in
- **y**: am0 · **alt**: strings.xml per lang (❌ 13× by hand)
- **ck**: `bin/translate.py --only ui` finds 0 new · only launcher/shortcut/tile names i `res/values*`
### D-009 @1108 · GitHub release + Forgejo origin, gh fetch-only · ✔️
- **y**: mirror carries tags 2 GH; ship.sh waits ≤120 s · **ck**: `git remote -v` · `bin/ship.sh`
### D-008 @1107 · Obtainium DEV: stamp version into APK's 1st zip entry · ✔️
- **ctx**: default pseudo-version hashes 1st 1 KiB = identical across builds ➡ never updates
- **dc**: ommStamp + deploy guard · **y**: root cause measured · **ck**: `ref.md` (trap documented) · `bin/deploy.sh`
### D-007 @1107 · 1 dialog style (MarxDialog), ☭ = close only · ✔️
- **y**: am0: "windows look weird — frame colour, no frames, ☭ at odd spots" · **ck**: `ui/MarxDialog.kt` · no raw AlertDialog elsewhere
### D-006 @1105 · repo public, GPL-3.0, `priv/` git-ignored · ✔️
- **ctx**: friends need build-it-yourself · **dc**: public GitHub am0ebe/oh-my-marx, Hooked OCR out o tree + history · **y**: ❓ am0 · accepted risk: GH keeps old SHAs reachable
- **ck**: `.gitignore` priv/ · LICENSE
### D-005 @1103 · community = Mitlesen opt-in (dflt ON), anonymous daily token · ✔️
- **ctx**: social proof (Hooked tribe) vs privacy · **dc**: counter + Matrix comments/Kreise, switch off i ⚙️, not E2E (said i UI) · **y**: ❓ am0 (dflt ON by am0)
- **ck**: `data/Community.kt` · `Prefs.community` · 💬 toggle's fate = open Q
### D-004 @1101 · 🔥 streak w 🕊️ Sabbat-Joker + Stempelkarte counts *total* days · ✔️
- **y**: streak anxiety ➡ 1 miss = rage-quit (notes.md § judgment) · **alt**: plain streak (❌) · **ck**: Prefs flag "sabbath" · `logic/Daily.kt` · `OrdenTest.kt`
### D-003 @1028 · Android native Kotlin+Compose, offline, no server (server added @1103 opt-in) · ✔️
- **y**: L project + friends sideload · **ck**: `android/` · INTERNET perm only 4 Community/Update
### D-002 @1028 · book src = MEW 23 (DE PD) + Moore/Aveling 1887 (EN PD) · ✔️
- **y**: am0 owns MEW 23 ➡ page refs match · **alt**: DTA 1867 (❌ CC BY-NC, dif structure) · **ck**: `bin/fetch.py` · `ContentTest.kt`
### D-001 @1031 · UI langs DE + EN, EN dflt (fon lang if supported), DE 2nd · ✔️
- **y**: ❓ am0 · **ck**: `data/I18n.kt`

## ❓ open Qs
- 💬 **streak**: (a) any reading day · (b) plan days only · (c) hybrid chip — Mi: 2nd "done" looks meagre next 2 big streak widget
- 💬 **Mitlesen toggle**: rm? (QA lane B removed th note only) · 💬 ☭ close btn i MarxDialog: keep/rm? · 💬 "share app" prompt aft streak/medal?
- ❓ streak widget "shd show 2 at start" = ? (todo ‼️ A)
- later: Read = real book look + page flip · daily verse can't be turned off · Lesekreis progressive disclosure

## 🔍 drift (checked @1112)
- none vs ✔️ Ds · note: Changelog.kt old release lines still say "Heilige Stimmen"/"Orden" = history, ok

# eos omm — resume log · newest on TOP
_1 block per /eos · written by /eos § 3 · todo keeps 1 line: `▶ resume ➡ eos.md`_

## 261126 23:2x · #351 (M8): v1 sesh page + tonight's 🤖
- **at** · [v1_sesh_1127.md](v1_sesh_1127.md) = 🦠's 1 page (order · wh audio needs · 5 ❓) · D-089 voice label i reader's lang · D-090 `bin/voice_up.py` + DEV 10-min manifest recheck · 7 UI strings ➡ 11 langs (ship guard was red) · D-091 CI `langs` job · **DEV `0.14.0-dev.1126.2319`** (deploylog ✔️) · 159 unit + 63 py + lint green
- **▶ NEXT** · ck CI run w th new `langs` job (`cilog`) · aftr 🦠 pick + ❓ 1 yes: audio's out dir ➡ `bin/voice_up.py <dir>` ➡ `-u -y` ➡ 🦠 fon 🔊 · ReaderGestureTest only if red again
- **👤 🦠** · page § ❓ 1-5 · fon step 3 · read + tick step 4

## 261126 19:35 · #350 round 3 (M8): CI flake 🔍 + DEV deploy
- **at** · CI 1171 red = ReaderGestureTest again: all 3 retries failed ➡ stuck state, not a dropped tap · no local repro (1 core · TZ=UTC) ➡ failure dumps bounds + menu + tree, gradle testLogging FULL (bdab37a) · CI 1174 ✅ · **DEV `0.14.0-dev.1126.1931`** = D-087 + D-088 (deploylog ✔️)
- **▶ NEXT** · nxt CI red on that tst ➡ `cilog <run> | grep -A40 "3 right-taps"` ➡ fix th real cause · 🔵 🤖 rest: CI push guard A ⬄ B = ❓ 🦠 · rest 🦠 / 👤 gated
- **👤 🦠** · DEV 1931 on fon: Andacht 🔊 (plan card) + hold a stranger's comment = report · D-088 wording · + blocks below

## 261126 18:4x · #349 round 2 (M8): D-087 Andacht 🔊 · D-088 report a comment
- **at** · Today's 🕯️ Andacht hs its own 🔊 (`a:<plan>:<day>`, AI label per item) · hold a stranger's comment ➡ Matrix `/report` (D-073 gate half 1) · 157 unit + lint green · pushed · DEV unchanged (`0.14.0-dev.1126.1603`)
- **▶ NEXT** · 1 DEV deploy (D-087 + D-088, inform tier) · 🔵 top 🤖 left: CI push guard A ⬄ B ❓ · ReaderGestureTest root cause 🔍 · rest = 🦠 / 👤 gated
- **👤 🦠** · D-088 wording (todo 👀 Matrix list) · 👤 ops: who reads continuwuity's admin room (reports land there) · + all of th block below
- **⚠️** · report not run vs live continuwuity (wd spam th admin room) ➡ Matrix sesh · voice ids now only via `VoicePacks.quoteId` / `andachtId`

## 261126 16:05 · #345 full steam + #346 (M8)
- **at** · D-083 updates ask by dflt (kit `AUTO_DEFAULT` false + refused silent ➡ notif) · CI green again (1156) · D-084 LEVEL 1 "Commodity" (Proposed) · D-085 voice pack client (hidden til a pack exists) · D-086 build pins 91 ➡ 0 · nightly `@v1` · DEV `0.14.0-dev.1126.1603` · 150 unit + 17 kit + lint green · all pushed
- **▶ NEXT** · ck CI 1162 green · 🔵 top: Andacht 🔊 (`a:` clips) · Matrix "Melden" on a comment (D-073 gate)
- **👤 🦠** · stable ship ❓ (D-083 flips nvr-touched users ON ➡ OFF) + Changelog line (todo 👀) · D-084 name + 🧺 badge art · `scagent.de/oh-my-marx/voice/` = newly public ❓ · persona pick (audio) · Android 8-9 = no packs ok?
- **⚠️** · `--write-locks` only rewrites configs a task resolves ➡ run deps + tsts + lint + debug + release (D-086) · kit fix = pr/appkit 1st ➡ subtree pull · cali gets D-083 receiver fix on its nxt pull

## 261122 23:10 · /eos · #335-341 BL sweep (M8)
- **at** · night list closed exc F6 (ops D-065 💬 🦠) · D-080 paging DEV 2238 · D-081 · D-082 lockfile · 135 unit + lint + 6 server green locally · all pushed
- **▶ NEXT** · 🦠 fon step 5 (silent update, todo ⏳) ➡ Mo 11/23 Matrix sesh (📆) · 🤖 aftr ops fixes runner: `cilog` ➡ green ck
- **👤 🦠** · D-076/077/068 👀 boxes · retag log read (unchanged)
- **⚠️** · CI red = runner (todo 🔵 top) · dep bump ➡ re-write lock w full CI cmd + `--write-locks` (D-082) · raw gradle needs `JAVA_HOME=~/.local/opt/jdk21`

## 261122 23:00 · #339 F7 D-081 · #341 F15 · F12 lockfile D-082 (M8)
- **at** · README privacy line = continuwuity facts (4e39dc4) · `android/app/gradle.lockfile` (a0dd241, CI cmd green locally fr clean) · F9 ✔️ ops-proven · night list: only F6 (ops D-065 💬 🦠) + F13 (fine) left
- **🚧 CI red since run 1108** (b4 #335): Robolectric `MavenArtifactFetcher` ➡ ConnectException = runner can't fetch `android-all` jars ➡ 15 Robolectric classes fail, rest green · 👤 ops runner (net o cache) · local = 135 green
- **▶ NEXT** · same as below · 👤 ops: osv on `android/app/gradle.lockfile` · fix runner Robolectric download

## 261122 22:45 · #336 F2 · F5 · F9 (M8)
- **at** · `server/matrix/` rm (8460bcf, SERVICE.md § 2 generic) · comment paging D-080 (ea8c2af) ➡ **DEV `0.14.0-dev.1122.2238`** (deploylog ✔️) · F9 XFF doc (5c1188a) · 135 unit + lint + langs + 6 server green · pushed
- **▶ NEXT** · same as block below · 👤 ops: SERVICE.md changed (§ 1 proxy must replace XFF · § 2 no Synapse) + omm-api rebuild (F8)
- **⚠️** · Kreis timelines still newest 300 only (D-080 scope) · night left: F6 F12 F15 = ops · F7 = 🦠

## 261122 22:35 · #335 BL sweep (M8)
- **at** · `launcherReset` rm (cb37b25, D-066 amended) · night F8 omm_api keys `p:1-9999` only (ff0c68f, 6 server tsts) · appkit 0.2.1 subtree pull (c8d8cf7) ➡ `test_apptool.sh` 118 ✔️ + `deploy.sh -n` ✔️ · todo: apptool ✔️ · tags ✔️ · /triage ⏸️ (0 issues) · kit todo ✔️ (c32610e) · 133 app unit + lint green · DEV unchanged (0 visible change)
- **▶ NEXT** · same as block below (🦠 fon step 5 ➡ Mo Matrix sesh) · 👤 ops: rebuild omm-api (F8 live)
- **⚠️** · gradle needs `JAVA_HOME=~/.local/opt/jdk21 ANDROID_HOME=~/Android/Sdk` i a fresh shell (apptool sets it, raw `./gradlew` doesn't) · task names = `testDebugUnitTest` `lintDebug` (no flavor) · 🔵 left = 🦠 taste / design / gated

## 261122 22:20 · #327-333 in-app updater = main · appkit · v0.13.1 (M8)
- **at** · stable **v0.13.1** live (auto-update dflt ON D-078 · page APK 1st / Obtainium 2nd · updater fr kit) · DEV `0.14.0-dev.1122.2212` · kit = own repo `pr/appkit/` (793b5e7), omm vendors it at `appkit/` (git subtree --squash, D-079) · 151 unit (134 app + 17 kit) + lint + 59 py green · deploylog ×5 rows
- **▶ NEXT** · 🦠 fon: open stable 0.13.0 ➡ 0.13.1 = 1 tap ➡ nxt stable must land silent (todo ⏳ step 5) · then Mo 11/23 Matrix sesh (todo 📆)
- **👤 🦠** · D-076/077 + egg_arch + retag log boxes (👀, unchanged) · KB dup risk: vendored `appkit/adr.md` + `todo_appkit.md` show up twice (pr/appkit + omm) ❓ kb
- **⚠️** · kit fixes go i `pr/appkit` 1st ➡ `git subtree pull --prefix appkit ~/gopro/go/pr/appkit main --squash` (clean tree · stash) ➡ then `./gradlew :selfupdate:clean` (stale Kotlin IC = fake "conflicting declarations") · cali crew commits i pr/appkit too ➡ ck `git log` b4 committing · apptool hs 0 bash tsts

## 261122 16:20 · #311 Matrix fixes + #325 stable v0.13.0 (M8)
- **at** · stable **v0.13.0** live (page · version.json · GH + Forgejo, 90a54e3) · DEV `0.14.0-dev.1122.1615` · D-076 delete acct = redact evy own msg b4 deactivate · D-077 real id aftr oth ppl's names · live `--app` 13/13 vs continuwuity · 148 unit + lint + py green · deploylog row ✔️
- **▶ NEXT** · 🦠 updater tst on fon (0.12.0 browser APK ➡ Today banner ➡ Install) ➡ then Mo 11/23 Matrix sesh = todo 📆 (primer + agenda)
- **👤 🦠** · D-076 dialog wording · D-077 grey id look (both 👀, Proposed but shipped) · F7 `allowBackup` ❓ · F16 chip fit ❓ · ur Changelog line odd ("Marx presented hidden", ollama)
- **⚠️** · Element sign-up w reg token on continuwuity = untstd (fallback `!admin users create-user`) · rollback can't downgrade fons that alr updated · F5/F6/F8 still open (night_1122.md)

## 261122 04:xx · night #308 (M8): review + night data
- **at** · F1 dead-token re-register (D-075) · gpu_night `.py` fix + 2 tsts · retag + translate + build `tw` all committed · 22 drift 🔧 (adr · design_system · SERVICE.md) · 146 unit + 59 py + lint green · pushed · **DEV unchanged** (no deploy rule tonight)
- **▶ NEXT** · 1 DEV deploy (`bin/deploy.sh`) = F1 + i18n + `tw` chips + delete account ➡ tick th 2 🌙 🤖 boxes i todo 👀
- **👤 🦠** · F3 delete-account txt false on continuwuity (fix txt o redact b4 deactivate) · F16 chip quote fit ≈ ½ · F7 `allowBackup` ships mx pw · F2 rm `server/matrix/` ➡ all ❓ i [night_1122.md](night_1122.md) top
- **⚠️** · F3 = don't flip D-072 Accepted on th old rec · ops findings F6 (D-073 worst case) · F15 README privacy line still Synapse-era · night file also on a private artifact (unasked, harmless)

## 261122 00:xx · #305 eval card + auto · #307 "ok all" (M8)
- **at** · `eval.md` card ✔️ · matrix_e2e 1 state file per run (+ tst, 56 py green) · 🔵 owner moji on 21 lines · 12 M8-decide recs applied ➡ D-073 + D-074 Accepted · live e2e `--cleanup` 10/10 (priv/ hs 0 state files) · DEV unchanged
- **▶ NEXT** · aftr 03:00 night runs: same as block below (retag `--stats` ➡ build ➡ unit ➡ commit data ➡ 1 DEV deploy ➡ tick 🌙 boxes)
- **👤 🦠** · Q13 (D-015 ➡ Superseded by D-039, 0 code) · D-072 delete-account wording · D-068 egg_arch png · fon updater tst (⏳)
- **⚠️** · `#omm-kreis-pqv6wu` (1327 API run) = orphan on Synapse, state file lost b4 th fix ➡ only a Synapse admin can drop it · 🔵 = dry 4 🤖 til night data lands

## 261121 20:30 · #299 auto-finish 🔵 sweep (M8)
- **at** · 🔵 31 ➡ 29 · ⏳ 1 (fon-only) · qa_1112 R1-R5 + D1-D10 · qa_1116 #14 · qa_1117 R3/R4 closed w proof (8 commits 40c20a4..6e12f96, pushed) · 0 code behaviour change · DEV unchanged
- **▶ NEXT** · aftr 03:00 night runs: `data/retag_night_1122.log` + `translate_night_1122.log` ➡ `bin/retag.py --stats` ➡ `bin/build.py` ➡ unit green ➡ commit data + assets ➡ `bin/deploy.sh` (1 DEV) ➡ tick 🤖 boxes i todo 👀 🌙
- **👤 🦠** · M8 cd decide now 13 Qs (new Q13: D-015 ➡ Superseded by D-039, 0 code)
- **⚠️** · 🔵 rest = 🦠 taste / Fable design / gated (launcherReset aftr nxt stable · shared code on cali's 2nd copy) ➡ more auto rounds = low yield til night data lands

## 261121 16:30 · #292 cli flags · #297 app_lessons (M8)
- **at** · cli_lint omm 44 ➡ 17 (98d6545, 55 py green) · `app_lessons.md` 4 cali (efe6999) · 🔵 shared-code line i todo
- **▶ NEXT** · unchanged: block below (aftr 03:00 night runs ck retag log + 3 quotes/chip)
- **👤 🦠** · M8: SSOT app_lessons.md ⬄ cali/lessons.md ❓ · lint leftovers = 4 skipped shorts (`--qr` `--html` `--versions` `--stats`) + 14 dflt+w (real writes, D-031 💬)
- **⚠️** · new short flags = additive only · `notes.py -p` only after th version arg

## 261121 15:40 · 👀 diet #290 (M8)
- **at** · todo 👀: 🦠 boxes 6 ➡ 3 · ❓/💬 15 ➡ 0 open 4 🦠 (5 closed superseded · 12 ➡ `### 🐙 M8 cd decide` w rec) · 4f2acb8 pushed
- **▶ NEXT** · aftr 03:00 night runs: same as block below + print 3 quotes/chip i retag log tail (new 🤖 box)
- **👤 🦠** · egg_arch_de.png (D-068) · delete-account txt inline (D-072) · retag log 1 min · M8: "ok all" on th 12 ❓
- **⚠️** · Element cleanup waits on M8 dc (not run) · /eos cut short by usage limit: refl not written

## 261121 14:50 · M8 auto-finish: Matrix in-app e2e · retag · delete account · /eos
- **at** · `bin/matrix_e2e.py --app` = real app UI vs live Synapse 12/12 ✔️ (bc2d1b7 · D-070) · `bin/retag.py` + `tw` chip pool built, data pending (ebbb7bf · D-071) · ⚙️ Delete account (9ba6e75 · D-072 Proposed) · 145 unit + 55 py + lint + check_langs green · pushed · DEV unchanged (`0.13.0-dev.1121.0014`, nothing deployed)
- **▶ NEXT** · aftr 03:00: re `data/translate_night_1122.log` + `data/retag_night_1122.log` ➡ `bin/retag.py --stats` all ✔️ ➡ `bin/build.py` ➡ unit green ➡ commit `data/i18n` + `data/tags.jsonl` + assets ➡ `bin/deploy.sh` (1 DEV = translate + chips + delete account) ➡ tick 🤖 boxes i todo 👀
- **👤 🦠** · Element look: `#omm-kreis-pqv6wu` (API run) + `#omm-kreis-54t8rk` (app run 1427) ➡ `bin/matrix_e2e.py --cleanup` · Matrix v1 ❓ ×4 i todo 👀 (report/mod · spam token · lost fon · E2E) · D-072 wording/place on nxt DEV
- **⚠️** · `at` jobs 62 + 63 both 03:00 (same ollama, interleave = ok) · 6 delete-account strings hand-translated (`"by": "hand @1121"` i data/i18n) · a deleted user's Kreis alias stays on Synapse (by design; e2e test drops its own b4) · 2 dangling e2e aliases (7xs3su · 5h6s6h) only admin can drop

## 261121 13:35 · M8 status brief r1+r2: --show fix · matrix_e2e · night run · /eos
- **at** · `translate_full --show` = 1 flagged row per lang (96db6e6) · `bin/matrix_e2e.py` 18/18 ✔️ vs live Synapse (a3b0ff6) · `at` job 62 Sun 11/22 03:00 = `bin/gpu_night.sh bin/translate.py --only a` · 50 py green · pushed · DEV unchanged (`0.13.0-dev.1121.0014`)
- **▶ NEXT** · aftr 03:00: re `data/translate_night_1122.log` ➡ ✔️ + check_langs green ➡ commit `data/i18n` + assets ➡ `bin/deploy.sh` ➡ tick 🤖 box i todo 👀 🌙
- **👤 🦠** · Element look at `#omm-kreis-pqv6wu:scagent.de` (login: `bin/matrix_e2e.py --login`) ➡ then `--cleanup` · tags retag = unbuilt feat ❓ (todo 🔵 "tags over-applied")
- **⚠️** · e2e test accounts `@e2e-1121-1327-a/-b` + Kreis LIVE til `--cleanup` (state `priv/matrix_e2e.json`) · 63 q/lang stale on EN ref only ➡ deliberately not re-asked (could undo #248 hand fixes)

## 261121 · #260 rm dead Head enum (M8) · /eos
- **at** · `Head` enum + `Feast.head` gone (f48aa08) · D-066 amended · unit + lint green · not deployed (0 behaviour change)
- **▶ NEXT** · 🦠 👀 GPU slot Decide + qa_1121 r2 + D-068 box ➡ widget egg box on Black Friday 11/27
- **👤 🦠** · GPU free aftr audio ➡ 1 `translate.py` run + tags retag
- **⚠️** · gradlew outside bin/ needs `JAVA_HOME=~/.local/opt/jdk21 ANDROID_HOME=~/Android/Sdk`

## 261121 00:40 · #248 bg burn + #259 bin docs (M8) · /eos
- **at** · DEV `0.13.0-dev.1121.0014` live = all #248 work (5e4bc85 = +#259 docs only) · 🧹 translation junk fixed via `data/i18n/hand_fix.json` at export (D-069) · check_langs refuses half-Latin + still-German quotes · 📱 widget egg badge · guard Color 24➡18 · shapes 17➡6 · 142 unit + 46 py + lint green · pushed
- **▶ NEXT** · 🦠 👀 Decide ×2 (rm `Head` enum · GPU slot) + qa_1121 r2 + D-068 box ➡ widget egg box on Black Friday 11/27
- **👤 🦠** · GPU free aftr audio ➡ 1 `translate.py` run (3 src-fixed Andachten × 11 langs re-ask) + tags retag
- **⚠️** · 3 Andachten src changed (30tage d2 d24 · wertform d4) ➡ cache src stale on purpose, hand_fix covers til re-ask · new `T()` strings still need GPU translate (no Changelog lines written) · gradlew outside bin/ needs `JAVA_HOME=~/.local/opt/jdk21`

## 261120 23:00 · #246 (M8): arch title cut off on feast cards · /eos
- **at** · DEV `0.13.0-dev.1120.2256` live = 640e349 · feast arch = headline only (`Share.archSplit`), rest = small italic lines under th egg, arc 56➡100° (D-068 Proposed) · `EggArchShotTest` (split + 30×13 fit, shots `egg_arch_<lang>.png` w `-Pshots`) · full unit green · pushed
- **▶ NEXT** · 🦠 qa_1121 r2 (prev block) + #246 👀 box (shots / fon on nxt feast) ➡ D-068 Accepted o fix
- **👤 🦠** · D-068 judgment: headline + tail split ok?
- **⚠️** · AR feast tails carry translation junk ("تrier", "arus") = data, not layout ➡ 🔵

## 261120 22:30 · #229 + #241 (M8): 🥚 eggs in-app · drop cap · pinch · /eos
- **at** · DEV `0.13.0-dev.1120.2220` live = c52e1af · 30 eggs (`bin/eggs.py` ➡ vectors + EggRes) i top bar · hero (EggPop) · quote card · launcher icon = plain again (aliases built i 1911, dropped #241) · drop cap = bare red letter by baselines (D-067), no ¶ i 📜 · 📜 pinch back · unit 138 + lint + test_bin + check_langs green · pushed
- **▶ NEXT** · 🦠 does [qa_1121.md § r2](qa_1121.md) (4 checks, ~3 min) ➡ fix wh he marks ❌
- **👤 🦠** · r2 fon check · 💬 Fable polish pass over th eggs = only if he wants (🔵)
- **⚠️** · `launcherReset` i notify/UpdateWorker.kt = DEV 1911 cleanup (MainActivity could be left disabled) ➡ ✂️ aftr nxt stable · `Head` enum + `Feast.head` = dead since eggs (✂️ 🔵) · shot tsts need `-Pshots` (else stale PNGs, mem)

## 261120 late · #218 deploy + D-065 · #208 adr v2.1 · /eos
- **at** · DEV `0.13.0-dev.1120.1813` live = all work incl. #206 (perks · heads · initials · i18n fixes) + D-065 hold = menu + 5 static Literata styles · unit + lint + test_bin + check_langs green · pushed (a5cea67)
- **▶ NEXT** · 🦠 does [qa_1120.md § r2](qa_1120.md) (8 checks, ~10 min) ➡ flip D-048..054 + D-060..064 to Accepted o fix wh he marks ❌
- **👤 🦠** · th r2 fon check = th only open ask · 🔵 left: guest heads (needs portrait art) · share card + widget head · feast frames · stamp sound
- **⚠️** · adr.md = template v2.1 now (outcome titles, Y/Scope/Ck) ➡ new Ds i that shape · Robolectric prefs leak between test methods (mem) · translate re-asks lost %s/%d, check_langs refuses them

## 261120 ~21:00 · #200 + #201 + #206 (M8, autonomous)
- **State:** DEV `0.13.0-dev.1120.1730` is live (#200 fixes + #201 decisions). Everything after it (D-063 perks, initials, heads, translations) is pushed but not deployed: #206 forbids deploys.
- **Next:** 🦠 answers the two Decide items in todo 👀 (hold gesture, unified reading modes) and reviews qa_1120 § r2. Then deploy and build the gesture fix.
- **Watch out:** `translate.py` now re-asks any translation whose %s/%d got lost, and `check_langs` refuses to ship one. Robolectric screenshot numbers collide across tests, so check the file name you open. sed + cp file writes are blocked, use Edit.

## 261120 15:45 · #185 (M8): 🐛 tap fix · C1-C5 + C7 · ⏱️ D-059 · DEV 1542
- **at** · DEV = `0.13.0-dev.1120.1542` (= `b7659ec`+) · stable = v0.12.0 · unit + lint + test_bin 42 + check_langs green · pushed
  - D-045/046/047 ✔️ (🦠) · D-048..054 + D-059 built on 🤖 pick = still 💬 til fon eval
- **▶ NEXT** · 🦠 fon pass [qa_1120.md](qa_1120.md) ➡ fix ➡ C-lane gaps (todo 🔵 top line)
- **⚠️** · tap 🐛 nvr reproduced i Robolectric ➡ fix = no click on ¶ (BookTaps) · confirm on fon · sed→cp file writes = blocked (use Edit) · translate --only ui ~18 min w ~100 strings

## 261120 10:20 · eval DEV 0009 ✔️ · crash fix · C6 · D-057 tap = turn · D-058 Literata justified
- **at** · DEV = `0.13.0-dev.1120.1009` (= `e813452`) · stable = v0.12.0 · unit + lint + test_bin 40 + check_langs green · pushed
  - 🦠 ok'd 0009 (L9 · plans v2 · L6-L8) · D-055 ✔️ V2 · D-057 swaps D-044 · D-058 Literata + justify + hyphens
- **▶ NEXT** · 🦠 fon pass over qa_1119 § 👀 new i 1009 (7 lines) ➡ then C1 streak logic once D-045..047 answered
- **👤 🦠** · D-045/046/047 ❓ i qa_1119 (gate C1 ➡ C2) · ok 4 fonttools (font subset −1.3 MB, todo 🔵) · D-048..054 = nxt Fable round
- **⚠️** · DE hyphenation unverified (shots EN only) · jahr = v2 since 146a631 (day 161 heading only) · ShotsTest verse_menu now via double-tap · Robolectric swallows coroutine crashes ➡ use UpgradeStartTest's main-looper handler pattern

## 261119 15:30 · qa_1117 r1-r4 built · v0.12.0 stable · plan versioning + 6 🔒 plans
- **at** · stable = v0.12.0 (@1118) · DEV = `0.13.0-dev.1119.1507` (= all up 2 `01b7e27`) · lint + unit + 39 py + check_langs green · pushed
  - ⚠️ oth crew lanes landed aftr th deploy: `6f48c4d` L9 double-tap + `55fa867` design § 14 addendum ➡ NOT on DEV yet
- **▶ NEXT** · `bin/deploy.sh` (picks up L9) ➡ 🦠 fon pass over qa_1117 `👀 ck:` lines (r1-r3 L1-L5 · r4 L6-L8 · 🔒 plans via ⚙️ DEV unlock) ➡ Fable design spec (D6 · D8-D11 · design § 14)
- **👤 🦠** · Q1-Q9 i qa_1117 (Q1 one score + Q2 milestones unblock streak work) · skim bridge facts i 6 new plans · updater fon tst (todo ⏳) · fon cold-start ms
- **⚠️** · any plan cut change ➡ build.py auto-bumps `ver` ➡ commit `data/plan_versions.json` w it (test_bin fails otherwise) · translate `--only x a` on new plan days = ~1 h ➡ run i bg · 🔵 13,319/13,320 DE⬄EN split (align.py) · wertform v2 Andachten = local M tic

## 261117 · ship v0.11.0 (moved fr todo § ⏳ ▶ RESUME @1117)
- ▶ **RESUME @1117 ship** — v0.11.0 = stable (shipped @1117, GH release ✔️) · DEV = `0.11.0-dev.1117.1108` (`a859d58`, = all commits) · lint + unit + 31 py green · all pushed
  - 👤 🦠: fon tst in-app update msg on BOTH: DEV 2227 ➡ 1108 · normal 0.10.0 ➡ 0.11.0 (Today banner on app start)
  - ▶ nxt: fon QA ➡ Fable design spec (🦠: AFTER this round) · nxt `bin/deploy.sh` = `0.12.0-dev…`
  - 👤 🦠: fon tst updater (todo ⏳ 🦠📱 item) · fon cold start `adb shell am start -W` (L6 before ⬄ after) · baseline profile = device run
  - ⚠️ traps: new tr() string ➡ `bin/translate.py --only ui` or ship refuses (check_langs) · guard baseline strict both ways · new plan days ➡ `translate.py --only a x` · EN gaps = 0 (ContentTest fails on any blank ➡ fix i align.py: PIN / labels)

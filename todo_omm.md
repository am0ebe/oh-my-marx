▶ resume ➡ [🔗 eos.md](eos.md) (top block)
_✂️ rule (🦠 @1113): drop candidate ➡ flag `✂️` on its line, nvr rm wo 🦠 ok · dropped @1113 ➡ [notes.md § 🗃️](notes.md)_

## 🏔️
- ☭ **v1** (roadmap 1/3 now · 🦠 @1120 ➡ [compass § 🗺️](../compass.md)) — design § 14 C-lanes + **Matrix chat** + **audio** (many langs · how deployed) + feedback loop · v1 = **QY bar**: UX + func + purpose feel ~80-90 % aftr thorough eval/tst (🦠) · done = stable release at th bar ➡ then analytics/Hooked · spin-off: Matrix 4 ourselves (ops)

## 🎯 sprint goal
- 🧪 **QA + iterate 0.9.x b4 anything new** (was 0.7.0, bumped @1112) (🦠 @1106: "test existing feats + iterate a couple o times, else slop") ➡ friends' feedback in ➡ fix ➡ only then 0.8.0
  - 🚦 rule: no version bump / ship / big new feat til th current one had ≥1 QA round on th fon · big side-quests (voice, chat infra) ➡ own pr

## ⏳
- 🦠📱 **fon tst: updater** (D-038 · steps 1-4 ✔️ @1122) · step 5 silent ❌ GrapheneOS @1123 (🦠) ➡ **D-083: dflt OFF, banner ➡ Install = main** (#345) · silent stays opt-in, a refused silent try now asks (notif "Tap = install")
  6. (+30 s, fr qa_1121 r2 ck 4) any chapter ➡ 📜 Scroll ➡ pinch = txt grows / shrinks · back 2 📖 Pages = same size as b4
  - ❌ any step ➡ screenshot (auto-lands i `/opt/pix_fon`) + step nr ➡ 🤖
  - 🧮 **updater matrix** (@1116 · @1122: normal APK ✔️ fon) (same code both channels, only th URL differs: `/` ⬄ `/dev/`)
    | | APK (browser) = main (#327) | Obtainium = 2nd |
    |--|--|--|
    | DEV | ✔️ unit tsts · same code as normal (fon ran on stable) | ✔️ fon: Obtainium 1h ck auto-updated (🦠) · app's own notif ➡ obtainium://refresh = unit tst only |
    | normal | ✔️ fon @1122 (🦠): 0.12.0 browser APK ➡ 0.13.0 via in-app updater (notif/banner ➡ Install ➡ confirm) · ❌ silent on GrapheneOS @1123 ➡ opt-in only (D-083) | ✔️ Obtainium follows th page (same as DEV) |
    - 0.9.0 ➡ 0.10.0 APK users: banner ➡ page ➡ install by hand 1× (then 1-tap / silent fr there)

## 👀
### 🌙 night run (#248 · 🦠 ✔️ @1121)
- **🏷️ tags retag scheduled** (M8 @1121 · D-071) `at` job 63 · Sun 11/22 03:00 · `bin/gpu_night.sh bin/retag.py` (1867 Verse, ~1 h) ➡ log `data/retag_night_1122.log` ➡ `data/tags.jsonl`
  - built: `bin/retag.py` (0-2 topics ABOUT, weight 1-3) ➡ build.py `tw` ➡ Marxoskop chip pool = weight ≥ 2 · < 30 ➡ ≥ 1 · no data ➡ old `t` (= today) · `t` + plans untouched (D-042) · tsts: `Retag` 3 py · `marxoskopPoolPrefersAboutTopicsWithGuard`
  - [x] 🤖 `bin/retag.py --stats` all ✔️ (≥ 30/chip) ➡ `bin/build.py` (only `tw` added i verses.json) ➡ unit green ➡ commit `data/tags.jsonl` + assets ➡ deploy DEV (w th translate box above = 1 deploy)
  - [x] 🤖 print 3 quotes per chip (Chef · Miete · …) i th log tail ➡ 🦠 reads there, no fon · `bin/retag.py --peek 3` (f88ebe5)
  - 🌙 #308: retag ✔️ (af467a1) · translate died 644 ➡ wrapper fixed (69ad337) ➡ re-run ✔️ (5825b09) · build `tw` ✔️ (9dab3b6) · 146 unit green · both boxes: only th DEV deploy left
  - [ ] 🦠 1 min: `tail -40 data/retag_night_1122.log` ➡ quotes about their chip? (rec ✔️ if ≥ 2/3 fit · was ≈ random · #308 read ≈ ½, F16)
### 🐙 M8 cd decide
### 🏔️ v1
- **🔊 AI voice packs, app side** (D-085 · #345 ③ · audio contract) · 🔊 on a quote card ➡ "Load · N MB?" ➡ pack clip, else phone voice · ⚙️ Mine row · hidden til a pack exists
  - [x] 🤖 `VoicePackTest` 10 · real dry pack 20/20 sha match · 150 unit + lint + check_langs green
  - [ ] 🦠 aftr th 1st real pack (audio): DEV ➡ Today 🔊 ➡ dialog txt + MB ok? ➡ voice plays + "🔊 KI-Stimme · …" line under th card
- **🧺 start at LEVEL 1 w a name** (🦠 scrap @1126 · D-084 Proposed · #345) · new reader = LEVEL 1 "Ware / Commodity" (Kapital's 1st word) · old 7 ranks = levels 2-8 · perks unchanged (on cards)
  - [x] 🤖 `LevelTest` · `MineLevelTest` (real screen) · `MomentsShotsTest.rank3` = LEVEL 4 · 140 unit + lint + check_langs green
  - [ ] 🦠 ½ min, DEV ⚙️ Mine (fresh install o 🧪 Testmodus): "LEVEL 1 · Commodity" ok? name alt: Lehrling / Apprentice · Lohnarbeiter / Wage labourer · badge = faded shovel (rank 2's) ➡ own 🧺 art? (➡ D-084 Accepted)
- **🔄 updater dflt OFF = banner ➡ Install main** (🦠 @1123 · D-083 · #345) · kit `AUTO_DEFAULT = false` · refused silent try (GrapheneOS) ➡ "Tap = install" notif + no retry 4 th version
  - [x] 🤖 `UpdateWorkerTest` nvr touched ➡ NOTIFIED · ON ➡ SILENT · OFF ➡ NOTIFIED · `refusedSilentInstallAsksInstead` · `refusedForegroundInstallPostsNothing` · 137 + 17 unit + lint green
  - [ ] 🦠 stable ship ❓ (nvr-touched users flip ON ➡ OFF on it) · Changelog line draft (add on top b4 `bin/ship.sh`): "🔄 Updates fragen wieder: Banner ➡ Installieren · ⚙️ „Automatisch aktualisieren“ = still im Hintergrund (Android 12+)" / "🔄 Updates ask again: banner ➡ Install · ⚙️ “Update automatically” = quietly in the background (Android 12+)" ok?
- **💬 Matrix v1 — wh's left** (aftr th in-app tst, 1 list · 🤖 = can build · ❓ = 🦠 picks)
  - ✔️ 🤖 **delete my account** built @1121 (D-072 Proposed): ⚙️ under Name ➡ "Delete account" ➡ confirm ➡ **redact evy own msg (D-076)** ➡ Matrix deactivate ➡ app forgets account + Kreise · DEV `0.13.0-dev.1122.1353`
    - [x] 🤖 `MatrixTest.deactivateErasesWithPasswordAuth` + `redactAllMineWalksEveryRoomAndPage` · `--app` 13/13 vs live continuwuity (step 13 = app's Kreis msg gone 4 B) · check_langs green
    - ✔️ #311 F3 (night: continuwuity ignores `erase`) ➡ app redacts 1st · fail/offline ➡ stops b4 deactivate (D-076)
    - [ ] 🦠 ½ min, read here: dialog **"Delete account?"** "Deletes your comments and circle messages, then your account on scagent.de, and you leave all reading circles. Anyone who already loaded them may have a copy; in circles you left earlier they stay. Your notes, bookmarks and stamps on this phone stay." [Delete] [Keep] ➡ ok? (D-072 + D-076 ➡ Accepted)
  - ✔️ 🤖 **🪪 name spoof** (#311 F4 · D-077 Proposed): oth ppl's comments + Kreis msgs show th real id muted aftr th name: **strix** `strix-ab12 · 3 min ago` · fr Element elsewhere = `@x:matrix.org` · DEV 1353
    - [x] 🤖 `MatrixTest.handleShowsTheUnspoofableId` (caught 1 🐛: "strix" fr matrix.org hid th id) · 148 unit + lint green
    - [ ] 🦠 i th Matrix sesh (📆 @1123): someone else's comment ➡ grey id aftr th name reads ok? (D-077 ➡ Accepted)
  - ✔️ spam · lost fon · no E2E = accepted 4 v1 · report + mod = gate b4 strangers (D-073 · #307)
  - 🤖 Kreis = hourly poll, no push (UnifiedPush/ntfy = 🔵 community ideas) · fine 4 v1
  - ✔️ client-IP retention (#341, ops src-read): 1 IP per device, overwritten, no purge ➡ README § Known gaps fixed (F15) · 👤 ops: signing key i bup (`pr/ops/matrix.md`) · night #308 ➡ [night_1122.md](night_1122.md) F2/F3/F6
- **🥚 eggs + drop cap** (merged #290: qa_1121 r2 · #246 D-068 · #248 widget) · DEV `0.13.0-dev.1121.0014`
  - [x] 🤖 qa_1121 r2 ck 1 icon stays plain: manifest hs 0 `activity-alias` = 1 fixed icon · ck 2 in-app egg = r1 ✔️ (🦠) + EggResTest · ck 3 drop cap = shot `07_chapter.png` matches (red T, no box, top = line 1, foot = line 3) · 📜 no ¶ = shot `03_chapter_scroll.png` · pinch ➡ ⏳ fon tst step 6
  - [x] 🤖 `EggArchShotTest` 30 eggs × 13 langs fit · `EggResTest.widgetShowsTheDaysEgg` · unit + lint green · DEV deployed
  - [ ] 🦠 ½ min: `o android/app/build/shots/egg_arch_de.png` ➡ headline on th arch + small tail under th egg reads ok? (rec ✔️: all 30 fit · ➡ D-068 Accepted)

## 📆
- 🦠 💬 **Mo 11/23 Matrix sesh w crew** (🦠 #311: "wanna finally mv fwd") · ~45 min · 🔑 primer 1st (5 lines):
  - 🔑 account = `@name:scagent.de` + pw · login ➡ server hands th device an **access token** (random string) ➡ evy call sends `Authorization: Bearer <token>` · 1 device = 1 token · logout / deactivate / server swap kills it ➡ 401
  - 📱 omm app = no login screen: 1st comment o Kreis ➡ app registers w th **reg token** baked i th APK (gate vs open signup, 3 UIA steps: token ➡ dummy) ➡ keeps token + random pw on th fon only · lost fon = lost account (D-073) unless Google bup restores it (D-081) · dead token ➡ quietly re-registers (D-075, F1)
  - 💻 ur priv account on tow: Element Desktop / app.element.io ➡ homeserver `scagent.de` ➡ Sign up w th reg token (`android/omm.properties` `omm.regToken`) 🧪 nvr tstd on continuwuity · fallback = ops admin room `!admin users create-user <name>` ➡ pw printed
  - 📱 same account on fon: Element X ➡ **log in** (Element X can't sign up, only log in ⬅ `pr/ops/matrix.md`) ➡ own token per device, same msgs
  - 🛡️ then ops: `!admin users make-admin @<u>:scagent.de` ➡ u can redact / kick i th 31 chapter rooms (= th "Melden" path b4 strangers, D-073)
  - agenda: ① account on tow + fon (above) ② join `#omm-kap-1:scagent.de` i Element ⬄ comment i omm DEV 1353 ➡ both sides see it · F4 grey id (D-077 box 👀) ③ F1 proof: old omm account posts again (D-075 Ck) ④ Delete account txt (D-076 box 👀) ⑤ pick nxt: ✔️ F5 paging (D-080, DEV 2238) · F6 send rate limit (ops) · ✔️ F7 `allowBackup` = keep (D-081)
- 🛍️ Fr 11/27 Black Friday: home-screen widget shows th 🛍️ egg left o "Marx verse" · only if it looks off ➡ screenshot (no box · render = `EggResTest`)

## 🔵
- 🤖 ‼️ **v1 this w** (🦠 @1123) = audio (TTS/STT packs fr audio crew) + Matrix chat ➡ then only feedback
  - ✔️ audio app side = D-085 (pack client, DEV) · 👤 audio: real packs (🦠 persona pick 1st) · 🦠 ❓ newly public `scagent.de/oh-my-marx/voice/` · STT = 0 need i v1 · Ogg-Opus = Android 10+ only (8-9 = phone voice, ok?)
  - 🤖 Andacht 🔊 btn (plays `a:<plan>:<day>` clips) · Matrix: "Melden" on a comment = gate b4 strangers (D-073)
- 🤖 CI push guard A ⬄ B still ❓ (rest ✔️ @1126, below)
- 🤖🔍 CI 1145 + 1163: th runner sometimes drops th 1st 📖 right-tap aftr opening a chapter (local 5/5 fine) ➡ ReaderGestureTest retries it (≤ 2×, nvr 2 turns) · root cause unknown (slow box? Matrix/comments call on open?) · 🦠 if a fon ever ignores th 1st tap ➡ tell
- 🤖🎨 **C-lane gaps** (done @1120: rank perks, feast emblems, 11 heads, initials, ¶, rtl / zh shots, gold swoosh · @1120 #229: 🥚 eggs replace heads in top bar, hero, quote card, D-066 · launcher egg dropped #241 · `launcherReset` rm @1122 #335, D-066 amended):
  - 🖼️ Feast frames (design § 14.2.1) · 🔊 stamp thud sound (D-035).
- 🦠 ‼️ ⭐🧪 **QA @1116** ➡ [qa_1116.md](qa_1116.md) — 14 ⚡ i 6 lanes (L3 share 1st) · 3 🎨 (➡ Fable spec aftr) · 2 🔍 · 💬 Qs ✔️ answered @1116 ➡ lanes built (D-039 · D-040) · Q8 revised = no Werkbank (D-041) · left: D3 joker · fon items (#13 cold-start ms · ✔️ #14 lib baseline profiles alr ship i th APK, #299)
- 🦠 ‼️ ⭐🧪 **QA sprint 0.9.x** — r1-r3 ✔️ · left ➡ [qa_1112.md](qa_1112.md) (🎨 10 design · ✔️ 🔍 5 research closed @1121 #299 · 💬 Qs) · ✔️ streak widget "shd show 2 at start" = superseded by D-045 (hero = streak only, stamps ➡ Stempeluhr) #290
  - later (needs design): 📖 real book look · ⏰ daily verse off-switch · name entry · 🔮 Marxoskop tiles · 🔓 Lesekreis unlock · 🏷️ quotes off-topic ➡ [notes.md § 🗃️](notes.md)
- 🦠 ‼️ 📨⭐ **user feedback** — 1st real users = gold · log (M · M2 · Jan · Mi) ➡ `feedback.md` + [notes.md § 🗃️](notes.md) · phase 1 ✔️ (page mail + GH issue forms + app ⚙️ btn)
  - 🤖🛠️ `/triage` skill (gh issues + Migadu ➡ label · dedupe · draft) · ⏸️ @1122 #335: 0 GH issues ever (labels `needs-triage` + `from-app` ready) ➡ build when issues pile up (≥ ~5), not b4 · 🔵 phase 2 in-app form ➡ `POST /feedback` aft QA rounds
- 🦠📣 ‼️ send **scagent.de/omm** 2 2-3 friends (0.6.0 ppl need 1 manual update, banner only fr ≥0.7.0)
  - 🚧 gate (D-073): beyond friends ➡ 1st 🤖 min "Melden" on a comment (Matrix `/report`) + 👤 ops rate limit / captcha on signup
- 👤 ‼️ 🌍 **LLM book qy** — 👤 native QA zh/ar/hi/bn/ur · `bin/translate_full.py --show 5 --langs it es` · alt = existing translations (© check) ➡ [notes.md § 🗃️](notes.md)
- 🦠 ‼️ **P3 habit test 2w** — aft th nxt big version is out, w audio (🦠 @1114) · Hooked ch 8: identify ➡ codify ➡ modify · start = 🦠's 1st day, log it here
- 🦠📚 ‼️ **Matrix + chat: K + tst** — 🦠 re notes § 🏗️ technical + `server/SERVICE.md` § 2 + `pr/ops/matrix.md` · ✔️ `bin/matrix_e2e.py` smoke vs live 18/18 @1121 (👀 above) · ✔️ `Matrix.kt` JVM tst (fake server) @1116: MatrixTest 8 ➡ 13, all calls covered (+ Lesekreis: create · members · name · leave · redact · setName · code)
- 🦠 ‼️ ⭐🧪 **QA @1117** ➡ [qa_1117.md](qa_1117.md) § triage — r1 = qa_1116 again (✔️) · ⚡ L1-L5 built @1117 (🐛 Hooked notes on hero · reader hints · ¶ ref gone · labels) = 👀 ck on DEV · 🎨 D6 streak game · D7 note = comment · D8 holidays · D9 close variants · ✔️ 🔍 R3 Duolingo (➡ D-046/047) · R4 holiday gaps (➡ D-052, 30 feasts) · ✔️ r4 @1118 triaged (qa_1117 § triage r4) + ⚡ built @1119 · left = 🎨 Fable: D6 + rank-up level-up · D8 + Xmas/NY/Valentine/Frauentag · D10 Day complete · D11 holy-book look
  - ✔️ Q1-Q7 answered by Ds (#290: Q1-Q3 D-045..047 · Q4-Q5 D-054 · Q6 D-051/052 + eggs · Q7 D-055) · ✔️ Q8 + Q9 = keep as is (D-074 · #307)
  - ✔️ L1-L5 shipped i **v0.12.0** @1118 (+ DEV `0.12.0-dev.1118.0151`) · r4 ✔️ built @1119 (L6-L8 · plan versioning D-042 · 6 🔒 plans D-043) ➡ ✔️ 🦠 eval @1120 · D-045 to D-047 accepted @1120 (#185) and built (C1) · qa_1119 leftovers + D-048..054 ➡ ✔️ qa_1120 r2 passed @1120 (#229, Accepted)
- 🤖🎨⭐ **design system** — spec ✔️ @1113 ➡ [design_system.md](design_system.md) (tokens · 5 btn kinds · voices · per-screen deltas) · D-029..035 ✔️ @1114 · D-036 = A ✔️ @1116
  - 🤖 lanes: ✔️ L0 · L2 · L3 · L5 @1114 ➡ left L1 Today · L4 Mine/⚙️/Hooked ➡ fan-in ➡ fon QA · L6 later (dark c · font b 🧪 · sounds b)
    - ✔️ @1121 #248 guard `Color(0x` 24 ➡ 18 · `RoundedCornerShape(` 17 ➡ 6 (same-value swaps only, 0 visual change) · rest (`.sp` 80 · 8/10/18dp shapes · M3 btns/chips) = real look changes ➡ need th lanes' b4/aft shots
- 🦠🔑 keystore: ✔️ symlink @1105 · left: KeePass note (`base64 -w0`, notes.md § 🔑) · 1 offline/restic copy · lap same `ln -s` ✔️ IaC @1117 (sys `lap.yml --tags keystore`, applies on lap catch-up · real file i th way = left + warn)
- 🦠🪝⭐ **Hooked/Fogg pass** (aft QA sprint) — raw ➡ `re/boox/hooked/hooked.md` § 📥 @1112
  - + share-app prompt aftr streak / medal (parked here, D-074 · #307)
  - audit: variable rewards · friction removed? · novelty · observable impact (Fogg) · measured impact ➡ A/B · gen txts w persuasion ♟️ (4 share-txt drafts ✔️ [qa_1112 § research](qa_1112.md), A/B needs a share counter) · inspire (Sinek) · AI-Luddites hook
- 🦠📄 **README ⬄ GH ⬄ scagent.de/omm polish** (merged 2 dups) — ✔️ @1116: README mirrors page (Obtainium 1st ➡ APK ➡ build: GH + Forgejo) · badges row (release · license · Android 8+ · Forgejo) · stale `0.8.0` ➡ release badge · lang lists 5 ➡ 11 · iPhone line alr gone
  - left: badges discussion + matrix (✔️ 🦠 @1117: yes Matrix public room · 🦠 1st needs a Matrix 🔍 dive ➡ ops 🌐 personal Matrix item) · lang btn · 😉 fake star-history · stats row · 🔑 T: 1 SSOT ➡ gen both (md + html) = pattern 4 ALL docs (wiki · kb · pr scaffold)
- 🦠💭 small Marx animation (🦠 @1113) — tiny, e.g. Marx reacts 2 Amen / streak · ⬄ halo states i design_system.md § 7 hero · big avatar + xp idea ➡ moved 2 `pr/cali/notes.md` § 🔵
- 🦠💡 app ideas: zuletzt gelesen/angeguckt · who's online (Matrix · Lesekreis) · events/Termine near u (⚠️ location implications) · personalize (Hooked: user invests data · usage analytics) · notif toggle (Android may alr do it)
- 🦠📦 **Ask Marx**: everyday problem (boss, rent, overtime) ➡ Marx answers short + punchy w a Kapital quote + ref (tap ➡ chapter)
  - 🔍 persona prompt + RAG over scored verses (must cite 1) · LLM: on-device Gemma 3 1B (~600 MB 👎) ⬄ ollama on tow/vps ⬄ API · ⚠️ no real legal/medical/money advice, crisis ➡ real help · ironic, not cringe
- 🦠👥 community nxt: QR invite (zxing) · ⚠️ report btn + mod tools b4 strangers join (= gate D-073 on th "send beyond friends" line)
- 🦠💡 community ideas (split @1113): Kreis plan (all same day) · real push (UnifiedPush/ntfy) instead o hourly poll · ④ Salon (forum per Abschnitt) · 💬 on cards · Randnotizen teilen (opt-in per note) · weekly Sitzung = Jitsi link · short invite `scagent.de/omm/k?c=` (ops)
- 🦠 Mitlesen opt-in prompt on 1st start (now dflt on) · ops `user_ips_max_age` ➡ Matrix v1 list (👤 ops line)
- 🦠🤔 QA 10 sample Andachten (tone: wink, not cringe) ➡ tweak `bin/andacht.py` · ⚠️ local M Marx errors + "Heute lädt uns Marx ein" evy day ➡ regen w Claude?
  - 💡 @1119 ([omm D-056 plan re-eval](adr.md)): 11 hand-written ones (bibel + montag) i `data/andachten_hand.jsonl` = sample 4 th tone · win over th LLM cache
- 🦠📦 gestures left: "Morgen"-teaser blurred + locked · shortcut "Weiterlesen" (last-read pref) · 🪝 Hooked notes 4 th new gestures (pull = slot machine · stories · shake = variable reward)
- 🦠📦 "Was nervt?" free-txt + embeddings · Mehrwert-Rechner · Fußnoten-Burns plan (Dunning "300 %" = fn 24:250) · Feiertage 2 (✔️ political + remembrance days = D-052, 30 feasts · left: Marx as Santa · own notif txt · Black Friday ➡ Fetisch-Detox · widget colours) · export "Mein Kapital" (md/json)
- 🤖 aft QA: rm page fotos (boox proc step 8)

## ✔️
- ✔️ @1126 #346 (🦠 "ops yes all 3"): a 🔒 build-tool holes 91 ➡ 0 (D-086, efe6f87) · b nightly `@v1` (4eb574d) · c ReaderGestureTest = alr fixed i 1d4fc85 (run 1156 ✅)
- ✔️ @1126 #345 🟢 **CI green again** (`v1` = 8dbbfe3 · runner Robolectric ✔️ by ops · osv on lockfile = run 1155 ✅) · 1145 = 1 real red: `ReaderGestureTest.tapTurnsDoubleTapOpensMenu` (tap w menu open didn't turn on th runner) ➡ tst closes th menu 1st ➡ run 1156 ✅
- ✔️ @1122 #335 🧪 **apptool bash tst** (kit): cali built `tool/test_apptool.sh` (118 cks, e8836e8) + ERR trap ➡ omm subtree pull (c8d8cf7) ➡ 118 ✔️ + omm `bin/deploy.sh -n` exit 0 (nothing uploaded)
- ✔️ @1122 #335 🤖🔍 **tags over-applied** ("kapitalist" 83 %, root = score.py `maxItems: 4`) ➡ `bin/retag.py` + night run + `tw` chip pool built + deployed · only 🦠 1-min read left (👀 🏷️)
- ✔️ @1122 #335 ✂️ **`launcherReset` rm** (DEV 1911 cleanup, cb37b25) · 🛡️ night F8 omm_api keys = `p:1-9999` only (ff0c68f) · live = 👤 ops rebuild omm-api
- ✔️ @1122 #332 🆕 **D-078 A** (🦠 "A dflt on ok"): ⚙️ "Update automatically" dflt ON, toggle = opt-out · dflt lives i th kit (`UpdateConfig.autoDefault`, pr/appkit 793b5e7) · nvr touched ➡ ON · set OFF ➡ stays OFF · 151 unit (134 + 17) + lint + 59 py green · DEV deployed · stable ➡ nxt release
- ✔️ @1122 #332 🦠📱 **fon ck: kit updater updated itself** (D-079 · 🦠 "Yes omm updater updated itself"): DEV kit build ➡ nxt kit build via th in-app updater
- ✔️ @1122 #329 🧩 **appkit lifted** ➡ own repo `pr/appkit/` (history kept) · omm vendors it at `appkit/` (`git subtree add --squash`, src = `~/gopro/go/pr/appkit` til Forgejo) · update: `git subtree pull --prefix appkit ~/gopro/go/pr/appkit main --squash` · 149 unit + lint + 59 py green · DEV 2149 · later items + extras rule ➡ [appkit/README.md](appkit/README.md)
- ✔️ @1122 #327 🧩 **appkit** (D-079 · was 🔵 #297 shared app code): `appkit/` = 4 shelves (core/selfupdate · tool/apptool + app-version · templates page + README · lessons) · omm consumes it, 0 behavior change (storage names kept, page byte-same) · 149 unit (133 app + 16 kit) + lint + 59 py green · adopt recipe ➡ [appkit/README.md](appkit/README.md)
- ✔️ @1122 #327 📄 install docs: APK + in-app update = 1st path, Obtainium = 2nd "also works" (README · page Release + Dev tab · llms.txt · DEV line) · page live (deploylog) · 💬 D-078 auto-update dflt ➡ 🦠
- ✔️ @1122 #327 🦠 fon tst **updater on a browser-APK install**, steps 1-4 (D-038): stable 0.12.0 browser APK ➡ 0.13.0 via th in-app updater ("Now omm update worked") ➡ matrix normal · APK ✔️ · silent half = ⏳ (needs nxt release)
- @1122 12 Qs applied @1121 (#307, 🦠 via M8 "ok all") ➡ D-073 + D-074 · ✔️ below · left = 1 Q added aftr th batch he saw (#299 19:19):
  - D-015 "Amen only under th snippet" nvr built + D-039 @1116 kept th quote-card Amen (✓ pop) ➡ mark D-015 Superseded by D-039, 0 code (#299) ❓
- @1122 **translate re-ask scheduled** `at` job 62 · Sun 11/22 03:00 · `bin/gpu_night.sh bin/translate.py --only a` (3 src-fixed Andachten × 11 langs, waits if GPU busy, gives up 08:00) ➡ log `data/translate_night_1122.log`
  - [x] 🤖 log ✔️ + check_langs green ➡ commit `data/i18n` + assets ➡ deploy DEV (#311: DEV `0.13.0-dev.1122.1353`)
  - ⚠️ 63 q/lang stale on EN ref only (DE same) ➡ left out, re-ask could undo #248 hand fixes
- ✔️ @1121 #307 M8 cd decide (🦠 via M8 "ok all"): 12 recs applied ➡ D-073 (Matrix v1 risks accepted · report/mod = gate b4 strangers) + D-074 (8 small keeps/drops) · closed: Fable egg pass (drop) · AGPL (keep GPL-3, D-006) · `/omm` deep paths (no) · url on Spruchkarte PNG (no) · share-app prompt ➡ Hooked pass · qa_1117 Q8 + Q9 keep · Element look ➡ cleanup 10/10
- ✔️ @1121 #305 🩹 matrix_e2e state per run: API run ➡ `priv/matrix_e2e_<stamp>.json` (no overwrite) · `--cleanup` / `--login` read all `matrix_e2e*.json` (legacy incl) · 55 py green
- ✔️ @1121 #299 🔵 sweep (closed, not dropped): 🔑 cross-app insights board = done by [app_lessons.md](app_lessons.md) (#297, omm ➡ cali · nxt app adds its own) · 🦠🧪 "fon update banner on Today, 0.11.0" (#125) = superseded by th ⏳ updater fon tst (step 3 = notif + Today banner) · qa_1117 r4 "untriaged" = stale, triaged + built @1119
- @1121 **Matrix e2e vs live Synapse** (#M8 @1121 · 🦠 ✔️ run) · `bin/matrix_e2e.py` = 18/18 ✔️ (token signup · chapter comment + delete · Lesekreis join/chat/Amen · guards) ➡ [status_1121.md § r2](status_1121.md)
  - [x] 🤖 18/18 ✔️ live @1121 13:27
  - [x] 🤖 **in-app path** (M8 @1121 14:27): `bin/matrix_e2e.py --app` = `MatrixLiveTest` drives th REAL app UI (Robolectric) vs live = 11/11 ✔️ ×3 · 12/12 ×2 (+ ⚙️ delete account) · ch 8 Note + 👥 chip ➡ app signs up + posts ➡ B reads (txt · page 245 · name) ➡ B replies ➡ 💬 sheet shows it ➡ hold = delete ➡ gone 4 B · Circle: found ➡ B joins by code ➡ chat both ways (Refresh) ➡ members both ➡ app joins B's Kreis by code
  - [x] 🤖 Element look skipped (D-074 · #307) ➡ `bin/matrix_e2e.py --cleanup` @1121 23:2x = 10/10 ✔️ (run 1427: 2 aliases dropped · 2 accounts deactivated · 2 leftover msgs redacted)
  - ⚠️ th app tst counts 1 anonymous read of p. 245 on th live Mitlesen counter (always on, D-020) · not i CI (needs token + live) ➡ run by hand aftr Matrix changes
- ✔️ @1121 M8 🔵 sweep: ✂️ dropped obsolete "🧔 guest heads (Zetkin · Engels · Jenny)" = moot since eggs (D-066) + `Head` enum rm'd (#260)
- ✔️ @1121 #260: ✂️ dead `Head` enum + `Feast.head` rm'd (🦠 yes · D-066 amended) · unit + lint green
- ✔️ @1121 #248: 🧹 id 12 quotes were still German (model echoed th src) ➡ hand-translated · it/fr Dante gloss 3,22 · check_langs refuses a quote ≈ its German src (Dante = Italian, skipped)
- ✔️ @1121 #248: 🧹 translation junk ar 30 · ur 23 · ru 9 · bn 5 fixed (`data/i18n/hand_fix.json`, D-069) · check_langs refuses half-Latin words · 3 DE/EN Andachten cleaned at src (30tage d2 + d24 · wertform d4) · ✔️ Literata subset = done i 627731e (2 stale lines closed)
- ✔️ @1120 #241: qa_1121 r1 1-3 ticked ➡ D-066 Accepted in-app · launcher egg dropped ("too silly") · drop cap w/o box + no ¶ (D-067 amended)
- ✔️ @1120 #229: qa_1120 r2 passed (🦠 "omm looks good") ➡ D-048..054 + D-059..062 Accepted · 🥚 30 eggs (D-066) · 🔴 drop cap by baselines (D-067) · 🐛 📜 pinch back · DEV 1911
- ✔️ @1120 (M8: 🔵 wo 🦠 answer): 🐛 DEV 1944 startup crash (`newPlans` b4 content load · UpgradeStartTest) · ★ C6 close sheet ([img/close_sheet.png](img/close_sheet.png) ➡ ❓ D-055 i qa_1119) · 🔤 13,319 ⬄ 13,320 DE/EN fixed (split.py: long Part head = meta ➡ jahr auto v2, day 161 only) · 🏗️ plan versioning = D-042 ✔️ @1119 (line moved here)
- ✔️ @1117 bl_triage (🦠 via M8 #139): ❌ in-app analytics (dupe ops 📊) · ❌ persona voices pointer (lives i pr/audio) · ✂️ rule ➡ file header · Matrix public room = yes · `/omm` + Spruchkarte As inline
- ✔️ @1116b-1117: 🔵 sweep (lint warn 35 ➡ 25 · glue tsts · Ludditen bridges ×11 · README · tags 🔍) · qa_1116 Q1-Q9 lanes (☭ back · 📖⬄📜 · DEV unlock · Forgejo icon · stamps total) · D-039/040/041 · DEV 2227 · EN gaps 7 ➡ 0 · fx queue · MatrixTest 13 · R1 plan table · 0.11.0 notes draft
- ✔️ @1116: D-030 CI OOM (gradle 1280m · 2× green) · D-036 = A · ship.sh X.Y.Z + README how-to · **v0.10.0 shipped** · qa_1116 r1+r2 triage · 6 ⚡ lanes (Book · Today · share/★ · settings · i18n 47 strings + guard · startup lazy JSON) · updater BAL fix
- ✔️ @1114: design system L0 (`ui/ds/` + guard + ds_gallery) + lanes L2 Book · L3 Plans/Circle (Leave confirm 🐛) · L5 bars/dialogs/fx queue · D-029..035 picked · updater D-038 · omm-api no pip D-037 · DEV 1114.1620 + .2117
- ✔️ @1113: big round pt 1 (EN data via pipeline · 📜 feed · 🔓 Hooked unlock · 🤖 Ludditen plan) + follow-ups (FÜFNZEHNTES + IdMoves D-028 · 13,120-125 align · UI/plan fan-in 11 langs) · r5 + r5b updater (D-023) · ❌ not deployed
- ✔️ @1112: QA sprint r1-r3 (3 fan-ins: plan continuity · About cleanup · share panel + ★ close · Abzeichen + tone · arch title card · Lesesaal-Name) · adr.md SDD (D-001..016) · qa_1112 triage · r2 test lanes (lint · Update · Matrix · bin)
- ✔️ @1108: **0.9.0 shipped** (13 langs whole book · fr ru hi bn ur id) · Obtainium DEV fixed 4 good · translate_full output cap · Feedback btn · GitHub mirror live
- _older ✔️ (@1028 ➡ @1107) ➡ [notes.md § 🗃️ pruned @1113](notes.md)_

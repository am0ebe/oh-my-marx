▶ resume ➡ [🔗 eos.md](eos.md) (top block)
_✂️ rule (🦠 @1113): drop candidate ➡ flag `✂️` on its line, nvr rm wo 🦠 ok · dropped @1113 ➡ [notes.md § 🗃️](notes.md)_

## 🏔️
- ☭ **v1** (roadmap 1/3 now · 🦠 @1120 ➡ [compass § 🗺️](../compass.md)) — design § 14 C-lanes + **Matrix chat** + **audio** (many langs · how deployed) + feedback loop · v1 = **QY bar**: UX + func + purpose feel ~80-90 % aftr thorough eval/tst (🦠) · done = stable release at th bar ➡ then analytics/Hooked · spin-off: Matrix 4 ourselves (ops)

## 🎯 sprint goal
- 🧪 **QA + iterate 0.9.x b4 anything new** (was 0.7.0, bumped @1112) (🦠 @1106: "test existing feats + iterate a couple o times, else slop") ➡ friends' feedback in ➡ fix ➡ only then 0.8.0
  - 🚦 rule: no version bump / ship / big new feat til th current one had ≥1 QA round on th fon · big side-quests (voice, chat infra) ➡ own pr

## ⏳
- 🦠📱 **fon tst: updater on a browser-APK install** (D-038 · @1115) — DEV app, ~5 min
  1. Chrome ➡ `scagent.de/oh-my-marx/dev/` ➡ tap th apk ➡ install OVER ur DEV (same key = data stays · now th installer o record = browser, not Obtainium)
  2. tell 🤖 "deploy" ➡ newer DEV goes live (= th 6 qa_1116 lanes + th 🩹 below)
  3. open DEV ➡ ≤1 min: notification "🆕 … is out" + Today banner ➡ tap ➡ "Install"
  4. 1st ti: Android "allow fr this source" ➡ toggle ➡ back = goes on by itself ➡ Android confirm ➡ Update ➡ ✔️ ⚙️ shows new version · streak + notes still there
     - 🩹 bonus: tap Install, then Home mid-download ➡ expect notification "🆕 Update ready" (was: nothing)
  5. ⚙️ "Update automatically" ON ➡ 🤖 deploy again ➡ open app ➡ ✔️ no dialog, quiet "✔️ installed" notification (we're installer now = silent, Android 12+)
  - ❌ any step ➡ screenshot (auto-lands i `/opt/pix_fon`) + step nr ➡ 🤖
  - 🧮 **updater matrix @1116** (same code both channels, only th URL differs: `/` ⬄ `/dev/`)
    | | Obtainium | APK (browser) |
    |--|--|--|
    | DEV | ✔️ fon: Obtainium 1h ck auto-updated (🦠) · app's own notif ➡ obtainium://refresh = unit tst only | ⏳ unit tsts ✔️ · fon = 5 steps above |
    | normal | ✔️ Obtainium follows th page (same as DEV) | ❌ untestable yet: 0.9.0 has th OLD banner (➡ web page, no 1-tap) · 1st real self-update = 0.10.0 ➡ 0.10.1 |
    - 0.9.0 ➡ 0.10.0 APK users: banner ➡ page ➡ install by hand 1× (then 1-tap / silent fr there)

## 👀
### 🌙 night run (#248 · 🦠 ✔️ @1121)
- **translate re-ask scheduled** `at` job 62 · Sun 11/22 03:00 · `bin/gpu_night.sh bin/translate.py --only a` (3 src-fixed Andachten × 11 langs, waits if GPU busy, gives up 08:00) ➡ log `data/translate_night_1122.log`
  - [ ] 🤖 log ✔️ + check_langs green ➡ commit `data/i18n` + assets ➡ deploy DEV
  - ⚠️ 63 q/lang stale on EN ref only (DE same) ➡ left out, re-ask could undo #248 hand fixes
- **🏷️ tags retag scheduled** (M8 @1121 · D-071) `at` job 63 · Sun 11/22 03:00 · `bin/gpu_night.sh bin/retag.py` (1867 Verse, ~1 h) ➡ log `data/retag_night_1122.log` ➡ `data/tags.jsonl`
  - built: `bin/retag.py` (0-2 topics ABOUT, weight 1-3) ➡ build.py `tw` ➡ Marxoskop chip pool = weight ≥ 2 · < 30 ➡ ≥ 1 · no data ➡ old `t` (= today) · `t` + plans untouched (D-042) · tsts: `Retag` 3 py · `marxoskopPoolPrefersAboutTopicsWithGuard`
  - [ ] 🤖 `bin/retag.py --stats` all ✔️ (≥ 30/chip) ➡ `bin/build.py` (only `tw` added i verses.json) ➡ unit green ➡ commit `data/tags.jsonl` + assets ➡ deploy DEV (w th translate box above = 1 deploy)
  - [ ] 🦠 DEV: Today ➡ "Was nervt?" ➡ tap Chef / Miete 3× each: quotes now about that? (was ≈ random)
### 🏔️ v1
- **Matrix e2e vs live Synapse** (#M8 @1121 · 🦠 ✔️ run) · `bin/matrix_e2e.py` = 18/18 ✔️ (token signup · chapter comment + delete · Lesekreis join/chat/Amen · guards) ➡ [status_1121.md § r2](status_1121.md)
  - [x] 🤖 18/18 ✔️ live @1121 13:27
  - [x] 🤖 **in-app path** (M8 @1121 14:27): `bin/matrix_e2e.py --app` = `MatrixLiveTest` drives th REAL app UI (Robolectric) vs live = 11/11 ✔️ ×3 runs · ch 8 Note + 👥 chip ➡ app signs up + posts ➡ B reads (txt · page 245 · name) ➡ B replies ➡ 💬 sheet shows it ➡ hold = delete ➡ gone 4 B · Circle: found ➡ B joins by code ➡ chat both ways (Refresh) ➡ members both ➡ app joins B's Kreis by code
  - [ ] 🦠 Element (login: `bin/matrix_e2e.py --login`): `#omm-kreis-pqv6wu` (API run) + `#omm-kreis-54t8rk` (app run: Rosa ⬄ Juno) read right? ➡ then `bin/matrix_e2e.py --cleanup`
  - ⚠️ th app tst counts 1 anonymous read of p. 245 on th live Mitlesen counter (always on, D-020) · not i CI (needs token + live) ➡ run by hand aftr Matrix changes
- **💬 Matrix v1 — wh's left** (aftr th in-app tst, 1 list · 🤖 = can build · ❓ = 🦠 picks)
  - ❓ **delete my account** missing: no ⚙️ btn ➡ comments + name stay on scagent.de 4ever (GDPR Art. 17) · fix = ⚙️ "Konto löschen" ➡ Matrix deactivate (`erase: true`, pw is on th fon) + forget Kreise · ~1 h 🤖 · v1 must?
  - ❓ **report + mod** b4 strangers: comments = public 4 evy app user now · no report btn · no mod tool (only Synapse admin API by hand) · min = "Melden" on a comment ➡ Matrix `/report` ➡ ops reads it · v1 must?
  - ❓ **spam**: th reg token rides i th public APK (decompile ➡ unlimited signups) · ok 4 friends-scale · bigger ➡ ops rate limit / captcha · accept 4 v1?
  - ❓ **lost fon = lost account**: pw only on th fon, no login/export ➡ reinstall = new user, Kreise gone (rejoin by code works) · accept 4 v1?
  - ❓ **no E2E** (Kreise say so i th app) · accept 4 v1? (rust-sdk = big)
  - 🤖 Kreis = hourly poll, no push (UnifiedPush/ntfy = 🔵 community ideas) · fine 4 v1
  - 👤 ops: Synapse `user_ips_max_age` shorter (dflt 28d) · signing key i bup (`pr/ops/omm.md` § 3i ck)
  - 🦠 Element 👀 box above · Mitlesen opt-in prompt (🔵, now dflt on)
- **Review:** [qa_1121.md § r2](qa_1121.md) on DEV `0.13.0-dev.1120.2220`, 4 checks, about 3 minutes.
  - Look at: launcher icon stays plain · in-app egg · drop cap without box (D-067) · 📜 no ¶ + pinch.
  - [x] 🤖 EggResTest 2/2 · unit 138 + lint + test_bin + check_langs green · DEV deployed
  - [ ] 🦠 good / note
- **#246 arch title cut off (D-068):** feast arch = headline only, rest = small lines under th egg · DEV `0.13.0-dev.1120.2256`
  - [x] 🤖 `EggArchShotTest` (30 eggs × 13 langs fit) · full unit suite green · shots `egg_arch_{en,de,ru,ar}.png` · DEV deployed
  - [ ] 🦠 shots `android/app/build/shots/egg_arch_de.png` (+ ru · ar): headline + tail split reads ok?
- **📱 widget shows th day's egg** (#248 · D-066): feast day ➡ 22 dp egg badge left o th head (was: feast emoji) · else ☭ as before · quote card shares th same `drawEgg`
  - [x] 🤖 `EggResTest.widgetShowsTheDaysEgg` (30 eggs render) · unit 142 + lint (6 = baseline) green · DEV `0.13.0-dev.1121.0014` deployed (+ #248 translation fixes)
  - [ ] 🦠 nxt feast = **Black Friday Fr 11/27**: home-screen widget ➡ egg left o "Marx verse" reads ok?

## 📆

## 🔵
- 🤖🎨 **C-lane gaps** (done @1120: rank perks, feast emblems, 11 heads, initials, ¶, rtl / zh shots, gold swoosh · @1120 #229: 🥚 eggs replace heads in top bar, hero, quote card, D-066 · launcher egg dropped #241):
  - 🤖 ✂️ `launcherReset` (notify/UpdateWorker.kt) = DEV 1911 cleanup ➡ rm aftr th nxt stable
  - 🖼️ Feast frames (design § 14.2.1) · 🔊 stamp thud sound (D-035).
- 🎨 💬 Fable design pass over the 30 eggs (`bin/eggs.py`) — only if 🦠 wants more polish aftr the gallery
- ‼️ ⭐🧪 **QA @1116** ➡ [qa_1116.md](qa_1116.md) — 14 ⚡ i 6 lanes (L3 share 1st) · 3 🎨 (➡ Fable spec aftr) · 2 🔍 · 💬 Qs ✔️ answered @1116 ➡ lanes built (D-039 · D-040) · Q8 revised = no Werkbank (D-041) · left: D3 joker · fon items
- ‼️ ⭐🧪 **QA sprint 0.9.x** — r1-r3 ✔️ · left ➡ [qa_1112.md](qa_1112.md) (🎨 10 design · 🔍 5 research · 💬 Qs) · ❓ streak widget "shd show 2 at start" = ? unanswered
  - later (needs design): 📖 real book look · ⏰ daily verse off-switch · name entry · 🔮 Marxoskop tiles · 🔓 Lesekreis unlock · 🏷️ quotes off-topic ➡ [notes.md § 🗃️](notes.md)
- ‼️ 📨⭐ **user feedback** — 1st real users = gold · log (M · M2 · Jan · Mi) ➡ `feedback.md` + [notes.md § 🗃️](notes.md) · phase 1 ✔️ (page mail + GH issue forms + app ⚙️ btn)
  - 🤖🛠️ `/triage` skill (gh issues + Migadu ➡ label · dedupe · draft) · 🔵 phase 2 in-app form ➡ `POST /feedback` aft QA rounds
- 🦠📣 ‼️ send **scagent.de/omm** 2 2-3 friends (0.6.0 ppl need 1 manual update, banner only fr ≥0.7.0)
- ‼️ 🌍 **LLM book qy** — 👤 native QA zh/ar/hi/bn/ur · `bin/translate_full.py --show 5 --langs it es` · alt = existing translations (© check) ➡ [notes.md § 🗃️](notes.md)
- 🦠 ‼️ **P3 habit test 2w** — aft th nxt big version is out, w audio (🦠 @1114) · Hooked ch 8: identify ➡ codify ➡ modify · start = 🦠's 1st day, log it here
- 🦠📚 ‼️ **Matrix + chat: K + tst** — 🦠 re notes § 🏗️ technical + `server/matrix/README.md` · ✔️ `bin/matrix_e2e.py` smoke vs live 18/18 @1121 (👀 above) · ✔️ `Matrix.kt` JVM tst (fake server) @1116: MatrixTest 8 ➡ 13, all calls covered (+ Lesekreis: create · members · name · leave · redact · setName · code)
- ‼️ ⭐🧪 **QA @1117** ➡ [qa_1117.md](qa_1117.md) § triage — r1 = qa_1116 again (✔️) · ⚡ L1-L5 built @1117 (🐛 Hooked notes on hero · reader hints · ¶ ref gone · labels) = 👀 ck on DEV · 🎨 D6 streak game · D7 note = comment · D8 holidays · D9 close variants · 🔍 R3 Duolingo · R4 holiday gaps · ⏳ **r4 @1118 untriaged** (page nrs i scroll · award ⬄ ☭ i series · "bookmarx" · more plans + unlock · "Tag geschafft" + rank-up redesign · holidays Xmas/NY/Valentine/Frauentag · drop caps i scroll + holy-book feel)
  - 💬 🦠: Q1-Q9 i th file (Q1 one score + Q2 milestones unlock th streak work)
  - ✔️ L1-L5 shipped i **v0.12.0** @1118 (+ DEV `0.12.0-dev.1118.0151`) · r4 ✔️ built @1119 (L6-L8 · plan versioning D-042 · 6 🔒 plans D-043) ➡ ✔️ 🦠 eval @1120 · D-045 to D-047 accepted @1120 (#185) and built (C1) · the qa_1119 leftovers and D-048 to D-054 are now in the qa_1120 round-two check (👀 above)
- 🎨⭐ **design system** — spec ✔️ @1113 ➡ [design_system.md](design_system.md) (tokens · 5 btn kinds · voices · per-screen deltas) · D-029..035 ✔️ @1114 · D-036 = A ✔️ @1116
  - 🤖 lanes: ✔️ L0 · L2 · L3 · L5 @1114 ➡ left L1 Today · L4 Mine/⚙️/Hooked ➡ fan-in ➡ fon QA · L6 later (dark c · font b 🧪 · sounds b)
    - ✔️ @1121 #248 guard `Color(0x` 24 ➡ 18 · `RoundedCornerShape(` 17 ➡ 6 (same-value swaps only, 0 visual change) · rest (`.sp` 80 · 8/10/18dp shapes · M3 btns/chips) = real look changes ➡ need th lanes' b4/aft shots
- 🦠🔑 keystore: ✔️ symlink @1105 · left: KeePass note (`base64 -w0`, notes.md § 🔑) · 1 offline/restic copy · lap same `ln -s` ✔️ IaC @1117 (sys `lap.yml --tags keystore`, applies on lap catch-up · real file i th way = left + warn)
- 💬 ops (optional): `/omm/*` ➡ `/oh-my-marx/{path}` (Obtainium URL `scagent.de/omm/`) · `/omm/src` ➡ GitHub 302 · `/omm/dev` redirect = valid again (/dev back @1106) · 🦠 @1117 "alr redirects" ➡ A: bare `/omm` yes · Q = deep paths (`/omm/dev` · `/omm/src`) ➡ want them?
- 🪝⭐ **Hooked/Fogg pass** (aft QA sprint) — raw ➡ `re/boox/hooked/hooked.md` § 📥 @1112
  - audit: variable rewards · friction removed? · novelty · observable impact (Fogg) · measured impact ➡ A/B · gen txts w persuasion ♟️ · inspire (Sinek) · AI-Luddites hook
- 📄 **README ⬄ GH ⬄ scagent.de/omm polish** (merged 2 dups) — ✔️ @1116: README mirrors page (Obtainium 1st ➡ APK ➡ build: GH + Forgejo) · badges row (release · license · Android 8+ · Forgejo) · stale `0.8.0` ➡ release badge · lang lists 5 ➡ 11 · iPhone line alr gone
  - left: badges discussion + matrix (✔️ 🦠 @1117: yes Matrix public room · 🦠 1st needs a Matrix 🔍 dive ➡ ops 🌐 personal Matrix item) · lang btn · 😉 fake star-history · stats row · 🔑 T: 1 SSOT ➡ gen both (md + html) = pattern 4 ALL docs (wiki · kb · pr scaffold)
- ❓ **AGPL-3 ⬄ GPL-3** (scrap @1112) — M8 A: same copyleft + AGPL also triggers on **network use** · ➡ app = GPL fine · 🤔 `server/` (omm_api) ➡ AGPL if u want forks o th server 2 share back · 🦠 @1117 Qs (agpl⬄gpl⬄lgpl⬄MIT · wh is th omm server) ➡ A i [🔗 kb answers](../kb/bl_triage_1117_answers.md) § 7
- 💭 small Marx animation (🦠 @1113) — tiny, e.g. Marx reacts 2 Amen / streak · ⬄ halo states i design_system.md § 7 hero · big avatar + xp idea ➡ moved 2 `pr/cali/notes.md` § 🔵
- 💡 app ideas: zuletzt gelesen/angeguckt · who's online (Matrix · Lesekreis) · events/Termine near u (⚠️ location implications) · personalize (Hooked: user invests data · usage analytics) · notif toggle (Android may alr do it)
- 🔑 **cross-app insights board**: extract L fr evy app we build (omm · cali · …) ➡ shared ⬄ vibe++/kb
- 💬 viral: in-app Spruchkarte share ➡ `scagent.de/omm` under th card img? · ✔️ checked @1116: link alw rides i th share txt (`Share.kt` shareCard, appended if missing) · on th img ❌ since QA r4 (no app-name line on th card) ➡ 🦠 only if u want it back on th img · 🦠 @1117 "on wh img?" ➡ A: th Spruchkarte share PNG (quote card) ➡ url printed on th card itself
- 📦 **Ask Marx**: everyday problem (boss, rent, overtime) ➡ Marx answers short + punchy w a Kapital quote + ref (tap ➡ chapter)
  - 🔍 persona prompt + RAG over scored verses (must cite 1) · LLM: on-device Gemma 3 1B (~600 MB 👎) ⬄ ollama on tow/vps ⬄ API · ⚠️ no real legal/medical/money advice, crisis ➡ real help · ironic, not cringe
- 👥 community nxt: QR invite (zxing) · ⚠️ report btn + mod tools b4 strangers join
- 💡 community ideas (split @1113): Kreis plan (all same day) · real push (UnifiedPush/ntfy) instead o hourly poll · ④ Salon (forum per Abschnitt) · 💬 on cards · Randnotizen teilen (opt-in per note) · weekly Sitzung = Jitsi link · short invite `scagent.de/omm/k?c=` (ops)
- 🔵 Mitlesen opt-in prompt on 1st start (now dflt on) · ask ops: Synapse `user_ips_max_age` shorter (dflt 28d)
- 🦠🤔 QA 10 sample Andachten (tone: wink, not cringe) ➡ tweak `bin/andacht.py` · ⚠️ local M Marx errors + "Heute lädt uns Marx ein" evy day ➡ regen w Claude?
  - 💡 @1119 ([omm D-056 plan re-eval](adr.md)): 11 hand-written ones (bibel + montag) i `data/andachten_hand.jsonl` = sample 4 th tone · win over th LLM cache
- 📦 gestures left: "Morgen"-teaser blurred + locked · shortcut "Weiterlesen" (last-read pref) · 🪝 Hooked notes 4 th new gestures (pull = slot machine · stories · shake = variable reward)
- 📦 "Was nervt?" free-txt + embeddings · Mehrwert-Rechner · Fußnoten-Burns plan (Dunning "300 %" = fn 24:250) · Feiertage 2 (+ political holidays + remembrance days ~evy 1-2 w · Marx as Santa · own notif txt · Black Friday ➡ Fetisch-Detox · widget colours) · export "Mein Kapital" (md/json)
- 🔍 tags over-applied ("kapitalist" 83 %) ➡ relative re-tag ➡ ⏳ built + night run @1121 (👀 🏷️ above)
  - ✔️ 🔍 @1116: root = score.py `maxItems: 4` ➡ LLM pads evy quote 2 4 tags ➡ chip "chef" 1233/1511 ≈ random (miete only 94)
- 🤖 aft QA: rm page fotos (boox proc step 8)
- 🦠🧪 fon update banner on Today · DEV 0.11.0-dev + stable 0.11.0 (fr M8 board #125)

## ✔️
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

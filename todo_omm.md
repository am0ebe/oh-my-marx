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
### 🏔️ v1
- **Review:** 8 checks in [qa_1120.md § r2](qa_1120.md), about 10 minutes on DEV `0.13.0-dev.1120.1813`.
  - Look at: reader gestures (hold = menu, D-065) · holy book · note card · Werkbank chips · My Capital · feasts.
  - Each tick also accepts the decisions on its line: D-048 to D-054 and D-060 to D-064 (all Proposed in [adr.md](adr.md)).
  - [ ] 🦠 good / note

## 📆

## 🔵
- 🤖🎨 **C-lane gaps** (done @1120: rank perks, feast emblems, 11 heads, initials, ¶, rtl / zh shots, gold swoosh):
  - 🧔 Guest heads (Clara Zetkin · Engels · Jenny Marx) need real portrait art, so they stay a token for now.
  - 🧔 Share card and widget should draw the feast head through `MarxHead` (they still use the plain head).
  - 🖼️ Feast frames (design § 14.2.1) · 🔊 stamp thud sound (D-035).
- 🤖🏗️ Literata subset still needs fonttools installed (blocked in auto mode) ➡ see the line below.
- ‼️ ⭐🧪 **QA @1116** ➡ [qa_1116.md](qa_1116.md) — 14 ⚡ i 6 lanes (L3 share 1st) · 3 🎨 (➡ Fable spec aftr) · 2 🔍 · 💬 Qs ✔️ answered @1116 ➡ lanes built (D-039 · D-040) · Q8 revised = no Werkbank (D-041) · left: D3 joker · fon items
- ‼️ ⭐🧪 **QA sprint 0.9.x** — r1-r3 ✔️ · left ➡ [qa_1112.md](qa_1112.md) (🎨 10 design · 🔍 5 research · 💬 Qs) · ❓ streak widget "shd show 2 at start" = ? unanswered
  - later (needs design): 📖 real book look · ⏰ daily verse off-switch · name entry · 🔮 Marxoskop tiles · 🔓 Lesekreis unlock · 🏷️ quotes off-topic ➡ [notes.md § 🗃️](notes.md)
- ‼️ 📨⭐ **user feedback** — 1st real users = gold · log (M · M2 · Jan · Mi) ➡ `feedback.md` + [notes.md § 🗃️](notes.md) · phase 1 ✔️ (page mail + GH issue forms + app ⚙️ btn)
  - 🤖🛠️ `/triage` skill (gh issues + Migadu ➡ label · dedupe · draft) · 🔵 phase 2 in-app form ➡ `POST /feedback` aft QA rounds
- 🦠📣 ‼️ send **scagent.de/omm** 2 2-3 friends (0.6.0 ppl need 1 manual update, banner only fr ≥0.7.0)
- ‼️ 🌍 **LLM book qy** — 👤 native QA zh/ar/hi/bn/ur · `bin/translate_full.py --show 5 --langs it es` · alt = existing translations (© check) ➡ [notes.md § 🗃️](notes.md)
- 🦠 ‼️ **P3 habit test 2w** — aft th nxt big version is out, w audio (🦠 @1114) · Hooked ch 8: identify ➡ codify ➡ modify · start = 🦠's 1st day, log it here
- 🦠📚 ‼️ **Matrix + chat: K + tst** — 🦠 re notes § 🏗️ technical + `server/matrix/README.md` · 🤖🧪 `bin/matrix_e2e.py` smoke vs live (⚠️ makes real accounts/rooms on th live Synapse ➡ 🦠 ok 1st) · ✔️ `Matrix.kt` JVM tst (fake server) @1116: MatrixTest 8 ➡ 13, all calls covered (+ Lesekreis: create · members · name · leave · redact · setName · code)
- ‼️ ⭐🧪 **QA @1117** ➡ [qa_1117.md](qa_1117.md) § triage — r1 = qa_1116 again (✔️) · ⚡ L1-L5 built @1117 (🐛 Hooked notes on hero · reader hints · ¶ ref gone · labels) = 👀 ck on DEV · 🎨 D6 streak game · D7 note = comment · D8 holidays · D9 close variants · 🔍 R3 Duolingo · R4 holiday gaps · ⏳ **r4 @1118 untriaged** (page nrs i scroll · award ⬄ ☭ i series · "bookmarx" · more plans + unlock · "Tag geschafft" + rank-up redesign · holidays Xmas/NY/Valentine/Frauentag · drop caps i scroll + holy-book feel)
  - 💬 🦠: Q1-Q9 i th file (Q1 one score + Q2 milestones unlock th streak work)
  - ✔️ L1-L5 shipped i **v0.12.0** @1118 (+ DEV `0.12.0-dev.1118.0151`) · r4 ✔️ built @1119 (L6-L8 · plan versioning D-042 · 6 🔒 plans D-043) ➡ ✔️ 🦠 eval @1120 · nxt 👀 = [qa_1119.md](qa_1119.md) (DEV 1120.1009) · left: 💬 D-045..047 ➡ C1 · D-048..054 Fable
- 🎨⭐ **design system** — spec ✔️ @1113 ➡ [design_system.md](design_system.md) (tokens · 5 btn kinds · voices · per-screen deltas) · D-029..035 ✔️ @1114 · D-036 = A ✔️ @1116
  - 🤖 lanes: ✔️ L0 · L2 · L3 · L5 @1114 ➡ left L1 Today · L4 Mine/⚙️/Hooked ➡ fan-in ➡ fon QA · L6 later (dark c · font b 🧪 · sounds b)
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
  - 💡 @1119 ([D-056](adr.md#d-056-1119--plan-re-eval-bibel-hand-picked--30tage--montag-re-ordered--andachten-found-by-verse-ids----qa_1117-r1-via-m8-155-salvage)): 11 hand-written ones (bibel + montag) i `data/andachten_hand.jsonl` = sample 4 th tone · win over th LLM cache
- 📦 gestures left: "Morgen"-teaser blurred + locked · shortcut "Weiterlesen" (last-read pref) · 🪝 Hooked notes 4 th new gestures (pull = slot machine · stories · shake = variable reward)
- 📦 "Was nervt?" free-txt + embeddings · Mehrwert-Rechner · Fußnoten-Burns plan (Dunning "300 %" = fn 24:250) · Feiertage 2 (+ political holidays + remembrance days ~evy 1-2 w · Marx as Santa · own notif txt · Black Friday ➡ Fetisch-Detox · widget colours) · export "Mein Kapital" (md/json)
- 🔍 tags over-applied ("kapitalist" 83 %) ➡ relative re-tag
  - ✔️ 🔍 @1116: root = score.py `maxItems: 4` ➡ LLM pads evy quote 2 4 tags ➡ chip "chef" 1233/1511 ≈ random (miete only 94)
  - 🤖 nxt (aftr QA round): retag "0-2 tags, only if ABOUT it" + weight 1-3 · chip = weight ≥ 2 · guard ≥ ~30 quotes/chip
- 🤖🏗️ subset Literata (D-058): 2 variable TTFs = +1.86 MB ➡ static Latin + Cyrillic (fonttools instancer + pyftsubset) ≈ −1.3 MB · ⚠️ `uvx fonttools` blocked by auto mode @1120 ➡ 🦠 ok / install fonttools 1st
- 🤖 aft QA: rm page fotos (boox proc step 8)
- 🦠🧪 fon update banner on Today · DEV 0.11.0-dev + stable 0.11.0 (fr M8 board #125)

## ✔️
- ✔️ @1120 (M8: 🔵 wo 🦠 answer): 🐛 DEV 1944 startup crash (`newPlans` b4 content load · UpgradeStartTest) · ★ C6 close sheet ([img/close_sheet.png](img/close_sheet.png) ➡ ❓ D-055 i qa_1119) · 🔤 13,319 ⬄ 13,320 DE/EN fixed (split.py: long Part head = meta ➡ jahr auto v2, day 161 only) · 🏗️ plan versioning = D-042 ✔️ @1119 (line moved here)
- ✔️ @1117 bl_triage (🦠 via M8 #139): ❌ in-app analytics (dupe ops 📊) · ❌ persona voices pointer (lives i pr/audio) · ✂️ rule ➡ file header · Matrix public room = yes · `/omm` + Spruchkarte As inline
- ✔️ @1116b-1117: 🔵 sweep (lint warn 35 ➡ 25 · glue tsts · Ludditen bridges ×11 · README · tags 🔍) · qa_1116 Q1-Q9 lanes (☭ back · 📖⬄📜 · DEV unlock · Forgejo icon · stamps total) · D-039/040/041 · DEV 2227 · EN gaps 7 ➡ 0 · fx queue · MatrixTest 13 · R1 plan table · 0.11.0 notes draft
- ✔️ @1116: D-030 CI OOM (gradle 1280m · 2× green) · D-036 = A · ship.sh X.Y.Z + README how-to · **v0.10.0 shipped** · qa_1116 r1+r2 triage · 6 ⚡ lanes (Book · Today · share/★ · settings · i18n 47 strings + guard · startup lazy JSON) · updater BAL fix
- ✔️ @1114: design system L0 (`ui/ds/` + guard + ds_gallery) + lanes L2 Book · L3 Plans/Circle (Leave confirm 🐛) · L5 bars/dialogs/fx queue · D-029..035 picked · updater D-038 · omm-api no pip D-037 · DEV 1114.1620 + .2117
- ✔️ @1113: big round pt 1 (EN data via pipeline · 📜 feed · 🔓 Hooked unlock · 🤖 Ludditen plan) + follow-ups (FÜFNZEHNTES + IdMoves D-028 · 13,120-125 align · UI/plan fan-in 11 langs) · r5 + r5b updater (D-023) · ❌ not deployed
- ✔️ @1112: QA sprint r1-r3 (3 fan-ins: plan continuity · About cleanup · share panel + ★ close · Abzeichen + tone · arch title card · Lesesaal-Name) · adr.md SDD (D-001..016) · qa_1112 triage · r2 test lanes (lint · Update · Matrix · bin)
- ✔️ @1108: **0.9.0 shipped** (13 langs whole book · fr ru hi bn ur id) · Obtainium DEV fixed 4 good · translate_full output cap · Feedback btn · GitHub mirror live
- _older ✔️ (@1028 ➡ @1107) ➡ [notes.md § 🗃️ pruned @1113](notes.md)_

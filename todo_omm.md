## 🎯 sprint goal
- 🧪 **QA + iterate 0.9.x b4 anything new** (was 0.7.0, bumped @1112) (🦠 @1106: "test existing feats + iterate a couple o times, else slop") ➡ friends' feedback in ➡ fix ➡ only then 0.8.0
  - 🚦 rule: no version bump / ship / big new feat til th current one had ≥1 QA round on th fon · big side-quests (voice, chat infra) ➡ own pr

## ⏳
- ▶ **RESUME @1113 eos #3** — pt 1 + M8 follow-ups ✔️ (`042ec60`, 78 tsts green, ❌ not deployed) · DEV on fon = still A (r4+r5+r5b)
  - nxt: 🦠 fon tst updater D-023 (5 steps) ➡ fon QA r5 ➡ 📸 shots b4 UI handoff · detail + ⚠️ traps + 👤 strix Qs ➡ [notes.md § 🗃️ pruned @1113](notes.md)

## ‼️
- ⭐🧪 **QA sprint 0.9.x** — r1-r3 ✔️ · left ➡ [qa_1112.md](qa_1112.md) (🎨 10 design · 🔍 5 research · 💬 Qs) · ❓ streak widget "shd show 2 at start" = ? unanswered
  - later (needs design): 📖 real book look · ⏰ daily verse off-switch · name entry · 🔮 Marxoskop tiles · 🔓 Lesekreis unlock · 🏷️ quotes off-topic ➡ [notes.md § 🗃️](notes.md)
- 📨⭐ **user feedback** — 1st real users = gold · log (M · M2 · Jan · Mi) ➡ `feedback.md` + [notes.md § 🗃️](notes.md) · phase 1 ✔️ (page mail + GH issue forms + app ⚙️ btn)
  - 🤖🛠️ `/triage` skill (gh issues + Migadu ➡ label · dedupe · draft) · 🔵 phase 2 in-app form ➡ `POST /feedback` aft QA rounds
- 🦠📣 send **scagent.de/omm** 2 2-3 friends (0.6.0 ppl need 1 manual update, banner only fr ≥0.7.0)
- 🌍 **LLM book qy** — 👤 native QA zh/ar/hi/bn/ur · `bin/translate_full.py --show 5 --langs it es` · alt = existing translations (© check) ➡ [notes.md § 🗃️](notes.md)
- 🦠📚 **Matrix + chat: K + tst** — 🦠 re notes § 🏗️ technical + `server/matrix/README.md` · 🤖🧪 `bin/matrix_e2e.py` smoke vs live + `Matrix.kt` JVM tst (fake server)

## 📆

## 🔵
- 🤖 **big round** pts 3/4/6/7 left (QA r5 @1113) · src qa_1112.md § r4 sweep + § big round pt 1 · pt 1 ✔️
  - #7 btn styles mixed (Today circles · Kreis pills · Marxoskop chips · Hooked AssistChips) ➡ 1 system (D7) ➡ design spec · 🤖 translate.py PlanDay xd/xe (Ludditen bridges = EN i oth langs)
- 🧪 self-update (D-021) on a real fon: 1st DEV deploy aft r5 ➡ old DEV (adb-installed) gets th notification wi ~1 d · Obtainium-installed = skipped by design
- 🦠🔑 keystore: ✔️ symlink @1105 · left: KeePass note (`base64 -w0`, notes.md § 🔑) · 1 offline/restic copy · lap same `ln -s` (➡ sys ansible?)
- 💬 ops (optional): `/omm/*` ➡ `/oh-my-marx/{path}` (Obtainium URL `scagent.de/omm/`) · `/omm/src` ➡ GitHub 302 · `/omm/dev` redirect = valid again (/dev back @1106)
- 🪝⭐ **Hooked/Fogg pass** (aft QA sprint) — raw ➡ `re/boox/hooked/hooked.md` § 📥 @1112
  - audit: variable rewards · friction removed? · novelty · observable impact (Fogg) · measured impact ➡ A/B · gen txts w persuasion ♟️ · inspire (Sinek) · AI-Luddites hook
- 📊 **in-app analytics** (priv-friendly) — DLs by version · screen dwell · filter out am0 ➡ dash = ops 📊 bin (`todo_ops`)
- 📄 **README ⬄ GH ⬄ scagent.de/omm polish** (merged 2 dups) — README mirrors page (Obtainium 1st ➡ DL ➡ src Forgejo + GH) · rm iPhone line · badges row (discussion · matrix · license) · lang btn · 😉 fake star-history 0➡1 · stats row · 🔑 T: 1 SSOT ➡ gen both (md + html) = pattern 4 ALL docs (wiki · kb · pr scaffold)
- ❓ **AGPL-3 ⬄ GPL-3** (scrap @1112) — M8 A: same copyleft + AGPL also triggers on **network use** · ➡ app = GPL fine · 🤔 `server/` (omm_api) ➡ AGPL if u want forks o th server 2 share back
- 🤖 🩹 **lint baseline** (CI @1108, `android/app/lint-baseline.xml`): 4 err known ➡ fix + `./gradlew updateLintBaseline` · `LosungTile.kt:25` (`@SuppressLint("StartActivityAndCollapseDeprecated")`) · `ReadScreen.kt:197` unused BoxWithConstraints scope · `app_name` not translated "en" ×2 (➡ `translatable="false"`) · + 29 warn
- 🎮 gamify: Tamagotchi-style Marx avatar + xp · starts weak/fragile ➡ fit on streak · pick ur avatar · xfer 2 oth apps (cali!)
- 💡 app ideas: zuletzt gelesen/angeguckt · who's online (Matrix · Lesekreis) · events/Termine near u (⚠️ location implications) · personalize (Hooked: user invests data · usage analytics) · notif toggle (Android may alr do it)
- 🔑 **cross-app insights board**: extract L fr evy app we build (omm · cali · …) ➡ shared ⬄ vibe++/kb
- 🤖🧪 ship.sh guard: evy `Lang.menu` lang hs `full_xx.json` + i18n ➡ else refuse (0.9.0 changelog claimed ur/hi/bn book b4 it existed)
- 🤖🔧 translate_full: log secs per call + count failed calls' toks i th meter (runaways were invisible 4 days)
- 💡 viral: in-app Spruchkarte share ➡ `scagent.de/omm` under th card img? (check wh it has now)
- 💡 lang packs (only if APK > ~30 MB): ship EN+DE, oth `full_xx.json.gz` on VPS ➡ download on lang pick (sha256 fr version.json)
- 🔊⭐ **realistic persona voices** ➡ own engine i **pr/audio** (reusable: omm · pod · …) · omm = 1st consumer · plan notes.md § voice 🗺️ (A Chatterbox pre-render on tow ➡ C distill 2 Piper ~20-60 MB) · ref voice = 🦠/consenting friend · 🚫 real Merz (impression = ok)
- 🎨⭐ **design pass fr `pr/des/`** (17 screenshots o another app) ➡ themes · widgets · layouts ➡ 🤖 1st: go thru th shots w 🦠, list what 2 steal
- 📦⭐ **Ask Marx**: everyday problem (boss, rent, overtime) ➡ Marx answers short + punchy w a Kapital quote + ref (tap ➡ chapter)
  - 🔍 persona prompt + RAG over scored verses (must cite 1) · LLM: on-device Gemma 3 1B (~600 MB 👎) ⬄ ollama on tow/vps ⬄ API · ⚠️ no real legal/medical/money advice, crisis ➡ real help · ironic, not cringe
- 👥 community nxt: QR invite (zxing) · Kreis plan (all same day) · real push (UnifiedPush/ntfy) instead o hourly poll · ④ Salon (forum per Abschnitt) · 💬 on cards · Randnotizen teilen (opt-in per note) · weekly Sitzung = Jitsi link · short invite `scagent.de/omm/k?c=` (ops) · ⚠️ report btn + mod tools b4 strangers join
- 🔵 Mitlesen opt-in prompt on 1st start (now dflt on) · ask ops: Synapse `user_ips_max_age` shorter (dflt 28d)
- 🤖🧪 tsts 4 @1105 glue: `Update.check` vs fake version.json (newer/older/garbage/offline) · `notes.py` vs Changelog.kt · `page()` dry-run ➡ assert `<h2>` + mailto + valid JSON
- 🔑 real release key + `apksigner rotate` · in-app download + install dialog (costs REQUEST_INSTALL_PACKAGES)
- 🌐 web/PWA 4 iPhone friends — later, rebuild (no shake, new voices)
- 🦠🤔 QA 10 sample Andachten (tone: wink, not cringe) ➡ tweak `bin/andacht.py` · ⚠️ local M Marx errors + "Heute lädt uns Marx ein" evy day ➡ regen w Claude?
- 📦 gestures left: "Morgen"-teaser blurred + locked · shortcut "Weiterlesen" (last-read pref) · 🪝 Hooked notes 4 th new gestures (pull = slot machine · stories · shake = variable reward)
- 📦 "Was nervt?" free-txt + embeddings · Mehrwert-Rechner · Fußnoten-Burns plan (Dunning "300 %" = fn 24:250) · Feiertage 2 (+ political holidays + remembrance days ~evy 1-2 w · Marx as Santa · own notif txt · Black Friday ➡ Fetisch-Detox · widget colours) · export "Mein Kapital" (md/json)
- 🏗️ AGP 9 + compileSdk 37 · 🔍 tags over-applied ("kapitalist" 83 %) ➡ relative re-tag
- 🦠🧪 install Android Studio (tarball, reuses `~/Android/Sdk`) ➡ notes.md § reading guide
- 🦠 P3 habit test 2w · 🤖 aft QA: rm page fotos (boox proc step 8)

## ✔️
- ✔️ @1113: big round pt 1 (EN data via pipeline · 📜 feed · 🔓 Hooked unlock · 🤖 Ludditen plan) + follow-ups (FÜFNZEHNTES + IdMoves D-028 · 13,120-125 align · UI/plan fan-in 11 langs) · r5 + r5b updater (D-023) · ❌ not deployed
- ✔️ @1112: QA sprint r1-r3 (3 fan-ins: plan continuity · About cleanup · share panel + ★ close · Abzeichen + tone · arch title card · Lesesaal-Name) · arch.md SDD (D-001..016) · qa_1112 triage · r2 test lanes (lint · Update · Matrix · bin)
- ✔️ @1108: **0.9.0 shipped** (13 langs whole book · fr ru hi bn ur id) · Obtainium DEV fixed 4 good · translate_full output cap · Feedback btn · GitHub mirror live
- _older ✔️ (@1028 ➡ @1107) ➡ [notes.md § 🗃️ pruned @1113](notes.md)_

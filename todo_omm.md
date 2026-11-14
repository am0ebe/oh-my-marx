## 🎯 sprint goal
- 🧪 **QA + iterate 0.9.x b4 anything new** (was 0.7.0, bumped @1112) (🦠 @1106: "test existing feats + iterate a couple o times, else slop") ➡ friends' feedback in ➡ fix ➡ only then 0.8.0
  - 🚦 rule: no version bump / ship / big new feat til th current one had ≥1 QA round on th fon · big side-quests (voice, chat infra) ➡ own pr

## ⏳
- ▶ **RESUME @1114 eos** — ds L0 + lanes L2 · L3 · L5 merged + pushed (`cb465b5`, 90 tsts green) · DEV live = `0.10.0-dev.1114.2117` (B) · prev DEV A = 1114.1620 (1st w new updater D-038)
  - ▶ nxt: 🦠 fon: install A by hand (if not yet) ➡ open app ➡ B notification should pop ≤1 min (Obtainium path) · then browser-APK path (install B fr page ➡ 🤖 deploy C) · 🦠 D-036 abc (⚙️ "Today hero") ➡ pick ➡ 🤖 L1 Today + L4 Mine/⚙️/Hooked lanes (same worktree-agent pattern) ➡ `bin/translate.py --only ui` (new labels: Back · Install · Switch on · Copied · Reading room · Not now…)
  - 👤 🦠: D-036 pick · fon tst updater (Obtainium + browser) · fon QA o th whole ds look · D-029 ✔️ came via M8 relay ➡ 🦠 skim design_system.md § 1
  - ⚠️ traps: `.claude/worktrees/` 3 lane checkouts + branches `worktree-agent-*` still there (merged ➡ rm = 🦠 ok) · TodayScreen still calls fx directly ➡ L1: `fx.play(amenFx(rare))` b4 `vm.amen()` + "Day complete!" dialog into a `RewardSlot` (Fx.kt KDoc) · D-034 Hooked note not built (L1) · d036 b in-app = big hero, card NOT shrunk · kreis_detail + unlock = no shot (fon check: Leave confirm) · guard baseline = recompute aft evy merge (strict both ways) · omm-api live rebuild = ops (their refl log says done @1114, not verified here)

## ‼️
- ⭐🧪 **QA sprint 0.9.x** — r1-r3 ✔️ · left ➡ [qa_1112.md](qa_1112.md) (🎨 10 design · 🔍 5 research · 💬 Qs) · ❓ streak widget "shd show 2 at start" = ? unanswered
  - later (needs design): 📖 real book look · ⏰ daily verse off-switch · name entry · 🔮 Marxoskop tiles · 🔓 Lesekreis unlock · 🏷️ quotes off-topic ➡ [notes.md § 🗃️](notes.md)
- 📨⭐ **user feedback** — 1st real users = gold · log (M · M2 · Jan · Mi) ➡ `feedback.md` + [notes.md § 🗃️](notes.md) · phase 1 ✔️ (page mail + GH issue forms + app ⚙️ btn)
  - 🤖🛠️ `/triage` skill (gh issues + Migadu ➡ label · dedupe · draft) · 🔵 phase 2 in-app form ➡ `POST /feedback` aft QA rounds
- 🦠📣 send **scagent.de/omm** 2 2-3 friends (0.6.0 ppl need 1 manual update, banner only fr ≥0.7.0)
- 🌍 **LLM book qy** — 👤 native QA zh/ar/hi/bn/ur · `bin/translate_full.py --show 5 --langs it es` · alt = existing translations (© check) ➡ [notes.md § 🗃️](notes.md)
- 🦠 **P3 habit test 2w** — aft th nxt big version is out, w audio (🦠 @1114) · Hooked ch 8: identify ➡ codify ➡ modify · start = 🦠's 1st day, log it here
- 🦠📚 **Matrix + chat: K + tst** — 🦠 re notes § 🏗️ technical + `server/matrix/README.md` · 🤖🧪 `bin/matrix_e2e.py` smoke vs live + `Matrix.kt` JVM tst (fake server)

## 📆

## 🔵
- 🎨⭐ **design system** — spec ✔️ @1113 ➡ [design_system.md](design_system.md) (tokens · 5 btn kinds · voices · per-screen deltas) · D-029..035 ✔️ @1114 · 💬 🦠: D-036 Today hero (mockups `d036_all.png`, blocks L1)
  - 🤖 lanes: ✔️ L0 · L2 · L3 · L5 @1114 ➡ left L1 Today (needs D-036) · L4 Mine/⚙️/Hooked ➡ fan-in ➡ fon QA · L6 later (dark c · font b 🧪 · sounds b)
- 🤖 **big round** left (QA r5 @1113): translate.py PlanDay xd/xe (Ludditen bridges = EN i oth langs) · #7 btn styles ➡ design system ⬆ · src qa_1112.md § r4 sweep
- 🦠🔑 keystore: ✔️ symlink @1105 · left: KeePass note (`base64 -w0`, notes.md § 🔑) · 1 offline/restic copy · lap same `ln -s` (➡ sys ansible?)
- 💬 ops (optional): `/omm/*` ➡ `/oh-my-marx/{path}` (Obtainium URL `scagent.de/omm/`) · `/omm/src` ➡ GitHub 302 · `/omm/dev` redirect = valid again (/dev back @1106)
- 🪝⭐ **Hooked/Fogg pass** (aft QA sprint) — raw ➡ `re/boox/hooked/hooked.md` § 📥 @1112
  - audit: variable rewards · friction removed? · novelty · observable impact (Fogg) · measured impact ➡ A/B · gen txts w persuasion ♟️ · inspire (Sinek) · AI-Luddites hook
- 📊 **in-app analytics** (priv-friendly) — DLs by version · screen dwell · filter out am0 ➡ dash = ops 📊 bin (`todo_ops`)
- 📄 **README ⬄ GH ⬄ scagent.de/omm polish** (merged 2 dups) — README mirrors page (Obtainium 1st ➡ DL ➡ src Forgejo + GH) · rm iPhone line · badges row (discussion · matrix · license) · lang btn · 😉 fake star-history 0➡1 · stats row · 🔑 T: 1 SSOT ➡ gen both (md + html) = pattern 4 ALL docs (wiki · kb · pr scaffold)
- ❓ **AGPL-3 ⬄ GPL-3** (scrap @1112) — M8 A: same copyleft + AGPL also triggers on **network use** · ➡ app = GPL fine · 🤔 `server/` (omm_api) ➡ AGPL if u want forks o th server 2 share back
- 🤖 🩹 **lint baseline** (CI @1108, `android/app/lint-baseline.xml`): 4 err known ➡ fix + `./gradlew updateLintBaseline` · `LosungTile.kt:25` (`@SuppressLint("StartActivityAndCollapseDeprecated")`) · `ReadScreen.kt:197` unused BoxWithConstraints scope · `app_name` not translated "en" ×2 (➡ `translatable="false"`) · + 29 warn
- 💡 small Marx animation (🦠 @1113) — tiny, e.g. Marx reacts 2 Amen / streak · ⬄ halo states i design_system.md § 7 hero · big avatar + xp idea ➡ moved 2 `pr/cali/notes.md` § 🔵
- 💡 app ideas: zuletzt gelesen/angeguckt · who's online (Matrix · Lesekreis) · events/Termine near u (⚠️ location implications) · personalize (Hooked: user invests data · usage analytics) · notif toggle (Android may alr do it)
- 🔑 **cross-app insights board**: extract L fr evy app we build (omm · cali · …) ➡ shared ⬄ vibe++/kb
- 🤖🧪 ship.sh guard: evy `Lang.menu` lang hs `full_xx.json` + i18n ➡ else refuse (0.9.0 changelog claimed ur/hi/bn book b4 it existed)
- 🤖🔧 translate_full: log secs per call + count failed calls' toks i th meter (runaways were invisible 4 days)
- 💡 viral: in-app Spruchkarte share ➡ `scagent.de/omm` under th card img? (check wh it has now)
- 🔊 persona voices = lives i **pr/audio** now (`voice/` engine + `todo_audio.md`) · omm side: wait 4 audio D-010 (voice packs ok?) ➡ then `render_pack.py` · 🚫 real Merz (impression = ok)
- 📦 **Ask Marx**: everyday problem (boss, rent, overtime) ➡ Marx answers short + punchy w a Kapital quote + ref (tap ➡ chapter)
  - 🔍 persona prompt + RAG over scored verses (must cite 1) · LLM: on-device Gemma 3 1B (~600 MB 👎) ⬄ ollama on tow/vps ⬄ API · ⚠️ no real legal/medical/money advice, crisis ➡ real help · ironic, not cringe
- 👥 community nxt: QR invite (zxing) · ⚠️ report btn + mod tools b4 strangers join
- 💡 community ideas (split @1113): Kreis plan (all same day) · real push (UnifiedPush/ntfy) instead o hourly poll · ④ Salon (forum per Abschnitt) · 💬 on cards · Randnotizen teilen (opt-in per note) · weekly Sitzung = Jitsi link · short invite `scagent.de/omm/k?c=` (ops)
- 🔵 Mitlesen opt-in prompt on 1st start (now dflt on) · ask ops: Synapse `user_ips_max_age` shorter (dflt 28d)
- 🤖🧪 tsts 4 @1105 glue: `Update.check` vs fake version.json (newer/older/garbage/offline) · `notes.py` vs Changelog.kt · `page()` dry-run ➡ assert `<h2>` + mailto + valid JSON
- 🦠🤔 QA 10 sample Andachten (tone: wink, not cringe) ➡ tweak `bin/andacht.py` · ⚠️ local M Marx errors + "Heute lädt uns Marx ein" evy day ➡ regen w Claude?
- 📦 gestures left: "Morgen"-teaser blurred + locked · shortcut "Weiterlesen" (last-read pref) · 🪝 Hooked notes 4 th new gestures (pull = slot machine · stories · shake = variable reward)
- 📦 "Was nervt?" free-txt + embeddings · Mehrwert-Rechner · Fußnoten-Burns plan (Dunning "300 %" = fn 24:250) · Feiertage 2 (+ political holidays + remembrance days ~evy 1-2 w · Marx as Santa · own notif txt · Black Friday ➡ Fetisch-Detox · widget colours) · export "Mein Kapital" (md/json)
- 🔍 tags over-applied ("kapitalist" 83 %) ➡ relative re-tag
- 🤖 aft QA: rm page fotos (boox proc step 8)
- _✂️ rule (🦠 @1113): drop candidate ➡ flag `✂️` on its line, nvr rm wo 🦠 ok · dropped @1113 ➡ [notes.md § 🗃️](notes.md)_

## ✔️
- ✔️ @1114: design system L0 (`ui/ds/` + guard + ds_gallery) + lanes L2 Book · L3 Plans/Circle (Leave confirm 🐛) · L5 bars/dialogs/fx queue · D-029..035 picked · updater D-038 · omm-api no pip D-037 · DEV 1114.1620 + .2117
- ✔️ @1113: big round pt 1 (EN data via pipeline · 📜 feed · 🔓 Hooked unlock · 🤖 Ludditen plan) + follow-ups (FÜFNZEHNTES + IdMoves D-028 · 13,120-125 align · UI/plan fan-in 11 langs) · r5 + r5b updater (D-023) · ❌ not deployed
- ✔️ @1112: QA sprint r1-r3 (3 fan-ins: plan continuity · About cleanup · share panel + ★ close · Abzeichen + tone · arch title card · Lesesaal-Name) · arch.md SDD (D-001..016) · qa_1112 triage · r2 test lanes (lint · Update · Matrix · bin)
- ✔️ @1108: **0.9.0 shipped** (13 langs whole book · fr ru hi bn ur id) · Obtainium DEV fixed 4 good · translate_full output cap · Feedback btn · GitHub mirror live
- _older ✔️ (@1028 ➡ @1107) ➡ [notes.md § 🗃️ pruned @1113](notes.md)_

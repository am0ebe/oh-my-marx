## 🎯 sprint goal
- 🧪 **QA + iterate 0.9.x b4 anything new** (was 0.7.0, bumped @1112) (🦠 @1106: "test existing feats + iterate a couple o times, else slop") ➡ friends' feedback in ➡ fix ➡ only then 0.8.0
  - 🚦 rule: no version bump / ship / big new feat til th current one had ≥1 QA round on th fon · big side-quests (voice, chat infra) ➡ own pr

## ⏳
- ▶ **RESUME @1113 eos** — QA r1-r4 built on main `34028b3` (pushed, tsts + build green) · stable still 0.9.0 · **r3 + r4 NOT deployed** · board = [qa_1112.md](qa_1112.md) § r4 (built + 9 sweep Qs) · y ➡ [arch.md](arch.md) D-014..019
  - ▶ **NEXT** = 👤 am0 `bin/deploy.sh` ➡ fon QA r4 (card arch above face · note tap-outside save + ★ discard · In book pulse · 👥 chip) ➡ answer sweep Qs 1-9 ➡ 🤖 build D3 (Amen ➡ reading snippet bottom = D-015) as 1st 🎨 item
  - 📸 b4 handing any UI change 2 am0: `./gradlew testDebugUnitTest --tests '*ShotsTest*' -Pshots --rerun` ➡ `android/app/build/shots/` ➡ montage + look (D-019)
  - 👤 strix: Changelog 0.9.1 wording · name label (D-016 alts) · ★ vs ✕ on note (D-017) · verse share gone on Today too (D-018) · 💬 qa_1112 Q2 Mitlesen · Q5-7 · stray `pr/omm/▐qq` ➡ rm? · native check hi/bn/ur/id · KeePass keystore note
  - ⚠️ traps: origin = Forgejo (only push target) · `gh` = GitHub fetch-only, mirror carries it (ship.sh waits ≤120 s) · gradle needs `JAVA_HOME=~/.local/opt/jdk21` (lib.sh sets it) · zsh `tr` alias, bare `===` · gradle test cached aft deleting shots ➡ `--rerun` · VPS = ops-owned (pr/ops/omm.md), nvr edit Caddy//srv · `android/omm.properties` (reg token) needed 4 comments/Kreise · fon screenshots only aft asking

## ‼️
- ⭐🧪 **QA sprint 0.9.x** — r1-r3 ⚡ done (fan-out lanes, 0 conflicts) · left ➡ [qa_1112.md](qa_1112.md): 🎨 10 design proposals (D1 landing · D2 book look · D3 Amen ➡ snippet · D4 auto-scroll feed …) · 🔍 5 research (Duolingo · YouVersion share · Fogg txt · Luddites/AI plan · feedback) · 💬 open Qs
  - ❓ streak widget "shd show 2 at start" = ? (still unanswered)
  - **later** (needs design, not this sprint): 📖 Read = real book look + page flip · ⏰ daily verse can't be turned off (🔍 bible apps' timing) · ❓ name entry · 🔮 Marxoskop tiles · 🔓 Lesekreis progressive disclosure (unlock aft ~1 month, Mi) · 🏷️ quotes off-topic sometimes ➡ manual sort per cat · ⬆️ update nag ("stops working fr date X") + in-app APK DL ⬄ Obtainium
- 📨⭐ **user feedback** — 1st real users = gold · 🔑 most imp signal
  - **log** ➡ `feedback.md` (who · wh · date)
    - M: "share" unclear (share wh? th app?) ➡ didn't try · auto Signal/oth share ✔️ · loved shake (didn't K possible) · playful · Tagesflow + Ahnen anim "fetzig" · "Mahlzeit…" note hit right while eating (time-of-day ✔️) · übersichtlich · 12 MB = good code · Marxoskop nice
    - M2 (dev): uneasy w a precompiled APK ➡ wants source ➡ link GitHub/Forgejo src visibly
    - Jan (dev): liked th AI-slop-w-creative-funny-msg plans
    - Mi (@1112 scrap): 👍 read plan good + not 2 long · anim geil · langs awesome · Krise shake awesome · 👎 voice · neg conditioning · Orden 2 real-socialist · ☭ 2 much · leseplan not continuous (same page 2×) + no reward · share unclear · quotes sometimes off · 💡 Lesekreis = good platform, onboard users 1st, unlock later · constrain urself: wh's core? (Rick Rubin) · thumb-zone map · update nag · in-app DL · metrics by version (cohorts) · GA alternatives?
  - **system** — phase 1 = links only, no new server (plan: notes.md § 📨)
    - ✔️ page: mail (fast) + GitHub issue link · Forgejo = code only (sign-up off ➡ no issues there)
    - ✔️ GitHub: issue forms 🐛 + 💡 + "no account? mail" (`.github/ISSUE_TEMPLATE/`, live aft push) · labels `needs-triage` `from-app`
    - ✔️ app ⚙️ ➡ Über: tappable "💬 Feedback" (0.9.0) · am0 @1108: go direct 2 issue/mail (QA lane B)
    - 🤖🛠️ `/triage` skill: `gh issue list -l needs-triage` + Migadu inbox ➡ label · dedupe · draft reply · mail-feedback ➡ `gh issue create -l from-app` (reworded, no names)
    - 🔵 phase 2 (aft QA rounds): in-app form ➡ `POST /feedback` (omm_api) ➡ priv inbox ➡ triage · CONTRIBUTING · CI
- 🦠📣 send **scagent.de/omm** 2 2-3 friends (0.6.0 ppl need 1 manual update, banner only fr ≥0.7.0)
- 🌍 **LLM book qy** (all 11 i th app since 0.8/0.9 · notes.md § 🌍): 👤 native QA zh/ar/hi/bn/ur · samples `bin/translate_full.py --show 5 --langs it es`
  - 🔍 alt = existing translations: © per translator (life + 70 y, ZH 50 y) · ES Juan B. Justo 1898 (†1928 ➡ prob PD) · ZH 郭大力/王亚南 1938 (CN PD ~2027) · IT Cantimori (© til 2036) · marxists.org hosting ≠ PD · 🦠 decide aft comparing
- 🦠📚 **Matrix + chat: K + tst**
  - 🦠 re: notes.md § 🏗️ technical: Matrix (10 min) + `server/matrix/README.md`
  - 🤖🧪 `bin/matrix_e2e.py`: 2 throwaway users ➡ Kreis gründen · beitreten · chat · Amen ➡ repeatable smoke tst vs live (th @1104 run wasn't saved) · + a bot user 🦠 can chat w fr th fon = tst chat alone
  - 🤖🧪 `data/Matrix.kt` JVM tst vs a tiny fake HTTP server (register UIA 3 steps · join · timeline parse · redacted dropped) · needs `testImplementation("org.json:json:…")`

## 📆

## 🔵
- _⬇ demoted fr ‼️ @1112 (WIP ≤5)_
- 🦠🔑 keystore: ✔️ symlink @1105 · left: KeePass note (`base64 -w0`, notes.md § 🔑) · 1 offline/restic copy · lap same `ln -s` (➡ sys ansible?)
- 💬 ops (optional): `/omm/*` ➡ `/oh-my-marx/{path}` (Obtainium URL `scagent.de/omm/`) · `/omm/src` ➡ GitHub 302 · `/omm/dev` redirect = valid again (/dev back @1106)
- _⬇ new @1112 (scrap), prio order_
- 🪝⭐ **Hooked/Fogg pass** (aft QA sprint) — raw ➡ `re/boox/hooked/hooked.md` § 📥 @1112
  - audit: variable rewards (wh r our rewards? where?) · friction removed? · novelty (pop sth new, curiosity) · observable impact (Fogg: "N ppl joined 24h") · measured impact ➡ A/B (call2act · img · invite txt)
  - gen txts w persuasion ♟️ (pregiving · reciprocity · cooperation · social proof) · simple! · inspire (Sinek: y Marx matters 2day, hard ➡ digestible) · AI-Luddites hook (🔵 line below)
- 📊 **in-app analytics** (priv-friendly) — DLs by version · screen dwell · filter out am0 ➡ dash = ops 📊 bin (`todo_ops`)
- 📄 **GH README polish** (scrap @1112, merge w README ⬄ page line below) — badges row like anything-llm (discussion · matrix instead o reddit · license) · lang btn · 😉 fake "star history" 0➡1 graph w a wink line · stats row (stars · contributors · DLs) — fun even at 0, "pls star/DL" 4 friends
- ❓ **AGPL-3 ⬄ GPL-3** (scrap @1112) — M8 A: same copyleft + 1 clause: AGPL also triggers on **network use** (users talk 2 a modified version over th net ➡ must offer src) · GPL only on distribution · ➡ app = GPL fine · 🤔 `server/` (omm_api) ➡ AGPL if u want forks o th server 2 share back
- 🤖 🩹 **lint baseline** (CI @1108, `android/app/lint-baseline.xml`): 4 err known ➡ fix + `./gradlew updateLintBaseline` · `LosungTile.kt:25` deprecated `startActivityAndCollapse(Intent)` (false alarm: SDK<34 branch only ➡ `@SuppressLint("StartActivityAndCollapseDeprecated")`) · `ReadScreen.kt:197` unused BoxWithConstraints scope · `app_name` not translated "en" ×2 (➡ `translatable="false"`) · + 29 warn
- 📄 **README ⬄ scagent.de/omm out o sync** ➡ README mirrors page (Obtainium 1st ➡ direct DL ➡ src Forgejo + GH) · rm iPhone line (o say: not planned, hard + 💰) · 🔑 T: 1 SSOT ➡ gen both (md + html) = pattern 4 ALL docs (wiki · kb · pr scaffold) ⬄ kb
- 🎮 gamify: Tamagotchi-style Marx avatar + xp · starts weak/fragile ➡ fit on streak · pick ur avatar · xfer 2 oth apps (cali!)
- 🎄 holiday imgs/msgs: + political holidays + remembrance days, ~evy 1-2 w (Marx as Santa…) ⬄ merge w "Feiertage 2" below
- 💡 app ideas: zuletzt gelesen/angeguckt · who's online (Matrix · Lesekreis) · events/Termine near u (⚠️ location implications) · personalize (Hooked: user invests data · usage analytics) · notif toggle (Android may alr do it)
- 🔑 **cross-app insights board**: extract L fr evy app we build (omm · cali · …) ➡ shared ⬄ vibe++/kb
- 🤖🧪 ship.sh guard: evy `Lang.menu` lang hs `full_xx.json` + i18n ➡ else refuse (0.9.0 changelog claimed ur/hi/bn book b4 it existed)
- 🤖🔧 translate_full: log secs per call + count failed calls' toks i th meter (runaways were invisible 4 days)
- 💡 viral: in-app Spruchkarte share ➡ `scagent.de/omm` under th card img? (check wh it has now)
- 💡 lang packs (only if APK > ~30 MB): ship EN+DE, oth `full_xx.json.gz` on VPS ➡ download on lang pick (sha256 fr version.json)
- 💡 🦠 @1106: "can tk ludites passage >> 4 ai marxoscope..." (Luddites passage, Kap. 13 Maschinerie ➡ Marxoskop card about AI?) >> this pix up on AI as new tek where ppl are scared or it disrupts society with how tekk like trains/coal disrupted society 2 marx time. so it REALLY fits well. we shd build on this and emphasize this. while staying winky ;)
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
- 📦 "Was nervt?" free-txt + embeddings · Mehrwert-Rechner · Fußnoten-Burns plan (Dunning "300 %" = fn 24:250) · Feiertage 2 (own notif txt · Black Friday ➡ Fetisch-Detox · widget colours) · export "Mein Kapital" (md/json)
- 🏗️ AGP 9 + compileSdk 37 · 🔍 tags over-applied ("kapitalist" 83 %) ➡ relative re-tag
- 🦠🧪 install Android Studio (tarball, reuses `~/Android/Sdk`) ➡ notes.md § reading guide
- 🦠 P3 habit test 2w · 🤖 aft QA: rm page fotos (boox proc step 8)

## ✔️
- ✔️ @1112: QA sprint r1-r3 (3 fan-ins: plan continuity · About cleanup · share panel + ★ close · Abzeichen + tone · arch title card · Lesesaal-Name) · arch.md SDD (D-001..016) · qa_1112 triage · r2 test lanes (lint · Update · Matrix · bin)
- ✔️ @1108: **0.9.0 shipped** (13 langs whole book · fr ru hi bn ur id) · Obtainium DEV fixed 4 good (ommStamp, any entry updates) + deploy guard + `--qr` · translate_full output cap (ur 30 h ➡ 1.8 h) · lang switch no EN flash · Feedback btn · GitHub mirror live
- ✔️ @1107: Obtainium DEV cause found (entry on defaults) · DEV app honest build status (dev/version.json) · remotes split (origin = Forgejo, gh fetch-only) + ship.sh waits 4 mirror · Orden · Hero · Amen · MarxDialog · freeze fix · fr/ru/id whole book
- ✔️ @1106: 0.8.0 shipped (whole book IT/ES/PT/ZH/AR) · ship.sh self-bump · page Stable|Dev tabs + share + OG card + llms.txt · `--redo-bad` 243/272 · repo public + `priv/`
- ✔️ @1105: 🔓 repo public (GPL-3.0 · Hooked OCR out o tree + history ➡ `re/boox/hooked/pages/` · README all EN · renamed `oh-my-marx` · Forgejo mirror) · bup `pr/.old/bibleApp_pre-filter_1104.bundle` · ⚠️ GH keeps old SHAs reachable = accepted risk
- ✔️ @1105: 🆕 update check (`data/Update.kt`) · release flow (ship.sh ➡ tag + GH release, `notes.py`) · page EN + What's new + footer btns + favicon · /dev rm'd (deploy = adb) · Migadu `omm@` + `hey@` · keystore symlink · 🌍 full Kapital ➡ 5 langs overnight · `pr/bible/` rm'd
- ✔️ dev 0.7.0 @1104: 👥 Lesekreise (5th tab): gründen w 6-char code (no look-alikes) · beitreten by code · 📤 invite text · today ✔️ per person (Amen ➡ `de.ohmymarx.amen` event i evy Kreis) · 🎟️ shared card (day counts only if all read) · chat · leave · e2e tstd on live server w 2 throwaway users · 3 tests · ⚠️ not E2E-encrypted (said i th UI) · 0.6.0 shipped + scagent.de/omm short link (ops)
- ✔️ dev 0.6.0 @1103 (2): 📉 Krise translated (card + flash, 7 langs) · 🌅 Teilen aus dem Buch = exactly th shown paragraph (long ➡ "…") · 📜 ⚙️ "Was ist neu" (ui/Changelog.kt, add on top b4 ship) · 🔵 DEV icon blue · Matrix README (why · friends chat · Element X · federation) · ops handoff ➡ now ops-owned runbook `pr/ops/omm.md` (bup §3i, fleet auto-rebuild/bump, mon, rip-out list) · contract = `server/SERVICE.md`
- ✔️ dev 0.6.0 @1103: 💬 comments per book page (Matrix: Synapse `omm-matrix` on VPS, client-only, token signup, 31 chapter rooms, e2e tstd w throwaway user) · Caddy /omm/api + /_matrix live (via vps-claude, bak .bak-1103/-1103b) · /bible ➡ /oh-my-marx (0.5.2 shipped)
- ✔️ dev @1103: 👥 community ① — `server/omm_api.py` (stdlib + sqlite, daily tokens, 5 tests) runs on VPS (`~am0/omm-api`, docker, `web` net, no public route yet) · app: ⚙️ Mitlesen opt-in, 👥 N× per book page, Home crowd line, INTERNET perm · dev build = own app `.dev` · README.md 4 friends · lib.sh: /oh-my-marx when it exists
- ✔️ v0.5.1 @1103: name = **Oh My Marx** · `bin/deploy.sh` (dev channel /bible/dev ➡ Obtainium · `-a` = wireless adb) replaces dev.sh · `bin/ship.sh` + `-h/--help/-?` on both + tools list (`bin/lib.sh`) · 1 version (`val appVersion`) + auto versionCode
- ✔️ v0.5.0 @1103: 🃏 Home = card carousel (cards 1st-class, no popup on open) · 📉 Krise = full-screen red flash + buzz + crisis card · ☭ swoosh on Amen/Tag geschafft · 📖 book reader (708 MEW pages, paper, page turn) · 😇 Saint Marx on cards · 🔖 double-tap bookmark · gold frame on marked · ✍️ inline notes · ❌ highlight btn + ⋯ · share-only dialog · ☭ = close · Jahresplan quotes fallback
- ✔️ v0.4.0 @1103: gestures (popup stories · chapter swipe · pinch font · double-tap ☭ burst · hold palette · pull-down oracle · shake Krise) · 📋 copy icon · 🎉 confetti · 📳 haptics · 🚀 3 launcher shortcuts + ⚙️ QS tile · "Karl" restored (strix: can stay) · `dev.sh -q` = install wo launch
- ✔️ v0.3.2 @1103: 🌅 notif/widget tap ➡ full-screen Losung (blurred card bg) · 🔊 prerender (tap = instant) + voice sample on chip tap · ⏰ picker always 24 h · 😇 new icon (halo + beard, notif + themed) · lang menu says "full txt: EN" · 🚫 "Karl" out o code/UI · 🩹 doubled quote marks · adb = SDK one (apt rm'd) · dev.sh tstd ✔️
- ✔️ v0.3.1 @1102: 🔊 holy voices on-device (TTS ➡ wav per pitch layer ➡ mix + Freeverb hall ➡ MediaPlayer, cached) · ☭☁️ Gottvater Marx = new default · 5 DSP tests · repo ➡ GitHub am0ebe/bibleApp (priv) + gitea remote (repo not created yet)
- ✔️ v0.3.0 @1101: 🎉 12 Feiertage (card motif + verse + greeting) · 🌅 time-o-day hero · 🔥 streak + 🕊️ Sabbat-Joker · 🎟️ Stempelkarte (2 gift) ➡ 7 ranks · 1 verse = reading day · 📱 widget · 🪝 Hooked-Modus + lesson page (4 phases + 6 extras) · card popup on tap + on evy Teilen · About: proudly vibecoded · notes: friends-wo-adb table + store FAQ
- ✔️ v0.2.0 @1031: name bibleApp · nav 🐛 (restoreState ➡ ⚙️) · Marx icon = home · lang dropdown + 7 langs (UI 101/101) · wheel time picker, greyed when Losung off · 🧪 Testmodus (= Fetisch + reroll/notif/popups/reset) · TTS 🐛 `<queries>` + personas · 🌅 daily Losung popup w share · verse tap ➡ labelled icon bar (palette replaces 4 ○) · copy · "Morgen: …" teaser (load nxt trigger) · About: version + vibecoding
- ✔️ plan + research @1028
- ✔️ P0 KB intake: OCR (gemma3 + glm-ocr 4 TOC) · hooked.md § 7 · kapital_toc.md · notes.md · tags.py ☭ · pr/CLAUDE.md row @1028
- ✔️ P1 fetch · split (1949 Absätze, 1103 Fußnoten, MEW pages) · align DE⬄EN (10/10 spot-check) @1028
- ✔️ P1 score (1873) · rank ⭐288 🔥82 · verbatim quotes · 9 plans / 404 days · 92 Andachten DE+EN @1028
- ✔️ P2 Android MVP: APK 15 MB builds · 4/4 JVM tests · Marx icon w halo · not yet run on a device @1028
- ✔️ toolchain user-space: JDK 21 · Gradle 8.14.3 · Android SDK 36 @1028
- ✔️ `.stignore`: + `**/.gradle/` `**/.kotlin/` @1028

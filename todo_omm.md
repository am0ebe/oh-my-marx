## 🎯 sprint goal
- 🧪 **QA + iterate 0.7.0 b4 anything new** (🦠 @1106: "test existing feats + iterate a couple o times, else slop") ➡ friends' feedback in ➡ fix ➡ only then 0.8.0
  - 🚦 rule: no version bump / ship / big new feat til th current one had ≥1 QA round on th fon · big side-quests (voice, chat infra) ➡ own pr

## ⏳
- ▶ **RESUME @1107 eos2** — stable **0.8.0** live · dev `0.9.0-dev.1107.1302` on /dev · all committed + pushed 2 Forgejo (omm 06bc09b)
  - ✔️ verified: Obtainium cause = th DEV entry on fon is on **defaults** (regex empty · Partial APK hash · reconcile off — strix screenshots 12:27 `/opt/pix_fon/pictures/Screenshots`) ➡ hash o identical 1st bytes = nvr updates. Our ➕ link's settings r fine (Obtainium import keeps them, src read) — th entry just wasn't made by it
  - ✔️ **Obtainium DEV solved 4 good @1107** (ref.md § trap): build stamps version into th APK's 1st zip entry ➡ even th defaults entry sees evy deploy · deploy.sh guard · live = `0.9.0-dev.1107.1902`
  - ▶ **NEXT = 👤 strix, once**: Obtainium ➡ pull 2 refresh ➡ DEV shows `1902` ➡ **Update** (Obtainium = installer now ➡ later ones can bg-update) ➡ then QA (below)
    - optional (real version names instead o hash): fon browser `scagent.de/oh-my-marx/dev/` ➡ ➕ ➡ Import (overwrites, no delete)
    - still no update shown ➡ tell me th DEV entry's "latest version" string
  - ⏳ bg on tow: `translate_full` chain (bn 56 % @1248, ur aft, then redo-bad + export) ➡ then `translate.py --only ui` (all 11 · also picks up th new DEV-status strings) ➡ commit `data/i18n_full/{hi,bn,ur}.jsonl` + `assets/content/full_{hi,bn,ur}.json` ➡ deploy
  - ⚠️ traps: **remotes @1107** origin = Forgejo only · `gh` = GitHub fetch-only (push DISABLED) · ✔️ push-mirror live @1107 (gh/main = origin/main fa9a8ae) ➡ `ship.sh` can tag again · `data/i18n_full/{hi,bn}.jsonl` untracked on purpose (partial) · zsh: `tr` alias, bare `===` · page meta "7 languages" ➡ fix at 0.9.0 ship
  - 👤 strix: entry fix ☝️ · fon QA dev: Orden grid + popup · streak card · Amen tap · impact fx · plans · `MarxDialog` · FR/RU/ID book · DEV-status line i ⚙️ What's new · native check hi/bn/ur/id glossary · 29 bad translations (`--bad`) · KeePass keystore note
  - ⚠️ VPS = **ops-owned** (pr/ops/omm.md) ➡ ask ops (msg `ops`), nvr edit Caddy//srv · `/srv/omm-matrix/compose.yml` owns synapse tag · `android/omm.properties` (reg token) needed 4 comments/Kreise · fon screenshots only aft asking (mem)
- 🦠 QA stable 0.8.0 (shipped wo fon QA): Obtainium ➡ update ➡ lang IT/中文/عربي ➡ 📖 book + footnote + 🔊 · Signal link ➡ preview card?

## ‼️
- 🦠 **QA notes @1107 (dev 1902)** — ✔️ 1+2 done i dev `2017`, rest open:
  - ✔️ hi/bn book missing = full_xx.json not exported yet (changelog ran ahead) ➡ exported · ur book still translating ➡ changelog says "(Urdu: book coming)" til then
  - ✔️ lang switch flashed EN 1st (new book parsed aft th switch) ➡ book loads 1st, then 1 jump (AppVm.loadFull)
  - 🔊 voice: 1 sober dflt, rm th oth voices (cringe) · real voices = own pr (pr/audio, see 🔵) · must pronounce th lang
  - 🐛 share btn lags ➡ y?
  - 🐛 1st share: badge + animation fire while th share sheet is still open + even on cancel ➡ fire aft a real share (o drop th claim)
  - 🎲 crisis-proof medal: needs more shakes (ppl shake a lot, too ez)
  - 📖 Read = look like a book: physical pages, page-turn · chapter jump = flip many pages at once
  - ⏰ can't turn off th daily verse ➡ 🔍 how bible apps etc time it: fixed ti · user-set · smart? · T: ready all day + nudge
  - 🧩 widgets: rm evythg aft "omm" i th widget names/descr (streak color…) ➡ it shows, no need 2 write it
  - 🏠 Home tab tap ➡ scroll 2 top (not remembered pos) · streak widget at th top
  - ✂️ rm th "Mitlesen / read along" note
  - ❓ name entry: 1st comment ask? dflt = random Marxist name (gender-equal)? ➡ then accounts? (name + Kreis later)
  - 🔮 Marxoskop: mk it look + appealing · tiles w icons per topic, tappable
- 🦠📣 send **scagent.de/omm** 2 2-3 friends (0.6.0 ppl need 1 manual update, banner only fr ≥0.7.0)
- 📨⭐ **feedback** — phase 1 = links only, no new server (plan: notes.md § 📨)
  - ✔️ page: mail (fast) + GitHub issue link · Forgejo = code only (sign-up off ➡ no issues there)
  - ✔️ GitHub: issue forms 🐛 + 💡 + "no account? mail" (`.github/ISSUE_TEMPLATE/`, live aft push) · labels `needs-triage` `from-app`
  - ✔️ app ⚙️ ➡ Über: tappable "💬 Feedback" ➡ `Update.page + #feedback` (MineScreen.kt, compiles) @1107 · ⏳ i nxt dev deploy · new string ➡ `translate.py --only ui` (queued aft chain)
  - 🤖🛠️ `/triage` skill: `gh issue list -l needs-triage` + Migadu inbox ➡ label · dedupe · draft reply · mail-feedback ➡ `gh issue create -l from-app` (reworded, no names)
  - 🔵 phase 2 (aft QA rounds): in-app form ➡ `POST /feedback` (omm_api) ➡ priv inbox ➡ triage · CONTRIBUTING · CI
- 📦⭐ **full txt IT/ES/PT/ZH/AR** (✔️ translated · not i th app yet · how + tst: notes.md § 🌍)
  - 🦠 read samples: `bin/translate_full.py --show 5 --langs it es`
  - 🔵 wire into app = new feat ➡ aft 0.7.0 QA: Read screen lang ≠ DE/EN ➡ own txt + "🤖 maschinell übersetzt" badge · jsonl 1.9-3 MB/lang (~12 MB all 5, APK zips it ⇩) ➡ i APK o per-lang download
  - 👤 native QA zh/ar
  - 🔍 alt = existing translations: © per translator (life + 70 y, ZH 50 y) · ES Juan B. Justo 1898 (†1928 ➡ prob PD) · ZH 郭大力/王亚南 1938 (CN PD ~2027) · IT Cantimori (© til 2036) · marxists.org hosting ≠ PD · 🦠 decide aft comparing
- 🦠📚 **Matrix + chat: K + tst**
  - 🦠 re: notes.md § 🏗️ technical: Matrix (10 min) + `server/matrix/README.md`
  - 🤖🧪 `bin/matrix_e2e.py`: 2 throwaway users ➡ Kreis gründen · beitreten · chat · Amen ➡ repeatable smoke tst vs live (th @1104 run wasn't saved) · + a bot user 🦠 can chat w fr th fon = tst chat alone
  - 🤖🧪 `data/Matrix.kt` JVM tst vs a tiny fake HTTP server (register UIA 3 steps · join · timeline parse · redacted dropped) · needs `testImplementation("org.json:json:…")`
- 🦠🔑 keystore: ✔️ symlink @1105 · left: KeePass note (`base64 -w0`, notes.md § 🔑) · 1 offline/restic copy · lap same `ln -s` (➡ sys ansible?)
- 💬 ops (optional): `/omm/*` ➡ `/oh-my-marx/{path}` (Obtainium URL `scagent.de/omm/`) · `/omm/src` ➡ GitHub 302 · `/omm/dev` redirect = valid again (/dev back @1106)

## 📆

## 🔵
- 🩹 /dev wo settings: hand-pasted URL (no ➕) ➡ Obtainium partial APK hash = same 4 evy build ➡ stuck · fix = mk th APK's 1st bytes differ per build (hacky) o tell testers "➕ only"
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

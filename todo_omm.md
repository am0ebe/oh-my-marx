## 🎯 sprint goal
- 🧪 **QA + iterate 0.7.0 b4 anything new** (🦠 @1106: "test existing feats + iterate a couple o times, else slop") ➡ friends' feedback in ➡ fix ➡ only then 0.8.0
  - 🚦 rule: no version bump / ship / big new feat til th current one had ≥1 QA round on th fon · big side-quests (voice, chat infra) ➡ own pr

## ⏳
- ✔️ @1106 **0.8.0 SHIPPED** = whole book i IT/ES/PT/ZH/AR (reader · footnotes · 🔊) · banner only 4 hand-installed APKs · dev `0.9.0-dev.1106.1211` on /dev
  - ✔️ `ship.sh minor|patch|major` bumps itself · dev auto-labelled w NEXT version · docs [ref.md](ref.md) (wiki entry) + README § Versions
  - ✔️ page: Stable | 🧪 Dev APK tabs · GitHub/Forgejo icons · share row + QR + OG preview card (`bin/og.png`) · `llms.txt` + JSON-LD
  - ⚠️ shipped WITHOUT fon QA (🦠 asked) ➡ ▶ NEXT: 🦠 Obtainium ➡ update both apps ➡ QA: lang IT/中文/عربي ➡ 📖 book + tap a footnote + 🔊 · share a link i Signal ➡ preview card shows?
  - ✔️ @1106 b: share panel · What's new newest-only (app + page) · EN dflt + DE 2nd · DEV top bar blue + "DEV" · annotated tags i ship.sh · 0.9.0 notes drafted · dev `0.9.0-dev.1106.1243`
  - 🐛💬 DEV app didn't update via Obtainium (stable did) ➡ server apk ok (pkg + key checked) ➡ prob installed via adb earlier ➡ 🦠 tap Install i Obtainium / send error txt / o uninstall DEV once + reinstall via Obtainium
  - 🔍 more langs (80 % goal): FR · RU · HI · BN · ID nxt (answer @1106 i chat)
  - 💡 viral: in-app Spruchkarte share ➡ add `scagent.de/omm` under th card img? (check wh it has now)
- ▶ **RESUME @1106** — repo **public** github.com/am0ebe/oh-my-marx (GPL-3.0) · Forgejo mirror `am0/oh-my-marx` · stable **0.7.0** live (tag v0.7.0) · dev `0.7.0-dev.1106.0007` on `/dev`
  - ✔️ @1106 page: no ⬇️ heading · 3rd option "Or build it yourself" (GitHub | Forgejo tabs, issue link) · What's new = pane, 🤏, no emoji · footer = Feedback (mailto w version) + Share, `#feedback` anchor
  - ✔️ @1106 `/dev` back = bare Obtainium source (no page) · `deploy.sh` ➡ /dev · `-a` = + adb · DEV app's update banner works again
  - ✔️ @1106 `--redo-bad`: 243/272 length warns fixed (29 left, `--bad`) · warns total 696 ➡ 453 (rest = fn markers) · ✔️ `translate.py --only ui` ➡ i18n_*.json updated (3 new 0.7.0 strings) ➡ nxt dev build has them
  - ✔️ all committed + pushed @1106 eos (issue forms live on GitHub) · 🔒 `priv/` = git-ignored scratch (scratch.md + img/) ➡ repo is PUBLIC, todo + notes too
  - ⚠️ th dev on /dev (`.1106.0007`) was built BEFORE th UI-string translation ➡ `bin/deploy.sh` again 4 IT/ES/PT/ZH/AR
  - ▶ NEXT: 🦠 Obtainium DEV ➡ pull 2 refresh (should update now) ➡ 🦠 QA (notes.md § 🧪, top 2 lines) ➡ 📣 friends · 🤖 smallest useful: Über "💬 omm@" ➡ tappable Feedback (‼️ below)
  - 👤 blocked on strix: fon QA · friends feedback · KeePass keystore note · 29 leftover bad translations = `--bad` (accept o redo)
  - ⚠️ traps: VPS = **ops-owned** (pr/ops/omm.md) ➡ omm nvr edits Caddy//srv, asks ops (msg `ops` session) · box's `/srv/omm-matrix/compose.yml` owns th synapse tag (fleet.py bumps) ➡ nvr rsync ours over it · `android/omm.properties` (reg token, git-ignored) needed 4 builds w comments/Kreise · screenshots of his fon only after asking (mem) · zsh aliases: `command mv/grep`, nvr bare `tr`

## ‼️
- 🦠📣 send **scagent.de/omm** 2 2-3 friends (0.6.0 ppl need 1 manual update, banner only fr ≥0.7.0)
- 📨⭐ **feedback** — phase 1 = links only, no new server (plan: notes.md § 📨)
  - ✔️ page: mail (fast) + GitHub issue link · Forgejo = code only (sign-up off ➡ no issues there)
  - ✔️ GitHub: issue forms 🐛 + 💡 + "no account? mail" (`.github/ISSUE_TEMPLATE/`, live aft push) · labels `needs-triage` `from-app`
  - 🤖📦 app ⚙️ ➡ Über: "💬 omm@" = plain txt now ➡ tappable "💬 Feedback" ➡ opens `scagent.de/oh-my-marx/#feedback` (o mailto w version + fon prefilled) · 🤏 change, next dev
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
- 💡 🦠 @1106: "can tk ludites passage >> 4 ai marxoscope..." (Luddites passage, Kap. 13 Maschinerie ➡ Marxoskop card about AI?)
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

## 🎯 sprint goal
- Vers des Tages on 🦠's fon via own Android app

## ⏳
- ▶ **RESUME @1105 (eos)** — repo **public** github.com/am0ebe/oh-my-marx (GPL-3.0, history scrubbed o Hooked OCR) · dir `pr/omm` · Forgejo `am0/oh-my-marx` · stable **0.6.0** live · dev **0.7.0-dev.1105.0129** on /dev (Kreise + pills + no-flicker + Über/Quellen + 🆕 update banner · EN page w What's new + omm@) · all committed + pushed (`f608166`+)
  - ⚠️ live: `translate_full.py` running overnight (nohup, GPU) ➡ `data/i18n_full/*.jsonl` growing + deliberately uncommitted ➡ commit only aft it's done (`pgrep -f translate_full` = still running)
  - ⚠️ stable page still old DE layout til `ship.sh` 0.7.0 · update banner only reaches ppl on ≥0.7.0 (0.6.0 users need 1 manual update)
  - ▶ NEXT (order): ✔️ Migadu aliases `omm@` + `hey@scagent.de` (@1105 strix) ➡ 🦠 read th overnight translation (`bin/translate_full.py --stats` + `--show 5 --langs it`) ➡ 🦠 QA dev (update banner: install an older dev, open app ➡ 🆕 banner) ➡ `bin/ship.sh` 0.7.0
  - 👤 blocked on strix: keystore symlink (cmd below, he cp'd it @1105) · fon QA · friends feedback
  - 🤖 aft th overnight job: `bin/translate.py --only ui` (3 new 0.7.0 strings timed out @1105 while GPU busy ➡ show EN i IT/ES/PT/ZH/AR til then) ➡ commit `data/i18n_full/` + i18n json
  - 🧹 `pr/bible/` = only a `.claude/` stub th old session re-created ➡ `rm -r ~/gopro/go/pr/bible` once no session runs there
  - ⚠️ traps: VPS = **ops-owned** (pr/ops/omm.md) ➡ omm nvr edits Caddy//srv, asks ops (msg `ops` session) · box's `/srv/omm-matrix/compose.yml` owns th synapse tag (fleet.py bumps) ➡ nvr rsync ours over it · `android/omm.properties` (reg token, git-ignored) needed 4 builds w comments/Kreise · screenshots of his fon only after asking (mem) · zsh aliases: `command mv/grep`, nvr bare `tr`

## ‼️
- 🔗 repo links: GitHub `github.com/am0ebe/oh-my-marx` = canonical (issues, releases, ppl trust it) · Forgejo `git.scagent.de/am0/oh-my-marx` = public mirror (same code, 200 anon ✔️) · ez 2 say: `scagent.de/omm` ➡ page links th src · 💡 ask ops 4 `scagent.de/omm/src` ➡ GitHub 302
- 🦠👀 QA dev **0.7.0-dev.1104.1352** (@1104 feedback round): card btns = 5 equal pills w labels (Teilen quiet) · tap Marx/Heute/⚙️ when alr there = no flicker · bottom "runterziehen/schütteln" hint gone · ⚙️ ➡ Über = 📜 Quellen + 🤖 wh th app adds + 🔓 src link
- 🔓 **public repo** (@1104 strix: show code 2 a privacy-aware IT guy): ✔️ ocr/ (Hooked ©) out o tree + history ➡ `re/boox/hooked/pages/` · ✔️ LICENSE GPL-3.0 · ✔️ README.md all EN @1105 (install · specs · build-it-urself beginner steps · online bits casual · known gaps · sources · centaur credit) · ✔️ repo ➡ `oh-my-marx` · ✔️ public @1104
  - ✔️ Forgejo renamed + remotes updated @1105 · pre-rewrite bup: `pr/.old/bibleApp_pre-filter_1104.bundle` · old transcripts: `~/.claude/projects/-home-strix-gopro-go-pr-bible/` (mem pool now `~/dotfiles/.claude/memory/-home-strix-gopro-go-pr-omm`)
  - ⚠️ GitHub keeps th pre-rewrite commits reachable by exact SHA (unknown 2 anyone) ➡ accepted risk
- 🦠🔑 **keystore = th app's identity**: Android installs an update only if signed w th SAME key · ours = `~/.android/debug.keystore` (only on tow) ➡ lose it = friends can't update (uninstall = data gone)
  - ✔️ symlink > cp (1 truth, ST syncs it 2 p15 + lap = lap builds same key · `secrets/` is git-ignored ✔️) · run once:
    `mkdir -p ~/dotfiles/secrets/android && mv ~/.android/debug.keystore ~/dotfiles/secrets/android/omm.keystore && ln -s ~/dotfiles/secrets/android/omm.keystore ~/.android/debug.keystore`
  - verify: build ➡ `~/Android/Sdk/build-tools/*/apksigner verify --print-certs android/app/build/outputs/apk/debug/app-debug.apk | grep SHA-256` = same as th shipped APK fr scagent.de/oh-my-marx/
  - ⚠️ ST deletes propagate ➡ also 1 offline/restic copy · lap: same `ln -s` (➡ sys: ansible task?)
  - 🔑 password manager: keystore = binary ➡ store as text: `base64 -w0 ~/dotfiles/secrets/android/omm.keystore` ➡ paste as a note (~3 KB) · restore: `base64 -d > omm.keystore` · + its pw (debug key: store/key pw `android`, alias `androiddebugkey`) · or attach th file (KeePassXC/Bitwarden do attachments)
  - 🔵 later: real release key + `apksigner rotate` (v3 lineage, Android 9+) ➡ friends update wo reinstall · "CN=Android Debug" = looks unfinished 2 secu ppl
- 📦⭐ **full Kapital txt i IT/ES/PT/ZH/AR** (🧪 experiment, @1105): `bin/translate_full.py` (DE source, EN ref, per-lang glossary, resumable, ⚠️ flags: length ratio + footnote markers)
  - tstd: 12 IT + 6 ZH paragraphs ✔️ read well · speed IT ~100 tok/s, ZH ~150 ➡ **~1.5-2 h/lang, ~8-9 h all 5** (2 M chars/lang: book 1.5 M + footnotes 0.5 M)
  - 🌙 run b4 leaving (order it ➡ es ➡ pt ➡ zh ➡ ar, whatever finishes is usable):
    `cd ~/gopro/go/pr/omm && nohup bin/translate_full.py > data/i18n_full/run.log 2>&1 &` · progress: `tail -f data/i18n_full/run.log` · stop: `pkill -f translate_full` (resume = same cmd)
  - 🌅 morning: `bin/translate_full.py --stats` · `--show 5 --langs it` (DE | EN | IT side by side)
  - then 🤖: wire into th app (Read screen: lang ≠ DE/EN ➡ own txt + "🤖 maschinell übersetzt" badge) · update Über + README (no more "full txt only DE/EN") · APK +~10 MB (consider per-lang download later)
  - 🔍 alt path = existing translations: © per translator (life + 70 y, ZH 50 y) · candidates 2 verify: ES Juan B. Justo 1898 (†1928 ➡ prob PD) · ZH 郭大力/王亚南 1938 (†1976/†1969 ➡ CN PD ~2027) · IT Cantimori (†1966 ➡ © til 2036) · ES Scaron/Siglo XXI + Roces (©) · marxists.org hosting ≠ PD · 🦠 decide aft comparing quality
- ✔️ **in-app update check** @1105 (`data/Update.kt`): each app start reads `version.json` (stable app ➡ stable page · DEV ➡ /dev/) ➡ `code` > installed versionCode ➡ gold banner on Heute ➡ tap = download page ➡ install over, data kept · no self-install (would need REQUEST_INSTALL_PACKAGES = scary perm 4 secu ppl)
  - 🔵 maybe later: in-app download + system install dialog (1 tap less, costs that permission)
- 🔵🤖 tsts 4 th @1105 glue (refl: all 3 shipped w 0 tsts, page bug only caught by curl): `Update.check` vs a fake version.json (newer/older/garbage/offline) · `bin/notes.py` vs Changelog.kt (dev suffix ➡ base, unknown ➡ empty) · `page()` dry-run in a tmp dir ➡ assert `<h2>` + mailto + valid JSON (would've caught th `'` i `${:+}` heredoc)
- ✔️ **release flow** @1105: `ship.sh` ➡ git tag + GitHub release w notes fr Changelog.kt (`bin/notes.py`) = archive o evy version · download page = EN, same structure as README, shows "What's new" + link 2 older releases
  - 🔄 loop: feature on dev (`deploy.sh`, ur fon) ➡ good ➡ bump `appVersion` + Changelog entry ➡ commit + push ➡ `ship.sh` · ship often (prototype)
- 📨 **mail**: README + page + app Über = `omm@scagent.de` · 🦠 Migadu admin: alias `omm@` + `hey@` ➡ ur inbox · optional per-source tracking: `omm-gh@` on GitHub etc. ➡ filter on To: · ⚠️ GMX has no `+` sub-addressing
- 🔵 Mitlesen opt-in prompt on 1st start (now dflt on) · 🔵 ask ops: Synapse `user_ips_max_age` shorter (dflt 28d)
- 📚 **dev ⬄ release** (4 strix, new 2 apps): 2 apps, 1 key · stable `de.strix.marxversion` "Oh My Marx" (red) ⬄ dev `….dev` "Oh My Marx DEV" (blue) = side by side, own data
  - `bin/deploy.sh` ➡ /oh-my-marx/dev/ (evy build, only ur fon) · `bin/ship.sh` ➡ /oh-my-marx/ + GitHub release (friends) · versionCode = minutes since 2026 ➡ always "newer"
- 🦠🧪 Obtainium on fon: /bible is gone ➡ edit/re-add: stable `https://scagent.de/oh-my-marx/` · ＋ 2nd app `https://scagent.de/oh-my-marx/dev/` = "Oh My Marx DEV" · no regex needed
- 🦠📣 send **scagent.de/omm** 2 2-3 friends ➡ feedback (short link live @1104, ops' Caddy 302 ➡ /oh-my-marx/)
- 🦠👀 QA 0.6.0 (shipped @1104): ⚙️ 👥 Mitlesen ON ➡ book: header "👥 N×" + bottom "💬 Mitreden" ➡ pick name ➡ comment ☭ ➡ hold own = delete · Home "👥 Heute lesen N mit · M × Amen" · ⚙️ rename
- 🦠👀 QA dev 0.7.0 👥 Kreise: tab 👥 Kreis ➡ gründen ➡ antippen ➡ 📤 Einladen ➡ friend taps th link ➡ page ➡ "In Oh My Marx öffnen" ➡ app ➡ Beitreten · Amen on Heute ➡ ✔️ i th Kreis · chat ☭ · 🎟️ only when all read · 🔔 friend's msg ➡ notif within ~1 h · evening nudge ➡ 🤖 ship 0.7.0
- 🔵 community ③c: QR code 4 th invite (zxing lib) · Kreis plan (all read th same plan day) · real push (UnifiedPush/ntfy — we alr run ntfy!) instead o hourly polling · ④ Salon (forum per Abschnitt) · 💬 on cards · ⚠️ report btn + mod tools b4 strangers join · short invite link `scagent.de/omm/k?c=` (= ops redir)
- 🔵 web/PWA version 4 iPhone friends — later, rebuild (no shake, new voices) · this proj stays Android 🤖📱
- 🦠👀 QA v0.5.0 on fon (via Obtainium!):
  - 🃏 Home = card deck: swipe ⬅ = earlier days · Teilen big · Amen ➡ ☭ swoosh · Marx icon/Heute tab = back 2 1st card
  - 📉 shake = red flash + buzz + KRISE card i front · ⬇ pull = 🔮 card · Marxoskop chips ➡ card up top
  - 📖 book: drag ⬅ = page turns (hinged left) · paper look · MEW page numbers · 🤏 font
  - 🔖 double-tap = bookmark + ☭ burst · marked verses = gold frame · ✍️ note inline (no popup) · no highlight btn, no ⋯
  - 😇 cards: Saint Marx w beard on top · Teilen on a verse = only big Share + ☭
  - Jahresplan: no more empty quotes (opening sentence)
- 🦠👀 QA v0.4.0 gestures on fon:
  - 🌅 Losung popup: tap card left/right = older/newer day · hold = voice pauses · swipe ⬆ = Amen · ⬇ = close · 📋 copy btn
  - 📖 chapter: swipe ⬅➡ = nxt/prev chapter · 🤏 pinch = font size (kept) · double-tap verse = ☭ burst + highlight · hold = palette
  - ☀️ Today: pull down = 🔮 oracle card · 📳 shake = 📉 Krise roast · 📋 copy icon on th Losung
  - 🚀 long-press app icon = Losung · Orakel · Krise! · ⚙️ QS tile: pull down 2× ➡ ✏️ ➡ drag "Marx-Losung" in
  - 🎉 confetti when th Stempelkarte fills (🪝 chip "Rang-Aufstieg" shows it)
- 🦠👀 QA v0.3.0 on fon: hero colours (🪝 🌅 chip cycles) · 🎟️ Stempelkarte + 🕊️ · "✔️ Gelesen"/"Amen" counts day · tap Losung ➡ card popup · Teilen on any verse ➡ card preview · 🎉 chip cycles 12 Feiertage · 📱 add widget · 🪝 notes tap ➡ Hooked page · About
- 🦠👀 QA v0.3.1 🔊 on fon: ⚙️ ➡ ☭☁️ Gottvater / ☁️ Gott / 📚 Marx im Lesesaal ➡ 🔊 on Losung · 1st tap 1-3 s wait, 2nd = instant? · hall too wet/dry? (`Room` i `logic/Voice.kt`) · zh/ar voice installed?
- 🔵 later: real cloned voice (own / friend's Merz *impression*, 🚫 real Merz) ➡ 🤖 pre-render w Chatterbox
- 🦠🤔 friends route: Obtainium + GitHub Releases? (notes.md § friends wo adb) ➡ needs ‼️ release key + license + public repo · store = later
- 🦠🤔 QA 10 sample Andachten (tone: wink, not cringe) ➡ tweak prompt i `bin/andacht.py` · ⚠️ local M makes Marx errors + "Heute lädt uns Marx ein" evy day ➡ maybe regen w Claude
- 🦠🧪 install Android Studio (tarball, reuses `~/Android/Sdk`) ➡ open `pr/omm/android/` ➡ read code per notes.md § reading guide

## 📆

## 🔵
- 🤖 🧪 tst `data/Matrix.kt` (refl @1104: 0 tsts, only th python flow): JVM tst vs a tiny fake HTTP server (register UIA 3 steps · join · timeline parse · redacted msgs dropped) · org.json needs `testImplementation("org.json:json:…")` (Android stub throws i unit tsts)
- 🎨⭐ **design pass fr `pr/des/`** (🦠 @1103: 17 screenshots o another app = inspiration) ➡ pick themes · widgets · layouts ➡ apply 2 OMM (cards, Home, book) · 🤖 1st: go thru th shots w 🦠, list what 2 steal
- 📦⭐ **realistic holy voice** (🦠 @1103 "STT horrible + slow") ➡ plan i notes.md § voice 🗺️: A pre-render Losung w Chatterbox on tow ➡ C distill into a Piper voice (~20-60 MB) 4 th reader
- 📦 gestures left over: "Morgen"-teaser blurred + locked (anticipation) · shortcut "Weiterlesen" (needs a last-read-chapter pref)
- 🪝 Hooked notes 4 th new gestures (pull = slot machine · stories = Instagram/Snapchat · shake = variable reward)
- 📦⭐ **Ask Marx: chat w an advisor Marx** (🦠 idea @1102): user describes an everyday problem (boss, rent, overtime, shopping urge) ➡ Marx answers short + punchy, w a matching Kapital quote + ref (tap ➡ open th chapter)
  - 💡 why it fits teaching: ties Kapital 2 th user's own life (transfer, not abstract theory) + a direct positive experience o th app ➡ th reason 2 come back (Hooked: variable reward o th self + investment)
  - 🔍 how: persona prompt + RAG over our scored verses (embed ➡ top-k quotes ➡ LLM must cite 1) · LLM where? on-device (Gemma 3 1B via MediaPipe/LiteRT, offline, ~600 MB 👎) ⬄ own server (ollama on tow/vps ➡ needs INTERNET + hosting) ⬄ API (costs, privacy) · ⚠️ guardrails: no real legal/medical/money advice, crisis ➡ point 2 real help · ironic tone, not cringe
- 📦 🔊 pre-rendered holy voices (Chatterbox ML + church reverb) · DE+EN 1st
- 📦 full verse txt IT/ES/PT/ZH/AR (1.3 M chars/lang, ~1-2 h/lang local) — or PD translations? (zh: 郭大力/王亚南 1938 → PD i CN fr 2027)
- 🔍 QA zh/ar translations w a native speaker 👤
- 📦 "Was nervt?" ➡ free-txt + embeddings match (MVP = 7 chips ➡ tags)
- 📦 Mehrwert-Rechner (salary ⬄ revenue/employee)
- 📦 Fußnoten-Burns: rank footnotes 4 roasts (Dunning "300 %" = fn 24:250) ➡ own plan
- 📦⭐ **Kapital-Lesekreis** (🦠 @1103: community = th missing Hooked piece · YouVersion's #1 driver = "Plans With Friends")
  - 💡 MVP wo accounts: host makes a circle ➡ invite link/QR (`scagent.de/kreis/ABC`) ➡ all read th same plan day ➡ see who said Amen today (✔️ dots, no streak shaming) ➡ 1 comment thread per day's verse
  - 💡 "Randnotizen teilen": ur ✍️ notes on a verse visible 2 th circle (opt-in per note) = social proof + investment
  - 💡 weekly "Sitzung": 1 tap ➡ Jitsi/Signal group call link 4 th circle · agenda = th week's 🔥 verses
  - 💡 Kreis-Stempelkarte: card fills when *evyone* read ➡ solidarity, not competition (Marx-y twist on streaks)
  - 🔍 server: tiny API on th VPS (FastAPI + SQLite, behind Caddy) · or Matrix room per circle (E2E, existing clients) · or Nextcloud (alr hosted) · ⚠️ needs INTERNET permission + privacy note
  - 🪝 Hooked: social trigger ("Anna hat heute Amen gesagt") · variable reward o th tribe · investment = shared notes
- 📦 Feiertage 2: own notif txt per feast · ctx plans (Black Friday ➡ Fetisch-Detox · Halloween ➡ Vampir-Plan) · widget in feast colours
- 📦 export "Mein Kapital" (md/json) — Mine screen promises it
- 🏗️ AGP 9 + compileSdk 37 upgrade (AndroidX pinned 2 late-2025) = L step
- 🏗️ release signing key (friends' updates need same key once off debug)
- 🔍 tags over-applied by LLM ("kapitalist" 83 %) ➡ relative re-tag like rank.py?
- 🦠 P3 habit test 2w · identify ➡ codify ➡ modify
- 🤖 aft QA: rm page fotos (boox proc step 8)

## ✔️
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

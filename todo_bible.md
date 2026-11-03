## 🎯 sprint goal
- Vers des Tages on 🦠's fon via own Android app

## ⏳

## ‼️
- 🦠🧪 Obtainium on fon: /bible is gone ➡ edit/re-add: stable `https://scagent.de/oh-my-marx/` · ＋ 2nd app `https://scagent.de/oh-my-marx/dev/` = "Oh My Marx DEV" · no regex needed
- 🦠👀 QA dev 0.6.0: ⚙️ 👥 Mitlesen ON ➡ book: header "👥 N×" + bottom "💬 Mitreden" ➡ pick name ➡ comment ☭ ➡ hold own = delete · Home "👥 Heute lesen N mit · M × Amen" · ⚙️ rename
- 🦠🔒 `ssh -t vps sudo rmdir /srv/www/bible` (empty leftover, root-owned parent)
- 🦠📣 send scagent.de/oh-my-marx 2 2-3 friends ➡ collect feedback (after dev QA ➡ 🤖 ship 0.6.0)
- 🔵 community ③ Kreise (private rooms, invite link/QR) + friends/DMs ➡ ④ Salon (forum per Abschnitt) · 💬 on cards (today's Losung thread) · ⚠️ report btn + mod tools b4 strangers join
- 🔵 web/PWA version 4 iPhone friends — later, rebuild (no shake, new voices) · this proj stays Android 🤖📱
- 🔵 rename GitHub/Forgejo repo bibleApp ➡ OhMyMarx (remotes + Obtainium URL unchanged on VPS)
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
- 🦠🧪 install Android Studio (tarball, reuses `~/Android/Sdk`) ➡ open `pr/bible/android/` ➡ read code per notes.md § reading guide

## 📆

## 🔵
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

## 🎯 sprint goal
- Vers des Tages on 🦠's fon via own Android app

## ⏳

## ‼️
- 🦠🧪 fon: Wireless debugging ON ➡ "Pair w code" ➡ tow `adb pair IP:PORT CODE` (once) ➡ then evy iter = `bin/dev.sh` (build + install + launch, 0 taps)
- 🦠🤔 friends/away updates: 🤖 write `bin/ship.sh` ➡ APK on scagent.de ➡ Obtainium on fon? (needs go 4 public URL + `/srv/www` subdir)
- 🦠👀 QA v0.3.0 on fon: hero colours (🪝 🌅 chip cycles) · 🎟️ Stempelkarte + 🕊️ · "✔️ Gelesen"/"Amen" counts day · tap Losung ➡ card popup · Teilen on any verse ➡ card preview · 🎉 chip cycles 12 Feiertage · 📱 add widget · 🪝 notes tap ➡ Hooked page · About
- 🦠👀 QA v0.3.1 🔊 on fon: ⚙️ ➡ ☭☁️ Gottvater / ☁️ Gott / 📚 Marx im Lesesaal ➡ 🔊 on Losung · 1st tap 1-3 s wait, 2nd = instant? · hall too wet/dry? (`Room` i `logic/Voice.kt`) · zh/ar voice installed?
- 🔵 later: real cloned voice (own / friend's Merz *impression*, 🚫 real Merz) ➡ 🤖 pre-render w Chatterbox
- 🦠🤔 friends route: Obtainium + GitHub Releases? (notes.md § friends wo adb) ➡ needs ‼️ release key + license + public repo · store = later
- 🦠🤔 QA 10 sample Andachten (tone: wink, not cringe) ➡ tweak prompt i `bin/andacht.py` · ⚠️ local M makes Marx errors + "Heute lädt uns Marx ein" evy day ➡ maybe regen w Claude
- 🦠🧪 install Android Studio (tarball, reuses `~/Android/Sdk`) ➡ open `pr/bible/android/` ➡ read code per notes.md § reading guide

## 📆

## 🔵
- 📦⭐ **Ask Marx: chat w an advisor Marx** (🦠 idea @1102): user describes an everyday problem (boss, rent, overtime, shopping urge) ➡ Marx answers short + punchy, w a matching Kapital quote + ref (tap ➡ open th chapter)
  - 💡 why it fits teaching: ties Kapital 2 th user's own life (transfer, not abstract theory) + a direct positive experience o th app ➡ th reason 2 come back (Hooked: variable reward o th self + investment)
  - 🔍 how: persona prompt + RAG over our scored verses (embed ➡ top-k quotes ➡ LLM must cite 1) · LLM where? on-device (Gemma 3 1B via MediaPipe/LiteRT, offline, ~600 MB 👎) ⬄ own server (ollama on tow/vps ➡ needs INTERNET + hosting) ⬄ API (costs, privacy) · ⚠️ guardrails: no real legal/medical/money advice, crisis ➡ point 2 real help · ironic tone, not cringe
- 📦 🔊 pre-rendered holy voices (Chatterbox ML + church reverb) · DE+EN 1st
- 📦 full verse txt IT/ES/PT/ZH/AR (1.3 M chars/lang, ~1-2 h/lang local) — or PD translations? (zh: 郭大力/王亚南 1938 → PD i CN fr 2027)
- 🔍 QA zh/ar translations w a native speaker 👤
- 📦 "Was nervt?" ➡ free-txt + embeddings match (MVP = 7 chips ➡ tags)
- 📦 Mehrwert-Rechner (salary ⬄ revenue/employee)
- 📦 Fußnoten-Burns: rank footnotes 4 roasts (Dunning "300 %" = fn 24:250) ➡ own plan
- 📦 Lesekreis-Modus (host pushes passage 2 friends) — needs server
- 📦 Feiertage 2: own notif txt per feast · ctx plans (Black Friday ➡ Fetisch-Detox · Halloween ➡ Vampir-Plan) · widget in feast colours
- 📦 export "Mein Kapital" (md/json) — Mine screen promises it
- 🏗️ AGP 9 + compileSdk 37 upgrade (AndroidX pinned 2 late-2025) = L step
- 🏗️ release signing key (friends' updates need same key once off debug)
- 🔍 tags over-applied by LLM ("kapitalist" 83 %) ➡ relative re-tag like rank.py?
- 🦠 P3 habit test 2w · identify ➡ codify ➡ modify
- 🤖 aft QA: rm page fotos (boox proc step 8)

## ✔️
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

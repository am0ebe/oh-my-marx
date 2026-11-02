## 🎯 sprint goal
- Vers des Tages on 🦠's fon via own Android app

## ⏳

## ‼️
- 🦠🧪 fon: dev options + USB debugging ➡ `adb install -r android/app/build/outputs/apk/debug/app-debug.apk` (cmd i notes.md § build)
- 🦠👀 QA v0.3.0 on fon: hero colours (🪝 🌅 chip cycles) · 🎟️ Stempelkarte + 🕊️ · "✔️ Gelesen"/"Amen" counts day · tap Losung ➡ card popup · Teilen on any verse ➡ card preview · 🎉 chip cycles 12 Feiertage · 📱 add widget · 🪝 notes tap ➡ Hooked page · About
- 🦠🤔 pick: holy voice — own voice / friend's Merz *impression* as ref? (notes.md § voice · 🚫 real Merz clone) ➡ 🤖 pre-render pipeline w Chatterbox
- 🦠🤔 friends route: Obtainium + GitHub Releases? (notes.md § friends wo adb) ➡ needs ‼️ release key + license + public repo · store = later
- 🦠🤔 QA 10 sample Andachten (tone: wink, not cringe) ➡ tweak prompt i `bin/andacht.py` · ⚠️ local M makes Marx errors + "Heute lädt uns Marx ein" evy day ➡ maybe regen w Claude
- 🦠🧪 install Android Studio (tarball, reuses `~/Android/Sdk`) ➡ open `pr/bible/android/` ➡ read code per notes.md § reading guide

## 📆

## 🔵
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
- ✔️ v0.3.0 @1101: 🎉 12 Feiertage (card motif + verse + greeting) · 🌅 time-o-day hero · 🔥 streak + 🕊️ Sabbat-Joker · 🎟️ Stempelkarte (2 gift) ➡ 7 ranks · 1 verse = reading day · 📱 widget · 🪝 Hooked-Modus + lesson page (4 phases + 6 extras) · card popup on tap + on evy Teilen · About: proudly vibecoded · notes: friends-wo-adb table + store FAQ
- ✔️ v0.2.0 @1031: name bibleApp · nav 🐛 (restoreState ➡ ⚙️) · Marx icon = home · lang dropdown + 7 langs (UI 101/101) · wheel time picker, greyed when Losung off · 🧪 Testmodus (= Fetisch + reroll/notif/popups/reset) · TTS 🐛 `<queries>` + personas · 🌅 daily Losung popup w share · verse tap ➡ labelled icon bar (palette replaces 4 ○) · copy · "Morgen: …" teaser (load nxt trigger) · About: version + vibecoding
- ✔️ plan + research @1028
- ✔️ P0 KB intake: OCR (gemma3 + glm-ocr 4 TOC) · hooked.md § 7 · kapital_toc.md · notes.md · tags.py ☭ · pr/CLAUDE.md row @1028
- ✔️ P1 fetch · split (1949 Absätze, 1103 Fußnoten, MEW pages) · align DE⬄EN (10/10 spot-check) @1028
- ✔️ P1 score (1873) · rank ⭐288 🔥82 · verbatim quotes · 9 plans / 404 days · 92 Andachten DE+EN @1028
- ✔️ P2 Android MVP: APK 15 MB builds · 4/4 JVM tests · Marx icon w halo · not yet run on a device @1028
- ✔️ toolchain user-space: JDK 21 · Gradle 8.14.3 · Android SDK 36 @1028
- ✔️ `.stignore`: + `**/.gradle/` `**/.kotlin/` @1028

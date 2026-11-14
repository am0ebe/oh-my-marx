# dfs — **Oh My Marx** (was bibleApp · code pkg `marxversion`) · Kapital als Bible-App

_Marx' Kapital Bd.1 i bite-sized daily verses, w bible-app mechanics + Augenzwinkern · 1st Android app = L project_
_raw idea: [plan.md](plan.md) · full plan @1028: `~/.claude/plans/read-plan-md-for-floofy-eich.md` · todo: [todo_omm.md](todo_omm.md) · dir/repo renamed bible ➡ `pr/omm` · `github.com/am0ebe/oh-my-marx` @1104 (app id `de.strix.marxversion` unchanged)_

_🧾 arch + decision log (y, alt, ck) ➡ [arch.md](arch.md) · "decided @1028" below = early snapshot_

## 🎯 goals
1. strix actually gets thru Kapital — habit, not willpower
2. apply **Hooked** L hands-on ➡ [hooked.md § 7 Bible App](../../re/boox/hooked/hooked.md#7-case-studies)
3. L Android: Kotlin + Jetpack Compose
4. fun · Augenzwinkern evywhere · app is honest about its own hooks (Fetisch-Modus)

## decided @1028
| wh | decision |
|----|----------|
| usrs | 🦠 + few friends (sideload APK, no store) |
| platform | Android native · Kotlin + Compose · offline-first, no server |
| lang | DE + EN · toggle btn ➡ @1031 7 langs (+IT ES PT 中文 عربي) · top-bar dropdown |
| name | **bibleApp** @1031 (launcher + top bar) · ⚠️ ≠ store name — YouVersion's app *is* "Bible App" |
| who codes | 🤖 builds · 🦠 reads + tweaks |
| book | strix's copy = **MEW 23** (Dietz) ➡ page refs = MEW · [kapital_toc.md](kapital_toc.md) |
| txt src | DE: MEW-23-based PD txt · EN: Moore/Aveling 1887 (PD) |
| quotes | curated fr Marx (juicy first) + Marx portrait as icon (PD, w Heiligenschein) |

## 🧩 concept — Hooked ⬄ feature ⬄ wink
| phase | feature | wink |
|--|--|--|
| ext trigger | daily "Marx-Losung" local notif @ usr ti · ctx days (Mo 8:55, 1. Mai, 5.5., Black Friday, Halloween) | Herrnhuter-Losungen parody · "Klassenkampfjahr" statt Kirchenjahr |
| int trigger | "Was nervt?" box (Chef · Miete · Überstunden · Konsum) ➡ matched Vers | Forer effect = "Marxoskop" |
| action | 1 tap ➡ Vers + 60-sec Andacht · TTS audio | ≤2 min = "notwendige Arbeitszeit" |
| var reward | tribe: Spruchkarte share · hunt: Zufallsvers, Fußnoten-Burns · self: Fortschritt, Mehrwert-Rechner | kitsch-sunset Kalenderspruch |
| investment | highlight · notiz · bm · eigene Spruchkarten (IKEA) ➡ lädt nxt trigger | "Mein Kapital" = deine Akkumulation |
| endowed progress | start @ 2/25: Vorwort ✔️ + Kap 8 "gelebt" ✔️ | "du arbeitest ja schon" |
| ethics (ch6) | **Hooked-Modus**: evy nudge shows its Hook label + Marx' critique + lesson page | app entlarvt sich selbst · 🦠 = facilitator |
| habit (@1101) | 🔥 streak w 🕊️ Sabbat-Joker (1 rest day/wk) · 🎟️ Stempelkarte 10 fields, 2 gift (car-wash) ➡ ranks · 1 verse "Amen" = reading day · 🌅 time-o-day hero (Insight Timer) · 📱 widget · 🎉 12 Feiertage | ranks = Kapital's history: ursprüngl. Akkumulation ➡ … ➡ Expropriation |

**🪞 judgment: streaks (@1101)**
- 👍 work: loss aversion + visible chain (Duolingo, Insight Timer, YouVersion "Day Complete") · 👍 tiny unit (1 verse) = Fogg ability ⇧
- 👎 streak anxiety ➡ duty not desire ➡ 1 miss = rage-quit ("what-the-hell effect") ➡ ⤵️ Sabbat-Joker + punch card counts *total* days (nvr resets)
- 👎 counts opening, not understanding ➡ ok 4 a habit toy · Hooked ch6: we r "entertainer w Bildungsauftrag", not facilitator

- cite like th bible: **Kapital 8,12** (Kap,Absatz)
- fr Hooked ch7: "Tag geschafft! ☭" + Kette · Mitternachts-Reveal o nxt Vers · day-1 miss ➡ "versuch doch *Marx für Erschöpfte*" · Xmas-notif 🧪 ➡ 1. Mai-notif
- plans: "Kapital i 30 Tagen" · "Montagmorgen-Marx" · "Vampir-Plan 🧛" · "Bibelstellen im Kapital" · "Fetisch-Detox (Black Friday)" · "Wertform – für Masochisten" (last)

## 🔍 research
**YouVersion**
- ~1 bn installs (Nov 2025) · ~14 M daily engaged (end 2024) · "Plans With Friends" = main driver · Mar 2026: YouVersion Platform (core tech given away free)
- Hooked ch7 details ➡ [hooked.md](../../re/boox/hooked/hooked.md#7-case-studies)
- 🔗 [wikipedia](https://en.wikipedia.org/wiki/YouVersion) · [churchleaders 1 bn](https://churchleaders.com/news/516606-bobby-gruenewald-gods-word-life-bible-app-billion-downloads.html/3) · [download record 2025](https://churchleaders.com/news/503794-youversion-bible-app-breaks-download-record.html)

**Kapital Bd.1**
- 🔑 front-load has pedigree: Althusser (1969, preface 2 French ed) — skip Abschnitt 1 (Wertform) on 1st read ➡ start w Kap 8 Arbeitstag / 24 ursprüngl. Akkumulation / 1.4 Fetisch
- Marx is alr biblical 😄: "Akkumuliert, akkumuliert! Das ist Moses u. die Propheten!" (Kap 22) · Offenbarung i Kap 2 · "Im Anfang war die Tat" (Kap 2, Faust) · "Hic Rhodus, hic salta!" (Kap 4) · Kapital als Vampir (Kap 8) · "von Kopf bis Zeh … blut- u. schmutztriefend" (Kap 24) · Dunning-Fußnote "300 % … kein Verbrechen" (Kap 24)
- MEW 23 p915 "Verzeichnis literarischer, **biblischer** u. mythologischer Namen" ➡ src 4 Bibelstellen-plan
- ⚠️ EN (Moore/Aveling) numbering ≠ DE: DE Kap 24 ➡ EN Part VIII ch 26–33 ➡ align by section + paragraph
- DTA has 1867 1. Aufl (CC BY-NC, dif structure) ❌ 4 us
- 🔗 [DTA 1867](https://www.deutschestextarchiv.de/book/show/marx_kapital01_1867) · [OLL 1867 facsimile](https://oll.libertyfund.org/pages/marx-k1-1867)

## 🛠️ phases
- **P0** KB intake — fotos ➡ `img/` (rotated) ➡ `ocr/` · hooked.md § 7 · kapital_toc.md · this file
- **P1** content pipeline (`bin/`, local ollama) — fetch ➡ split into verses ➡ DE⬄EN align ➡ LLM score (quotability · difficulty · tags) ➡ plans ➡ Andachten DE+EN ➡ export JSON 2 app assets
- **P2** Android MVP (`app/`) — screens Heute · Pläne · Lesen · Mein Kapital · Settings · Room + DataStore · WorkManager notif · share card · TTS · Marx icon
- **P3** habit test on self + friends (Hooked ch8: identify ➡ codify ➡ modify)

## dirs
- `img/marx_portrait/` PD src 4 icon · Hooked page fotos + OCR ➡ moved 2 KB `re/boox/hooked/pages/` @1104 (© book, not 4 a public repo · `img_orig/` = local raw, git-ignored)
- `bin/` pipeline · `data/` verses/plans/scores · `android/` Android Studio project (module `app`) · [kapital_toc.md](kapital_toc.md)

## 🔄 pipeline (P1) — re-run order
```
bin/fetch.py      # raw html ➡ data/raw/{de,en}  (idempotent, ~10 min fr wayback)
bin/split.py      # DE ➡ data/de.jsonl + notes_de.jsonl  (Kap,Absatz · MEW page · fn)
bin/align.py      # DE⬄EN DP alignment ➡ data/verses.jsonl + notes.jsonl  (--show 8 = eyeball) · drops EN chapter TOC · k-1 beads ➡ EN split by sentences · en_head (D-024)
bin/score.py      # LLM: quote_de/en · juice · difficulty · tags ➡ data/scores.jsonl  (resumable, ~1s/Vers, ~35 min)
bin/rank.py       # LLM relative pass per section: ⭐ best ~1/6 · 🔥 real roasts ➡ data/rank.jsonl (~2 min)
bin/build.py      # verbatim quotes + plans + EN sec titles (ste) + export ➡ android/app/src/main/assets/content/*.json · curated plans = data/plans_extra.json (ids + xd/xe bridge txt, D-027)
bin/andacht.py    # LLM Andachten per plan day ➡ data/andachten.jsonl  ➡ then build.py again
bin/translate.py  # AFT build.py: quotes · Andachten · plans · chapters · UI strings ➡ it/es/pt/zh/ar ➡ assets/content/i18n_xx.json (~8 min/lang)
```
- i18n: DE+EN inline `tr("de", "en")` · oth langs = lookup by EN string i `i18n_xx.json` · non-composable strings = `T("de", "en")` pairs (translate.py greps both)
- ⚠️ no `$var` inside tr()/T() ➡ use `"%d".f(x)` (else lookup misses) · translate.py warns
- full verse txt + footnotes = DE/EN only (1.3 M chars/lang) ➡ IT/ES/… show EN w a note · AR = RTL (whole layout mirrors via LocalLayoutDirection)
- cache `data/i18n/<lang>.jsonl` · re-run = only changed strings · `--export-only` rebuilds th json fr cache
- src DE = MEW 23 via archived mlwerke.de (wayback) — has `<245>` page markers ✔️ · EN = marxists.org
- ⚠️ mlwerke html is sloppy: missing `</A>` i headings · page anchors `M341` ⬄ footnote anchors `M8` · 1 typo'd fn link (`#Z203`) — split.py handles all 3
- align: 10/10 random pairs ✔️ · 1903/1949 Absätze w EN @1113 (was 1719: merged EN paras now split, D-024) · ⚠️ 13,122-125 EN shifted ~1 para
- scores joined by DE txt (not id) cuz ids shifted once aft th heading fix ➡ `data/verses_scored_v0.jsonl` = th id map
- 👾 absolute LLM ratings inflated (73 % juice=5 · 92 % "polemic") ➡ rank.py = relative picks ⇒ ⭐288 · 🔥82
- 👾 26 % LLM quotes not verbatim (elided "…", paraphrase) ➡ build.py fuzzy-matches ➡ uses th **source** sentence(s) ⇒ 1514 DE / 1232 EN quotes, all verbatim
- local M = qwen3:30b-a3b-instruct (ollama @tow) · Andachten: tone ok, ⚠️ occasional Marx errors (eg "Wert entsteht im Austausch") ➡ QA

## 📱 build + install (P2)
```
cd android
export JAVA_HOME=~/.local/opt/jdk21 ANDROID_HOME=~/Android/Sdk
./gradlew testDebugUnitTest      # JVM tests: JSON ⬄ Kotlin classes, streak, endowed progress
./gradlew assembleDebug          # ➡ app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk   # fon via USB debugging
```
- toolchain i user space (no sudo): JDK 21 `~/.local/opt/jdk21` · Gradle 8.14.3 `~/.local/opt/gradle-8.14.3` · SDK `~/Android/Sdk` (= Android Studio dflt ➡ Studio reuses it)
- versions: AGP 8.13.2 · Kotlin 2.4.21 · KSP 2.3.12 · compileSdk 36 · AndroidX pinned 2 late-2025 (newer need compileSdk 37 ➡ AGP 9 = 🔵 upgrade as L step)
- no KVM on tow ➡ no emulator · test on fon
- friends: send th APK (ST / Signal) ➡ "install unknown apps" · debug-signed

## 📖 reading guide — wh 2 read 1st (Claude builds, 🦠 reads + tweaks)
1. `MainActivity.kt` — 1 Activity · Scaffold (top bar + bottom tabs) · NavHost = which screen
2. `ui/TodayScreen.kt` — th Hook loop i 1 file: streak ➡ Vers des Tages ➡ plan day ➡ "Tag geschafft" ➡ Marxoskop
3. `ui/AppVm.kt` — ViewModel: StateFlows in, funs out · screens nvr touch db directly
4. `data/Db.kt` (Room) · `data/Prefs.kt` (DataStore) · `data/Content.kt` (assets JSON)
5. `logic/Daily.kt` — pure Kotlin, no Android: verse-o-day, streak, endowed progress ➡ tested i `ContentTest.kt`
6. `notify/Reminder.kt` — WorkManager daily notif · `ui/Share.kt` — Spruchkarte png + TTS
7. `ui/Hooks.kt` — 🪝 Hooked-Modus notes (th app confessing its tricks · was Testmodus/Fetisch-Modus) · `ui/HookedScreen.kt` — Hooked lesson page (4 phases × book ⬄ YouVersion ⬄ this app)
   - `logic/Feast.kt` — 🎉 Feiertage (colours + emoji + verse) + 🌅 DayTime gradients · `notify/Widget.kt` — RemoteViews home-screen widget
8. `ui/WheelPicker.kt` — scroll-wheel time picker (LazyColumn + snap fling + graphicsLayer fade) · `data/I18n.kt` — 7 langs
- 🧪 ez tweaks 2 start: colours i `Theme.kt` · Hook txts · `NERVT` chips i `Daily.kt` · plan defs i `bin/build.py`

## android L
_per concept 2-3 lines_
- **Compose** = UI as functions (`@Composable`) · state changes ➡ only affected parts re-run ("recomposition") · no XML layouts
- **state**: `remember { mutableStateOf(x) }` = local UI state · survives recomposition, not rotation ➡ long-lived state goes i th ViewModel
- **ViewModel + StateFlow**: survives rotation · `collectAsStateWithLifecycle()` turns a Flow into Compose state + stops collecting when app is i background
- **composable-only calls**: `tr()` / `verseQuote()` read a CompositionLocal ➡ can't be called i `onClick` lambdas ➡ read into a `val` 1st (hit this as 1st compile err)
- **CompositionLocal** (`LocalLang`): implicit param 4 th whole tree ➡ 1 lang toggle, instant, no Android resource reload
- **Room**: `@Entity` = table · `@Dao` = queries checked at compile time (KSP generates th code) · returning `Flow` ➡ UI auto-updates on change
- **DataStore**: async key-value prefs · Flow-based (replaces SharedPreferences)
- **WorkManager**: background work that survives reboots · periodic min 15 min · not minute-exact (battery batching)
- **runtime permissions**: Android 13+ needs POST_NOTIFICATIONS asked at runtime ➡ `rememberLauncherForActivityResult`
- **FileProvider**: share a private file (png) w oth apps via `content://` uri
- **Gradle version catalog** (`gradle/libs.versions.toml`): all versions i 1 place · AGP ⬄ compileSdk ⬄ AndroidX versions must fit together (hit: newest libs need SDK 37)
- **adaptive icon**: foreground + background layers, launcher masks 2 circle/squircle ➡ keep content i inner 66dp

## 🔊 voice (@1031)
- 🐛 TTS was silent: Android 11+ package visibility ➡ needs `<queries>` TTS_SERVICE i manifest ✔️ fixed
- v0.2: Android TTS + persona = pitch/speed preset
- ✔️ v0.3.1 (@1102) **holy = on-device DSP** (`logic/Voice.kt` · `Speaker` i `ui/Share.kt`) · no ML o our own, no download
  - th voice = th fon's system TTS (usually Google Speech Services, neural, offline) · `synthesizeToFile` 1× per layer w its own pitch ➡ 16-bit PCM wav
  - mix layers (gain + few ms delay = choir smear, not unison) ➡ **Freeverb** (Jezar 2000: 8 damped feedback combs ∥ ➡ 4 allpasses →, tunings scaled 44.1k ➡ TTS rate) ➡ wet set by RMS ratio ➡ peak-normalize ➡ wav ➡ `MediaPlayer` · cache 30 files
  - "voice o God" = same line at octave ⇩ (authority) + ⇧ (choir) + long hall · film/radio trick
  - personas: ☭☁️ Gottvater Marx (dflt · 0.6 + 0.35, Kirche) · ☁️ Gott (0.4 + 0.8 + 1.3, Kathedrale 3.5 s) · 📚 Marx im Lesesaal (0.62, small room) · ⛪ Pfarrer (+ Kirche) · 🙂 · 🤖 plain
  - ⚠️ pitch shift inside th TTS engine ⇩ 0.35 can sound grainy · zh/ar need th voice installed on th fon
  - 🪞 @1103 strix on th fon: **sounds horrible + takes long** ➡ v0.3.2 prerenders th Losung (popup/Today open ➡ tap = instant) · real fix = plan ⬇️

### 🗺️ plan: realistic voices (@1103)
- 🐛 y bad now: system TTS + pitch shifted far down = robot · y slow: 2-3 TTS passes i a row + DSP, all on th fon
- **A ⭐ pre-render on tow** (content is fixed!) — best quality, 0 compute on th fon, instant
  - M: **Chatterbox Multilingual** (MIT, 23 langs incl de/en/it/es/pt/zh/ar, zero-shot clone fr ~10 s ref, "exaggeration" knob = preacher pathos) · alt F5-TTS (code MIT, ⚠️ weights CC-BY-NC) · ❌ XTTS-v2 (non-commercial CPML)
  - ➡ our Freeverb + octave layer on top (same `logic/Voice.kt` chain, run i a Kotlin/py script on tow) ➡ opus 24 kbit
  - size: ⭐288 quotes × ~25 KB ≈ 7 MB/lang·voice · + 92 Andachten × ~180 KB ≈ 17 MB ➡ DE+EN 1 voice ≈ 50 MB ➡ ship as **download pack** fr scagent.de, not i th APK
  - covers: Losung + Andachten · full reader txt ➡ falls back 2 B/system TTS
- **B on-device neural TTS** — any txt, offline: **sherpa-onnx** runtime (Apache) + **Piper** voices (VITS) ~20 MB (low) – 60 MB (medium) per voice·lang, faster than real time on a fon · Kokoro-82M int8 ~90 MB (en/es/it/pt/zh, ❌ de/ar)
- **C = "train big, ship small"** (distillation): Chatterbox (A) makes ~1-2 h o th holy voice (any Kapital txt) ➡ fine-tune a Piper checkpoint on tow (3090: hours, not days) ➡ ONNX ~20-60 MB ➡ B plays it · = own Marx/God voice 4 evy txt, small, offline
- **ref voice 4 cloning** (~10-30 s clean speech): 🦠 own / a consenting friend ✔️ · LibriVox = public-domain *recordings* (⚠️ readers r real ppl ➡ ask) · 🚫 real public figures
- order: A 4 DE+EN Losung (1 evening) ➡ judge ➡ C if th reader voice matters
- 💡 holy voice = **pre-render on tow** (content is fixed!) ➡ ship audio i assets, no on-device M
  - M: **Chatterbox Multilingual** (Resemble AI, MIT, zero-shot clone fr ~10 s ref, 23 langs incl de/it/es/pt/zh/ar, emotion knob) · 3090 Ti ok
  - "holy" = mostly post-proc: church reverb (impulse response, `ffmpeg afir`) + pitch ⇩ + soft organ drone (Musopen CC0)
  - ref voice: 🦠 own / consenting friend ✔️ · 🚫 cloning real pastors/actors (consent + Play impersonation policy)
  - size: ⭐288 quotes × ~25 KB opus + 92 Andachten × ~180 KB ≈ 24 MB per lang×persona ➡ DE+EN only 1st
- 🚫 **Merz voice** (@1101 asked) ➡ ❌ no clone o a real politician (Persönlichkeitsrecht: own voice protected · deepfake o a head o govt · Play bans impersonation) · ⤵️ legit: 🦠/friend *doing* a Merz impression as ref = Kabarett ✔️ · or generic "🎩 Kanzler" pitch preset

## 🏪 Play Store (@1031 research · @1101 update)
- $25 **once** ➡ unlimited apps + updates forever (no per-update fee) · updates = upload new AAB w higher `versionCode`, same signing key ➡ review ~h–days
- personal acct ➡ **12 testers × 14 days closed test** per app b4 production (accounts aft Nov 2023)
  - testers = ur own friends (Google Group o email list), opted in via link, must *keep* it installed 14 days · Google doesn't test 4 u
  - Google's part: automated policy/malware scan + human policy review · pre-launch report = robo-tests on Firebase Test Lab devices (crashes, a11y, screenshots)
- AAB + own upload key (Play App Signing) · privacy policy URL · data-safety form (we collect nothing ✔️) · content rating · 512 icon + 1024×500 graphic + screenshots
- 🚧 name: "Bible App" = YouVersion ➡ impersonation/trademark risk ➡ store name must differ ("Das Kapital — Tägliche Losung"?) · religious-parody may draw reports ➡ keep it clearly satire, no Bible branding
- 🚧 MEW 23 editorial footnotes (Dietz/IML) maybe © ➡ check b4 public · Marx' own txt + Moore/Aveling = PD
- 🚧 voice clones / real persons ➡ Play impersonation policy (see § voice)
- 🚧 **developer verification**: from Sep 2026 enforced i BR/ID/SG/TH, global 2027 ➡ even sideloaded APKs need a verified dev · free **limited distribution acct** (hobbyist, no ID, no $25, ≤20 authorized devices) = th friends route · else usrs need th "advanced flow" (dev options + 24 h wait)

## 📦 friends wo adb (@1101)
| route | friends do | updates | effort 4 🦠 |
|--|--|--|--|
| ⚡ APK link (VPS / GitHub Release) | tap link ➡ "allow this source" ➡ install | manual, re-download | 🟢 |
| ⭐ **Obtainium** + GitHub Releases | install Obtainium once ➡ add repo URL | auto-notified | 🟢 tag + attach APK |
| own **F-Droid repo** (`fdroidserver` on VPS / GH Pages) | F-Droid/Droid-ify ➡ add repo (QR/link) | auto | 🟡 `fdroid init` ➡ APKs i `repo/` ➡ `fdroid update -c` ➡ `fdroid deploy` |
| IzzyOnDroid repo | in F-Droid clients by dflt | auto (pulls ur GH releases) | 🟡 FOSS license + public src + request on Codeberg |
| official F-Droid | search "bibleApp" i F-Droid | auto, *they* build fr src | 🛑 MR 2 gitlab `fdroiddata` · reproducible build · FOSS deps only (✔️ we have none proprietary) · weeks–months review |
- prereq 4 all: ‼️ **release signing key** (debug key = per-machine, can't be used 4 real releases · lose th key ➡ friends must uninstall 2 update) ➡ keystore outside th repo + backup
- prereq 4 F-Droid/Izzy: pick a license (code GPL-3? content PD) + public repo
- 💡 rec: Obtainium + GitHub Releases now (1 cmd per release) · own F-Droid repo later if >5 friends
- @1105: /dev web channel rm'd · @1106 back as bare Obtainium source (strix: no dev webpage, but th Obtainium URL) · 1 page = stable · favicon = app icon (`bin/favicon.png`)
- ✔️ @1103 live: **2 channels** on th VPS · `bin/ship.sh` ➡ scagent.de/oh-my-marx/ (stable, friends) · `bin/deploy.sh` ➡ scagent.de/oh-my-marx/dev/ (/bible rm'd @1103) (🦠's fon, evy build = new version `x.y.z-dev.MMDD.HHMM`) · versionCode = minutes since 2026 ➡ dev ⬄ stable nvr "downgrade"

## 📱🍏 sharing apps w friends on ANY fon (@1103 · strix: "keepandroidopen sounds like a dead end")
- 🚧 Android 2027: certified fons block apps fr unregistered devs (incl. sideload/F-Droid/Obtainium)
  - ⤵️ free "limited distribution" dev acct: no ID, no $25, ≤20 devices = a friends circle ✔️ · o $25 + ID = unlimited
  - ⤵️ friend's side: 1-time "advanced flow" (settings unlock + 24 h wait) ➡ then installs anything
  - ✔️ GrapheneOS / LineageOS etc. = not Google-certified ➡ prob unaffected
- 🍏 iPhone: **no APK, no sideload** (EU: alt marketplaces, still Apple-notarized + $99/yr acct) · TestFlight = $99/yr, 90-day builds
- ⭐ **th universal escape hatch = a web app (PWA)**: a URL ➡ "Add 2 Home Screen" ➡ icon, offline, push (iOS ≥16.4) · no store, no review, no verification, same code 4 Android + iPhone + desktop
  - 4 Oh My Marx: th core (cards · daily Losung · book · notes) = web-friendly · ⚠️ widget / QS tile / shake / TTS voices = native-only o weaker on web
  - routes: (a) **Compose Multiplatform** (Kotlin ➡ Android + iOS + web/wasm, reuse ~70 % o our code · web target = beta) · (b) small separate PWA (Svelte/vanilla) reading th same assets JSON · (c) Kotlin/Wasm later
  - 💡 rule 4 future fun prototypes: **web 1st** (share = a link) · native only when th fon's sensors/widgets r th point

## 🧭 ownership: omm ⬄ ops (@1103 · strix: "responsibility gets diffused")
- **omm** (this repo, was bible) = th product: app · server **code** + config templates · [server/SERVICE.md](server/SERVICE.md) = th contract ("what it needs")
- **ops** (`pr/ops`) = running it on our VPS: dirs, Caddy, TLS, pins/updates, backups, monitoring ➡ `pr/ops/omm.md`
- rules: bible doesn't change prod (asks ops) · ops doesn't change code (asks bible) · only shared seam = SERVICE.md · exception: bible's ship/deploy drop APKs into `/srv/www/oh-my-marx`
- rip-out: drop omm ➡ ops' doc lists what 2 delete · drop th VPS ➡ SERVICE.md is enough 2 run it elsewhere

## 👥 community / Lesekreis (@1103 · strix: fitness + meditation apps all have it · ppl share experience + knowledge · also 4 hardcore Marxists)
### 🧑 user experience
- **Mitlesen** (opt-in, ⚙️): pick a name (+ emoji avatar) ➡ no email, no pw (account made in th background)
- 📖 **evy book page / section**: `👥 34 lasen diese Seite · 💬 12` ➡ tap ➡ th thread 4 that page: comments, ☭ reactions, replies
  - 🔖 ✍️ share a margin note ➡ it appears i th thread, quoted w th verse (opt-in per note)
- 🃏 **evy card**: `💬` ➡ what ppl say about today's Losung · "Heute: 212 Amen ☭" (crowd = social proof)
- 👥 **Kreise**: make one ➡ invite link / QR ➡ friends join ➡ same plan, circle Stempelkarte (fills when *all* read = solidarity), circle chat
- 🤝 **friends**: add via QR / name ➡ DM · see their Amen today (opt-in)
- 🧙 **Salon** (forum 4 deep dives): topic rooms per Abschnitt ("Wertform", "Akkumulation") ➡ long posts, threads · hardcore readers can use a desktop client (Element) on th same rooms
- 🛡️ report · mute · block · circle admins kick · rate limit
### 🏗️ technical: Matrix
- **homeserver on th VPS**: Synapse (mature, Python, ~300-500 MB RAM) o **continuwuity/conduwuit** (Rust, ~50-100 MB, 1 binary + RocksDB) ⭐ 4 a small box · docker compose ➡ Caddy `matrix.scagent.de` + `/.well-known/matrix/*` on scagent.de ➡ users = `@name:scagent.de`
- **mapping**: book section = public room `#kap-08:scagent.de` · page = **thread** i that room (thread root = "MEW S. 247") · Kreis = private room (invite) · friend = DM room · reaction ☭ = m.reaction · shared note = message w a `de.ohmymarx.verse` field
- **app side**: Matrix Client-Server REST (register · login · sync · send · threads) via OkHttp/ktor — tiny subset · o **matrix-rust-sdk** Kotlin bindings (what Element X uses: E2E, sync, offline cache) = heavier but done
- **"34 lasen diese Seite"**: NOT via Matrix (would leak who reads what) ➡ tiny counter API on th VPS (FastAPI + SQLite): `POST /read/{page}` w a daily random token ➡ aggregate counts only
- **accounts**: app auto-registers w a registration token (no open signup = no spam) · pw lives i Android Keystore · "delete my account" = Matrix deactivate
- 👍 open standard, own server i DE, federation (users fr matrix.org can join), E2E 4 Kreise/DMs, existing clients 4 power users · 👎 Matrix = cpx (sync, state, moderation tools) · a homeserver = 1 more service 2 run + bup
- ⬄ alt: own FastAPI + SQLite (comments + counts only): simpler, full ctrl, no federation/E2E/desktop client · ⬄ Discourse (forum, heavy, ~2 GB RAM)
- order: ① counter API + "👥 N lasen" (1 evening, read-only social proof) ➡ ② homeserver + section rooms (comments) ➡ ③ Kreise + friends ➡ ④ Salon
- ✔️ @1103 **① live**: `server/omm_api.py` ➡ scagent.de/omm/api · **② live**: Synapse 1.164.2 `omm-matrix` (client-only, token signup, 31 chapter rooms) ➡ scagent.de/_matrix · app: 💬 under evy book page (bottom sheet, name on 1st comment, hold own = delete) · details `server/README.md` + `server/matrix/README.md`
  - chose Synapse over continuwuity: known config + admin API (registration tokens, rate-limit override) · 105 MB RAM idle
  - 💬 = public 2 all Mitlesenden · no DMs/Kreise yet (③)
- ⚠️ needs INTERNET permission + privacy page (DSGVO: what's stored, where, delete) · moderation = a real job once strangers join

## 🌍 full txt translation — `bin/translate_full.py` (@1105)
- in: DE paragraphs (`data/verses.jsonl`) + Marx's footnotes (`data/notes.jsonl`) · EN Moore/Aveling = reference only
- out: `data/i18n_full/<lang>.jsonl`, 1 line/item `{kind v|fn, k, src=sha1(de)[:10], t, warn?}` · ✔️ @1106 i th app (0.8.0): `--export` ➡ `assets/content/full_<lang>.json` (stale src ➡ left out ➡ EN) · footnote refs `[n]` repaired 2 match DE (model invented `[52]` ~4 %) · app loads only th cur lang (`ContentRepo.full`) · APK 17 ➡ 21 MB
- how: batches (≤8 items · ≤2.5k chars) ➡ ollama qwen3:30b-a3b, temp 0.2, JSON schema = 1 string per id ➡ bad JSON ➡ retry ➡ missing ids redone singly
  - glossary pins th Marxist terms per lang (Mehrwert = plusvalore / plusvalía / mais-valor / 剩余价值 …) · resumable by src hash (DE changed ➡ redone)
  - checks per item: length ratio vs DE (per-lang window) · same `[n]` footnote markers
- ✔️ run @1104-05: all 5 langs 1949/1949 paras + 1103/1103 fn · ~2 h/lang · ⚠️ it 125 · es 147 · pt 138 · zh 111 · ar 175 (~4 %)
  - most warns = fn markers moved/dropped (cosmetic) · **severe ~1 %** (ratio <0.3 o >2.5, ~130 items all langs) = 2 bugs:
  - 🐛 ratio ⇧ (eg 2,5 = 4×, 3,54 = 6×, same i all langs): DE⬄EN misaligned (EN para = several DE paras: 2004 vs 488 chars) ➡ M translates th EN extra ➡ dup txt i neighbour paras · fix: redo DE-only (no EN ref) wh EN ≫ DE
  - 🐛 ratio ⇩ (eg V6,16 zh 0.01): stops at th 1st quote mark (`"Was äußerst sonderbar erscheint", sagt …`) ➡ fix: redo singly
- tst: `--stats` (coverage + warns) · `--show 5 --langs it` (random DE | EN | IT) · severe list = 🤖 `--bad` (todo)

## 🔑 keystore = th app's identity (@1105)
- Android installs an update only if signed w th SAME key · ours = debug key, symlinked `~/.android/debug.keystore` ➡ `~/dotfiles/secrets/android/omm.keystore` (ST ➡ p15 + lap, git-ignored) · lose it = friends can't update (uninstall = data gone)
- verify: `~/Android/Sdk/build-tools/*/apksigner verify --print-certs <apk> | grep SHA-256` = same as th shipped APK
- password manager = txt: `base64 -w0 <keystore>` ➡ paste as note (~3 KB) · restore `base64 -d > omm.keystore` · debug key pw `android`, alias `androiddebugkey` · or attach th file (KeePassXC does attachments)
- ⚠️ ST deletes propagate ➡ also 1 offline/restic copy · later: real release key + `apksigner rotate` (v3 lineage, Android 9+) · "CN=Android Debug" looks unfinished 2 secu ppl

## 📦 dev ⬄ release (4 strix, new 2 apps)
- 2 apps, 1 key · stable `de.strix.marxversion` "Oh My Marx" (red) ⬄ dev `….dev` "Oh My Marx DEV" (blue) = side by side, own data
- `bin/deploy.sh` ➡ `/dev/` = bare Obtainium source 4 th DEV app (no webpage, @1106) · `-a` = + wireless adb now · `bin/ship.sh` ➡ /oh-my-marx/ + git tag + GitHub release w notes fr Changelog.kt (`bin/notes.py`) · `ship.sh -p` = page only · versionCode = minutes since 2026 ➡ always "newer"
- 🔄 loop @1106: dev ➡ QA ➡ Changelog entry ➡ commit + push ➡ `ship.sh minor|patch` (bumps itself) · dev auto-labelled w th NEXT version · full wflo ➡ [🔗 ref.md § 🌊](ref.md) (README § Versions = EN copy 4 devs)
  - was: hand-bump at ship ➡ dev builds said th OLD version (0.7.0-dev while building 0.8.0) ➡ "deploy didn't bump" confusion · 🦠 @1106: "cant script include a bump w some flag"
- 🆕 banner @1106: only hand-installed APKs (installer ≠ Obtainium/F-Droid/store, `Update.wanted`) · DEV nvr · 🦠: "obtainium tk care o this"
- update check (`data/Update.kt`, hand-installed only ⬆): app start reads `version.json` ➡ `code` > installed ➡ gold banner on Heute ➡ tap = download page · no self-install (REQUEST_INSTALL_PACKAGES = scary perm)

## 📨 feedback loop (@1105 · strix: "make feedback as ez as possible ➡ iterate faster")
- 🎯 **many doors, 1 queue**: evy channel ends i **GitHub Issues** (ticket SSOT) ➡ 🤖 triages via `gh` ➡ fix ➡ release notes close th loop
- 🔑 GitHub: **no account = no issue** (no anon/guest issues) ➡ non-devs need a door wo GitHub · Forgejo: sign-up off ➡ issues there = only us
- ✔️ @1106 phase 1 (links, no new server — QA-first rule): page footer mailto (version prefilled) · GitHub tab ➡ issue forms 🐛/💡 + "no account? mail" · labels `needs-triage` `from-app`
- gh cheat: `gh issue list` · `gh issue view 12` · `gh issue list -l needs-triage --json number,title,body` (4 agents) · `gh issue comment 12 -b "thx"` · `gh issue close 12` · commit "Fixes #12" ➡ auto-close on push
- doors, friction ⇩ first:

| door | who | friction | lands i |
|--|--|--|--|
| ⭐ in-app 💬 Feedback (⚙️ + Über) | evy1 | txt + Send · no acct, no mail app | omm_api `/feedback` ➡ priv inbox |
| page footer 💬 btn | web visitors | same form (fetch POST) | same |
| mail `omm@` | old-school | needs a mail app | Migadu IMAP ➡ `bin/feedback.py` |
| GitHub issue forms | devs | GH acct | Issues direct |

- in-app form: txt (only required field) · 📸 attach screenshot (prev screen, PixelCopy) · contact optional · ☑ "ok 2 post publicly on GitHub" dflt off · auto + visible: version · Android ver · lang · screen · offline ➡ queued, sent nxt start
- 🔒 privacy (secu-aware audience): app feedback NOT auto-public ➡ priv inbox (sqlite on VPS) ➡ `bin/feedback.py pull` ➡ 🤖 drafts issue (rewords, strips names/contact) ➡ 🦠 ok ➡ `gh issue create -l from-app`
  - 👍 nothing leaks · spam nvr hits GH · 👎 1 approve step (batch it, 1×/w o on 🔔)
  - ⬄ alt: server posts straight 2 GH w a fine-grained PAT (issues:write, 1 repo) = 0 manual, but evy word public + a token on th VPS
- server: `server/omm_api.py` + `POST /feedback` (rate limit per IP-hash · ≤4 KB txt · ≤1 MB img) · `GET /feedback?since=` behind a bearer token · ntfy ping 2 🦠 on new (alr run ntfy) · deploy = ops (SERVICE.md)

### GitHub = ticket sys
- **issue forms** `.github/ISSUE_TEMPLATE/`: 🐛 bug (what · expected · version · fon) · 💡 idea (problem · wish) · `config.yml` ➡ "no GitHub? use th app's 💬 Feedback o omm@"
- **labels**: type `bug` `idea` `question` · src `from-app` `from-mail` · `needs-triage` (dflt on new) · `agent-ok` 🤖 (clear + small ➡ agent may do it alone) · `good-first-issue` · `p1`/`p2`
- **milestones** = versions (0.8.0) ➡ `gh issue list -m 0.8.0` = th release plan
- **Discussions** on 4 open-ended ideas/Qs ➡ Issues stay actionable only
- close th loop: commit/PR "Fixes #12" ➡ auto-close + reporter notified · `notes.py` puts "(#12)" i release notes ➡ ppl see their wish shipped = reward 2 report again (Hooked 🔄)

### 🤖 agent wflo (terminal + herdr)
- `/triage` skill: `gh issue list -l needs-triage --json number,title,body,labels` + `bin/feedback.py pull` ➡ per item: dedupe vs open · label · prio · 1-line plan ➡ ask 🦠 only th unclear ➡ `gh issue edit` + short thx reply
- work: `gh issue develop 12 -c` (branch) o worktree ➡ fix ➡ `gh pr create` "Fixes #12" ➡ self-merge (solo) ➡ ship
- herdr: 1 pane runs triage daily ➡ ntfy "3 new · 1 bug p1"

### todo_omm.md ⬄ Issues (scale rule)
- now (solo, a few friends): todo_omm.md = main board · Issues = inbox 4 outside input
- **switch** wh outside items > ~10 open o a 2nd dev joins ➡ Issues + 1 GitHub Project (Inbox · Next · Doing · Done, `gh project` CLI) = SSOT 4 product work
- todo_omm.md then keeps only priv/ops (keystore · ops msgs · fon QA · ideas not ready 4 public) + `🔗 #12` pointers ➡ nvr copy issue txt (2 truths)

### others contributing (vibe coding)
- `CONTRIBUTING.md`: build (README § build) · 1 PR = 1 issue · tsts pass · AI-made code ok ➡ say so i th PR, u still own it
- CI: GitHub Actions on PR = `./gradlew test assembleDebug` ➡ green b4 🦠 reads · no secrets i CI (debug build)
- PR template: what · y (#issue) · how tstd · 📸 4 UI
- `AGENTS.md` i repo ➡ contributors' agents get our rules 2 (lang policy · no `$var` i tr() · VPS = ops)

## 🧪 QA checklist (feature tour · moved fr todo @1105 · run on evy dev b4 ship)
- 0.7.0 👥 Kreise: tab 👥 ➡ gründen ➡ 📤 Einladen ➡ friend taps link ➡ page ➡ "In Oh My Marx öffnen" ➡ Beitreten · Amen on Heute ➡ ✔️ i th Kreis · chat ☭ · 🎟️ only when all read · 🔔 friend's msg ➡ notif ≤~1 h
- 0.7.0 UI: card btns = 5 equal pills (Teilen quiet) · tap Marx/Heute/⚙️ when alr there = no flicker · ⚙️ ➡ Über = 📜 Quellen + 🤖 + 🔓 src · 🆕 update banner (install an older dev ➡ open ➡ banner)
- 0.6.0 💬: ⚙️ 👥 Mitlesen ON ➡ book header "👥 N×" + "💬 Mitreden" ➡ name ➡ comment ☭ ➡ hold own = delete · Home "👥 Heute lesen N mit · M × Amen" · ⚙️ rename
- 0.5.0 🃏: Home deck swipe ⬅ = earlier days · Amen ➡ ☭ swoosh · Marx icon/Heute = back 2 1st card · shake = red flash + buzz + KRISE · ⬇ pull = 🔮 · 📖 drag ⬅ = page turn, MEW page nrs, 🤏 font · 🔖 double-tap = bookmark + ☭ burst, gold frame · ✍️ inline note · 😇 Saint Marx on cards
- 0.4.0 gestures: Losung popup tap ⬅➡ = days · hold = voice pause · ⬆ = Amen · ⬇ = close · 📋 copy · chapter swipe · pinch font · 🚀 long-press icon = 3 shortcuts · ⚙️ QS tile · 🎉 confetti on full Stempelkarte
- 0.3.x: hero colours · 🎟️ Stempelkarte + 🕊️ · Teilen ➡ card preview · 🎉 12 Feiertage · 📱 widget · 🪝 notes ➡ Hooked page · 🔊 personas (1st tap 1-3 s, 2nd instant?) · zh/ar voice installed?

## icon
- Marx (J. J. E. Mayall, London 1875, PD) · crop face ➡ sepia ➡ circle ➡ gold halo · on Parteibuch-rot
- `magick` cmds: crop 700x700+480+290 ➡ 220px ➡ circle mask ➡ ellipse halo @y106 ➡ `res/mipmap-xxxhdpi/ic_marx_fg.png`

## 🗃️ pruned fr todo_omm @1113 (detail + old ✔️)

### ✂️ dropped @1114 (🦠 "drop des")
- ✂️ 🎨 **design pass fr `pr/des/`** (17 screenshots o another app) ➡ themes · widgets · layouts · _y: dir not found + design_system.md replaces it_

### ✂️ BL hygiene @1113 — prune_1113.md applied (🦠 ✔️ via M8) · old lines verbatim
- **dropped** (5):
  - 🌐 web/PWA 4 iPhone friends — later, rebuild (no shake, new voices) · _y: no iPhone demand logged_
  - 🔑 real release key + `apksigner rotate` · in-app download + install dialog (costs REQUEST_INSTALL_PACKAGES) · _y: Play-Store-only need · in-app install = done (D-023)_
  - 💡 lang packs (only if APK > ~30 MB): ship EN+DE, oth `full_xx.json.gz` on VPS ➡ download on lang pick (sha256 fr version.json) · _y: 29.7 MB, no pressure_
  - 🏗️ AGP 9 + compileSdk 37 · _y: bump when forced_
  - 🦠🧪 install Android Studio (tarball, reuses `~/Android/Sdk`) ➡ notes.md § reading guide · _y: gradle CLI builds evything_
- **demoted**: 📦 Ask Marx ⭐ ➡ no ⭐ · 👥 community nxt ➡ split: QR + report/mod stay "nxt", 7 oth ➡ 💡 ideas line
- **moved**: 🎮 gamify: Tamagotchi-style Marx avatar + xp · starts weak/fragile ➡ fit on streak · pick ur avatar · xfer 2 oth apps (cali!) ➡ `pr/cali/notes.md` § 🔵 · 🔊⭐ realistic persona voices (plan § voice 🗺️: A Chatterbox pre-render on tow ➡ C distill 2 Piper ~20-60 MB · ref voice = 🦠/consenting friend) ➡ pr/audio (`voice/` + todo_audio alr hv it), omm keeps a 1-line pointer
- **promoted**: 🦠 P3 habit test 2w ➡ ‼️ ("do now o drop" ➡ softer)
- **new**: 💡 small Marx animation (🔵) · ✂️ rule: flag drop candidates i todo, nvr rm wo 🦠 ok

### ⏳ resume @1113 eos #3 — detail
- ▶ **RESUME @1113 eos #3** — big round pt 1 on main `7ff0aa3` (74 tsts + build green, ❌ not deployed) ➡ qa_1112 § big round pt 1 · D-024..027 · DEV on fon = still A (r4+r5+r5b)
  - ✔️ M8 follow-ups @1113: FÜFNZEHNTES + id migration (D-028) · 13,120-125 + ch17 intro align · UI + plan fan-in 11 langs · ❌ not deployed
  - then 🦠 fon tst updater (D-023), 5 steps i last sesh's reply: 1 Obtainium ➡ install A (⚙️ What's new shows `installer: dev.imranr.obtainium`) · 2 "B" ➡ 🤖 `bin/deploy.sh -q` ➡ banner ➡ "Update in Obtainium" · 3 browser APK over it · 4 "C" ➡ DL + 1 confirm ➡ installer = `….dev` · 5 "D" ➡ "🧪 bg update check now" ➡ silent + "✔️ installed" notif (🔑 least sure) · 🤖 deploy only on am0's letter
  - then fon QA r5 (note bubble · double-tap/dog-ear · % · ⚙️) ➡ 🤖 D3 Amen ➡ snippet bottom (D-015) · big round 3/4/6/7 (🔵)
  - 📸 b4 handing any UI change 2 am0: `./gradlew testDebugUnitTest --tests '*ShotsTest*' -Pshots --rerun` ➡ `android/app/build/shots/` ➡ montage + look (D-019)
  - 👤 strix: Changelog 0.9.1 wording · name label (D-016 alts) · ★ vs ✕ on note (D-017) · verse share gone on Today too (D-018) · 💬 qa_1112 Q2 Mitlesen · Q5-7 · stray `pr/omm/▐qq` ➡ rm? · native check hi/bn/ur/id · KeePass keystore note
  - ⚠️ traps: origin = Forgejo (only push target) · `gh` = GitHub fetch-only, mirror carries it (ship.sh waits ≤120 s) · gradle needs `JAVA_HOME=~/.local/opt/jdk21` (lib.sh sets it) · zsh `tr` alias, bare `===` · gradle test cached aft deleting shots ➡ `--rerun` · VPS = ops-owned (pr/ops/omm.md), nvr edit Caddy//srv · `android/omm.properties` (reg token) needed 4 comments/Kreise · fon screenshots only aft asking

### ‼️ QA sprint 0.9.x + feedback — detail
- ⭐🧪 **QA sprint 0.9.x** — r1-r3 ⚡ done (fan-out lanes, 0 conflicts) · left ➡ [qa_1112.md](qa_1112.md): 🎨 10 design proposals (D1 landing · D2 book look · D3 Amen ➡ snippet · D4 auto-scroll feed …) · 🔍 5 research (Duolingo · YouVersion share · Fogg txt · Luddites/AI plan · feedback) · 💬 open Qs
  - ❓ streak widget "shd show 2 at start" = ? (still unanswered)
  - **later** (needs design, not this sprint): 📖 Read = real book look + page flip · ⏰ daily verse can't be turned off (🔍 bible apps' timing) · ❓ name entry · 🔮 Marxoskop tiles · 🔓 Lesekreis progressive disclosure (unlock aft ~1 month, Mi) · 🏷️ quotes off-topic sometimes ➡ manual sort per cat
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

### ‼️ LLM book qy + Matrix tst — detail
- 🌍 **LLM book qy** (all 11 i th app since 0.8/0.9 · notes.md § 🌍): 👤 native QA zh/ar/hi/bn/ur · samples `bin/translate_full.py --show 5 --langs it es`
  - 🔍 alt = existing translations: © per translator (life + 70 y, ZH 50 y) · ES Juan B. Justo 1898 (†1928 ➡ prob PD) · ZH 郭大力/王亚南 1938 (CN PD ~2027) · IT Cantimori (© til 2036) · marxists.org hosting ≠ PD · 🦠 decide aft comparing
- 🦠📚 **Matrix + chat: K + tst**
  - 🦠 re: notes.md § 🏗️ technical: Matrix (10 min) + `server/matrix/README.md`
  - 🤖🧪 `bin/matrix_e2e.py`: 2 throwaway users ➡ Kreis gründen · beitreten · chat · Amen ➡ repeatable smoke tst vs live (th @1104 run wasn't saved) · + a bot user 🦠 can chat w fr th fon = tst chat alone
  - 🤖🧪 `data/Matrix.kt` JVM tst vs a tiny fake HTTP server (register UIA 3 steps · join · timeline parse · redacted dropped) · needs `testImplementation("org.json:json:…")`

### 🔵 big round — detail
- 🤖 **big round** (QA r5 @1113, am0 "rest yes" ➡ parked 4 a data/btn round) · src ➡ qa_1112.md § r4 sweep + § big round pt 1
  - ✔️ @1113 pt 1 (M8 order, non-design): #3 EN titles/gaps · #4 ch.1 TOC · #6 Today snippets · 📜 feed · 🔓 Hooked unlock · 🤖 Ludditen plan ➡ arch D-024..027 · not deployed
  - #7 btn styles mixed: Today tinted circles (CardAction, now also ⚙️ About) · Kreis filled pills · Marxoskop outlined chips · Hooked AssistChips ➡ 1 system (D7) ➡ design system spec (pt 2)
  - 🤖 teach translate.py PlanDay xd/xe (Ludditen bridges = EN i oth langs) · UI fan-in ✔️ @1113

### ✔️ archive (≤@1107)
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
- 🪝 "Hijack the brain" slide (3 levers: ⬆ stimulation · ⬇ friction · ⬇ satiation) ➡ OCR + notes i `re/boox/hooked/hooked.md` (end) · img `re/boox/hooked/img/Screenshot_20261110-030906.png` @1114

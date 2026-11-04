# dfs — **Oh My Marx** (was bibleApp · code pkg `marxversion`) · Kapital als Bible-App

_Marx' Kapital Bd.1 i bite-sized daily verses, w bible-app mechanics + Augenzwinkern · 1st Android app = L project_
_raw idea: [plan.md](plan.md) · full plan @1028: `~/.claude/plans/read-plan-md-for-floofy-eich.md` · todo: [todo_omm.md](todo_omm.md) · dir/repo renamed bible ➡ `pr/omm` · `github.com/am0ebe/oh-my-marx` @1104 (app id `de.strix.marxversion` unchanged)_

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
bin/align.py      # DE⬄EN DP alignment ➡ data/verses.jsonl + notes.jsonl  (--show 8 = eyeball)
bin/score.py      # LLM: quote_de/en · juice · difficulty · tags ➡ data/scores.jsonl  (resumable, ~1s/Vers, ~35 min)
bin/rank.py       # LLM relative pass per section: ⭐ best ~1/6 · 🔥 real roasts ➡ data/rank.jsonl (~2 min)
bin/build.py      # verbatim quotes + plans + export ➡ android/app/src/main/assets/content/*.json
bin/andacht.py    # LLM Andachten per plan day ➡ data/andachten.jsonl  ➡ then build.py again
bin/translate.py  # AFT build.py: quotes · Andachten · plans · chapters · UI strings ➡ it/es/pt/zh/ar ➡ assets/content/i18n_xx.json (~8 min/lang)
```
- i18n: DE+EN inline `tr("de", "en")` · oth langs = lookup by EN string i `i18n_xx.json` · non-composable strings = `T("de", "en")` pairs (translate.py greps both)
- ⚠️ no `$var` inside tr()/T() ➡ use `"%d".f(x)` (else lookup misses) · translate.py warns
- full verse txt + footnotes = DE/EN only (1.3 M chars/lang) ➡ IT/ES/… show EN w a note · AR = RTL (whole layout mirrors via LocalLayoutDirection)
- cache `data/i18n/<lang>.jsonl` · re-run = only changed strings · `--export-only` rebuilds th json fr cache
- src DE = MEW 23 via archived mlwerke.de (wayback) — has `<245>` page markers ✔️ · EN = marxists.org
- ⚠️ mlwerke html is sloppy: missing `</A>` i headings · page anchors `M341` ⬄ footnote anchors `M8` · 1 typo'd fn link (`#Z203`) — split.py handles all 3
- align: 10/10 random pairs ✔️ · 1719/1949 Absätze w EN (rest = tables/headings/merged paras)
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

## icon
- Marx (J. J. E. Mayall, London 1875, PD) · crop face ➡ sepia ➡ circle ➡ gold halo · on Parteibuch-rot
- `magick` cmds: crop 700x700+480+290 ➡ 220px ➡ circle mask ➡ ellipse halo @y106 ➡ `res/mipmap-xxxhdpi/ic_marx_fg.png`

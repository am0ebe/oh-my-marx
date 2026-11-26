# ☭ Oh My Marx

[![release](https://img.shields.io/github/v/release/am0ebe/oh-my-marx?label=release&color=b3261e)](https://github.com/am0ebe/oh-my-marx/releases)
[![license](https://img.shields.io/badge/license-GPL--3.0-e8b931)](LICENSE)
[![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-1d1230)](https://scagent.de/oh-my-marx/)
[![Forgejo mirror](https://img.shields.io/badge/source-Forgejo-1d1230?logo=forgejo)](https://git.scagent.de/am0/oh-my-marx)

*Read Marx like the Bible.* An Android app that serves Karl Marx's *Capital*, vol. 1, as a daily verse, with the
mechanics of bible apps: verse cards, the whole book to page through, reading plans, streaks, a stamp card, share
cards. Its **Hooked mode** labels every habit-forming trick the app plays on you.

A fun prototype, shared with friends outside the app stores.

## Install

Same steps as the download page **<https://scagent.de/oh-my-marx/>**, in the same order:

**1. Download the APK** (recommended: the app updates itself)

1. On your Android phone open **<https://scagent.de/oh-my-marx/>** ➡ tap **Download**
2. Allow installing from this source (once) ➡ **Install**
3. Play Protect may warn about an "unknown app" (normal outside the Play Store) ➡ **Install anyway**
4. Updates: the app tells you when a new version is out (notification + banner on Today) ➡ **Install** ➡
   confirm, your data stays. ⚙️ **Update automatically** = new versions install without asking (Android 12+)

**2. Also works: Obtainium** (if you already use it to update apps)

1. Get [Obtainium](https://github.com/ImranR98/Obtainium/releases/latest) (free, open source app updater)
2. In Obtainium tap ＋ and paste `https://scagent.de/oh-my-marx/` ➡ **Add** ➡ **Install**
   (or tap **➕ Add to Obtainium** on the download page)

**3. Or build it yourself** from this repo ([GitHub](https://github.com/am0ebe/oh-my-marx) or the
[Forgejo mirror](https://git.scagent.de/am0/oh-my-marx)): see [below](#build-it-yourself).

Every version stays downloadable under [Releases](https://github.com/am0ebe/oh-my-marx/releases), with its notes.

## Specs

| | |
|---|---|
| **version** | latest: see the release badge above · single source: `appVersion` in [`android/app/build.gradle.kts`](android/app/build.gradle.kts) · release notes in-app (⚙️) and on each release |
| **platform** | Android 8.0+ (`minSdk 26`), `targetSdk`/`compileSdk 36` |
| **stack** | Kotlin 2.4 · Jetpack Compose (Material 3) · Room · DataStore · WorkManager · kotlinx-serialization |
| **build** | AGP 8.13 · Gradle 8.14 (wrapper) · JDK 17+ |
| **dependencies** | AndroidX + kotlinx only. No Google Play Services, Firebase, analytics or ads. HTTP via plain `HttpURLConnection`. |
| **permissions** | `INTERNET` · `POST_NOTIFICATIONS` (daily verse) · `VIBRATE` |
| **languages** | English (Moore/Aveling 1887; default, or your phone's language if listed) · German (Marx's original) · IT · ES · PT · 中文 · عربي (RTL) · FR · RU · हिन्दी · বাংলা · اردو (RTL) · ID: everything incl. the whole book, translated from German by a local LLM (not a published edition) |
| **size** | ~21 MB APK, the whole book ships inside (works offline) |
| **package** | `de.strix.marxversion` · dev builds `….dev` install side by side |
| **license** | code [GPL-3.0](LICENSE) · book texts public domain |

## Build it yourself

**Easiest: Android Studio.** It brings its own JDK and Android SDK.

1. Install [Android Studio](https://developer.android.com/studio) and start it once (it downloads the SDK)
2. `git clone https://github.com/am0ebe/oh-my-marx` (or the mirror: `git clone https://git.scagent.de/am0/oh-my-marx.git`)
3. Android Studio ➡ **Open** ➡ pick the `android/` folder ➡ wait for the Gradle sync
4. Phone: Settings ➡ About ➡ tap *Build number* 7× ➡ Developer options ➡ **USB debugging** on ➡ plug in
5. Press ▶ **Run**. The app lands on your phone.

**Command line** (JDK 17+ and an Android SDK with platform 36 installed):

```sh
git clone https://github.com/am0ebe/oh-my-marx && cd oh-my-marx/android
export ANDROID_HOME=~/Android/Sdk      # where your SDK lives
./gradlew testDebugUnitTest            # unit tests (content, streak logic, circles, voice DSP)
./gradlew assembleDebug                # ➡ app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest --tests '*ShotsTest*' -Pshots --rerun   # headless screenshots (Robolectric) ➡ app/build/shots/
adb install app/build/outputs/apk/debug/app-debug.apk
```

Your build is signed with your own key, so it installs as its own app next to the official one. Comments and
reading circles need a server token that isn't in the repo; everything else works the same.

## Online bits

Bookmarks, notes, streak and settings stay on the phone. Three things talk to `scagent.de` (a small server in
Germany): the update check (reads `version.json`), the anonymous *read along* counter
([`server/omm_api.py`](server/omm_api.py), ~200 lines of Python stdlib: page keys + a daily random token, no IPs) and,
once you use them, comments and reading circles (a [Matrix](https://matrix.org) homeserver, what it needs:
[`server/SERVICE.md`](server/SERVICE.md)). *Read along* is always on since 0.10.0 (no switch).

### Known gaps

- The comment server's registration token is compiled into the APK. It only keeps drive-by bots out.
- Circle chats are not end-to-end encrypted.
- The comment server (continuwuity) keeps one IP address per device of a comment account: the last one it saw,
  overwritten on each use, no history. It has no time limit: the IP goes only when that device is removed on the server.
  The web server in front logs an IP only when a comment account is registered, for at most 48 hours.
- Releases are signed with a debug key so far.
- *Read along* cannot be switched off (anonymous daily token, no IPs).

## Layout

```
android/          Android Studio project (module app)
  app/src/main/java/de/strix/marxversion/
    MainActivity.kt   1 Activity · top bar + tabs · NavHost
    ui/               Compose screens: Today (cards), Read (book), Plans, Circle, My Capital, Settings, Hooked
    data/             Room DB · DataStore · assets JSON · i18n · counter · Matrix · update check
    logic/            pure Kotlin: verse of the day, streak, holidays, voice DSP ➡ unit-tested
    notify/           daily notification · widget · quick-settings tile · update worker
bin/              content pipeline (Python + local LLM via ollama) · deploy.sh (dev) · ship.sh (release)
data/             pipeline intermediates: verses, alignment, scores, translations
server/           counter API · Matrix config · SERVICE.md (what the server side needs)
notes.md          design notes + decisions (my own shorthand)
```

Pipeline: `fetch.py` ➡ `split.py` ➡ `align.py` (DE⬄EN) ➡ `score.py` + `rank.py` (LLM picks quotable verses) ➡
`build.py` (app JSON) ➡ `andacht.py` (devotionals) + `tease.py` (one "tomorrow?" question per plan day) ➡ `translate.py` (quotes, devotionals, plan texts, UI: 11 languages) ➡
`translate_full.py` + `--export` (the whole book + footnotes, ~6-9 h on a local GPU).
`font_subset.sh` cuts the 5 Literata styles the app uses (Latin, Greek, Cyrillic) from the original variable fonts.
`eggs.py` draws the 30 holiday icons ("eggs") once in Python and writes the Android vectors (shown in the app on each holiday) and a preview gallery (`img/eggs/gallery.png`).

### Versions and releases

There is one version number: `val appVersion = "x.y.z"` in `android/app/build.gradle.kts`, the last shipped release.
Nobody edits it by hand: `bin/ship.sh minor|patch|major` bumps it. Two channels, two apps on the phone:

| | script | version | goes to | phone |
|---|---|---|---|---|
| 🧪 dev | `bin/deploy.sh` (`-a` = also install over wireless adb) | next release + `-dev.MMDD.HHMM`, e.g. `0.9.0-dev.1107.1430` | `scagent.de/oh-my-marx/dev/` (also the 🧪 Dev tab on the page) | "Oh My Marx DEV" (`….dev`), updates itself (or via Obtainium) |
| 📦 stable | `bin/ship.sh minor` | `0.9.0` | `scagent.de/oh-my-marx/` + git tag + GitHub release | "Oh My Marx" |

Dev builds are labelled with the coming version automatically (shipped `0.9.0` ➡ dev shows `0.10.0-dev…`), so
`appVersion` lagging one step behind the DEV app is normal: it only moves when you ship.
`versionCode` = minutes since 2026-01-01, so every build counts as newer and switching channels never needs a
downgrade. The main update path is the app's own updater: hand-installed APKs get a check on app start +
daily, a notification and a one-tap install. After that first install, ⚙️ "Update automatically" lets the app
update itself silently on Android 12+. Other installers keep their own job: stores update it themselves;
Obtainium installs get a notification that opens Obtainium (`obtainium://refresh`), and Obtainium's own
background check often wins. The DEV app follows `scagent.de/oh-my-marx/dev/`.

**How to ship** (TL;DR):

- 🧪 DEV, any time: `bin/deploy.sh` ➡ `…/dev/` ➡ the DEV app (its own updater, or Obtainium) picks it up · no commit, no tag
- 📦 stable: write the `Release("0.10.0", …)` notes on top of `ui/Changelog.kt` ➡ commit + push ➡
  `bin/ship.sh 0.10.0` (or `minor` / `patch`: bumped from the last tag) ➡ APK + page + tag + GitHub release
- the version you pass = what the DEV app already shows, without `-dev…`

Release cycle:

1. Code ➡ `bin/deploy.sh` ➡ phone: open the DEV app ➡ notification / Today banner ➡ **Install** (Obtainium: pull to refresh ➡ **Update**)
2. QA on the phone (feature tour: `notes.md` § QA checklist) ➡ fix ➡ back to 1
3. New or changed UI strings? `bin/translate.py --only ui` · new plan days? `--only a x` (`bin/check_langs.py` refuses the ship otherwise)
4. Add `Release("0.9.0", …)` on top of `ui/Changelog.kt` (in-app notes, GitHub release, download page)
5. Commit + push ➡ `bin/ship.sh minor` (features), `patch` (fixes only) or an exact `X.Y.Z`: bumps, commits, pushes, tags, publishes.
   Refuses on a dirty tree, an unpushed HEAD, a missing changelog entry or an already-shipped version

## bin

Every script answers `--help` (why · what · examples) and does nothing else when asked.

| script | what it does |
|---|---|
| `fetch.py` | downloads the raw Kapital html (MEW 23 German, Moore/Aveling English) into `data/raw/` |
| `split.py` | MEW html ➡ one numbered paragraph ("verse") per line in `data/de.jsonl` |
| `align.py` | pairs each German paragraph with its English one(s) ➡ `data/verses.jsonl` |
| `score.py` · `rank.py` | local LLM rates every verse, then picks the best quotes per section |
| `retag.py` | second, narrow LLM pass: the 0-2 topics a verse is really about (weighted) ➡ the "What bugs you?" chips · `--stats` · `--peek N` (N quotes per chip: do they fit?) |
| `build.py` | all of `data/` ➡ the app's `assets/content/*.json` + reading plans |
| `andacht.py` · `tease.py` | local LLM writes the daily devotional and the "tomorrow?" question per plan day |
| `translate.py` | app texts ➡ 11 languages (local LLM, cached) · `--export-only` rebuilds the files without the LLM |
| `translate_full.py` | the whole book + footnotes ➡ one language (hours, resumable) |
| `check_langs.py` | ship guard: every menu language is complete and free of translation junk |
| `gpu_night.sh` | waits until the shared GPU is free, then runs one LLM job + `check_langs` (for `at` night runs) |
| `matrix_e2e.py` | end-to-end smoke test against the live Matrix server (test accounts, comments, reading circle) · `--app` = the real app UI does it · `--cleanup` |
| `eggs.py` | the 30 holiday icons ➡ Android vectors + preview gallery |
| `font_subset.sh` | cuts the 5 Literata styles the app uses |
| `notes.py` | one version's release notes from `ui/Changelog.kt` |
| `voice_up.py` | checks the AI voice packs with the app's own rules (size, hash, zip contents, ids, how many clips still match the text) · `-u` uploads them, manifest last |
| `deploy.sh` · `ship.sh` | DEV build to the phone · stable release (both = [`appkit/tool/apptool`](appkit/README.md), the shared app kit) |
| `test_bin.py` | tests for the scripts above |

## Sources

- **German:** Karl Marx, *Das Kapital* vol. 1, 4th ed. 1890 (ed. Engels), text and page numbers as in MEW 23 (Dietz),
  via the archived mlwerke.de
- **English:** Samuel Moore & Edward Aveling 1887, via marxists.org
- **Portrait:** J. J. E. Mayall, London 1875
- all public domain · quote picks, devotionals and the other languages are generated with a local LLM (Qwen3);
  quotes are always Marx's verbatim sentence
- **IT · ES · PT · 中文 · عربي · FR · RU · हिन्दी · বাংলা · اردو · ID:** the whole book + footnotes translated from the German by that LLM, with a glossary of
  the standard terms per language. Not a published edition: it may stumble

## Credits & contact

Proudly vibe-coded by a centaur: **am0ebe** (human: ideas, taste, testing) + **Claude Opus 5.5** in Claude Code
(Anthropic: most of the typing). Translations by Qwen3, locally on an RTX 3090 Ti.

Feedback, bugs, ideas: **omm@scagent.de** or open an issue.

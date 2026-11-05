# ☭ Oh My Marx

*Read Marx like the Bible.* An Android app that serves Karl Marx's *Capital*, vol. 1, as a daily verse, with the
mechanics of bible apps: verse cards, the whole book to page through, reading plans, streaks, a stamp card, share
cards. Its **Hooked mode** labels every habit-forming trick the app plays on you.

A fun prototype, shared with friends outside the app stores.

## Install

1. On your Android phone open **<https://scagent.de/omm>**
2. Tap **Download** ➡ allow installing from this source (once) ➡ **Install**
3. Play Protect may warn about an "unknown app" (normal outside the Play Store) ➡ **Install anyway**

**Updates:** the app tells you when a new version is out ➡ tap ➡ install over it, your data stays.
Want it fully automatic? [Obtainium](https://github.com/ImranR98/Obtainium) ➡ ＋ ➡ `https://scagent.de/oh-my-marx/`

Every version stays downloadable under [Releases](https://github.com/am0ebe/oh-my-marx/releases), with its notes.

## Specs

| | |
|---|---|
| **version** | `0.7.0` · single source: `appVersion` in [`android/app/build.gradle.kts`](android/app/build.gradle.kts) · release notes in-app (⚙️) and on each release |
| **platform** | Android 8.0+ (`minSdk 26`), `targetSdk`/`compileSdk 36` |
| **stack** | Kotlin 2.4 · Jetpack Compose (Material 3) · Room · DataStore · WorkManager · kotlinx-serialization |
| **build** | AGP 8.13 · Gradle 8.14 (wrapper) · JDK 17+ |
| **dependencies** | AndroidX + kotlinx only. No Google Play Services, Firebase, analytics or ads. HTTP via plain `HttpURLConnection`. |
| **permissions** | `INTERNET` · `POST_NOTIFICATIONS` (daily verse) · `VIBRATE` |
| **languages** | German first (Marx's original). Full book text: DE + EN. App, quotes, devotionals, plans: + IT · ES · PT · 中文 · عربي (RTL) |
| **size** | ~17 MB APK, the whole book ships inside (works offline) |
| **package** | `de.strix.marxversion` · dev builds `….dev` install side by side |
| **license** | code [GPL-3.0](LICENSE) · book texts public domain |

## Build it yourself

**Easiest: Android Studio.** It brings its own JDK and Android SDK.

1. Install [Android Studio](https://developer.android.com/studio) and start it once (it downloads the SDK)
2. `git clone https://github.com/am0ebe/oh-my-marx`
3. Android Studio ➡ **Open** ➡ pick the `android/` folder ➡ wait for the Gradle sync
4. Phone: Settings ➡ About ➡ tap *Build number* 7× ➡ Developer options ➡ **USB debugging** on ➡ plug in
5. Press ▶ **Run**. The app lands on your phone.

**Command line** (JDK 17+ and an Android SDK with platform 36 installed):

```sh
git clone https://github.com/am0ebe/oh-my-marx && cd oh-my-marx/android
export ANDROID_HOME=~/Android/Sdk      # where your SDK lives
./gradlew testDebugUnitTest            # unit tests (content, streak logic, circles, voice DSP)
./gradlew assembleDebug                # ➡ app/build/outputs/apk/debug/app-debug.apk
adb install app/build/outputs/apk/debug/app-debug.apk
```

Your build is signed with your own key, so it installs as its own app next to the official one. Comments and
reading circles need a server token that isn't in the repo; everything else works the same.

## Online bits

Bookmarks, notes, streak and settings stay on the phone. Three things talk to `scagent.de` (a small server in
Germany): the update check (reads `version.json`), the anonymous *read along* counter
([`server/omm_api.py`](server/omm_api.py), ~200 lines of Python stdlib: page keys + a daily random token, no IPs) and,
once you use them, comments and reading circles (a [Matrix](https://matrix.org) homeserver, config in
[`server/matrix/`](server/matrix/)). *Read along* can be switched off in ⚙️.

### Known gaps

- The comment server's registration token is compiled into the APK. It only keeps drive-by bots out.
- Circle chats are not end-to-end encrypted.
- Synapse keeps its default client-IP records (~28 days) for comment accounts.
- Releases are signed with a debug key so far.
- *Read along* is on by default.

## Layout

```
android/          Android Studio project (module app)
  app/src/main/java/de/strix/marxversion/
    MainActivity.kt   1 Activity · top bar + tabs · NavHost
    ui/               Compose screens: Today (cards), Read (book), Plans, Circle, My Capital, Settings, Hooked
    data/             Room DB · DataStore · assets JSON · i18n · counter · Matrix · update check
    logic/            pure Kotlin: verse of the day, streak, holidays, voice DSP ➡ unit-tested
    notify/           daily notification · widget · quick-settings tile
bin/              content pipeline (Python + local LLM via ollama) · deploy.sh (dev) · ship.sh (release)
data/             pipeline intermediates: verses, alignment, scores, translations
server/           counter API · Matrix config · SERVICE.md (what the server side needs)
notes.md          design notes + decisions (my own shorthand)
```

Pipeline: `fetch.py` ➡ `split.py` ➡ `align.py` (DE⬄EN) ➡ `score.py` + `rank.py` (LLM picks quotable verses) ➡
`build.py` (app JSON) ➡ `andacht.py` (devotionals) ➡ `translate.py` (IT/ES/PT/ZH/AR) · experimental:
`translate_full.py` (the whole book).

Channels: `bin/deploy.sh` ➡ the page's [Dev tab](https://scagent.de/oh-my-marx/#dev) (separate "DEV" app, every build) ·
`bin/ship.sh` ➡ stable page + git tag + GitHub release.

## Sources

- **German:** Karl Marx, *Das Kapital* vol. 1, 4th ed. 1890 (ed. Engels), text and page numbers as in MEW 23 (Dietz),
  via the archived mlwerke.de
- **English:** Samuel Moore & Edward Aveling 1887, via marxists.org
- **Portrait:** J. J. E. Mayall, London 1875
- all public domain · quote picks, devotionals and the other languages are generated with a local LLM (Qwen3);
  quotes are always Marx's verbatim sentence

## Credits & contact

Proudly vibe-coded by a centaur: **am0ebe** (human: ideas, taste, testing) + **Claude Opus 5.5** in Claude Code
(Anthropic: most of the typing). Translations by Qwen3, locally on an RTX 3090 Ti.

Feedback, bugs, ideas: **omm@scagent.de** or open an issue.

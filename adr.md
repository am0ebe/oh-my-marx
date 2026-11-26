# arch — omm ☭ 🧩 (living · hu + agents read it)
_SDD: code says **wh** · th file says **y** · drift = a bug · fmt ➡ `pr/vibe++/adr_tpl.md` · backfilled @1112 fr notes.md "decided" · todo ✔️ · git log · y unknown = "❓ am0"_

## 🎯 intent
- 4 strix + few friends: actually get thru Kapital Bd.1 — habit, not willpower · Hooked L hands-on · L Android (Kotlin + Compose) · fun, honest about its own hooks (notes.md § 🎯 goals)
- good = daily ≤2 min verse + Andacht, streak wo anxiety, whole book readable offline i 13 langs

## 📐 req + constraints
- must: offline-first (whole book i APK) · sideload (no store) · DE + EN UI evywhere, rest via translate.py
- must-not: ❌ PII · keys i public repo (`priv/` git-ignored) · ❌ bump/ship/deploy wo am0's fon QA · ❌ edit VPS/Caddy (ops-owned)
- limits: APK ~30 MB · local LLM 4 translation (ollama) · lone dev + AI crew

## 🧩 arch (current)
- app `android/` (pkg `de.strix.marxversion`): `logic/` (Daily · Feast · Feed · Orden · PlanUnlock · Voice) · `data/` (Prefs · Content · Db · I18n · IdMoves · Update · Community · Matrix) · `ui/` (Compose screens) · `notify/` (Reminder · Widget · KreisWorker · UpdateWorker · LosungTile)
- content pipeline `bin/*.py` (fetch ➡ split ➡ align ➡ score ➡ translate) ➡ `assets/content/*.json` · cache `data/i18n/<lang>.jsonl`
- server (VPS, ops-owned): `server/omm_api.py` counter + Matrix homeserver = continuwuity `matrix` since @1113 (comments · Kreise · was Synapse `omm-matrix`, config SSOT ➡ `pr/ops/matrix.md`) · contract `server/SERVICE.md`
- release: `bin/deploy.sh` (DEV) · `bin/ship.sh` (bump + tag + page + GH release) = 🧰 appkit `apptool` · updater = appkit `core/selfupdate` (D-079) · map ➡ [ref.md](ref.md)
- 🔥 hot files (1 owner at a ti — fan-out lanes nvr split these): `assets/content/i18n_*.json` + `data/i18n/*.jsonl` (translate pass = fan-in only) · `android/app/build.gradle.kts` (version) · `ui/Changelog.kt` · `res/values*/strings.xml` · `TodayScreen.kt` · `MineScreen.kt` · `Share.kt`

## 🧾 decisions (newest on top)
_Template v2.1 since 20 Nov (converted via `pr/vibe++/adr_convert.md`). Field names: Y = why · Ctx = what forced it · Scope = what it covers · Ck = how to check._

### D-084 @1126 · 🧺 Every reader starts at level 1, "Commodity", instead of level 0 with no rank · Proposed (crew @1126, 🦠 scrap via M8 #345; name = 🦠's taste)
- **Ctx:** 🦠 scrap @1126: "start w lvl1. It shd also have a name." ⚙️ Mine showed **LEVEL 0 · "No rank yet"** until the 1st full Stempelkarte (D-061 row). LEVEL 0 + "no rank" = a new reader starts with nothing.
- **Dc:** shown level = full cards + 1 (`ui/Moments.kt` `level()` · `levelName()`) · level 1 = **🧺 Ware / Commodity**, Kapital's first word ("die einzelne Ware als seine Elementarform") · the 7 old rank names keep their order and sit at levels 2-8 · Mine, Promotion pill ("LEVEL N ▲") and the plan lock hint ("at level N") all shift +1 · perks + unlocks stay on cards (D-050 numbers = cards, now shown 1 higher) · i18n 11 langs (it + hi hand-fixed: Merce · माल).
- **Y:** a name from the 1st minute = identity from day 1 (Hooked: investment) · Commodity fits the rank path (Kapital's own history of capitalism) and is where the book starts.
- **Alt:** "📕 Lehrling / Apprentice" (generic, off the Kapital path) · "🧑‍🏭 Lohnarbeiter / Wage labourer" (heavier, the reader = labour power) · keep 0, only add a name (con: "level 0" still reads as nothing).
- **Open:** level 1 shows th faded rank_1 badge (shovel = Primitive accumulation, "not yet" look) · own 🧺 badge art = 🦠 ❓ · D-050 / D-063 perk numbers = cards (shown level = +1, top = 8).
- **Ck:** `LevelTest` (new reader = 1 + Commodity · 1st card = 2 + Primitive accumulation · 7 = 8) · `MineLevelTest` (real Mine screen: no LEVEL 0 / "No rank yet") · `MomentsShotsTest.rank3` = LEVEL 4 · 140 unit + lint + check_langs green @1126.

### D-083 @1126 · 🔄 Updates ask by default: the banner's Install button is the main path, silent auto-update is opt-in again · Accepted (🦠 @1123 via M8 #345)
- **Ctx:** 🦠 @1123: silent auto-update failed on GrapheneOS · in-app banner ➡ Install worked ➡ "banner = main path 4 all users, silent auto = OFF by dflt · Obtainium stays 2nd" (1 non-tech user gave up on Obtainium). D-078 had flipped the default to ON 1 day earlier.
- **Dc:** kit `Update.AUTO_DEFAULT = false` (pr/appkit) · omm passes no own `autoDefault` ➡ kit dflt · key `auto_update` absent ➡ OFF · ⚙️ "Update automatically" row stays = opt-in (as D-038) · silent path code unchanged (a usr who turns it ON still gets it; a failed silent try already falls back 2 asking).
- **Y:** the 1 path proven on a real phone (banner ➡ Install ➡ confirm) beats a path th fails quietly on some ROMs; a usr who wants silent can still pick it.
- **Alt:** keep ON + detect GrapheneOS (fragile, unknown root cause, oth ROMs may fail th same way) · drop silent code (con: works on stock 12+, cheap 2 keep).
- **Existing installs:** nvr touched ➡ OFF from th build th carries D-083 on (DEV now · stable = 🦠's call) · set ON by hand ➡ stays ON · set OFF ➡ stays OFF · cali alr passed `autoDefault = false` ➡ 0 change there.
- **Prev:** D-078 (default ON ➡ superseded) · D-038 (OFF default, back).
- **Ck:** kit `UpdateTest.autoDefaultIsOffButAnOnChoiceStays` · omm `UpdateWorkerTest.autoDefaultOffAndOnChoiceStays` (nvr touched ➡ OFF · ON ➡ SILENT · OFF ➡ NOTIFIED) · 135 + 17 unit + lint green @1126.

### D-082 @1122 · 🔒 The app's exact dependency versions sit in a Gradle lockfile, so ops' scanner can check them · Accepted (crew @1122, M8 #341 · night F12)
- **Ctx:** ops' nightly osv-scanner reads lockfiles, not `gradle/libs.versions.toml` (ranges + no transitive deps) ➡ the app's ~380 resolved libraries were never scanned.
- **Scope:** `:app` only: `dependencyLocking { lockAllConfigurations() }` ➡ `android/app/gradle.lockfile` (+ an empty `settings-gradle.lockfile`) · `:selfupdate`'s deps are inside the app's classpaths ➡ no file in the vendored kit · `kotlin-stdlib-common` ignored (Kotlin 2.x resolves this empty compat jar in or out depending on task order ➡ flaky lock; `kotlin-stdlib` stays locked) · a dependency bump must re-write the lock: `./gradlew :app:dependencies lintDebug testDebugUnitTest assembleRelease --write-locks` (the plain `dependencies` task misses configurations the build resolves).
- **Alt:** lock only `releaseRuntimeClasspath` (what ships; rejected: test + lint deps would go unscanned) · a scanner that reads the version catalog (ops' pick, not ours) · lock the kit module too (rejected: a file in the vendored subtree = merge noise on every pull).
- **Ck:** CI cmd from `.forgejo/workflows/ci.yml` green from `clean` without `--write-locks` · `bin/deploy.sh -n` green (DEV = `assembleDebug -Pdev`).

### D-081 @1122 · ☁️ The comment login stays in Android's backup, so a new phone keeps your account · Accepted (🦠 @1122 via M8 #339, "keep")
- **Ctx:** night F7: `android:allowBackup="true"` with no extraction rules ➡ Android's Google backup and device-to-device transfer also copy the DataStore prefs, including `mx_user` · `mx_token` · `mx_pw`.
- **Scope:** keep as is, 0 code change · on a restore the app finds the old login: the token may be dead (new device) ➡ D-075 re-registers, or the password logs back in later · amends D-073's "lost phone = lost account": that is now true only without a Google backup.
- **Cost:** the comment login (random password + token) sits off the device in the user's Google backup (encrypted with the screen lock on Android 9+). Someone holding that backup can post as that reader. The account carries only comments and circles, no real name or mail.
- **Alt:** exclude `mx_*` via `dataExtractionRules` / `fullBackupContent` (rejected for now: a new phone = a new account, old comments can't be deleted from it any more) · `allowBackup="false"` (rejected: also drops notes, bookmarks and streak from the backup).
- **Ck:** `AndroidManifest.xml` `allowBackup="true"`, no extraction rules · `data/Prefs.kt` comment matches.

### D-080 @1122 · 📜 The comment sheet can load older comments, so a flood of new messages can't hide the real ones · Accepted (🦠 @1122 via M8 #336 "sure" on night F5 · 🤖 built)
- **Ctx:** night F5: the app read only the newest 300 messages of a chapter room. One account posting 300 times buried every older comment in that chapter, on every page.
- **Scope:** `Matrix.commentPage(from)` pages `/messages` backwards with the `end` token (no `end`, or an empty page = start of the room) · the sheet shows "↑ Load older comments" on top while the room has more · each tap = 300 messages further back, added above · a refresh after posting or deleting keeps the older pages already loaded (newest page wins, the deleted id is dropped) · Kreis timelines still read the newest 300 (private rooms, small).
- **Alt:** auto-load until the page has comments (rejected: a flood = many silent calls, and the sheet can't tell "no comments" from "not loaded yet") · Matrix filter by page (rejected: filters can't match our custom `de.ohmymarx.page` key) · only rate-limit on the server (ops F6, still wanted, but it doesn't help with what is already posted).
- **Ck:** `MatrixTest.commentPageFollowsEndTokenToTheStart` + `mergeKeepsOlderPagesDropsGone` · DEV: a chapter with > 300 messages shows the button.

### D-079 @1122 · 🧩 The updater, ship/deploy tool, version script, templates and lessons live in one kit folder, appkit/, that every app vendors; omm only holds config · Accepted (🤖, routine · 🦠 order via M8 #327 "u pick where, D w y" + add-ons "own dir", "4 shelves")
- **Y:** One folder with no omm names lifts out to `pr/appkit/` as is (🦠: "their OWN dir = an app platform project"), and every app carries it at the same path `<app>/appkit/`, so code paths (`../appkit/core/selfupdate`, `appkit/tool/apptool`) never change. A vendored copy (git subtree) keeps a public clone and CI building from one checkout.
- **Scope:** 4 shelves (pr/appkit D-001): `core/selfupdate` (Android lib, `UpdateConfig` = the app part) · `tool/` (`apptool` deploy · ship · page · oblink, `apptool.sh` lib, `app-version.gradle.kts`) · `templates/` (page · README) · `lessons/` (omm app_lessons + cali general items). omm keeps: `notify/UpdateWorker.kt` (its config · `launcherReset` gone @1122, D-066) · `ui/UpdateDialog.kt` (its design system) · `bin/lib.sh` (config + page + publish hooks) · `bin/deploy.sh` / `ship.sh` = 1-line wrappers. List in / later / no + chat sketch: [appkit/README.md](appkit/README.md).
- **No behavior change:** WorkManager stores the worker's class name, so `de.strix.marxversion.notify.UpdateWorker` stays as a subclass; prefs `omm_update`, work `marx_update`, session `omm.apk` and stamp key `ommBuild` keep their names (`UpdateWorkerTest.ommConfigKeepsUrlsAndStorageNames`). Page + version.json render byte-same as before. Receivers move to the kit manifest (merged).
- **Cost:** a kit bug breaks every app; a kit change needs each app's tests green. The kit code becomes public through omm's repo even if `pr/appkit` stays private.
- **Alt:** git submodule (rejected: every clone and CI needs `--recursive`) · sibling path `../../appkit` (rejected: the public omm clone would not build) · publish to a maven repo (rejected: infra for 2 apps) · keep copying per app (rejected: pr/appkit D-001).
- **Prev:** #297 (extract on the 2nd copy: cali copied apptool + app-version @1122, #326).
- **Lift:** @1122 #329 the kit moved to its own repo `pr/appkit/` (history via `git subtree split`, its D-002 points here); omm vendors it back at `appkit/` with `git subtree add --squash`. Kit fixes go there first, then `git subtree pull` (`appkit/README.md` § 🔄).
- **Ck:** kit `UpdateTest` 16 + app 133 = 149 unit green (148 + 1 new guard) · lint green · `bin/test_bin.py` 59 · `appkit/tool/apptool -h` · `grep -ri "marx\|omm" appkit/core appkit/tool appkit/templates` = 0.

### D-078 @1122 · 🆕 Browser installs update themselves by default; the ⚙️ toggle stays as an opt-out · Accepted (🦠 @1122 via M8 #332, "A dflt on ok"); Superseded by D-083
- **Ctx:** You @1122 (via M8, #327): "omm had always ship with auto-updater. Its not an option. lets shift to this update mechanism as default and main instead of obtainium". The in-app updater is now the main install path (README + page: APK first, Obtainium second). D-038 (@1114, your words then: "should ask user … or they select autoupdate in settings") set ⚙️ "Update automatically" to **OFF** by default. "Not an option" can mean two things: (1) the updater always ships: already true since D-021, every build has it, nothing to change · (2) silent install is not opt-in any more: that is this D.
- **What Android allows (any option):** the **first install** from the browser always needs "allow from this source" + 1 Install tap, no app can skip it. Silent updates need Android 12+ **and** our app as installer of record: after a browser install the installer is Chrome, so the **first in-app update** needs 1 confirm tap, then we are the installer and later ones can go silent. Android 8-11: every update = 1 tap, always. Obtainium / store installs: their job, unchanged.
- **Options:**
  - **A) toggle stays, default ON** (rec): new + existing browser installs update silently from the 2nd update on (12+), a notification "✔️ installed" says so · ⚙️ row stays as the opt-out (data saver, "don't touch my app") · 1 default flip + 1 migration (an OFF that was never set by hand counts as ON) · fits "default and main" and keeps D-038's consent as an escape hatch
  - **B) no toggle, always auto:** same behaviour as A, minus the ⚙️ row · simplest UI · con: no way out for a user who doesn't want 21 MB on mobile data or a surprise new version (only uninstall), and a bad release reaches everyone at once with no one holding back
  - **C) as is (D-038):** OFF = every update asks (notification ➡ tap ➡ confirm) · con: contradicts "not an option"; most friends never open ⚙️, so they stay on manual
- **Y (rec A):** your "default and main" = the default is what matters, and A gives it; the opt-out costs nothing (already built + tested) and keeps one brake for a bad release.
- **Scope (built @1122, #332):** the default lives in the kit so every app gets it: `appkit` `UpdateConfig.autoDefault` (= `Update.AUTO_DEFAULT` = true) · the app passes the **stored** toggle, `null` = never touched ➡ default (`Update.autoOn`) · omm `Prefs`: key `auto_update` absent ➡ ON · Settings row text unchanged.
- **Existing installs** (the default is app code, so a phone flips on the update that carries it: DEV now, stable with the next release):
  - **never touched the toggle** (no `auto_update` key stored): counts as ON from that build on · that update itself still asks (notification ➡ Install ➡ confirm; after a browser install Chrome is the installer) · from the next one: silent on Android 12+ with a quiet "✔️ installed" notification · Android 8-11: still 1 tap each
  - **set it OFF by hand** (key stored `false`): stays OFF, every update asks as before · ⚙️ ON any time
  - set it ON by hand: unchanged · Obtainium / store installs: no toggle, their job, unchanged
- **Prev:** D-038 (default amended: OFF ➡ ON) · D-023 (silent path) · D-021 (updater).
- **Ck:** kit `UpdateTest.autoDefaultIsOnButAnOffChoiceStays` · `UpdateWorkerTest.autoDefaultOnAndOffChoiceStays` (OFF ➡ NOTIFIED, ON ➡ SILENT via omm Prefs) · fon: next stable on a 0.13.0 browser install ➡ the update after it is quiet with ⚙️ untouched (⏳ todo step 5).

### D-077 @1122 · 🪪 Next to someone's name the app shows their real account id, so a copied name can't pass as another reader · Proposed (🤖 built · 🦠 judges the look, #311)
- **Y:** The name above a comment or circle message travels inside the message (`de.ohmymarx.name`), set by whoever sends it. Anyone with the app's token, or with Element, could post as "strix" and readers could not tell (night F4). The account id is set by the server and cannot be faked.
- **Scope:** others' comments + circle chat: muted `rosa-ab12 · 3 min ago` after the name (our server = the part before `:scagent.de`; another server = the full `@x:matrix.org`) · hidden when the name already is the id (no doubling) · your own messages unchanged (they say "you") · the name itself stays sender-set (history keeps the name used at the time).
- **Alt:** show the server profile name instead (rejected: also user-set, same spoof) · warn when name and id differ (rejected: they always differ, the id carries 4 random hex) · verified badge for the founder (rejected: needs a trust list; later if strangers come, D-073 gate).
- **Ck:** `MatrixTest.handleShowsTheUnspoofableId` · 🦠 on the DEV: a comment by someone else shows the grey id after the name.

### D-076 @1122 · 🗑️ Delete account first deletes every message you sent, because our server ignores the erase flag; the dialog promises only that · Proposed (🤖 built, 🦠 dc #311 "fix + deploy" · 🦠 judges the wording)
- **Ctx:** D-072 relied on `erase: true`, which Synapse honours. The live server is continuwuity (@1113), whose deactivate never reads `erase` (upstream source: `// TODO: Redact all messages`). The dialog said "new readers no longer see your comments", which was false (night F3).
- **Scope:** before deactivating, the app lists the rooms you are in (`/joined_rooms`), pages each one backwards (filter = you) and redacts every own comment, circle message and Amen · `429` ➡ wait as told + retry · any failure or offline ➡ stop before deactivating (account kept, "try again" redacts the rest; deactivating first would lose the token and leave the rest forever) · then the profile name is blanked and the account deactivated · new dialog text says what this cannot reach: copies others already loaded, and circles you left earlier (you can't redact in a room you're no longer in) · 1 new UI string hand-translated into 11 langs (`"by": "hand @1122"`).
- **Alt:** only fix the text (rejected: GDPR Art. 17 wants the content gone, and the app can do it) · ask ops for an admin purge per request (rejected: needs a contact path; manual).
- **Prev:** D-072 (place + flow unchanged; its "Synapse hides your messages" line no longer holds on continuwuity).
- **Ck:** `MatrixTest.redactAllMineWalksEveryRoomAndPage` (2 rooms, 2 pages, 429 retry, others' + already-redacted untouched, unique txn ids, 1 refusal = false) · `check_langs` green · live: `bin/matrix_e2e.py --app` step 12 on the next run.

### D-075 @1122 · 🔁 When the server no longer knows the phone's comment account, the app quietly makes a new one with the same name · Accepted (🤖, bug fix · night #308)
- **Ctx:** ops swapped the homeserver Synapse ➡ continuwuity @1113 (fresh database, `pr/ops/matrix.md`). Every account made before that still sits on the phone with a token the new server never issued ➡ every comment / circle call got 401 and the app only said "failed", forever. The fix was written @1112 on branch `matrix-reregister` (befc620) and never merged.
- **Scope:** once per app run the first Matrix call asks `GET /account/whoami` · 200 ➡ go on · 401 ➡ drop user + token + password (name stays) ➡ register again with that name · no answer / other error ➡ keep the session, ask again next call (a proxy blip never deletes an account) · the stored name is used only after a dead token, so a reader who never commented still gets no account by just reading · circles saved on the phone stay listed (their rooms died with the old server; leaving them is a 🔵).
- **Alt:** branch as written (rejected: it also re-registered from the stored name on a plain read, which would make accounts for people who only typed a name in ⚙️) · tell the user to use ⚙️ Delete account (rejected: not deployed, and nobody would guess it).
- **Ck:** `MatrixTest.tokenValidTellsDeadFromOffline` (200 / 401 / 502) · full unit green · live proof = next DEV on a phone with an old account: a comment posts again.

### D-074 @1121 · 🧹 Eight small open questions are settled by keeping what exists or dropping the extra work · Accepted (🦠 @1121 via M8 "ok all", #307)
- **Ctx:** The 👀 board held 12 reversible questions with a recommendation each (#290). You accepted all of them as written. The four Matrix ones are D-073; the rest are here.
- **Scope:** Element look at the test rooms skipped (`--app` already checks what B reads) ➡ `bin/matrix_e2e.py --cleanup` run · no deep `/omm/dev` · `/omm/src` redirects (bare `/omm` already redirects, Obtainium uses the full URL) · licence stays GPL-3 for app and server (D-006; the server is a tiny counter API, AGPL adds nothing) · no URL printed on the Spruchkarte PNG (the link rides in the share text) · the share-app prompt after a streak/medal is parked in the 🪝 Hooked pass · qa_1117 Q8 gesture line under every page stays (D-065 hints) · qa_1117 Q9 licence icon stays a gavel · no Fable polish pass over the 30 eggs (gallery passed, all arches fit, D-068).
- **Alt:** answer each one separately on the phone (rejected: each was a keep or a drop, and every one is reversible).
- **Ck:** todo 🔵 has no AGPL, Spruchkarte-URL, `/omm` deep-path or Fable-egg line · Hooked pass line names the share prompt · `priv/` has no `matrix_e2e*.json` left.

### D-073 @1121 · 💬 Matrix v1 accepts spam risk, phone-bound accounts and no encryption at friends scale; report + moderation gate any wider sharing · Accepted (🦠 @1121 via M8 "ok all", #307)
- **Y:** v1 is for you and a few friends. Each of these risks is real only with strangers, and each fix is large (rust-sdk for E2E, account recovery, a mod queue). Shipping v1 without them is fine if the step to strangers is blocked until they exist.
- **Scope:** spam: the registration token sits in the public APK, accepted (rate limit / captcha = ops, before a public push) · lost phone = lost account, accepted (rejoin a circle by its code) · no E2E, accepted (the app already says so) · report + moderation: not for friends · before strangers a minimal "Melden" on a comment ➡ Matrix `/report` ➡ ops = 🚧 gate on "send scagent.de/omm beyond friends".
- **Alt:** build E2E / recovery / mod tools first (rejected: months of work for a friends-only test) · keep Matrix off until then (rejected: chat is a v1 goal).
- **Ck:** todo 🔵 "send scagent.de/omm" carries the 🚧 gate line · todo 🏔️ v1 Matrix list has no open ❓.

### D-072 @1121 · 🗑️ You can delete your comment account in ⚙️; it is deactivated and erased on the server, your phone data stays · Proposed (🤖 built · 🦠 judges wording + place)
- **Y:** The app makes a Matrix account with your first comment or circle, but there was no way to remove it: comments and name stayed on scagent.de forever (GDPR Art. 17 right to erasure). Found while listing what Matrix v1 still lacks.
- **Scope:** ⚙️ under the Name field, only once an account exists · danger text button "Delete account" ➡ confirm dialog (Delete / Keep, like "Leave circle") ➡ `Matrix.deactivate` (`erase: true`, password auth with the random pw kept on the phone) ➡ Synapse leaves every room and hides your messages from new joiners ➡ the app forgets the account + its circles · name, notes, bookmarks, stamps stay · next comment = a new account · a circle you founded keeps its code (friends still use it).
- **Alt:** delete only comments, keep the account (rejected: the name + circles stay) · ask ops to delete by hand (rejected: needs an email, not GDPR-friendly).
- **Ck:** `MatrixTest.deactivateErasesWithPasswordAuth` · `bin/matrix_e2e.py --app` step 12 (token dead + left the circle) · 6 UI strings hand-translated into 11 langs (`"by": "hand @1121"` in `data/i18n/*.jsonl`), `check_langs` green.

### D-071 @1121 · 🏷️ The "What bugs you?" chips draw from a second, narrow tag pass; the old tags keep cutting the plans · Accepted (🤖, routine · M8 🔵 sweep)
- **Y:** `score.py` let the model pad every quote to 4 tags, so "Boss" matched 1233 of 1511 quotes and the chip felt random (🔍 @1116).
- **Scope:** `bin/retag.py` = 0-2 topics a paragraph is ABOUT, weight 1-3 ➡ `data/tags.jsonl` (keyed by a hash of the German text) ➡ `build.py` adds `tw` · `marxoskopPool`: weight ≥ 2 · fewer than 30 ➡ weight ≥ 1 · still fewer / no data ➡ the old `t` · `t` itself is untouched, because the plans are cut by it and a re-cut would move days under readers (D-042).
- **Alt:** re-run `score.py` with `maxItems: 2` (rejected: rewrites quotes + juice too and re-cuts plans) · hide chips below 30 (rejected: a chip vanishing reads as a bug).
- **Ck:** `test_bin` `Retag` (3) · `ContentTest.marxoskopPoolPrefersAboutTopicsWithGuard` · after the night run `bin/retag.py --stats` all ✔️.

### D-070 @1121 · 💬 The in-app Matrix path is tested by driving the real app against the live server, run by hand, not in CI · Accepted (🤖, routine · M8 v1 Matrix)
- **Y:** You asked for the taps you would do on the phone (comment ➡ shows on the server ➡ circle found/join ➡ chat both ways) to run without you. `MatrixTest` only knew a fake server, `matrix_e2e.py` only the raw API: neither proved the app's own buttons reach Synapse.
- **Scope:** `MatrixLiveTest` (Robolectric, real `MainActivity`) = app side · the other person = plain HTTP in the test (independent of `Matrix.kt`) · skipped without `-Plive` · `bin/matrix_e2e.py --app` runs it and prints the table · each run's accounts + rooms ➡ `priv/matrix_e2e_app_<stamp>.json` ➡ `--cleanup [MATCH]` (also redacts what a crashed run left in a public chapter room).
- **Alt:** emulator + Espresso (rejected: no KVM on tow) · in CI (rejected: needs the reg token + makes real accounts on every push) · fake server only (rejected: that's what we had).
- **Ck:** `bin/matrix_e2e.py --app` = 11/11 ✔️ (12/12 since D-072 step 12) · `test_bin` `MatrixCleanup` (2 tests, no network · 3 since #305).

### D-069 @1121 · 🧹 Translation junk is fixed by hand in a small fix table, applied when the translations are exported · Accepted (🤖, routine · M8 #248)
- **Y:** The local model sometimes gives up mid-word ("تrier", "колophonе", "keranے") or says its last question twice. Re-running it needs the GPU and may produce new junk, so a hand fix is faster and stays put.
- **Scope:** `data/i18n/hand_fix.json` = per-lang substring fixes (lang `*` = all) · `translate.py export()` applies them + cuts a repeated last sentence · the cache stays raw (a re-translation without the junk = the fix no-ops) · `check_langs` refuses a Latin letter glued to Arabic/Cyrillic/Devanagari/Bengali letters (zh skipped: no spaces) · source junk (DE/EN Andachten) is fixed at the source.
- **Alt:** re-ask the LLM (rejected: GPU, and no guarantee) · edit the cache lines (rejected: lost on the next re-run, no record of what was fixed).
- **Ck:** `test_bin` `test_half_latin_word_is_refused` + `test_hand_fix_and_dup_tail` · `bin/check_langs.py` green.

### D-068 @1120 · 🌈 On a feast day the card's arch holds only the greeting's headline; the rest sits in small lines under the egg · Proposed (🦠 via M8 #246)
- **Y:** You saw the arched title cut off left and right. The full greeting (up to ~100 chars) overflowed the fixed 56° arc, and the spill-over glyphs piled up at both ends like symbols.
- **Scope:** headline = text before the 1st ` — ` · `:` · `!` · `?` · `.` (a date dot like "14. März" does not count) · the arc widens 56° ➡ 100° (ends wrap down beside the egg) and the text shrinks 48 ➡ 24 px · last resort "…" · "Quote of the day" is unchanged · the shared card = the same picture.
- **Alt:** shorter greetings in the data (rejected: 13 langs × 30 feasts to rewrite, and the punchline would go) · arch text only, rest dropped (rejected: loses the joke).
- **Ck:** `Share.archSplit` + `cardBitmap` · `EggArchShotTest` (`everyHeadFits` = all 30 × 13 langs fit · shots `egg_arch_<lang>.png` with `-Pshots`)

### D-067 @1120 · 🔴 A drop cap is placed by baselines: ink top = line 1's cap height, foot = line N's baseline · Accepted (🦠 via M8 #229)
- **Y:** You saw the red letter too small and sitting low. It was centred in a 3-line box, and Literata's tall ascender (1.18 em) pushed the ink down.
- **Scope:** classic rule for both sizes: top flush with line 1's capitals, foot on the last line's baseline (2-line section initial · 3-line chapter initial) · Literata capHeight .70 em is a constant (`CAP_H`, from the font's OS/2 table).
- **Amended @1120 (🦠 #241):** the chapter initial lost its gold box ("rm th bounding box") ➡ a bare red letter like the section ones · the small red ¶ at each 📜 paragraph start is gone too (D-053 b).
- **Prev:** D-053 b (initials) · D-064 (no cap in ar/ur/hi/bn) unchanged.
- **Ck:** `VerseView.DropCapText` · shot `chapter` (ShotsTest, `-Pshots`)

### D-066 @1120 · 🥚 On a feast day Marx turns into the day's egg inside the app: top bar, Today hero and the quote card · Accepted (🦠 qa_1121 1-3), launcher part dropped (🦠 #241 "too silly")
- **Y:** You said colours plus a line are not an Easter egg; "the icon should change". A visible costume change is the surprise people screenshot (Hooked ch7, YouVersion's Christmas push).
- **Ctx:** D-051's small props on the photo head were hard to see at 44 dp.
- **Scope:**
  - 30 cartoon eggs, one per feast, all drawn in `bin/eggs.py` (one source ➡ Android vectors + SVG previews + `img/eggs/gallery.png` + `EggRes.kt`). Playful days get a comic Marx (Todestag = X eyes + tongue out, Halloween = pumpkin with his beard, Christmas = Santa, Manifest = the spectre as a bearded ghost …). The 6 death days of others stay quiet: a symbol, no joke (carnation, candle, rose, hard hat + ribbon, flag over a broken chain, Peterloo banner). Women's Day = ♀ with a red fist.
  - In the app: top-bar head (`MarxHead` ➡ `EggBadge`) · Today hero (`EggPop`: pops in, wiggles, tap = wiggle) · quote card / share PNG (egg replaces the saint + emoji).
  - The launcher icon stays the plain Marx, every day.
- **Amended @1120 (🦠 #241):** DEV 1911 also swapped the launcher icon (30 `activity-alias` entries, switched at 00:01 and when the app left the screen, ⚙️ off-switch). It worked on your phone (qa_1121 2-3 ticked) but you found it "too silly" ➡ aliases, `EggIcon`, the ⚙️ toggle and the 30 adaptive icons are gone, `MainActivity` is the launcher activity again. `launcherReset` in the update receiver re-enables it once, because 1911 could leave it disabled (✂️ after the next stable). Android limits we had found: icons must ship in the APK · launchers redraw after 1-10 s · switching while the app is on screen can close its task.
- **Alt:** a) keep props on the photo (D-051, too small) · b) redraw the photo per feast (raster, 30 × ~80 KB, no comic range) · c) Fable design pass on top of this set (🔵, only if you want more polish) · d) launcher egg too (built, dropped: too silly).
- **Prev:** D-051 (superseded) · D-052 (the 30 days) unchanged.
- **Amended @1121 (🦠 #260 "rm dead Head enum"):** D-051's `Head` enum + `Feast.head` were unused since the eggs ➡ removed from `logic/Feast.kt`. The eggs are the only feast heads now.
- **Amended @1122 (crew, #335):** `launcherReset` removed. Condition met: 2 stables (0.13.0 · 0.13.1) and ~5 DEV builds shipped after 1911, and every self-update ran the reset ➡ no 1911 state is left. Undo = `git revert`.
- **Ck:** `EggResTest` (30 eggs inflate) · `FeastSheetTest.sheet` + `eggCards` · `ShotsTest.zEgg` · [img/eggs/gallery.png](img/eggs/gallery.png)

### D-065 @1120 · ✋ Holding a passage opens its menu, and pages and scroll share every gesture · Accepted (🦠 @1120 via M8 #218)
- **Y:** You found that hold was often read as a tap, and wanted both reading modes to behave the same. Turning is the only thing a tap does; the menu is hold (or double-tap) everywhere.
- **Scope:** 📖 tap = turn (left back, right forward) · hold or double-tap a passage = its menu with a buzz · 📜 the same, except a tap does nothing · no passage is clickable in the reader in either mode, so no ripple. The top bar was already the same in both modes.
- **Prev:** D-057 (hold = turn). D-057 stays for tap = turn.
- **Ck:** `ReaderGestureTest.holdOpensMenuInBothModes` · `tapTurnsDoubleTapOpensMenu`

### D-064 @1120 · Arabic, Urdu, Hindi and Bengali get no drop cap; Chinese keeps its initial box · Accepted (crew @1120)
- **Y:** The Arabic screenshot showed a cut-off, unjoined first letter: these scripts join or stack letters, so pulling one out breaks the word. Chinese characters stand alone, so the box works there.
- **Scope:** Chapter and section initials only. Headpiece, incipit, seal and ribbon stay in every language. Decided by crew during M8 #206.
- **Prev:** design § 14.3 said "hi · bn = initial box with first glyph"; that breaks conjuncts the same way.
- **Ck:** `ShotsTest.zLangs` (chapter_ar · chapter_zh) · `VerseView.kt DropCapText` `splitBreaks`

### D-063 @1120 · 🎖️ All 7 rank perks exist, so the promotion screen shows its perk line · Accepted (crew @1120)
- **Y:** The perks table in D-050 was a promise with no code behind it. Hiding the line was honest but empty; building the perks keeps the promise.
- **Scope:** Each level gives: 1 gold rim on the hero · 2 stamp card turns blue cloth · 3 opens the "Marx Basics" plan (new lock source `rank`, D-043) · 4 your rank title next to "you" in Circles (your screen only, nothing is sent) · 5 gear rim around the star · 6 your name in the book's colophon · 7 the star turns gold. Level 7 also turns the ☭ swoosh gold (`CelebrationQueue.gold`, added later on 20 Nov). Built during M8 #206 (autonomous).
- **Ck:** `PlanUnlockTest.rankPerk_opensAtLevel3` · `MomentsShotsTest.rank3` · `Moments.kt PERKS_LIVE`

### D-062 @1120 · ⏱️ Reading time counts only while you touch the reader; a long sitting counts in full · Accepted (🦠 qa_1120 r2 via M8 #229)
- **Y:** You said "a long session should count, but only if really reading; no input = nothing happening". Input is the honest signal, a per-page cap is not.
- **Options:**
  - **a) 15 s ticks while the reader is open and was touched ≤ 90 s ago, no cap, plus 🌙 Überstunden badge = 1 sitting with ≥ 60 active minutes** (rec, built by crew)
  - b) keep D-059 (≥ 10 s, ≤ 5 min per page)
  - c) a only, no sitting badge
- **Cost:** A slow reader who doesn't touch the screen for more than 90 s on one page loses that time.
- **Prev:** D-059 (page cap rule).
- **Ck:** `OrdenTest.readingTimeMedals` (`activeSecs`) · ChapterScreen tick loop (`repeatOnLifecycle(RESUMED)`) · `Orden.LONGSHIFT`

### D-061 @1120 · My Capital shows a level with its badge, not a second stamp card · Accepted (🦠 qa_1120 r2 via M8 #229)
- **Y:** You said "rank isn't another stamp card, too much: a rank with a nice symbol, maybe a level". Stamps already live in the Stempeluhr pane.
- **Options:**
  - **a) badge 56 + "LEVEL N" + rank name + 1 thin gold bar + "N days to level N+1"** (rec, built by crew)
  - b) badge and name only
  - c) keep the stamp row
- **Ck:** `MineScreen` stamp-card block · your eye on the phone

### D-060 @1120 · Promotion pops a gold "LEVEL N ▲" with an XP bar; the red GENEHMIGT stamp is gone · Accepted (🦠 qa_1120 r2 via M8 #229)
- **Y:** You said "GENEHMIGT feels bureaucratic, make it more level-up, gamify". A level number and a filling bar are the game language people know.
- **Options:**
  - **a) gold LEVEL N ▲ pill (overshoot pop) + XP bar from old to new level, "RANK N/7" line dropped** (rec, built by crew)
  - b) keep the stamp, say "BEFÖRDERT!"
  - c) arcade "RANK UP!" banner across the screen
- **Prev:** D-050 (the GENEHMIGT seal).
- **Ck:** `MomentsShotsTest.rank3` (LEVEL 3, no GENEHMIGT) · shot `moment_rank_3`

### D-059 @1120 · ⏱️ The "long in the app" award measures time in the book, not time the app is open · Accepted (🦠 qa_1120 r2 via M8 #229), time rule superseded by D-062
- **Y:** The chapter 8 frame (the 8-hour day had to be fought for) is a wink and honest. Counting total time, not one sitting, avoids binge pressure; counting book time, not app time, can't be farmed by leaving the app open.
- **Ctx:** You asked for an award "for staying in the app a long time" (via M8 #185). D-014 earned-only · D-004 no anxiety · Hooked ch. 6 (facilitator, not dealer).
- **Options:**
  - a) 1 long session (≥ N minutes at once): rejected, rewards bingeing
  - **b) total active reading time in the 📖 book, 2 tiers** (rec, built by crew)
  - c) daily minutes streak: rejected, a second streak = anxiety
  - d) skip: rejected, you asked for it
- **Scope:** ⏱️ **Normalarbeitstag** = 8 h total · 🏭 **Arbeitswoche** = 40 h total · group READ. The original rule (a page counts ≥ 10 s, capped at 5 min, a paused app stops the clock) was replaced by D-062.
- **Ck:** `OrdenTest.readingTimeMedals` · `Prefs.readSecs` · ChapterScreen timer
- **Note:** @1120 (M8 #185): D-048, 049, 050, 051, 052, 053 and 054 were built on the crew's pick (lanes C2-C5 + C7) and stay Proposed until you evaluate them on the phone. Gaps at that time (🔵 todo): perk effects · 12 heads still emoji tokens · Share/Widget head · illuminated + section initials · 📜 pilcrow (most closed later, see D-063).

### D-058 @1120 · 🖨️ Book text is set in Literata, justified, with automatic hyphenation · Accepted (🦠 @1120 via M8)
- **Y:** You wanted text that looks like a printed book ("block-justified like a printed book + a good book font, hyphenation DE/EN so justify doesn't gap"). Literata is made for long reading on screens (large x-height, sturdy at 17sp), and hyphenation in the text's own language keeps justified lines from leaving rivers on a 360dp column.
- **Scope:** All serif text (`BookSerif`): body justified, auto hyphens, paragraph line breaker, hyphenation follows the shown language. Literata covers Latin, Cyrillic (ru) and Greek; Chinese, Arabic, Hindi, Bengali and Urdu fall back to the system font glyph by glyph. Drop cap: the 3-line block clips the whole paragraph, so line 3 stays justified and keeps its hyphen. Licence (OFL 1.1) in `assets/licenses/Literata-OFL.txt`.
- **Cost:** First +1.86 MB APK (2 variable TTFs, not subset). Since @1120 #218 (you OK'd fonttools): 5 static styles, Latin + Greek + Cyrillic = 1.07 MB (−748 KB, not −1.3 MB, because 5 styles are kept) · `bin/font_subset.sh`.
- **Alt:** EB Garamond (small x-height, thin at 17sp on a phone; more "old" but less readable) · Source Serif 4 · system Noto Serif (the old look, D-033 a).
- **Prev:** D-033 b (you said "try b" @1114: Literata vs. EB Garamond, never built).
- **Ck:** shot `chapter` (justified, "im-" | "mense" at the drop cap) · `OmmType.book*` uses `BookSerif`

### D-057 @1120 · 📖 In the book, a tap turns the page and double-tap opens the passage menu · Accepted (🦠 @1120 via M8), hold part superseded by D-065
- **Y:** Turning pages is 90 % of what you do there, so it gets the cheapest gesture, like other e-readers. The menu is rare, so a double-tap is fine.
- **Ctx:** D-044 had double-tap = turn and tap = menu. In your evaluation of DEV 0009 you said: swap.
- **Scope:** Book view only: tap or hold anywhere (passage or paper) turns, the side comes from the finger position, the whole half counts (the edge-quarter rule is gone). Double-tap a passage = menu with a buzz. Scroll view and Today have no page turn, so tap and hold still open the menu there. The hints (folio line + 📖 chip pill) describe this. Hold was changed to "menu" by D-065.
- **Alt:** Hold opens the menu (rejected at the time: you said "single tap / hold = turn").
- **Prev:** D-044.
- **Ck:** `ReaderGestureTest` (double-tap = "Note" · tap right = next page) · `ShotsTest` verse_menu via double-tap

### D-056 @1119 · Plans with a promise are hand-picked, and each Andacht is found by its verse ids · Accepted (🦠 qa_1117 r1 via M8 #155)
- **Y:** A generated pick is fine for "juicy verse per day" plans, but wrong for a plan with a promise (the Bible plan). Re-ordered days keep their Andachten, and hand-written Andachten survive `andacht.py --redo`.
- **Ctx:** You asked in r1 "do the plans build on each other?". The re-evaluation found: bibel used the score.py `bib` flag, so 7 of 10 days had no Bible at all · 30tage had chapter 13 before 10-12 and 23 before 22 (Marx's ladder backwards) · montag day 1 was 1,27 (coat and linen, not Monday).
- **Scope:** bibel v2 = 10 hand-picked references (Lamb of God, Revelation, Bible for linen, Paradise, Eden, Jehovah, Moses, original sin, Holy Ghost) · 30tage v2 and montag v2 (no chapters 1-3) · all via D-042 · Andacht lookup = (plan, verse ids), not the day index · `data/andachten_hand.jsonl` beats andacht.py's LLM cache.
- **Alt:** Tune the `bib` regex (rejected: "Gott" and "heilig" hit everywhere) · LLM Andachten for new bibel days (rejected: quality, todo "Heute lädt uns Marx ein").
- **Ck:** `test_bin.py PlanContent` (5 · 6 @1122) · translate `--only a` re-run (Andacht translations are keyed plan:idx)

### D-055 @1119 · ★ The close button is a bare star (V2) · Accepted (🦠 @1120 via M8)
- **Y:** You said in r1 "many versions, different contexts", so the crew rendered the options instead of arguing, and you picked by eye.
- **Options:**
  - V1 the old one (★ in a tinted circle)
  - **V2 bare ★** (picked, now the `CloseStar` default)
  - V3 mono ✕: rejected, it loses ★ = close (§ 1)
  - V4 ★ with a ✕ cut-out (crew's eye-guess: brand + new users read "close")
  - V5 inverted red disc · V6 outline ★ · V7 wax seal · V8 V1 + spin on press
- **Ck:** `close_sheet.png` (8 variants × 3 contexts + dark) · lane C6, no gate
- **Note:** D-045 to D-055 (@1119) are the design addendum (qa_1117 D6-D11 + r4), all decided by you with the crew's pick marked. Spec, mockups and lanes C1-C7: [design_system.md](design_system.md) § 14 (§ 14.0 = the same list as a table).

### D-054 @1119 · ✍️ A note is a margin-comment card, one component for notes and comments · Accepted (🦠 qa_1120 r2 via M8 #229)
- **Y:** Comment cards are what other apps do (your r2 note), and one card can serve both notes and comments.
- **Ctx:** qa D7 · Q4.
- **Options:**
  - a) yellow sticky + 📌: rejected, yellow is the Hooked voice (§ 7)
  - **b) comment card: paperHi + blue ruler + tail to the verse + initial disc + name/time + ✎** (rec, built by crew in C5)
  - c) keep the ruler, new background only: too close to "a blue quote", which you disliked
- **Ck:** shots `note_card` `note_edit` `comments` (C5) · no ★ in the editor · tap away = save, menu stays

### D-053 @1119 · 📜 The book gets the old-book devices that are worth their effort · Accepted (🦠 qa_1120 r2 via M8 #229)
- **Y:** Option b gives the most "worthy old book" per effort, and everything is drawn (no raster images).
- **Ctx:** qa D11.
- **Options:**
  - a) ⚡ set only (section initials · red ¶ · incipit · headpiece · running head · catchword · colophon)
  - **b) ⚡ + 🟡 (illuminated initial · tailpiece + chapter seal · ribbon bookmark · gilt edge)** (rec, built by crew in C4)
  - c) + 🧪 paper grain and margin glosses: rejected, costs performance and clashes with notes
- **Scope:** No margin ¶ numbers (r3 removed the ¶ reference line). The catchword was removed after your r1 note ("why? confusing"). @1120 #241: the illuminated initial's gold box and the red ¶ before each 📜 paragraph are gone too (see D-067).
- **Ck:** shots `chapter` `chapter_scroll` `chapter_end_seal` `bookmark_ribbon` + rtl + zh (C4)

### D-052 @1119 · 🎉 The feast calendar gets 18 more days, so no gap is longer than about a month · Accepted (🦠 qa_1120 r2 via M8 #229)
- **Y:** 5 May to 14 September had 4 months with nothing. With all 18 the longest gap is about 4 weeks. Fun days are a variable reward (your r3 note); quiet († days) get no confetti.
- **Ctx:** R4 gaps.
- **Options:**
  - **a) all 18** (labour history + 3 fun days, § 14.2.3) (rec, built by crew in C3)
  - b) history only
  - c) you pick on the sheet
- **Ck:** `Feast.on()` test per date · `feast_sheet.png` (C3)

### D-051 @1119 · 🧔 The holiday Marx is one base head plus overlay props · Superseded by D-066 (props too small 4 a "wow", 🦠 @1120)
- **Y:** One style, cheap per feast, no drift. Option c was "half-baked" (your r3 note).
- **Ctx:** qa D8 · Q6.
- **Options:**
  - **a) 1 base head + overlay layers** (rec, built by crew in C3)
  - b) a full redraw per feast, only for 🎄 Santa-Marx · 🎃 pumpkin-Marx · 🎂
  - c) emoji next to the head (the old way)
- **Ck:** `ui/ds/MarxHead.kt` = the only place that picks a head · top bar + share card + widget call it

### D-050 @1119 · 🎖️ A rank-up is a fullscreen "Beförderung", like a game level-up · Accepted (🦠 qa_1120 r2 via M8 #229), seal part superseded by D-060
- **Y:** You asked for "promotion · hero upgrade · MMORPG level-up" (r4). It happens about every 10 reading days, rare enough for a big moment (D-034). Perks are cosmetic, which keeps it honest (D-004).
- **Options:**
  - **a) badge flies in · Marx seals "GENEHMIGT" · rays · 1 perk line · share** (rec, built by crew in C2; the seal was replaced by D-060)
  - b) bigger dialog only
  - plus: cosmetic perks per rank · rank 3 as a new D-043 unlock source (both built, D-063)
- **Ck:** shot `moment_rank_3` · rays stop at 2300 ms · reduced motion = end frame (C2)

### D-049 @1119 · Celebrations form one ladder and show one big screen per tap · Accepted (🦠 qa_1120 r2 via M8 #229)
- **Y:** Your r4 note: "Amen, then award, then ☭, in series". Size follows rarity (D-034), and there are no walls of dialogs.
- **Scope:** T0 press · T1 ✓ · T2 ☭ + Stempeluhr · T2b chapter seal · T3 milestone · T4 Beförderung · T5 reward dialogs · the queue follows that order · 2 or more due = "Moment … ▸" · more than 2 dialogs = "+N" chip. Built as written; you accept or change it.
- **Ck:** `FxQueueTest`: 1 big screen per tap (C2)

### D-048 @1119 · ⏱️ "Day complete" is the Stempeluhr: a stamp, one line and a teasing question · Accepted (🦠 qa_1120 r2 via M8 #229)
- **Y:** Your r4 note: "bland · text-stuffed · Moses? · not teasing". So: image + motion + 1 line + 1 question, and "Losung kommt um" goes (Settings already has it).
- **Ctx:** qa D10 · Q11.
- **Options:**
  - **a) stamp thump + star count** (rec)
  - **b) 1 punchy line from a pool** (rec)
  - **c) a teasing question for tomorrow (new `qd`/`qe`)** (rec; a + b + c built by crew in C2 + C7)
  - d) Marx head reacts (🧪 after C3)
- **Ck:** shots `moment_shift` `moment_shift_full` (C2) · all plans exc. `jahr` have `qd` + `qe` (C7 · `test_bin` skips jahr)

### D-047 @1119 · 📉 After a break the star tier falls one step per missed day ("Abschreibung") · Accepted (🦠 @1120 via M8 #185)
- **Y:** Fixed capital depreciates, it doesn't vanish: kind, but there are still stakes.
- **Options:**
  - a) back to 0: rejected, anxiety (D-004)
  - b) best ever: rejected, no stakes
  - **c) 1 tier down per missed day** (picked)
- **Ck:** `StreakGameTest`: tier falls 1 per day (C1) · built in `logic/Daily.kt streakGame` (forward replay of the DayLog, no Prefs state)

### D-046 @1119 · ✊ Streak milestones earn a Streiktag, and the Sabbath joker stays · Accepted (🦠 @1120 via M8 #185)
- **Y:** The Duolingo set (R3) plus 200 (to close the 100 to 365 gap). Earned only, no shop (D-014). The Sabbath is the weekly grace; a Streiktag is an earned buffer, used after the Sabbath.
- **Options:**
  - steps 7 · 14 · 30 · 50 · 100 · 200 · 365 · then every +100 · 1 freeze per step, max 2 (picked)
  - name: a) ❄️ Freeze · **b) ✊ Streiktag** (picked) · c) 🛟
  - Sabbath: **keep** (picked) or fold in
- **Scope:** Built in C1 (@1120): ✊ is earned only on milestone days from 2026-11-20 on (no retro bridges). Lazy Sabbath: a second 1-day gap in a week uses a ✊ if one is held, otherwise the break moves to the earlier gap (the same result as the old backward walk; checked on 300 random histories in `oldStreaksUnchanged`).
- **Ck:** `StreakGameTest`: earn · cap 2 · use order (C1)

### D-045 @1119 · The hero shows only the streak; stamps and rank move to the Stempeluhr and My Capital · Accepted (🦠 @1120 via M8 #185)
- **Y:** Two worlds, two places: the streak is the hero; the career (total days, then ranks) is the stamp card. Hero size and look stay as D-036 A; only the stamp row leaves.
- **Ctx:** qa Q1 ("1 score").
- **Options:**
  - a) drop the gift stamps
  - b) caption each counter (tried @1116, still confusing in r3)
  - **c) hero = streak only (star + week + next-milestone bar), stamps + rank go to the Stempeluhr pane and My Capital** (picked)
- **Prev:** D-036 A (amended).
- **Ck:** shot `hero_tiers` · stamp card in `mine` (C2 · amended by D-061: mine = LEVEL + bar, stamps i th Stempeluhr)

### D-044 @1119 · 📖 Double-tap turned the page by side (left back, right forward) · Superseded by D-057
- **Y:** It matched the one-tap edge zones (left back, right forward), so one mental model. No always-on peek (the D-022 pill was removed @1116 as distracting).
- **Ctx:** You said "read book: add hint, double tap = next/prev page". D-022 had double-tap = always forward. Accepted by you @1119 (scrap).
- **Scope:** The side came from the x of the double-tap (paper) or from the last finger-down on the book (passage; VerseView has no position). Hint = folio line + 📖 chip pill. Swapped @1120 by D-057 (tap = turn, double-tap = menu).
- **Alt:** Double-tap always forward + left-quarter tap for back (rejected: you asked for previous too) · hint pill on every chapter open (rejected: distracting).
- **Prev:** D-022 (amended).
- **Ck:** 👀 qa_1117 L9

### D-043 @1118 · 🔒 New reading plans unlock step by step, as bonus levels, not a paywall · Accepted (🦠 qa_1117 Q10 and Q12)
- **Y:** Hooked variable reward plus progress gates (ch. 4). Using the best-ever streak means a broken chain never re-locks a plan (D-014 kindness); feast and lucky unlocks are stored and never taken back.
- **Ctx:** r4: "more plans, unlocked later (+ DEV)". Q12, your words: "timeline: 3 awards or 1-3-5 plans or streak 20 · like a bonus easter egg = random reward · + 1 holiday/xmas present".
- **Scope:** `Plan.lock` = any one of {plans · orden · streak (best ever) · feast gift · lucky 1-in-5 per Amen day after N days} (rank was added by D-063). A locked plan is a 🔒 mystery card at the end (no title, only how it opens). Opening it shows a 🎁 dialog in the reward queue. The DEV switch opens all, without fanfare.
  - 6 plans: 💰 reich (1 plan) · 🎓 grundkurs (3 badges) · 🏛️ faschismus (3 plans, bridges only — Vol. 1 never names it) · 🛵 gig (🎲 lucky after 10 days) · 🌍 erde (streak 20) · 🎁 weihnachten (Christmas gift, 3 days)
- **Alt:** All open (rejected: no discovery) · fixed path (rejected: less surprise) · paid unlock (never).
- **Ck:** `logic/PlanUnlock.kt` · `PlanUnlockTest` · `data/plans_extra.json` `lock`

### D-042 @1118 · 🏛️ Each reader stays on the plan cut they started; every shipped cut is archived · Accepted (🦠 qa_1117 "do the plan versioning")
- **Y:** Bumping automatically means nobody can forget it, and people in the middle of a plan never see a different day.
- **Ctx:** DayLog stores plan days by index, so any re-cut (R1 🟡 wertform) would have shifted everyone mid-plan.
- **Scope:** `data/plan_versions.json` = every cut ever shipped (frozen days incl. Andacht) · build.py bumps `ver` automatically when a plan's verse ids change · plans.json ships the `old` cuts · `Prefs.planVer` is set at start (null = v1) · an old cut skips index-keyed translations (EN fallback).
  - First use: wertform v2 = cut at Marx's sub-section heads (8 days, none starts mid-argument) · jahr text "~10 min" became "~5 min" (text only, no re-cut).
- **Alt:** Migrate DayLogs by verse id (rejected: fragile) · freeze plans forever (rejected: no fixes).
- **Effects:** Plans can be fixed and re-cut freely. Every change to a plan's verse ids must commit `data/plan_versions.json` with it (test_bin fails otherwise).
- **Lock-in:** Readers' progress is stored as (plan, version, day index); dropping the archive would move people to the wrong day.
- **Ck:** `bin/build.py versions()` · `test_bin.PlanVersions` (incl. archive vs. plans.json) · `PlanUnlockTest.daysFor`

### D-041 @1116 · There is no Werkbank tab: Circles stays, and the Hooked entry stays in the top bar · Accepted (🦠 qa_1116 Q8 revised)
- **Y:** 6 tabs are too many (Material 3 maximum is 5). Circles comes alive once Matrix is tested (later). A 🪝 at the top is a bit odd, but accepted.
- **Ctx:** qa_1116 D5 proposed a "Werkbank" bottom tab (Hooked notes + tools). Your first Q8 answer was b (replace Circle); you revised it the same day.
- **Scope:** 5 tabs stay (Today · Plans · Read · Circle · My Capital).
- **Alt:** Werkbank replaces Circle (rejected: Circles has a future) · a 6th tab (rejected: crowded).

### D-040 @1116 · 📖📜 The reader has two modes, pages and endless scroll, and the feed left Home · Accepted (🦠 qa_1116 Q4 and Q7)
- **Y:** You want both tried before picking one design (Fable spec after that round).
- **Ctx:** The endless feed lived at the end of Home (D-024), which fought the "daily ≤ 2 min" goal. Your r2: Read = 2 modes, Home ends at the Marxoskop.
- **Scope:** Toggle chip in the chapter's slim head (D-031 b) · remembers the last mode (Prefs `read_scroll`) · Home has no feed tail.
- **Alt:** A Settings toggle · a choice on first open (rejected, Q7 a).
- **Ck:** `ShotsTest.zFeed` (`chapter_scroll`) · `ReadScreen.kt` ChapterScreen
- **Note:** 📜 was `ReadOnFeed` from the current page; since @1120 (#200) it is `ScrollBook`, a lazy list over the whole book that scrolls both ways and keeps the page when you switch modes.

### D-039 @1116 · ☭ The swoosh marks a finished plan day; the quote Amen gets a small ✓ pop · Accepted (🦠 Q9 b)
- **Y:** The plan day is the real effort, so it gets the big reward; the quote tap is cheap, so it stays small.
- **Ctx:** D-034 b (swoosh = rare) plus QA r2 `4708ab7` turned "Day complete!" into a ✓ pop, so the hammer and sickle never showed (a bug). Your words: "finish the day's read plan, then boom".
- **Scope:** "Day complete!" = ☭ swoosh (full stamp card = confetti instead) · quote-of-the-day Amen = ✓ pop (full card = confetti).
- **Prev:** D-034 (amended).
- **Ck:** `TodayScreen.kt` `amen` + plan Button

### D-038 @1114 · 🆕 Updates are announced at once; browser installs ask first, with an opt-in to auto-install · Accepted (🦠 @1114); default OFF superseded by D-078 (ON, @1122), back 2 OFF by D-083 (@1126)
- **Y:** You see the update and decide (consent). Obtainium users: their own background update may still win, and then our ping is moot (same version, no re-nag).
- **Ctx:** You deployed 1114 and DEV (1113, Obtainium) showed nothing ("app doesnt show UPDATE popup"). The Obtainium path waited 8 h (GRACE_H) and the worker ran about daily, so nothing was visible for hours.
- **Scope:** Your words "do notification immediately": GRACE_H dropped, plus one OneTime check on every app start (WorkManager, network) next to the daily one. Your words "browser apk selfupdate should ask user … or they select autoupdate in settings": `Settings.autoUpdate` default OFF = notification, tap = UpdateDialog (1 confirm) · ON (Settings row, browser installs only) + Android 12+ + we are the installer = silent (D-023 path).
- **Alt:** Keep the grace period (rejected: you couldn't see it work) · silent by default (rejected: you want to be asked first).
- **Prev:** D-023 (amended).
- **Ck:** `UpdateWorkerTest` (obtainiumNotifiesAtOnce · autoOffAsksEvenIfWeCouldGoSilent · weInstalledOurselvesIsSilent with auto) · phone: deploy B, notification within about 1 min of opening the app

### D-037 @1114 · The omm-api image ships without pip · Accepted (@1114)
- **Y:** No pip means nothing to patch, for good. The app is stdlib only and never calls pip.
- **Ctx:** The first nightly live sweep (run 935, 03:15) failed: trivy = 0, the red one was **pip-audit**. The base `python:3.12-alpine` bundles pip 25.0.1 with 6 advisories (PYSEC-2026-1795 · 1796 · 2875 · 2876 · 196 · 3721, fixed in ≤ 26.2).
- **Scope:** `server/Dockerfile` uninstalls pip, removes `ensurepip` (the same pip as a wheel) and has a build-time guard (`! import pip`). The nightly caller keeps `pip: true` (the template skips with a warning when there is no pip, so the audit comes back by itself if dependencies ever land).
- **Alt:** `pip install -U pip` (rejected: needs PyPI at build time and goes red again on the next pip advisory) · `pip: false` in nightly.yml (rejected: hides the finding, pip stays in the image).
- **Ck:** local build: `pip` not found · `/v1/health` ok · trivy 0.72.0 (CI flags) exit 0 · CI canary green · live after ops rebuilds `/srv/omm-api` (ops-owned, SERVICE.md)

### D-036 @1113 · Today keeps its original hero (version A) · Accepted (🦠 @1116, A)
- **Y:** You chose "A (smaller)" after trying A and B on one DEV build.
- **Ctx:** You said "landing should fit verse + streak widget + read-plan button"; the hero (star 92 + week row + stamp row + rank) plus the card pushed the plan entry below the fold. @1114 you asked "which is which? show me" (mockups `d036_all.png` a · b · c). @1115 (via M8): "as it was before, or bigger", so compact (a) and collapse (c) were out, and A/B went on one DEV APK for friends (⚙️ "🧪 Today hero", DEV only: A = pre-ds hero, pixel-same · B = bigger: star 124, headline 30, week + stamps fill the width; shots `priv/img/d036/hero_ab.png`).
- **Options:**
  - a) compact hero 120dp (star 48 + 7 day dots + Tag "☭ 3/10"), full stamp card in My Capital (crew's first pick: the only one that fits both screen sizes by budget, 412×915: 599 of 730 · 360×760: 542 of 592, and keeps the card big)
  - b) keep the hero, shrink the quote card (rejected: a smaller quote = worse share image match)
  - c) hero collapses on scroll (rejected: nothing fits before the first scroll)
  - **A** = the pre-ds hero (picked @1116 via M8 after #101)
- **Scope:** A is the only hero; the toggle, B, Prefs `hero_ab` and `ShotsTest.hero` were dropped (git: 384cbab). L1 Today unblocked.
- **Ck:** `ShotsTest.hero` (hero_a + hero_b, removed after the pick) · Prefs key `hero_ab` (old abc picks don't leak)
- **Note:** D-029 to D-036 (@1113) are the design system (you decide, crew pick = first option). Spec: [design_system.md](design_system.md) § 0 has the same list as a table. @1114: D-029 + D-030 accepted, so L0 was built (`ui/ds/`) · D-031 to D-035 accepted by you @1114 · D-036 = A by you @1116.

### D-035 @1113 · 🔊 Three tiny sounds exist, off by default · Accepted (🦠 @1114, b, "sure lez try", lane L6) · not built yet (L6, no `res/raw` @1122)
- **Y:** Haptics come first: 90 % of the feel for zero assets. Sound is opt-in (reading in bed or on a train); a surprise noise means an uninstall.
- **Ctx:** qa R1: "Duolingo-satisfying".
- **Options:**
  - a) none, haptics only
  - **b) page rustle · stamp thud · badge chime (≤ 300 ms), 1 Settings switch, default off, follows the ringer** (picked, later lane L6)
  - c) on by default (rejected: surprise noise)
- **Ck:** Settings switch · no audio focus taken · APK grows < 100 KB

### D-034 @1113 · The daily Amen gets a ✓ pop; the sickle swoosh is rare · Accepted (🦠 @1114, b)
- **Y:** A variable reward (Hooked) needs rare to differ from daily. D-004 calm. Your QA: you want it satisfying, not slow.
- **Ctx:** The 2.3 s swoosh every day plus the Orden dialog after a fixed 2600 ms wait made the daily tap feel long, and rewards collided.
- **Options:**
  - a) as it was
  - **b) daily = CheckPop 1.2 s + 📳 · swoosh + confetti only on rank-up, full stamp card or plan done · 1 celebration at a time (queue)** (picked)
- **Scope:** Your words @1114: "lez try b. not everyday (variable reward!!!), add to hooked mode". So there is one HookNote (Hooked mode on) at the rare swoosh: "this one is rare on purpose = variable reward (Hooked ch 4)". Lanes L5 (Fx queue) + L1 (Hooks.kt).
- **Ck:** `Fx.kt` queue · test: 2 rewards in the same tap are shown one after the other, no fixed delay

### D-033 @1113 · The book font was the system serif, with a bundled face as a later trial · Superseded by D-058
- **Y:** 13 languages incl. ar · hi · bn · ur · zh: a bundled face covers 3-4 of them and the rest falls back, which gives 2 looks. The APK was already 29.7 of about 30 MB. Option b was a trial for after the system font landed (a 1-token swap).
- **Options:**
  - **a) system serif (Noto Serif)** (crew pick)
  - b) bundle Literata or EB Garamond (OFL, +0.3-1 MB, Latin + Cyrillic only)
- **Scope:** Your words @1114: "try b! maybe ab-test / compare". Plan: L6 bundles one face (Literata vs EB Garamond), side-by-side shots `chapter` ×3 (system · Literata · Garamond) for you to pick, then maybe a Settings toggle as a self A/B; non-Latin languages keep the system serif. Accepted as b 🧪 @1114; built as Literata in D-058.
- **Ck:** (at the time) no `res/font` · `OmmType.book*` = `FontFamily.Serif`

### D-032 @1113 · 🌙 A dark theme "Nachtschicht" exists as tokens, follows the system, with a Settings toggle · Accepted (🦠 @1114, c "C!") · wiring not built yet (L6: `MarxDarkScheme` unused @1122)
- **Y:** Night readers are real (a NIGHT hero already exists). Tokens cost nothing now and keep hard-coded colours out (guard). Wiring needs its own QA round (shots ×2), so not in the first pass.
- **Options:**
  - a) light only
  - b) follow the system (crew pick, L6)
  - **c) b + Settings toggle (light · dark · system), default system** (picked, lane L6)
- **Ck:** `Tokens.kt` has both schemes · `Theme.kt` uses light until L6

### D-031 @1113 · 📖 In a chapter the reader has a slim paper head and no bottom navigation · Accepted (🦠 @1114, b as a trial)
- **Y:** About 100dp more page, the page becomes the screen, and the exit stays visible.
- **Ctx:** qa D2 "book look: old · worthy · holy". The red top bar and lavender navigation framed the page as an app, not a book. Your words @1114: "not sure which is which · try b · but immersive maybe", so b was built in L2, c stays 🧪 (mockups `d031_all.png` a · b · c), re-ask after phone QA.
- **Options:**
  - a) as it was
  - **b) slim paper head (back · chapter · 💬 N), bottom navigation hidden, status bar = page colour** (picked)
  - c) full immersive, tap = toggle chrome (rejected: tap is taken by the verse menu and the double-tap turn of D-022, and the exit would hide)
- **Ck:** shot `chapter`: no NavigationBar · back works from a deep link

### D-030 @1113 · There are 5 button kinds, tonal pills with a press edge on the one big button · Accepted (🦠 @1114, A+)
- **Y:** Round means tappable. The press edge + 📳 makes the daily tap satisfying (qa R1) without making every button loud.
- **Ctx:** You called the old buttons "clunky-generic-microsoft98style" and wanted 2-3 mockups. r4 sweep #7: 7 button looks mixed. You picked by eye from the rendered gallery (`62be257`: now · A · A+ · B · C).
- **Options:** (mockups in design_system.md § 5)
  - **A+ = A pill tonal + B's 4dp press edge on Primary-L only** (picked; crew pick)
  - A pure (calm, but the one daily button felt flat)
  - B letterpress everywhere (loud, and r8 breaks round = tappable)
  - C rubric (text + red rule; rejected: weak affordance for the daily Amen)
- **Scope:** Primary (red pill, L 52 / S 40, max 1 per view) · Tonal (blueTint pill) · Text · ActionItem (icon + label, Circle / Bare) · Chip (Choice / Suggest) + singletons (★ · Switch · MoreChip · CountChip · DogEar) · no outlined buttons, no emoji in labels.
- **Ck:** L0 @1114: `ui/ds/Buttons.kt` (Primary L/S · Tonal · TextBtn · ActionItem · singletons) + `Chips.kt` · `ds_gallery` shot · `DesignGuardTest`: M3 `Button(` / `OutlinedButton(` / `TextButton(` / `AssistChip(` / `FilterChip(` outside `ui/ds/` = baseline, down to 0 by fan-in

### D-029 @1113 · 🎨 The design system has one source of truth, tokens in ui/ds/, and a ratchet guard · Accepted (🦠 @1114, as written)
- **Y:** Every rule answers a QA line (qa D2 book look · D6 voices · D7 buttons · r4 #7) or a measured failure (gold text contrast 1.6 · 5 of 9 plan accents < 4.5 · `Color.Gray` 3.1).
- **Ctx:** M8 order (from you): one spec for buttons · navigation · type · colour · space · components · motion. Before: 21 text sizes · about 60 hard-coded colours · 9 radii · `Theme.kt` filled 8 M3 roles (the rest = lavender defaults).
- **Scope:** Spec = [design_system.md](design_system.md). Rules: 3 materials (paper · ink · cloth-blue) + red = act · gold = earned · round = tappable, rectangle = container · serif = Marx, sans = app + people · chrome = vector icons, emoji = content · flat (only the quote card lifts) · 9 text sizes, sentences ≥ 14sp · 4dp grid · 5 radii · 5 voices (**my bubbles are blue, not gold**) · plan accents = stripe / disc / tag only, titles ink. The guard also counts `TextButton(` (the spec listed 4 M3 buttons; `TextButton` is the same drift, ds `TextBtn` replaces it); the ratchet is strict both ways: a lower count must lower the baseline in the same commit.
- **Alt:** Restyle screen by screen without tokens (rejected: it drifts again, which is how we got here) · a ready M3 theme builder palette (rejected: loses paper + rubric).
- **Ck:** `DesignGuardTest` baseline only goes down (L0 start @1114: `Color(0x` 48 · `.sp` 174 · `RoundedCornerShape(` 33 · buttons/chips 48) · lanes L0-L5 in design_system.md § 12 · ⚠️ overlaps D-015 (Amen to snippet bottom, unbuilt) = lane L1

### D-028 @1113 · 🏛️ Verse ids may move; a recorded map moves pipeline data and users' marks with them · Accepted (@1113)
- **Y:** Fix the data at its source, not with a display hack. The German text is the stable identity across splits.
- **Ctx:** M8 follow-up (you said "Go"): a typo in the German source ("FÜFNZEHNTES KAPITEL") put the opening of chapter 15 into chapter 14 (14,37-39), and chapter 15 ids ran on from 15,40. Fixing it moves 30 ids, and users' 🔖 ✍️ and progress must not break.
- **Scope:** split.py `TYPOS` alias + guard (a section anchor with a new chapter resets the paragraph count) · moves = same German text, unique, so `data/id_moves.json` is cumulative (chains composed) · the same run renames ids in rank · andachten · i18n q · i18n_full v (no LLM re-run) · build.py copies `id_moves.json` to assets · the app's `data/IdMoves.kt`: on AppVm start, if moves.size > Prefs `id_moves_done`, one Room transaction renames Mark + ReadLog (collision = merge) · DayLog holds plan indices, so it is untouched.
- **Alt:** Keep the wrong ids forever (rejected: chapter 15 would start at "15,40…", wrong footnotes) · an alias table in the app only (rejected: two id systems).
- **Effects:** Re-splitting the book is safe; marks and reading progress follow the text.
- **Lock-in:** Shipped apps migrate from `id_moves.json`; the file must stay cumulative forever, or older installs lose their marks.
- **Ck:** `IdMovesTest` (Robolectric + in-memory Room + the real asset map: 4 tests: map dead to live ids · marks/reads follow · merge · runs once) · `bin/test_bin.py` · 1868 verses, 92 Andachten + ⭐288 kept (@1113 · 1867 / 93 @1122)
- **Note:** (about D-024) v1 `drop_toc` cut "toc to next heading", which ate chapter 17's 3-paragraph intro; now only contiguous toc/index lines are dropped. align: name anchors (EN mid-sentence capitals vs. DE text + cited footnotes) + length may match text+footnotes (Moore/Aveling inline long footnotes), so 13,120-125 are exact. Churn: 34 verses, sampled = fixes (V3 · 8 · 14 · 22 · 23).

### D-027 @1113 · 🤖 Curated plans live in data/plans_extra.json; "Ludditen & KI" pairs chapter 13 with new "Und heute?" bridge texts · Accepted (@1113)
- **Y:** A bridge is its own field, not an Andacht (an Andacht is an LLM devotional, a bridge is an authored argument). The Kapital text stays untouched by construction.
- **Ctx:** QA @1112: "add a luddites read plan and link directly to AI · explaining text new, bible text original" (R4) · big round #6.
- **Scope:** build.py `extra_plans()` appends hand-curated plans (verse ids only; an unknown id fails the build) right after the flagship · PlanDay `xd`/`xe` = the Today box "💡 Und heute?" + "new text — Marx's paragraph above is the original" · 7 days: 13,73 Aristoteles · 13,62 · 13,110 · 13,124-125 Ludditen · 13,127+130 · 13,165 "an sich vs. kapitalistisch angewandt" · 13,144 · Marxoskop "KI & Maschinen" leads to a card, a confirm, then `startPlan("ludditen")`.
- **Alt:** LLM bridges via andacht.py (rejected: the local model makes Marx errors) · only a new Marxoskop card (rejected: one-shot, no habit).
- **Ck:** `BigRoundTest.ludditesPlanIsKap13WithBridges` · other languages showed the EN bridge until translate.py learned xd/xe (fan-in)

### D-026 @1113 · 🔓 Hooked mode unlocks after 7 reading days or the first finished plan, with its own badge and animation · Accepted (@1113)
- **Y:** Total days, not streak (D-004 no anxiety). The dialog names its own trick (honest Hooked). The Settings bar is a goal gradient.
- **Ctx:** QA @1112: "hooked mode only after 7 days or first read plan, animation/award 'unlocked!'" · big round #5.
- **Scope:** `Orden.HOOKED` 🪝 "Hinter den Kulissen" (`hookedUnlocked`: dates ≥ 7 or plansDone ≥ 1) · MainActivity: HOOKED new = `HookedUnlockDialog` first (padlock wiggle, shackle opens, 🪝 drops + 📳 + confetti, button "Einschalten" = testMode on) · Settings: locked row + days left + bar until earned · already on = stays on.
- **Alt:** Hide the row until unlocked (rejected: no curiosity) · a 7-day streak (rejected: anxiety).
- **Ck:** `BigRoundTest.hookedUnlocksAfter7DaysOrFirstPlan` · shot `settings_2` = 🔒 row

### D-025 @1113 · 📜 After today's snippet comes an endless feed in book order, loading by itself · Accepted (@1113, v1)
- **Y:** Book order gives context (plans jump around). Break cards keep D-014's honest tone. The Marxoskop stays above it, reachable.
- **Ctx:** QA @1112: "after finishing the snippet, continue for the motivated, endless feed, hooked". You answered Q3: auto-scroll like Instagram. r4 #6: plan verses were text walls · big round #3 + #4.
- **Scope:** `VerseView(preview = true)` with `snippet()` cut at a sentence end ≤ 320 chars + pill "weiterlesen ▾ · noch ~N Min." (🔊 reads all) · Today: plan verses + feed use it · after "Tag geschafft", `ReadOnFeed` at the tail of Home: passages right after today's last one in BOOK order, 3 per load when ≤ about 1800 px are left (no tap) · chapter % bar · "⬇ gleich: »…«" teaser · ☕ break card every 10 · scrolled past = `vm.read` (Read % + badges), NOT plan days (D-011).
- **Alt:** LazyColumn rewrite of Today (rejected: big hot-file churn) · a "weiter" button (rejected: your Q3) · the plan's next day (rejected: breaks D-011 continuity).
- **Ck:** `BigRoundTest` snippet/feedStart · shots `feed` `feed_2` · ⚠️ (v1) it loads a bit ahead, so "read" marks passages before they are fully seen
- **Note:** The feed moved into the reader's 📜 mode (D-040) and was replaced by `ScrollBook` @1120 (#200).

### D-024 @1113 · English reader data comes from the pipeline: align.py drops the chapter TOC, splits merged paragraphs and takes section titles from marxists.org · Accepted (@1113)
- **Y:** No hand edits. Plans and scores stay unchanged (German text identical, plans.json byte-equal); full_xx is untouched.
- **Ctx:** r4 sweep #3 (German section titles in EN + "no English paragraph here") and #4 (chapter 1 page 1 = TOC as body text) · big round #1 #2.
- **Scope:** `drop_toc` (`<p class="toc">` to the next heading; also fixed the first verses of chapters 3 · 13 · 15 · 23) · `split_en`: a k-1 bead is cut into EN sentences by DE length share (199 down to 11 verses without EN) · a `Heads` stack per EN paragraph gives verse `en_head`, then build.py `en_sec_titles` (innermost label match 'a)' = 'a.' · 'IV' = '4', else the EN chapter title for DE chapters 4/24) writes verses.json `ste`, which `secTitle()` uses (DE = st, else ste).
  - @1117 (11, then 7, then 0 gaps): a sentence can end after a closing quote · `Mr.`/`Dr.`… don't end one · `PIN` (DE id to EN opening) for DP mis-beads (13,58 · 20,13) = the only hand-given hints · 🏷️ labels (`[95]` · ch. 23 "12. Worcestershire.") are their own EN and never eat the body's first sentence · too few sentences = cuts at ; / : then the longest paragraph · tables keep "all on the first".
- **Alt:** LLM-translate the DE titles (rejected: the Moore/Aveling wording exists) · an override table (rejected: drifts).
- **Ck:** `bin/test_bin.py AlignEn` · `ContentTest.englishReaderData` (0 blanks; all 90 DE titles have EN) · ⚠️ known: 13,122-125 EN shifted about 1 paragraph (pre-existing DP slip) · the DE split typo "FÜFNZEHNTES" put chapter 15's opening into chapter 14 (fixed later by D-028)

### D-023 @1113 · 🆕 The updater acts by installer: Obtainium gets poked, self-installs go silent after the first · Accepted (@1113), phone test pending
- **Y:** Friends need 0 taps from the second update on. Obtainium keeps its job: we don't install over it (it declares ENFORCE_UPDATE_OWNERSHIP, which would add prompts).
- **Ctx:** You had Obtainium background updates on, but always beat its 6 h interval (deploy, reload, update by hand = the same friction as the website). Your words: "use Obtainium if it's there, else the website apk". A 6 h delay is fine for users.
- **Scope:** `Update.Via` from the installer of record: OBTAINIUM = notify only after `GRACE_H` 8 h on the same version, tap = `obtainium://refresh?id=<pkg>` (Obtainium wiki deep link; no link can INSTALL) · SELF = PackageInstaller session (not ACTION_VIEW), so WE become the installer of record, then Android 12+ + `UPDATE_PACKAGES_WITHOUT_USER_ACTION` lets the worker install silently and `UpdatedReceiver` posts "✔️ X installed" · the first self-update or < 12 = 1 confirm · STORE = nothing.
- **Alt:** Install over Obtainium installs ourselves (rejected: ownership prompt + 2 updaters fighting) · Shizuku (rejected: setup for friends) · a daily nag for Obtainium too (rejected: noise, its background update mostly wins).
- **Prev:** D-021 (amended). D-038 later dropped the grace period.
- **Ck:** `UpdateWorkerTest` 8 (9 since D-038 · per installer · grace · silent · fallback) · `UpdateTest` via/silentRule · DEV Settings "🧪 bg update check now · installer: X"

### D-022 @1113 · 📖 Double-tap turned the page, the dog-ear turns it, and the first chapters showed a hint · Superseded by D-044
- **Y:** Three cues (motion · object · words) without a tutorial screen. Outside the book (Today) there is no double-tap, so a tap reacts at once.
- **Ctx:** QA r5: you found page turning by accident ("NICE"), so it needed discovery. Your words: "double tap = turn page, drop bookmark-via-double-tap".
- **Scope:** `LocalPageTurn` (ChapterScreen) fed by VerseView `onDoubleClick` + paper `detectTapGestures` · `DogEar` bottom-right (tap = turn) · until 3 own turns (`omm_ui` SharedPrefs "turns"): the page lifted 2× + pill "swipe or double-tap" for about 6 s · 🔖 = verse menu only. Amended by D-044, swapped by D-057, hold changed by D-065.
- **Alt:** HorizontalPager with dots (rejected: loses the spine-hinged turn) · persistent ‹ › buttons (rejected: clutter on the "pure Marx" page).
- **Ck:** (at the time) `ui/ReadScreen.kt` TURN_HINT · DogEar · VerseView `onDoubleClick` = turnPage

### D-021 @1113 · 🆕 Hand-installed APKs update themselves: a daily check, a notification, then download and install · Accepted (@1113), amended by D-023
- **Y:** The same debug key installs over the old app, so data stays. If the installer is Obtainium, F-Droid or Play, skip (they update themselves). lib.sh already writes the apk + sha256.
- **Ctx:** QA r5 🔑: friends install the APK directly and must never sit on a stale version. Audit: only `Update.check` on app start, then a Today banner that opened the web page (manual download). DEV: a Changelog line "Obtainium" only. No background check, no notification.
- **Scope:** `notify/UpdateWorker.kt` WorkManager, daily (network): `Update.check` on the app's own channel (`.dev` = `<web>/dev/version.json`, else `<web>/version.json`), notification "🆕 X is out" (a new version at once, the same one again every 2 days), tap or Today banner opens `UpdateDialog`: download the `apk` from version.json (plain name only, sha256 checked), `ACTION_VIEW` the apk via FileProvider (`REQUEST_INSTALL_PACKAGES`; first time: the "allow this source" page, which resumes by itself) · on failure: the web page.
- **Alt:** PackageInstaller session API (rejected at the time: more code, same system dialog) · in-app silent install (impossible without device owner) · Obtainium for all (rejected: friends won't).
- **Ck:** `UpdateTest` (parse · channel · nag · sha · installer) + `UpdateWorkerTest` (Robolectric, fake version.json: notification, no repeat, older = silent) · manifest REQUEST_INSTALL_PACKAGES

### D-020 @1113 · 👥 Mitlesen is always on, and the name lives only in Settings, labelled "Name" · Accepted (@1113)
- **Y:** Your call: one place to change identity. It is still an anonymous daily token (D-005); the UI just says nothing about it now.
- **Ctx:** QA r5: "remove the Mitlesen/read-along option and its text (always on)" · "label just 'Name'" · sweep #8: name field on Settings + Kreis (+ comments sheet).
- **Scope:** `Settings.community = true` (an old stored "off" is ignored) · Settings Name field (D-016 prefill + save on blur stay) · Kreis + comments: `WritesAs` line "you write as X · change: ⚙️".
- **Alt:** Keep the switch (rejected by you) · a name field everywhere (rejected: 3 fields drift).
- **Prev:** D-005 (the switch) · D-016 (the label).
- **Ck:** no `vm.setCommunity(` caller in ui/ (th VM fn `AppVm.setCommunity` itself is left, dead) · `nameLabel()` = "Name"
- **Note:** (about D-017) QA r5 amended the note editor: no ★ (only the app-wide close elsewhere) · ANY short tap while open = close (save if changed, blank = gone), and the tap is eaten (it can't toggle the action row) · the 👥 chip is exempt · look = `NoteBubble` (side ruler · ✍️ icon · blue tint · sans), later replaced by the D-054 card.

### D-019 @1113 · 📸 Screenshots are made headless with Robolectric native graphics · Accepted (@1113)
- **Y:** The real app, real Compose, 20 s, no device. It catches overlaps and stale copy before your phone does.
- **Ctx:** Your r4: "design keeps slipping through, compare screens side by side". No /dev/kvm on tow, so no emulator; phone screencaps are private (memory).
- **Scope:** `test/…/ShotsTest.kt` drives the real MainActivity (tabs · chapter · verse menu · note · settings · In book) + cardBitmap variants into `app/build/shots/*.png`; skipped in a normal `testDebugUnitTest`.
- **Alt:** Emulator (no KVM) · Paparazzi (library modules only) · Roborazzi (extra plugin, same engine).
- **Ck:** `./gradlew testDebugUnitTest --tests '*ShotsTest*' -Pshots` · ⚠️ Robolectric fonts: newer emoji (🪝) render as tofu, ignore

### D-018 @1113 · The reader has no share: the verse menu is listen, bookmark, note and copy · Accepted (@1113)
- **Y:** Read stays pure Marx; sharing lives on the Today card (Karte teilen · App teilen).
- **Ctx:** QA r1: "2 boxes in each other look weird · drop sharing here?" · r4: "remove share from the Read page" · r4: "Listen on the LEFT".
- **Scope:** Removed the verse-menu Teilen (also on Today's plan verses, which use the same menu) and r3's 2 pills at the page end · `CardDialog` + `LosungPopup` deleted (dead) · 🔊 first in every action row.
- **Alt:** Keep verse share on Today plans only (rejected: 2 different menus) · 💬 you may want it back.
- **Ck:** grep `CardDialog` = 0 · VerseView action order

### D-017 @1113 · ✍️ Writing on the book is one pattern: a passage's ✍️ Notiz, inline, with a 👥 chip to also post it · Accepted (@1113)
- **Y:** One entry, one composer, privacy = one toggle, and the comment keeps its passage reference.
- **Ctx:** r4: the upper "Notiz" and the lower "comment on this page" were the same function with 2 looks, so merge. Note editor: tap outside = save, empty = cancel, small X, no Save/Cancel words.
- **Scope:** Removed the page-end "Mitreden" button · inline editor: tap outside (root watcher `tapOutsideNote`) = commit · ★ (CloseButton, app-wide close) = discard · blank = no note / note gone · with Mitlesen on, the chip "👥 auch im Lesesaal posten — als <name>" posts "¶ <id> — text" to the page's room · reading others = 💬 N in the running head, which opens the sheet.
- **Alt:** 2 buttons side by side in the menu (Notiz · Kommentar) · a Matrix field for the verse id (later, if threads per passage are wanted).
- **Ck:** VerseView `NoteFocus` · ReadScreen has no OutlinedButton · ★ not ✕ = the r1 rule "red star closes" — 💬 you said "small X"

### D-016 @1112 · The public name is prefilled with a random comrade name and saves on blur · Accepted (@1112), label superseded by D-020
- **Y:** Your r3 ask. The label fit D-014's reading-room theme. No Lenin, Stalin or Mao (D-014: a lens, not a regime).
- **Ctx:** QA r3: the "Your name" label was dull, an empty field is friction, a Save button is an extra step.
- **Scope:** Label was "Lesesaal-Name / Reading-room name" (now "Name", D-020) · prefill = a socialist thinker's first name + a movement year (Rosa 1871 …), 1 per app run · the Settings field saves on blur/Done only if changed.
- **Alt:** Genoss:in (gendered, party-ish) · Kampfname (militant) · Name / Username (generic): you could pick if you preferred.
- **Ck:** `nameLabel()` + `comradeName()` in `ui/Comments.kt` · `NameTest.kt`

### D-015 @1112 · The streak action is the easy Amen, placed under the reading snippet · Accepted (@1112), not built
- **Y:** Your words @1112: "its not the quote … its the actual book" and "Amen counts for the streak, keep it an easy action · investment = reading a bit".
- **Ctx:** The streak counted the quote Amen; the plan "done" looked meagre next to it (Mi). Options were a any day · b plan days · c hybrid. Amended @1112.
- **Scope:** Amen = the streak action and stays easy (1 tap, no read-gate or timer) · one place: the bottom of the reading snippet (remove the card + hover duplicate), so the investment (Hooked/IKEA) is actually reading a bit.
- **Alt:** a/b/c chip (rejected) · gate Amen on scroll or time read (rejected: friction).
- **Ck:** (after the build) Amen only in the read snippet · Amen button in `Cards.kt` = 0 · streak test

### D-014 @1112 · 🎖️ Orden became Abzeichen (badges), with a reading-room look in blue and gold · Accepted (@1112)
- **Y:** User feedback (Mi) via you: Orden felt too real-socialist and authoritarian, with ☭ everywhere.
- **Scope:** Rename + re-theme (reading-room look), positive reframes ("missed a day" tone out), the stamp gift = ☭, Marx = a lens, not a utopia. Amended @1112 (you): tone = positive and friendly, **nudging allowed** (a miss gets a kind nudge, e.g. a sad Marx) · no guilt, shame or loss threats.
- **Alt:** Keep Orden (rejected: alienates non-hardcore users).
- **Ck:** `grep -rn "Orden" ui/` = only enum/code names, UI text says Abzeichen/badges · `OrdenTest.kt` · nudge strings carry no blame ("verpasst" / "verloren")

### D-013 @1112 · 📉 The crisis badge needs 15 shakes · Accepted (@1112)
- **Y:** Mi: "needs more shakes".
- **Ctx:** The crisis medal was too easy.
- **Scope:** `KRISE_SHAKES = 15`.
- **Ck:** `logic/Orden.kt` KRISE_SHAKES

### D-012 @1112 · 🔊 There is one sober voice only · Accepted (@1112)
- **Y:** Mi and you agree the holy voices (Gottvater, Gott, Pfarrer…) are cringe.
- **Scope:** `Persona.picker = [NORMAL]`; the rest is hidden in code, not deleted · an old stored default becomes sober.
- **Alt:** Keep them as opt-in (rejected: clutter).
- **Ck:** `Persona.picker` in `ui/Share.kt` · `VoiceTest.kt`

### D-011 @1112 · 📖 A reading plan continues day by day, never repeats, and everyone starts in a default plan · Accepted (@1112)
- **Y:** Mi's feedback.
- **Ctx:** A real-user bug: the same page twice. The plan didn't train regular reading.
- **Scope:** `planIndex` continuity, a finished plan doesn't repeat, a default plan for all.
- **Ck:** `PlanTest.kt`

### D-010 @1031 · UI strings are written inline in German and English in the code; other languages come from translate.py · Accepted (@1031)
- **Y:** Your call: 13 languages and a lone developer.
- **Scope:** `T("de","en")` / `tr(..)` pairs in Kotlin (key = EN) are translated by `bin/translate.py --only ui` into i18n_xx.json · not translate_full for UI · one pass at fan-in.
- **Alt:** strings.xml per language (rejected: 13× by hand).
- **Ck:** `bin/translate.py --only ui` finds 0 new · only launcher/shortcut/tile names live in `res/values*`

### D-009 @1108 · Releases go to GitHub, Forgejo is the origin, and gh only fetches · Accepted (@1108)
- **Y:** The mirror carries tags to GitHub; ship.sh waits up to 120 s.
- **Ck:** `git remote -v` · `bin/ship.sh`

### D-008 @1107 · The DEV build stamps its version into the APK's first zip entry, so Obtainium sees updates · Accepted (@1107)
- **Y:** The root cause was measured.
- **Ctx:** Obtainium's default pseudo-version hashes the first 1 KiB, which was identical across builds, so it never updated.
- **Scope:** ommStamp + a deploy guard.
- **Ck:** `ref.md` (the trap is documented) · `bin/deploy.sh`

### D-007 @1107 · There is one dialog style (MarxDialog), and ☭ only closes · Accepted (@1107)
- **Y:** Your words: "windows look weird — frame colour, no frames, ☭ at odd spots".
- **Ck:** `ui/MarxDialog.kt` · no raw AlertDialog elsewhere

### D-006 @1105 · The repo is public under GPL-3.0, with priv/ git-ignored · Accepted (@1105)
- **Y:** ❓ am0 (no reason recorded). Accepted risk: GitHub keeps old SHAs reachable.
- **Ctx:** Friends need to build it themselves.
- **Scope:** Public GitHub am0ebe/oh-my-marx; the Hooked OCR is out of the tree and out of history.
- **Ck:** `.gitignore` priv/ · LICENSE

### D-005 @1103 · 👥 Community reading (Mitlesen) uses an anonymous daily token, on by default · Accepted (@1103), switch superseded by D-020
- **Y:** ❓ am0 (on by default was your call). Social proof (Hooked tribe) weighed against privacy.
- **Scope:** Counter + Matrix comments/Kreise, a switch-off in Settings (gone since D-020), not end-to-end encrypted (the UI says so).
- **Ck:** `data/Community.kt` · `Prefs.community` · 💬 the toggle's fate was an open question

### D-004 @1101 · 🔥 The streak has a 🕊️ Sabbath joker, and the stamp card counts total days · Accepted (@1101)
- **Y:** Streak anxiety: one miss leads to rage-quitting (notes.md § judgment).
- **Alt:** A plain streak (rejected).
- **Ck:** Prefs flag "sabbath" · `logic/Daily.kt` · `OrdenTest.kt`

### D-003 @1028 · 🏛️ The app is native Android (Kotlin + Compose), offline, with an optional server · Accepted (@1028)
- **Y:** It is a learning project, and friends sideload it.
- **Ctx:** One developer plus AI crew; friends get the APK directly, no store.
- **Scope:** Offline-first, the whole book in the APK. A server was added @1103, opt-in.
- **Alt:** none recorded.
- **Effects:** Everything works without a network; online parts (counter, Matrix, updates) degrade gracefully.
- **Lock-in:** All UI, logic and tests are Kotlin/Compose; a cross-platform rewrite would start from scratch.
- **Ck:** `android/` · the INTERNET permission is only for Community/Update

### D-002 @1028 · 📕 The book source is MEW 23 (German, public domain) and Moore/Aveling 1887 (English, public domain) · Accepted (@1028)
- **Y:** You own MEW 23, so page references match.
- **Alt:** DTA 1867 (rejected: CC BY-NC, different structure).
- **Ck:** `bin/fetch.py` · `ContentTest.kt`

### D-001 @1031 · 🌍 The UI languages are German and English, English by default (phone language if supported), German second · Accepted (@1031)
- **Y:** ❓ am0 (no reason recorded).
- **Ck:** `data/I18n.kt`

## ❓ open Qs
- ✔️ closed @1121 (#290 👀 diet): streak a/b/c ➡ D-015 Amen + D-045..047 · Mitlesen toggle ➡ D-020 (always on) · ☭ close btn ➡ D-007 + D-055 (★ V2) · streak widget "2 at start" ➡ D-045 (hero = streak only)
- ✔️ "share app" prompt aft streak/medal ➡ parked i th Hooked pass (D-074)
- later: Read = real book look + page flip · daily verse can't be turned off · Lesekreis progressive disclosure

## 🔍 drift (checked @1122, night #308 ➡ [night_1122.md](night_1122.md) § drift)
- @1122: Ck counts + spec § 14.3 fixed · open = 🦠 rows i night_1122 (D-025 status · D-053 shots · § 5 chips/btns · widget colors.xml) · @1112: none vs ✔️ Ds · note: Changelog.kt old release lines still say "Heilige Stimmen"/"Orden" = history, ok

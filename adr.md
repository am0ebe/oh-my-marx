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
- app `android/` (pkg `de.strix.marxversion`): `logic/` (Daily · Feast · Orden · Voice) · `data/` (Prefs · Content · Update · Community · Matrix) · `ui/` (Compose screens) · `notify/` (Reminder · Widget · KreisWorker)
- content pipeline `bin/*.py` (fetch ➡ split ➡ align ➡ score ➡ translate) ➡ `assets/content/*.json` · cache `data/i18n/<lang>.jsonl`
- server (VPS, ops-owned): `server/omm_api.py` counter + Synapse `omm-matrix` (comments · Kreise) · contract `server/SERVICE.md`
- release: `bin/deploy.sh` (DEV, Obtainium) · `bin/ship.sh` (bump + tag + page + GH release) · map ➡ [ref.md](ref.md)
- 🔥 hot files (1 owner at a ti — fan-out lanes nvr split these): `assets/content/i18n_*.json` + `data/i18n/*.jsonl` (translate pass = fan-in only) · `android/app/build.gradle.kts` (version) · `ui/Changelog.kt` · `res/values*/strings.xml` · `TodayScreen.kt` · `MineScreen.kt` · `Share.kt`

## 🧾 decisions (newest on top)
### D-059 @1120 · ⏱️ "long i th app" award = time IN TH BOOK, not app-open time · 🤖 built as rec (🦠 @1120 via M8 #185 asked) · 💬 eval on fon
- **ctx**: 🦠 "award 4 staying i th app a long time" · D-014 earned-only · D-004 no anxiety · Hooked ch6 (facilitator, not dealer)
- **options**: a) 1 long session (≥ N min at once) · **b) total active reading time i th 📖 book, 2 tiers** (🤖) · c) daily minutes streak · d) skip
- **dc**: b · ⏱️ **Normalarbeitstag** = 8 h total · 🏭 **Arbeitswoche** = 40 h total · group READ · a page counts ≥ 10 s, cap 5 min (idle screen ≠ reading) · paused app = clock stops
- **y**: Kap. 8 frame (8-hour day fought for) = wink + honest · total, not at-once ➡ no binge pressure · a + c reward compulsive use = th exact Hooked trap th app mocks · counting book time, not app time ➡ can't be farmed by leaving it open
- **alt**: a ❌ binge · c ❌ 2nd streak = anxiety · d ❌ 🦠 asked 4 it
- **ck**: `OrdenTest.readingTimeMedals` · `Prefs.readSecs` · ChapterScreen page timer (DisposableEffect + lifecycle)
_🛠️ @1120 (M8 #185): D-048 · 049 · 050 · 051 · 052 · 053 · 054 = built on th 🤖 pick (C2-C5 + C7) ➡ still 💬 til 🦠 evals on th fon · gaps (🔵 todo): perk effects · 12 heads still emoji tokens · Share/Widget head · illuminated + section initials · 📜 pilcrow_
### D-058 @1120 · 🖨️ book type: Literata bundled · block-justified · auto hyphens · ✔️ (🦠 @1120 via M8 · closes D-033 b 🧪)
- **ctx**: 🦠 "block-justified like a printed book + a good book font, hyphenation DE/EN so justify doesn't gap" · D-033 b (🦠 @1114 "try b") = Literata ⬄ EB Garamond, never built
- **dc**: **Literata** (OFL 1.1, Google's e-book face, `res/font` variable wght + opsz, opsz 12 body · 18 italic) = `BookSerif` 4 all serif tokens · `bookBody` = `TextAlign.Justify` + `Hyphens.Auto` + `LineBreak.Paragraph` · `localeList` = shown lang (DE rules 4 DE txt) · drop cap: 3-line block = whole ¶ clipped (line 3 stays justified + hyphen) · licence ➡ `assets/licenses/Literata-OFL.txt`
- **y**: Literata = built 4 long reading on screens (large x-height, sturdy at 17sp) · has Latin + Cyrillic (ru) + Greek · justify wo hyphens = rivers on a 360dp column · Paragraph breaker = fewer gaps
- **alt**: EB Garamond (❌ small x-height, thin at 17sp on a fon · more "old" but less readable) · Source Serif 4 · system Noto Serif = old (D-033 a)
- **cost**: +1.86 MB APK (2 variable TTFs, not subset) · 🔵 subset 2 Latin + Cyrillic static (fonttools) ≈ −1.3 MB · zh · ar · hi · bn · ur glyphs fall back 2 system per glyph
- **ck**: shot `chapter` (justified, "im-" | "mense" at th drop cap) · `OmmType.book*` = `BookSerif`
### D-057 @1120 · 📖 gestures SWAPPED: tap o hold = turn (left half back · right fwd) · double-tap a passage = its menu · ✔️ (🦠 @1120 via M8) · supersedes D-044 (below)
- **ctx**: D-044 = double-tap turns (side by x) · tap = passage menu · 🦠 eval DEV 0009: swap
- **dc**: i th 📖 book: tap o hold anywhere (passage o paper) = turn, side = finger x · double-tap a passage = menu (+ buzz) · edge-quarter rule gone (whole half counts) · 📜 scroll + Today: no turn ➡ tap = menu, hold = menu (as b4) · hints (folio line + 📖 chip pill) say th new model
- **y**: turning = th 90 % action ➡ th cheapest gesture (1 tap, like e-readers) · menu = rare ➡ double-tap is fine
- **alt**: hold = menu (❌ 🦠 said "single tap / hold = turn")
- **ck**: `ReaderGestureTest` (double-tap ➡ "Note" · tap right ➡ nxt page · hold left ➡ back) · ShotsTest verse_menu via double-tap
### D-056 @1119 · plan re-eval: bibel hand-picked · 30tage + montag re-ordered · Andachten found by verse ids · ✔️ (🦠 qa_1117 r1 via M8 #155 salvage)
- **ctx**: 🦠 r1 "do th plans build on each other?" · re-eval: bibel = score.py `bib` flag ➡ 7/10 days hd no Bible at all · 30tage hd 13 b4 10-12 + 23 b4 22 (Marx's ladder backwards) · montag day 1 = 1,27 (coat + linen, not Monday)
- **dc**: bibel v2 = 10 hand-picked refs (Lamb o God ➡ Revelation ➡ Bible-4-linen ➡ Paradise ➡ Eden ➡ Jehovah ➡ Moses ➡ original sin ➡ Holy Ghost) · 30tage v2 + montag v2 (no ch. 1-3) · all via D-042 · Andacht lookup = (plan, verse ids), not day idx · `data/andachten_hand.jsonl` beats andacht.py's LLM cache
- **y**: a generated pick is fine 4 "juicy verse/day" plans, wrong 4 a plan w a promise (Bible) · re-ordered days keep their Andachten · hand Andachten survive `andacht.py --redo`
- **alt**: tune th `bib` regex (❌ "Gott"/"heilig" hits evywhere) · LLM Andachten 4 new bibel days (❌ quality, todo "Heute lädt uns Marx ein")
- **ck**: `test_bin.py PlanContent` 5 · translate `--only a` re-run (Andacht translations r keyed plan:idx)
_🎨 D-045..055 @1119 = design addendum (qa_1117 D6-D11 + r4) · all 💬 · 🤖 pick marked · spec + mockups + lanes C1-C7 ➡ [design_system.md](design_system.md) § 14 (§ 14.0 = same list as a table)_
### D-055 @1119 · close btn (D9 · Q7): variants sheet ➡ 🦠 picks · ✔️ **V2 bare ★** (🦠 @1120 via M8 · `CloseStar` dflt)
- **options**: V1 now (★ i tinted circle) · V2 bare ★ · V3 mono ✕ · **V4 ★ w ✕ cut-out** (🤖 eye-guess) · V5 inverted red disc · V6 outline ★ · V7 wax seal · V8 V1 + spin on press
- **y**: 🦠 r1 "many versions, diff ctx" ➡ render, don't argue · V4 = brand + new usrs read "close" · V3 ❌ loses ★ = close (§ 1)
- **ck**: `close_sheet.png` (8 × 3 ctx + dark) · lane C6, no gate
### D-054 @1119 · note = margin-comment card (D7 · Q4) · 💬
- **options**: a) yellow sticky + 📌 · **b) comment card: paperHi + blue ruler + tail 2 th verse + initial disc + name/time + ✎** (🤖) · c) keep ruler, new bg only
- **y**: a ❌ yellow = Hooked's voice (§ 7) · b = wh "oth apps" do (r2) + 1 component 4 notes AND comments · c = too close 2 "a blue quote" 🦠 disliked
- **ck**: shots `note_card` `note_edit` `comments` (C5) · ❌ ★ i editor · tap away = save, menu stays
### D-053 @1119 · holy-book devices (D11) · 💬
- **options**: a) ⚡ set (section initials · red ¶ · incipit · headpiece · running head · catchword · colophon) · **b) ⚡ + 🟡 (illuminated initial · tailpiece + chapter seal · ribbon bookmark · gilt edge)** (🤖) · c) + 🧪 paper grain · margin glosses
- **y**: b = most "worthy old book" per effort, all drawn (no raster) · c = perf + clashes w notes · ❌ margin ¶ numbers (r3 rm'd th ¶ ref line)
- **ck**: shots `chapter` `chapter_scroll` `chapter_end_seal` `bookmark_ribbon` + rtl + zh (C4)
### D-052 @1119 · feast calendar: + 18 feasts (R4 gaps) · 💬
- **options**: **a) all 18** (labour history + 3 fun days, § 14.2.3) (🤖) · b) history only · c) 🦠 picks on th sheet
- **y**: 5.5 ➡ 14.9 = 4 months w nothing · a ➡ longest gap ~4 w · fun days = variable reward (🦠 r3) · quiet († days) = no confetti
- **ck**: `Feast.on()` tst per date · `feast_sheet.png` (C3)
### D-051 @1119 · holiday Marx head (D8 · Q6) · 💬
- **options**: **a) 1 base head + overlay layers** (🤖) · b) full redraw per feast · c) emoji next 2 th head (now)
- **y**: a = 1 style, cheap per feast, no drift · b only 4 🎄 Santa-Marx · 🎃 pumpkin-Marx · 🎂 · c ❌ "half-baked" (🦠 r3)
- **ck**: `ui/ds/MarxHead.kt` = only place th picks a head · top bar + share card + widget call it
### D-050 @1119 · rank-up = Beförderung, fullscreen RPG · 💬
- **options**: **a) badge flies in · Marx seals "GENEHMIGT" · rays · 1 perk line · share** (🤖) · b) bigger dialog · + cosmetic perks per rank · + rank 3 = new D-043 unlock source
- **y**: 🦠 r4 "promotion · hero upgrade · MMORPG lvl-up" · ~evy 10 reading days = rare enough 4 big (D-034) · perks cosmetic = honest (D-004)
- **ck**: shot `moment_rank_3` · rays stop at 2300 · reduced motion = end frame (C2)
### D-049 @1119 · celebrations = 1 ladder, 1 big screen per tap · 💬 (✔️ as written / change)
- **dc**: T0 press · T1 ✓ · T2 ☭ + Stempeluhr · T2b chapter seal · T3 milestone · T4 Beförderung · T5 reward dialogs · queue order = that · ≥ 2 due ➡ "Moment … ▸" · > 2 dialogs ➡ "+N" chip
- **y**: r4 "Amen ➡ award ➡ ☭ i series" · D-034 size ∝ rarity · no dialog walls
- **ck**: FxQueueTest: 1 big screen per tap (C2)
### D-048 @1119 · Day complete = Stempeluhr (D10 · Q11) · 💬
- **options**: **a) stamp thump + star count** · **b) 1 punchy line fr a pool** · **c) teasing Q 4 tomorrow (new `qd`/`qe`)** (🤖 a+b+c) · d) Marx head reacts (🧪 aftr C3)
- **y**: 🦠 r4 "bland · txt stuffed · Moses? · not teasing" ➡ image + motion + 1 line + 1 Q · drop "Losung kommt um" (⚙️ has it)
- **ck**: shots `moment_shift` `moment_shift_full` (C2) · all plans hv `qd`+`qe` (C7)
### D-047 @1119 · star tier aftr a break = "Abschreibung" · ✔️ **c** (🦠 @1120 via M8 #185)
- **options**: a) back 2 0 · b) best ever · **c) 1 tier down per missed day** (🤖)
- **y**: a = anxiety (D-004) · b = no stakes · c = fixed capital depreciates, doesn't vanish
- **ck**: StreakGameTest: tier falls 1/day (C1) · built = `logic/Daily.kt streakGame` (fwd replay o DayLog, no Prefs state)
### D-046 @1119 · milestones + ✊ Streiktag (Q2 · Q3) · ✔️ as rec: ✊ Streiktag · keep Sabbat · steps 7…365 (🦠 @1120 via M8 #185)
- **options**: steps 7 · 14 · 30 · 50 · 100 · 200 · 365 · +100 · 1 freeze per step, max 2 · name a) ❄️ Freeze · **b) ✊ Streiktag** (🤖) · c) 🛟 · Sabbat: **keep** (🤖) o fold in
- **y**: Duolingo set (R3) + 200 (gap 100 ➡ 365) · earned only, no shop (D-014) · Sabbat = weekly grace, Streiktag = earned buffer, used aftr Sabbat
- **ck**: StreakGameTest: earn · cap 2 · use order (C1)
- **impl** (C1 @1120): ✊ earned only on milestone days ≥ 2026-11-20 (no retro bridges) · 🕊️ lazy: 2nd 1-day gap i a week ➡ ✊ if held, else th break moves 2 th earlier gap (= old backward walk ➡ `oldStreaksUnchanged` 300 random histories)
### D-045 @1119 · 1 score on th hero (Q1) · ✔️ **c** hero = streak only · stamps ➡ Stempeluhr + My Capital (🦠 @1120 via M8 #185) · ⚠️ amends D-036 A
- **options**: a) drop gift stamps · b) caption each counter (tried @1116 ❌ still confusing r3) · **c) hero = streak only (star + week + nxt-milestone bar) · stamps + rank ➡ Stempeluhr pane + My Capital** (🤖)
- **y**: 2 worlds, 2 places: streak = hero · career (total days ➡ ranks) = stamp card · hero size + look = A (only th stamp row leaves)
- **ck**: shot `hero_tiers` · stamp card i `mine` (C2)
### D-044 @1119 · 📖 double-tap: left half = back · right half = fwd · amends D-022 · ✔️ (🦠 scrap @1119) · ⚠️ superseded by D-057 @1120 (swap: tap = turn · double-tap = menu)
- **ctx**: 🦠 "read book > add hint double tap = next/prev page" · D-022 double-tap = alw fwd
- **dc**: side by x o th double-tap (paper) o th last finger-down on th book (passage, VerseView hs no position) · hint = folio line + 📖 chip pill
- **y**: matches th 1-tap edge zones (left back · right fwd) ➡ 1 mental model · no always-on peek (D-022 pill rm'd @1116 = distracting)
- **alt**: double-tap alw fwd + left-quarter tap 4 back (❌ 🦠 asked 4 prev too) · hint pill on evy chapter open (❌ distracting)
- **ck**: 👀 qa_1117 L9
### D-043 @1118 · new reading plans unlock progressively (bonus levels, not a paywall) · ✔️ (🦠 qa_1117 Q10 yes · Q12)
- **ctx**: r4 "more plans, unlocked later (+ DEV)" · Q12 🦠: "timeline: 3 awards o 1-3-5 plans o streak 20 · like a bonus easter egg = random reward · + 1 holiday/xmas present"
- **dc**: `Plan.lock` = any 1 of {plans · orden · streak (best ever) · feast gift · lucky 1-in-5 per Amen day aftr N days} · locked = 🔒 mystery card at th end (no title, only HOW) · open ➡ 🎁 dialog i th reward queue · DEV switch = all, no fanfare
  - 6 plans: 💰 reich (1 plan) · 🎓 grundkurs (3 badges) · 🏛️ faschismus (3 plans, bridges only — Vol. 1 never names it) · 🛵 gig (🎲 lucky aftr 10 days) · 🌍 erde (streak 20) · 🎁 weihnachten (Christmas gift, 3 days)
- **y**: Hooked variable reward + progress gates (ch. 4) · best streak ➡ a broken chain nvr re-locks (D-014 kind) · feast/lucky persisted ➡ nvr taken back
- **alt**: all open (❌ no discovery) · fixed path (❌ less surprise) · paid unlock (❌ nvr)
- **ck**: `logic/PlanUnlock.kt` · `PlanUnlockTest` · `data/plans_extra.json` `lock`
### D-042 @1118 · plan versioning: pin th cut u started on · ✔️ (🦠 qa_1117 "do th plan versioning")
- **ctx**: DayLog stores plan days by index ➡ any re-cut (R1 🟡 wertform) would shift evy1 mid-plan
- **dc**: `data/plan_versions.json` = evy cut ever shipped (frozen days incl. Andacht) · build.py bumps `ver` automatically when a plan's verse ids change · plans.json ships `old` cuts · `Prefs.planVer` set at start (null = v1) · old cut ➡ idx-keyed translations skipped (EN fallback)
  - 1st use: wertform v2 = cut at Marx' sub-section heads (8 days, none starts mid-argument) · jahr txt "~10 min" ➡ "~5 min" (txt only, no re-cut)
- **y**: automatic = can't forget a bump · ppl mid-plan nvr see a dif day
- **alt**: migrate DayLogs by verse id (❌ fragile) · freeze plans forever (❌ no fixes)
- **ck**: `bin/build.py versions()` · `test_bin.PlanVersions` (incl. archive ⬄ plans.json) · `PlanUnlockTest.daysFor`
### D-041 @1116 · no Werkbank tab · Circles stays · Hooked entry stays i th top bar · ✔️ (🦠 qa_1116 Q8 revised)
- **ctx**: qa_1116 D5 proposed a "Werkbank" bottom tab (Hooked notes + tools) · Q8 1st answer = b (replace Circle) ➡ revised same day
- **dc**: 5 tabs stay (Today · Plans · Read · Circle · My Capital) · 🪝 Hooked entry stays at th top as now
- **y**: 6 tabs = 2 much (M3 max 5) · Circles comes alive once Matrix is tested (later) · top 🪝 = a bit odd, accepted
- **alt**: Werkbank replaces Circle (❌ Circles has a future) · 6th tab (❌ crowded)
### D-040 @1116 · reader = 2 modes (📖 pages ⬄ 📜 endless) til 🦠 picks 1 · feed leaves Home · ✔️ (🦠 qa_1116 Q4 + Q7)
- **ctx**: endless feed lived at th end o Home (D-024) ⬄ "daily ≤2 min" goal · 🦠 r2: Read = 2 modes, Home ends at Marxoskop
- **dc**: toggle chip i th chapter's slim head (D-031 b) · remembers last (Prefs `read_scroll`) · 📜 = `ReadOnFeed` fr th current page · Home: no feed tail
- **y**: 🦠 wants both tried b4 picking 1 design (Fable spec aftr this round) · **alt**: ⚙️ toggle · 1st-open choice (❌ Q7 a)
- **ck**: `ShotsTest.zFeed` ➡ `chapter_scroll` · `ReadScreen.kt` ChapterScreen
### D-039 @1116 · ☭ swoosh = plan day done · quote-of-th-day Amen = small ✓ pop · amends D-034 · ✔️ (🦠 Q9 b + 🐛)
- **ctx**: D-034 b (swoosh = rare) + QA r2 `4708ab7` turned "Day complete!" into a ✓ pop ➡ th hammer + sickle nvr showed · 🦠: "finish th day's read plan ➡ boom"
- **dc**: "Day complete!" ➡ ☭ swoosh (full stamp card ➡ confetti instead) · quote-of-th-day Amen ➡ ✓ pop (full card ➡ confetti)
- **y**: th plan day = th real effort ➡ th big reward · th quote tap = cheap ➡ small · **ck**: `TodayScreen.kt` `amen` + plan Button
### D-038 @1114 · updater: notify at once (all installers) · browser installs ASK by dflt, ⚙️ opt-in auto · amends D-023 · ✔️ (fon tst pending)
- **ctx**: 🦠 deployed 1114, DEV (1113, Obtainium) showed nothing ("app doesnt show UPDATE popup") · Obtainium path waited 8 h (GRACE_H) + worker ran ~daily only ➡ nothing visible 4 hours
- **dc**: 🦠 "do notification immediately" ➡ GRACE_H dropped · + 1 OneTime check on evy app start (WorkManager, network) next 2 th daily one · "browser apk selfupdate shd ask user … o they select autoupdate in settings" ➡ `Settings.autoUpdate` dflt OFF ➡ notification ➡ tap = UpdateDialog (1 confirm) · ON (⚙️ row, browser installs only) + Android 12+ + we r installer ➡ silent (D-023 path)
- **y**: user sees + decides (consent) · Obtainium users: their own bg update may still win, then our ping is moot (same version ➡ no re-nag)
- **alt**: keep grace (❌ 🦠 can't see it work) · silent by dflt (❌ 🦠: ask 1st)
- **ck**: `UpdateWorkerTest` (obtainiumNotifiesAtOnce · autoOffAsksEvenIfWeCouldGoSilent · weInstalledOurselvesIsSilent w auto) · fon: deploy B ➡ notification within ~1 min o opening th app
### D-037 @1114 · omm-api image ships wo pip · ✔️
- **ctx**: 1st nightly live sweep (run 935, 03:15) ❌ — trivy = 0, red = **pip-audit**: base `python:3.12-alpine` bundles pip 25.0.1 ➡ 6 advisories (PYSEC-2026-1795 · 1796 · 2875 · 2876 · 196 · 3721, fix ≤ 26.2) · app = stdlib only, nvr calls pip
- **dc**: `server/Dockerfile` uninstalls pip + rm `ensurepip` (same pip as a wheel) + build-time guard (`! import pip`) · nightly caller keeps `pip: true` (template skips w a warning wh no pip ➡ audit comes back by itself if deps ever land)
- **y**: no pip = nothing 2 patch, 4 good · alt `pip install -U pip` ❌ needs PyPI at build + red again on th nxt pip advisory · alt `pip: false` i nightly.yml ❌ hides th finding, pip stays i th image
- **ck**: local build: `pip` not found · `/v1/health` ok · trivy 0.72.0 (CI flags) exit 0 · CI canary green · live = aft ops rebuilds `/srv/omm-api` (ops-owned, SERVICE.md)
_🎨 D-029..036 @1113 = design system (🦠 decides · 🤖 pick = 1st option) · spec ➡ [design_system.md](design_system.md) § 0 = same list as a table · @1114: D-029 + D-030 ✔️ ➡ L0 built (`ui/ds/`) · D-031..035 ✔️ (🦠 @1114) · D-036 ✔️ A (🦠 @1116)_
### D-036 @1113 · Today above th fold (qa-D1): compact hero + stamp tag · ✔️ A @1116 (🦠 @1114: "which is which? show me" ➡ mockups `d036_all.png` a · b · c)
- **🦠 @1115** (via M8): "as it was b4, o bigger" ➡ ❌ compact (a) + ❌ collapse (c) · now **A/B on 1 DEV APK** 4 friends: ⚙️ "🧪 Today hero" (DEV only) A = pre-ds hero (pixel-same) · B = bigger (star 124 · headline 30 · week + stamps fill th width) · shots `priv/img/d036/hero_ab.png` · 💬 pick aft friends ➡ drop th switch + loser
- **ck**: `ShotsTest.hero` ➡ hero_a + hero_b · Prefs key `hero_ab` (old abc picks don't leak)
- **✔️ A @1116** (🦠 via M8 aft #101: "A (smaller)") ➡ A = only hero · toggle + B + Prefs `hero_ab` + ShotsTest.hero dropped (git: 384cbab) · L1 Today unblocked
- **ctx**: 🦠 "landing shd fit verse + streak widget + read-plan btn" · now hero (star 92 + week row + stamp row + rank) + card ➡ plan entry below th fold
- **options**: **a) compact hero 120dp (star 48 + 7 day dots + Tag "☭ 3/10"), full stamp card ➡ My Capital** (🤖 pick) · b) keep hero, shrink quote card · c) hero collapses on scroll
- **y**: a = only one th fits both sizes by budget (412×915: 599 o 730 · 360×760: 542 o 592) + keeps th card big (it's th content) · b ❌ smaller quote = worse share img match · c ❌ nothing fits b4 th 1st scroll
- **ck**: shots `today*` at 360×760: hero + card + actions + plan row visible · stamp card i `mine`
### D-035 @1113 · sounds (qa-R1 "Duolingo-satisfying"): 3 tiny, dflt off · ✔️ b @1114 (🦠 "sure lez try", lane L6)
- **options**: a) none, haptics only · **b) page rustle · stamp thud · badge chime (≤ 300 ms), 1 ⚙️ switch, dflt off, follows ringer** (🤖 pick, later lane L6) · c) on by dflt
- **y**: haptics 1st = 90 % o th feel 4 0 assets · sound = opt-in (reading i bed / train) · c ❌ surprise noise = uninstall
- **ck**: ⚙️ switch · no audio focus taken · APK + < 100 KB
### D-034 @1113 · daily Amen fx = CheckPop · sickle swoosh = rare (rank-up / full stamp card) · ✔️ b @1114
- **🦠 @1114**: "lez try b. not everyday (variable reward!!!) ➡ add 2 hooked mode" ➡ + 1 HookNote (Hooked mode on) at th rare swoosh: "this one is rare on purpose = variable reward (Hooked ch 4)" · lane L5 (Fx queue) + L1 (Hooks.kt)
- **ctx**: swoosh 2.3 s evy day + Orden dialog aft a fixed 2600 ms wait ➡ daily tap feels long, rewards collide
- **options**: a) as now · **b) daily = CheckPop 1.2 s + 📳 · swoosh + confetti only on rank-up / card full / plan done · 1 celebration at a ti (queue)** (🤖 pick)
- **y**: variable reward (Hooked) needs rare ≠ daily · D-004 calm · 🦠 QA: wants satisfying, not slow
- **ck**: `Fx.kt` queue · tst: 2 rewards same tap ➡ shown 1 aft th oth, no fixed delay
### D-033 @1113 · book font = system serif now · bundled face = 🧪 later · ✔️ b 🧪 @1114
- **🦠 @1114**: "try b! maybe ab-tst / cmp" ➡ L6: bundle 1 face (Literata vs EB Garamond) ➡ side-by-side shots `chapter` ×3 (system · Literata · Garamond) 4 🦠 2 pick · then maybe ⚙️ toggle = self A/B · non-Latin langs keep system serif
- **options**: **a) system serif (Noto Serif)** (🤖 pick) · b) bundle Literata o EB Garamond (OFL, +0.3-1 MB, Latin + Cyrillic only)
- **y**: 13 langs incl. ar · hi · bn · ur · zh ➡ a bundled face covers 3-4 o them, rest falls back = 2 looks · APK alr 29.7 o ~30 MB · b = 🧪 aft th system lands (1 token swap)
- **ck**: no `res/font` · `OmmType.book*` = `FontFamily.Serif`
### D-032 @1113 · dark theme "Nachtschicht": tokens now, wire later, follow system · ✔️ c @1114 (🦠 "C!": ⚙️ toggle light · dark · system, dflt system · lane L6)
- **options**: a) light only · **b) follow system** (🤖 pick, L6) · c) b + ⚙️ toggle (light · dark · system)
- **y**: night readers = real (NIGHT hero alr exists) · tokens cost nothing now + keep hard-coded colours out (guard) · wiring = own QA round (shots ×2) ➡ not i th 1st pass · c = 1 more setting, only if asked
- **ck**: `Tokens.kt` hs both schemes · `Theme.kt` uses light til L6
### D-031 @1113 · reader chrome: slim paper head, no bottom nav i a chapter · ✔️ b (try) @1114
- **🦠 @1114**: "not sure which is which · try b · but immersive mayb" ➡ build b i L2 · c stays 🧪 (mockups `d031_all.png` a · b · c) ➡ re-ask aft fon QA
- **ctx**: qa-D2 "book look: old · worthy · holy" · now red top bar + lavender nav frame th page = app, not book
- **options**: a) as now · **b) slim paper head (back · chapter · 💬 N), bottom nav hidden, status bar = page colour** (🤖 pick) · c) full immersive, tap = toggle chrome
- **y**: b = +~100dp page + th page becomes th screen, exit stays visible · c ❌ tap is taken (verse menu · double-tap turn D-022) + hidden exit
- **ck**: shot `chapter`: no NavigationBar · back works fr deep link
### D-030 @1113 · buttons (qa-D7): 5 kinds · look = pill tonal + press edge on th 1 big btn · ✔️ @1114 (🦠: A+)
- **ctx**: 🦠 "clunky-generic-microsoft98style", wants 2-3 mockups · r4 sweep #7: 7 btn looks mixed · 🦠 picked by eye fr th rendered gallery (`62be257`: now · A · A+ · B · C)
- **dc** ✔️ **A+**: Primary (red pill, L 52 / S 40, max 1 per view) · Tonal (blueTint pill) · Text · ActionItem (icon + label, Circle / Bare) · Chip (Choice / Suggest) + singletons (★ · Switch · MoreChip · CountChip · DogEar) · no outlined btns, no emoji i labels
- **options** (mockups ➡ design_system.md § 5): **A pill tonal + B's 4dp press edge on Primary-L only** (🤖 pick) · A pure · B letterpress evywhere · C rubric (txt + red rule)
- **y**: round = tappable holds · press edge + 📳 = th satisfying daily tap (qa-R1) wo making evy btn loud · C ❌ weak affordance 4 th daily Amen · A pure = calm but th 1 daily btn felt flat · B evywhere = loud + r8 breaks round = tappable
- **ck**: L0 @1114: `ui/ds/Buttons.kt` (Primary L/S · Tonal · TextBtn · ActionItem · singletons) + `Chips.kt` · `ds_gallery` shot · `DesignGuardTest`: M3 `Button(` / `OutlinedButton(` / `TextButton(` / `AssistChip(` / `FilterChip(` outside `ui/ds/` = baseline ➡ 0 by fan-in
### D-029 @1113 · design system = [design_system.md](design_system.md) SSOT · tokens i `ui/ds/` · ratchet guard · ✔️ @1114 (🦠: as written)
- **ctx**: M8 order (🦠): 1 spec 4 btns · nav · type · colour · space · components · motion · now: 21 txt sizes · ~60 hard-coded colours · 9 radii · `Theme.kt` fills 8 M3 roles (rest = lavender dflts)
- **dc** ✔️ (rules as written): 3 materials (paper · ink · cloth-blue) + red = act · gold = earned · round = tappable, rect = container · serif = Marx, sans = app + ppl · chrome = vector icons, emoji = content · flat (only th quote card lifts) · 9 txt sizes, sentences ≥ 14sp · 4dp grid · 5 radii · 5 voices (**my bubbles = blue, not gold**) · plan accents = stripe / disc / tag only, titles ink
- **y**: evy rule answers a QA line (qa-D2 book look · D6 voices · D7 btns · r4 #7) o a measured fail (gold txt 1.6 · 5 o 9 plan accents < 4.5 · `Color.Gray` 3.1)
- **alt**: restyle screen by screen wo tokens (❌ drifts again, = how we got here) · adopt a ready M3 theme builder palette (❌ loses paper + rubric)
- **ck**: `DesignGuardTest` baseline only goes down (L0 start @1114: `Color(0x` 48 · `.sp` 174 · `RoundedCornerShape(` 33 · btns/chips 48) · lanes L0-L5 ➡ design_system.md § 12 · ⚠️ overlaps D-015 (Amen ➡ snippet bottom, unbuilt) = lane L1
  - guard + `TextButton(` (spec § 11 listed 4 M3 btns, `TextButton` = th same drift, ds `TextBtn` replaces it) · ratchet strict both ways: lower count ➡ lower th baseline i th same commit
### D-028 @1113 · verse ids may move: split.py writes `data/id_moves.json` (old ➡ new by DE txt) + remaps pipeline data · app migrates marks + read log once · ✔️
- **ctx**: M8 follow-up (am0 "Go"): DE source typo "FÜFNZEHNTES KAPITEL" ➡ Kap 15 opening sat i Kap 14 (14,37-39) + Kap 15 ids ran on fr 15,40 · fixing = 30 ids move · users' 🔖 ✍️ + progress must not break
- **dc**: split.py `TYPOS` alias + guard (sec anchor w new Kap resets Absatz count) · moves = same DE txt, unique ➡ `id_moves.json` cumulative (chains composed) · same run renames ids i rank · andachten · i18n q · i18n_full v (no LLM re-run) · build.py ➡ assets `id_moves.json` · app `data/IdMoves.kt`: AppVm start ➡ if moves.size > Prefs `id_moves_done` ➡ 1 Room transaction renames Mark + ReadLog (collision ➡ merge) · DayLog = plan indices ➡ untouched
- **y**: fix th data at its src, not a display hack · DE txt = stable identity across splits
- **alt**: keep wrong ids forever (❌ Kap 15 = "15,40…", wrong footnotes) · alias table i th app only (❌ 2 id systems)
- **ck**: `IdMovesTest` (Robolectric + in-memory Room + real asset map: 4 tsts: map dead ➡ live ids · marks/reads follow · merge · runs once) · `bin/test_bin.py` · 1868 verses, 92 Andachten + ⭐288 kept
- **note D-024**: v1 `drop_toc` cut "toc ➡ nxt heading" = ate ch17's 3-Absatz intro ➡ now only contiguous toc/index lines · align: name anchors (EN mid-sentence capitals ⬄ DE txt + cited fns) + length may match txt+fns (Moore/Aveling inline long fns) ➡ 13,120-125 exact · churn 34 verses, sampled = fixes (V3 · 8 · 14 · 22 · 23)
### D-027 @1113 · curated plans = `data/plans_extra.json` · 🤖 Ludditen & KI: Kap. 13 verbatim + NEW "💡 Und heute?" bridge txt (xd/xe) · ✔️
- **ctx**: QA @1112 "add a luddites read plan and link directly to AI · xplaining txt new, bible-txt original" (R4) · big round #6
- **dc**: build.py `extra_plans()` appends hand-curated plans (verse ids only, unknown id = build fails) right aft th flagship · PlanDay `xd`/`xe` ➡ Today box "💡 Und heute?" + "new text — Marx's paragraph above is the original" · 7 days: 13,73 Aristoteles · 13,62 · 13,110 · 13,124-125 Ludditen · 13,127+130 · 13,165 "an sich ⬄ kapitalistisch angewandt" · 13,144 · Marxoskop "KI & Maschinen" ➡ card ➡ confirm ➡ `startPlan("ludditen")`
- **y**: bridge = separate field ≠ Andacht (Andacht = LLM devotional, bridge = authored argument) · Kapital txt stays untouched by construction
- **alt**: LLM bridges via andacht.py (❌ local M Marx errors) · new Marxoskop card only (❌ 1-shot, no habit)
- **ck**: `BigRoundTest.ludditesPlanIsKap13WithBridges` · oth langs = EN bridge til translate.py learns xd/xe (fan-in)
### D-026 @1113 · 🔓 Hooked mode = progressive disclosure: unlock aft 7 reading days (total) o 1st finished plan · own badge + unlock anim · ✔️
- **ctx**: QA @1112 "hooked mode only after 7 days or first read plan ➡ animation/award 'unlocked!'" · big round #5
- **dc**: `Orden.HOOKED` 🪝 "Hinter den Kulissen" (`hookedUnlocked`: dates ≥ 7 ‖ plansDone ≥ 1) · MainActivity: HOOKED new ➡ `HookedUnlockDialog` 1st (padlock wiggle ➡ shackle opens ➡ 🪝 drops + 📳 + confetti, btn "Einschalten" = testMode on) · ⚙️: locked row + days left + bar until earned · alr on = stays
- **y**: total days not streak (D-004 no anxiety) · dialog names its own trick (honest Hooked) · goal gradient i ⚙️
- **alt**: hide th row entirely til unlock (❌ no curiosity) · streak-7 (❌ anxiety)
- **ck**: `BigRoundTest.hookedUnlocksAfter7DaysOrFirstPlan` · shot `settings_2` = 🔒 row
### D-025 @1113 · 📜 endless feed aft today's snippet = book order, insta-style auto-load · snippets + "weiterlesen" on Today · ✔️ v1
- **ctx**: QA @1112 "after finishing the snippet ➡ continue for the motivated, endless feed, hooked" · am0 Q3 ✔️ auto-scroll like insta · r4 #6 plan verses = txt walls · big round #3 + #4
- **dc**: `VerseView(preview = true)` ➡ `snippet()` cuts at sentence end ≤ 320 chars + pill "weiterlesen ▾ · noch ~N Min." (🔊 reads all) · Today: plan verses + feed use it · aft "Tag geschafft" ➡ `ReadOnFeed` at th tail o Home: passages right aft today's last one i BOOK order, 3 per load when ≤ ~1800 px left (no tap) · chapter % bar · "⬇ gleich: »…«" teaser · ☕ break card evy 10 · scrolled-past = `vm.read` (Read % + badges), NOT plan days (D-011)
- **y**: book order = context (plans jump) · break cards keep D-014 honest tone · Marxoskop stays above (reachable)
- **alt**: LazyColumn rewrite o Today (❌ big hot-file churn) · "weiter" btn (❌ am0 Q3) · plan's nxt day (❌ breaks D-011 continuity)
- **ck**: `BigRoundTest` snippet/feedStart · shots `feed` `feed_2` · ⚠️ loads a bit ahead ➡ "read" marks passages b4 fully seen (v1)
### D-024 @1113 · EN reader data = pipeline: align.py drops chapter TOC · splits merged EN paras over DE Absätze · EN section titles fr marxists.org headings · ✔️
- **ctx**: r4 sweep #3 (DE sec titles i EN + "no English paragraph here") + #4 (ch.1 page 1 = TOC as body txt) · big round #1 #2
- **dc**: `drop_toc` (`<p class="toc">` ➡ nxt heading; also fixed ch.3 · 13 · 15 · 23 1st verses) · `split_en`: k-1 bead ➡ EN sentences cut by DE length share (199 ➡ 11 verses wo EN) · `Heads` stack per EN paragraph ➡ verse `en_head` ➡ build.py `en_sec_titles` (innermost label match 'a)'⬄'a.' · 'IV'⬄'4', else EN chapter title 4 DE Kap 4/24) ➡ verses.json `ste` ➡ `secTitle()` (DE ➡ st, else ste)
- **y**: no hand edits · plans + scores unchanged (DE txt identical, plans.json byte-equal) · full_xx untouched
- ⊕ **@1117 (11 ➡ 7 ➡ 0 gaps)**: sentence end aftr a closing quote too · `Mr.`/`Dr.`… no end · `PIN` (DE id ➡ EN opening) 4 DP mis-beads (13,58 · 20,13) = th only hand-given hints · 🏷️ labels (`[95]` · ch.23 "12. Worcestershire.") = own EN, nvr eat th body's 1st sentence · too few sentences ➡ ; / : cuts ➡ longest Absatz · tables keep "all on th 1st" · **ck**: `ContentTest.englishReaderData` = 0 blanks · `test_bin.py AlignEn`
- **alt**: LLM translate DE titles (❌ Moore/Aveling wording exists) · override table (❌ drifts)
- **ck**: `bin/test_bin.py AlignEn` · `ContentTest.englishReaderData` (all 90 DE titles hv EN) · ⚠️ known: 13,122-125 EN shifted ~1 para (pre-existing DP slip) · DE split typo "FÜFNZEHNTES" ➡ Kap 15 opening sits i Kap 14 (ids would shift ➡ not touched)
### D-023 @1113 · updater by installer: Obtainium ➡ poke it · self ➡ silent aft 1st · amends D-021 · ✔️ (fon tst pending)
- **ctx**: am0: Obtainium bg updates were on but he always beat its 6 h interval (deploy ➡ reload ➡ update by hand = same friction as th website) · "use Obtainium if it's there, else th website apk" · 6 h delay ok 4 users
- **dc**: `Update.Via` fr installer o record: OBTAINIUM ➡ notify only aft `GRACE_H` 8 h o th same version, tap = `obtainium://refresh?id=<pkg>` (Obtainium wiki deep link; no link can INSTALL) · SELF ➡ PackageInstaller session (not ACTION_VIEW) so WE become installer o record ➡ Android 12+ + `UPDATE_PACKAGES_WITHOUT_USER_ACTION` ➡ worker installs silently, `UpdatedReceiver` posts "✔️ X installed" · 1st self-update / < 12 ➡ 1 confirm · STORE ➡ nothing
- **y**: 0 taps 4 friends fr th 2nd update on · Obtainium keeps its job (we don't install over it: it declares ENFORCE_UPDATE_OWNERSHIP ➡ extra prompts)
- **alt**: install over Obtainium installs ourselves (❌ ownership prompt + 2 updaters fighting) · Shizuku (❌ setup 4 friends) · daily nag 4 Obtainium too (❌ noise, its bg wins mostly)
- **ck**: `UpdateWorkerTest` 8 (per installer · grace · silent · fallback) · `UpdateTest` via/silentRule · DEV ⚙️ "🧪 bg update check now · installer: X"
### D-022 @1113 · reader gestures: double-tap = turn page · 📐 dog-ear = turn · 1st chapters peek + hint · ✔️
- **ctx**: QA r5: am0 found page turn by accident ("NICE") ➡ needs discovery · "double tap = turn page, drop bookmark-via-double-tap"
- **dc**: `LocalPageTurn` (ChapterScreen) ⬅ VerseView `onDoubleClick` + paper `detectTapGestures` · `DogEar` bottom-right (tap = turn) · until 3 own turns (`omm_ui` SharedPrefs "turns"): page lifts 2× + pill "swipe or double-tap" ~6 s · 🔖 = verse menu only
- **y**: 3 cues (motion · object · words) w/o a tutorial screen · outside th book (Today) no double-tap ➡ tap reacts at once
- **alt**: HorizontalPager w dots (❌ loses th spine-hinged turn) · persistent ‹ › btns (❌ clutter on "pure Marx" page)
- **ck**: `ui/ReadScreen.kt` TURN_HINT · DogEar · grep `onDoubleClick` VerseView = turnPage
### D-021 @1113 · self-update 4 hand-installed APKs: daily bg check ➡ notification ➡ download + install dialog · ✔️
- **ctx**: QA r5 🔑: friends install th APK directly ➡ must nvr sit on a stale version · audit: only `Update.check` on app start ➡ Today banner ➡ opened th web page (manual download) · DEV: Changelog line "➡ Obtainium" only · no bg, no notification
- **dc**: `notify/UpdateWorker.kt` WorkManager 1 d (network) ➡ `Update.check` on th app's own channel (`.dev` ➡ `<web>/dev/version.json`, else `<web>/version.json`) ➡ notification "🆕 X is out" (new version at once, same one again evy 2 d) ➡ tap / Today banner ➡ `UpdateDialog`: download `apk` fr version.json (plain name only, sha256 checked) ➡ `ACTION_VIEW` apk via FileProvider (`REQUEST_INSTALL_PACKAGES`; 1st time ➡ "allow this source" page, resumes by itself) · fail ➡ web page
- **y**: same debug key ➡ installs over, data stays · installer = Obtainium/F-Droid/Play ➡ skip (they update themselves) · lib.sh alr writes apk + sha256
- **alt**: PackageInstaller session API (❌ more code, same system dialog) · in-app silent install (❌ impossible wo device owner) · Obtainium 4 all (❌ friends won't)
- **ck**: `UpdateTest` (parse · channel · nag · sha · installer) + `UpdateWorkerTest` (Robolectric, fake version.json ➡ notification, no repeat, older = silent) · manifest REQUEST_INSTALL_PACKAGES
### D-020 @1113 · Mitlesen always on (toggle gone) · name field only i ⚙️, label "Name" · supersedes D-005 switch + D-016 label · ✔️
- **ctx**: QA r5: "rm the Mitlesen/read-along option + its txt (always on)" · "label just 'Name'" · sweep #8 name field on ⚙️ + Kreis (+ comments sheet)
- **dc**: `Settings.community = true` (old stored off ignored) · ⚙️ Name field (D-016 prefill + blur-save stay) · Kreis + comments: `WritesAs` line "you write as X · change: ⚙️"
- **y**: am0 · 1 place 2 change identity · still anonymous daily token (D-005), UI says nothing about it now
- **alt**: keep switch (❌ am0) · name field evywhere (❌ 3 fields drift)
- **ck**: grep `setCommunity` i ui/ = 0 · `nameLabel()` = "Name"
- **note D-017**: QA r5 amends th note editor: no ★ (only app-wide close elsewhere) · ANY short tap while open = close (save if changed, blank = gone) + th tap is eaten (can't toggle th action row) · 👥 chip exempt · look = `NoteBubble` (side ruler · ✍️ icon · blue tint · sans)
### D-019 @1113 · headless screenshots = Robolectric native graphics (`ShotsTest`, -Pshots) · ✔️
- **ctx**: am0 r4: "design keeps slipping thru ➡ compare screens side by side" · no /dev/kvm on tow ➡ emulator ❌ · fon screencap = private (memory)
- **dc**: `test/…/ShotsTest.kt` drives th real MainActivity (tabs · chapter · verse menu · note · settings · In book) + cardBitmap variants ➡ `app/build/shots/*.png` · skipped i normal `testDebugUnitTest`
- **y**: real app, real Compose, 20 s, no device · catches overlaps/stale copy before am0's fon
- **alt**: emulator (no KVM) · Paparazzi (lib modules only) · Roborazzi (extra plugin, same engine)
- **ck**: `./gradlew testDebugUnitTest --tests '*ShotsTest*' -Pshots` · ⚠️ Robolectric fonts: newer emoji (🪝) = tofu, ignore
### D-018 @1113 · no share i th reader: verse menu = 🔊 · 🔖 · ✍️ · 📋 · page share pills gone · ✔️
- **ctx**: QA r1 "2 boxes i each other look weird · drop sharing here?" · r4 "rm share from th Read page" · r4 "Listen on th LEFT"
- **dc**: rm verse-menu Teilen (also on Today's plan verses = same menu) + r3's 2 pills at page end · `CardDialog` + `LosungPopup` deleted (dead) · 🔊 1st i evy action row
- **y**: Read = pure Marx · sharing lives on th Today card (Karte teilen · App teilen)
- **alt**: keep verse share on Today plan only (❌ 2 dif menus) · 💬 am0 may want it back
- **ck**: grep `CardDialog` = 0 · VerseView Action order
### D-017 @1113 · writing on th book = 1 pattern: tap passage ➡ ✍️ Notiz (inline) · 👥 chip = also public · ✔️
- **ctx**: r4: upper "Notiz" + lower "comment on this page" = same function, 2 looks ➡ merge · note editor: tap outside = save, empty = cancel, small X, no Save/Cancel words
- **dc**: rm page-end "Mitreden" btn · inline editor: tap outside (root watcher `tapOutsideNote`) = commit · ★ (CloseButton, app-wide close) = discard · blank = no note/note gone · Mitlesen on ➡ chip "👥 auch im Lesesaal posten — als <name>" posts "¶ <id> — txt" 2 th page's room · reading oth's = 💬 N i th running head ➡ sheet
- **y**: 1 entry, 1 composer, privacy = 1 toggle · comment keeps its passage ref
- **alt**: 2 btns side by side i th menu (Notiz · Kommentar) · Matrix field 4 verse id (later, if threads per passage wanted)
- **ck**: VerseView `NoteFocus` · ReadScreen has no OutlinedButton · ★ not ✕ = r1 rule "red star closes" — 💬 am0 said "small X"
### D-016 @1112 · public name = "Lesesaal-Name", prefilled random comrade name, saves on blur · ✔️ (label 💬 am0 may swap)
- **ctx**: QA r3: "Your name" label dull · empty field = friction · Save btn = extra step
- **dc**: label "Lesesaal-Name / Reading-room name" · prefill = socialist thinker 1st name + movement year (Rosa 1871 …), 1 per app run · ⚙️ field saves on blur/Done only if changed
- **y**: am0 r3 ask · label fits D-014 reading-room theme · no Lenin/Stalin/Mao (D-014: lens, not regime)
- **alt**: Genoss:in (gendered/party-ish) · Kampfname (militant) · Name / Username (generic) ➡ am0 picks if he prefers
- **ck**: `nameLabel()` + `comradeName()` i `ui/Comments.kt` · `NameTest.kt`
### D-015 @1112 · streak = Amen (ez 1 tap), placed under th reading snippet · ✔️ (not built) · amended @1112
- **ctx**: streak counted quote Amen; plan "done" looked meagre next 2 it (Mi) · options a any day · b plan days · c hybrid
- **dc**: Amen = th streak action, stays ez (1 tap, no read-gate / timer) · 1 place: bottom o th reading snippet (rm card + hover dup) ➡ th investment (Hooked/IKEA) = actually reading a bit
- **y**: am0 @1112: "its not the quote … its the actual book" + "Amen counts 4 th streak, keep it an ez action · investment = reading a bit"
- **alt**: a/b/c chip (❌) · gate Amen on scroll/ti read (❌ friction)
- **ck**: (aft build) Amen only i read snippet · Amen btn i `Cards.kt` = 0 · streak tst
### D-014 @1112 · Orden ➡ Abzeichen (badges), reading-room blue/gold · ✔️
- **ctx**: Mi feedback: Orden = too real-socialism/authoritarian, ☭ evywhere
- **dc**: rename + re-theme (reading-room look), positive reframes ("missed a day" tone out), stamp gift = ☭, Marx = lens not utopia
- **y**: user feedback (Mi) via am0 · **alt**: keep Orden (❌ alienates non-hardcore users)
- **ck**: `grep -rn "Orden" ui/` = only enum/code names, UI text says Abzeichen/badges · `OrdenTest.kt`
- **amend @1112 (am0)**: tone = positive + friendly, **nudging allowed** (miss ➡ kind nudge, e.g. sad Marx) · ❌ guilt / shame / loss threats · ck: nudge strings carry no blame ("verpasst" / "verloren")
### D-013 @1112 · Krisenfest = 15 shakes · ✔️
- **ctx**: crisis medal too easy · **dc**: `KRISE_SHAKES = 15` · **y**: Mi: "needs more shakes" · **ck**: `logic/Orden.kt` KRISE_SHAKES
### D-012 @1112 · 1 sober voice only · ✔️
- **ctx**: holy voices (Gottvater, Gott, Pfarrer…) cringe · **dc**: `Persona.picker = [NORMAL]`, rest hidden i code, not deleted · old stored dflt ➡ sober
- **y**: Mi + am0 agree · **alt**: keep as opt-in (❌ clutter) · **ck**: `Persona.picker` i `ui/Share.kt` · `VoiceTest.kt`
### D-011 @1112 · reading plan: continuous, no repeat, evy1 i a default plan · ✔️
- **ctx**: real-user 🐛 same page 2× · plan didn't train regular reading
- **dc**: `planIndex` continuity, finished plan doesn't repeat, default plan 4 all · **y**: Mi feedback · **ck**: `PlanTest.kt`
### D-010 @1031 · UI strings inline DE+EN i code, rest via translate.py · ✔️
- **ctx**: 13 langs, lone dev · **dc**: `T("de","en")` / `tr(..)` pairs i Kotlin (key = EN) ➡ `bin/translate.py --only ui` ➡ i18n_xx.json · ❌ translate_full 4 UI · 1 pass at fan-in
- **y**: am0 · **alt**: strings.xml per lang (❌ 13× by hand)
- **ck**: `bin/translate.py --only ui` finds 0 new · only launcher/shortcut/tile names i `res/values*`
### D-009 @1108 · GitHub release + Forgejo origin, gh fetch-only · ✔️
- **y**: mirror carries tags 2 GH; ship.sh waits ≤120 s · **ck**: `git remote -v` · `bin/ship.sh`
### D-008 @1107 · Obtainium DEV: stamp version into APK's 1st zip entry · ✔️
- **ctx**: default pseudo-version hashes 1st 1 KiB = identical across builds ➡ never updates
- **dc**: ommStamp + deploy guard · **y**: root cause measured · **ck**: `ref.md` (trap documented) · `bin/deploy.sh`
### D-007 @1107 · 1 dialog style (MarxDialog), ☭ = close only · ✔️
- **y**: am0: "windows look weird — frame colour, no frames, ☭ at odd spots" · **ck**: `ui/MarxDialog.kt` · no raw AlertDialog elsewhere
### D-006 @1105 · repo public, GPL-3.0, `priv/` git-ignored · ✔️
- **ctx**: friends need build-it-yourself · **dc**: public GitHub am0ebe/oh-my-marx, Hooked OCR out o tree + history · **y**: ❓ am0 · accepted risk: GH keeps old SHAs reachable
- **ck**: `.gitignore` priv/ · LICENSE
### D-005 @1103 · community = Mitlesen opt-in (dflt ON), anonymous daily token · ✔️
- **ctx**: social proof (Hooked tribe) vs privacy · **dc**: counter + Matrix comments/Kreise, switch off i ⚙️, not E2E (said i UI) · **y**: ❓ am0 (dflt ON by am0)
- **ck**: `data/Community.kt` · `Prefs.community` · 💬 toggle's fate = open Q
### D-004 @1101 · 🔥 streak w 🕊️ Sabbat-Joker + Stempelkarte counts *total* days · ✔️
- **y**: streak anxiety ➡ 1 miss = rage-quit (notes.md § judgment) · **alt**: plain streak (❌) · **ck**: Prefs flag "sabbath" · `logic/Daily.kt` · `OrdenTest.kt`
### D-003 @1028 · Android native Kotlin+Compose, offline, no server (server added @1103 opt-in) · ✔️
- **y**: L project + friends sideload · **ck**: `android/` · INTERNET perm only 4 Community/Update
### D-002 @1028 · book src = MEW 23 (DE PD) + Moore/Aveling 1887 (EN PD) · ✔️
- **y**: am0 owns MEW 23 ➡ page refs match · **alt**: DTA 1867 (❌ CC BY-NC, dif structure) · **ck**: `bin/fetch.py` · `ContentTest.kt`
### D-001 @1031 · UI langs DE + EN, EN dflt (fon lang if supported), DE 2nd · ✔️
- **y**: ❓ am0 · **ck**: `data/I18n.kt`

## ❓ open Qs
- 💬 **streak**: (a) any reading day · (b) plan days only · (c) hybrid chip — Mi: 2nd "done" looks meagre next 2 big streak widget
- 💬 **Mitlesen toggle**: rm? (QA lane B removed th note only) · 💬 ☭ close btn i MarxDialog: keep/rm? · 💬 "share app" prompt aft streak/medal?
- ❓ streak widget "shd show 2 at start" = ? (todo ‼️ A)
- later: Read = real book look + page flip · daily verse can't be turned off · Lesekreis progressive disclosure

## 🔍 drift (checked @1112)
- none vs ✔️ Ds · note: Changelog.kt old release lines still say "Heilige Stimmen"/"Orden" = history, ok

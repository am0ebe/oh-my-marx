# design system — omm ☭ 🎨
_spec @1113 · docs only, ❌ code yet · SSOT 4 look + feel: rules ➡ tokens ➡ components ➡ per-screen deltas ➡ build lanes · § 14 addendum @1119 (celebrations · holidays · holy book · note · close, D-045..055)_
_y lives i [arch.md](arch.md) D-029..036 (💬 = open, 🤖 pick marked) · base = app @ `eb033ae` (`ui/Theme.kt` + 20 `ui/*.kt` + headless shots) · [qa_1112.md](qa_1112.md) § 🎨 qa-D1..D10 + r4 sweep #7_
_contrast = WCAG 2.x, computed (text ≥ 4.5 · large txt / icon / border ≥ 3) · sizes = dp / sp_

## 0 · 💬 open choices (🦠 decides · "picks ok" = enough)
| D | Q | options | 🤖 pick |
|---|---|---------|---------|
| D-029 | base rules o th spec (§ 1) | ✔️ as written · change X | ✔️ **as written** (🦠 @1114) |
| D-030 | btn look (qa-D7, mockups § 5) | A pill tonal · B letterpress · C rubric | ✔️ **A + B's press edge on th 1 big btn** (🦠 @1114) |
| D-031 | reader chrome | a) as now · b) slim paper head, no bottom nav · c) immersive, tap = toggle | ✔️ **b** (try, c = 🧪) 🦠 @1114 |
| D-032 | dark theme | a) light only · b) follow system · c) + ⚙️ toggle | ✔️ **c** 🦠 @1114 (L6) |
| D-033 | book font | a) system serif · b) bundle 1 (Literata / EB Garamond, +0.3-1 MB) | ✔️ **b 🧪** 🦠 @1114 (cmp shots, L6) |
| D-034 | daily Amen fx | a) sickle swoosh evy day (now) · b) CheckPop daily, swoosh = rank-up / full card | ✔️ **b** + Hooked note 🦠 @1114 |
| D-035 | sounds (qa-R1) | a) none · b) 3 tiny (page · stamp · badge) + ⚙️ switch, dflt off | ✔️ **b** 🦠 @1114 (L6) |
| D-036 | Today above th fold (qa-D1) | a) compact hero + stamp tag · b) keep hero, shrink card · c) hero collapses on scroll | 💬 **a** · mockups ➡ `d036_all.png` |
- 👀 mockups 4 th 2 unsure picks (`DsChoicesTest`, test-only): `android/app/build/shots/d031_all.png` · `d036_all.png`
- 🆕 **D-045..055** (celebrations · holidays · holy book · note · close) ➡ own table i § 14.0 (bottom)

## 1 · 🔑 concept — a book th acts like an app
- **3 materials**: 📄 Paper = surfaces · ✒️ Ink = text · 📘 Cloth = MEW blue (app's voice, 2nd actions)
- **2 signals**: 🔴 Red = act (primary btn, rubric, links i th book) · 🟡 Gold = kept / earned (🔖 · stamps · badges · active plan) — gold nvr = "tap me", red nvr = decoration
- **round = tappable · rect = container** — pill / circle ➡ u can press it · rounded rect ➡ it holds stuff
- **serif = Marx · sans = app + ppl** — Kapital txt nvr sits i a box, evything else may
- **chrome = vector icons · content = emoji** — nav / btns / menus = mono icons · emoji only i content (plan icon, badge, callout lead) · brand marks at fixed spots: ★ = close · ☭ = stamps, Mine tab, app name
- **1 loud thing per screen**: 1 Primary btn · 1 lifted object (quote card) · 1 celebration at a ti
- **sunset gradient = quote card (+ evening hero) only** — nvr i chrome
- **honest + kind** (D-004 · D-014): no red badges, no guilt copy, no infinite attention anims · Hooked yellow = th app showing its own tricks

## 2 · 🎨 colour
### light "Papier" (dflt)
| token | hex | use | contrast |
|-------|-----|-----|----------|
| `paper` | #F6EEDC | app bg | — |
| `paperHi` | #FFFBF2 | panels · dialogs · sheets · fields | — |
| `paperDeep` | #EDE3CC | nav bar · oth ppl's bubbles · tracks · chips off | — |
| `page` | #F3E7CF | reader page | — |
| `ink` | #3B2A1A | all body txt · titles | 11.9 paper · 11.2 page · 10.7 paperDeep |
| `inkMuted` | #6B5A48 | meta · hints · legal · unselected nav | 5.7 paper · 6.4 paperHi · 5.2 paperDeep |
| `line` | ink 14 % ≈ #DCD3C1 | hairlines, dividers (decor only) | 1.3 |
| `outline` | ink 55 % ≈ #8F8271 | field borders, locked badge dash | 3.3 paper · 3.6 paperHi |
| `red` | #B3261E | Primary btn · rubric heads · in-book links · selected nav | 5.7 paper · 6.3 paperHi · 5.3 page · white on it 6.5 |
| `redDeep` | #8C1D18 | press edge · error txt | 7.9 paper · white on it 9.1 |
| `redTint` | red 10 % ≈ #EFDAC9 | selected chip · nav indicator · "read more" pill | red on it 4.8 |
| `blue` | #1F2F4F | Tonal + Text btns · quote txt · app voice | 11.5 paper · white on it 13.3 |
| `blueTint` | blue 10 % ≈ #E0DBCE | Tonal btn · app callout · own note / msg | blue on it 9.6 |
| `gold` | #E8B931 | fills only: stamps · medals · 🔖 · active ring | ⚠️ 1.6 on paper ➡ nvr txt / thin icon on light · ink on it 7.5 |
| `goldDeep` | #7A5C00 | txt th means "earned / active" | 5.4 paper · 6.1 paperHi |
| `goldTint` | gold 18 % ≈ #F3E4BD | reward callout · break card | ink on it 10.9 |
| `hook` / `onHook` | #FFF1B8 / #3A2E00 | Hooked post-its only | 11.8 |
| `ok` | #2E7D32 | ✔ icons (≥ 18dp) · CheckPop ring | 4.4 paper ➡ icon / large only |
| `dev` | #1565C0 | DEV build marks only | — |
- 🚫 kill: `Color.Gray` (3.1 on paper) · `#6B6275` · `#B8860B` as txt (2.8) ➡ `inkMuted` / `goldDeep`
- 🚫 red = M3's dflt `error` (#B3261E) ➡ set `error = redDeep` + errors alw = icon + words, nvr colour alone
- highlights (4 markers @ 40 % alpha) stay as is = user content, not chrome

### M3 scheme = fill evy role (fixes lavender nav bar + grey cards 4 free)
- `primary` red · `onPrimary` white · `primaryContainer` redTint · `onPrimaryContainer` redDeep (6.8)
- `secondary` blue · `onSecondary` white · `secondaryContainer` blueTint · `onSecondaryContainer` blue
- `tertiary` gold · `onTertiary` ink · `tertiaryContainer` goldTint · `onTertiaryContainer` ink
- `background` paper · `onBackground` ink · `surface` paperHi · `onSurface` ink · `surfaceVariant` paperDeep · `onSurfaceVariant` inkMuted
- `surfaceContainerLowest` / `Low` / `High` = paperHi · `surfaceContainer` / `Highest` = paperDeep · `surfaceTint` = paperHi (no tonal tint)
- `outline` outline · `outlineVariant` line · `error` redDeep · `onError` white · `errorContainer` redTint · `inverseSurface` ink · `inverseOnSurface` paper · `scrim` black
- status bar: tab roots = red (as now) · reader = page colour w dark icons (if D-031 b)

### plan accents (9, `PLAN_COLORS`) — keep th hues, change th job
- 🐛 as title txt on white: orange 3.0 · blue 3.7 · pink 3.8 · olive 4.0 · teal 4.3 ➡ fail
- rule: accent = **stripe · emoji disc · tag bg** only · titles = `ink` · tag = accent 14 % + ink txt (≥ 10) · "✔ active" = `goldDeep`, not gold

### daytime hero (keep `Feast.kt` DayTime gradients)
- morning · day · evening · night gradients stay + 15 % black scrim · txt on it = white only, ≥ 14sp o bold
- feast days may swap th gradient, nvr th layout

### dark "Nachtschicht" (tokens fixed now · wiring = D-032)
| token | hex | contrast |
|-------|-----|----------|
| bg | #15120E | — |
| surface | #201B15 | ⚠️ 1.1 vs bg ➡ panels need th hairline (#332F29) |
| surfaceVariant | #2E271F | — |
| ink / inkMuted | #EDE3CC / #B3A792 | 14.6 · 7.9 on bg · 13.4 · 7.2 on surface |
| primary (fill) | #C8372D | white on it 5.2 |
| red (txt) | #FF8A7A | 8.2 bg · 7.5 surface |
| blue (txt) / container | #A9BEE8 / #1F2F4F + #DCE6FF | 10.0 · 10.7 |
| gold | #E8B931 | 10.2 bg ➡ may be txt i dark |
| reader page / ink | #1B1712 / #D9CDB8 | 11.4 |
| hook / onHook | #3A2E00 / #FFF1B8 | 11.8 |
| outline | #767064 | 3.8 |
- quote card + hero gradients = same i both themes (they r pictures)

## 3 · 🔤 type — 21 sizes ➡ 9
_now: 21 dif `.sp` · 13sp ×43 · 12sp ×36 · Bold ×74 · no `Typography` set ➡ evy screen invents its own_
| style | size / line | weight | use | M3 slot |
|-------|-------------|--------|-----|---------|
| display | 28 / 34 | 700 | streak number · fx | `displaySmall` |
| title-l | 22 / 28 | 700 | screen title | `titleLarge` |
| title-m | 18 / 24 | 700 | panel + dialog title | `titleMedium` |
| title-s | 16 / 22 | 700 | section heading | `titleSmall` |
| body-l | 16 / 24 | 400 | main UI sentences | `bodyLarge` |
| body-m | 14 / 20 | 400 | 2nd sentences · bubbles · callouts | `bodyMedium` |
| label-l | 14 / 20 | 500 | btn txt | `labelLarge` |
| label-m | 12 / 16 | 500 | chips · action labels · tags · counters | `labelMedium` |
| label-s | 11 / 16 | 500 | verse refs · timestamps · legal (**floor**) | `labelSmall` |
| book-title | 24 / 30 serif | 700 | chapter title, centred | `headlineSmall` |
| book-head | 17 / 24 serif | 700 | section title i th book, **red** (rubric) | ext `OmmType.bookHead` |
| book-body | 17 / 26 serif × readScale | 400 | Kapital txt (now 16 / 23) | ext `bookBody` |
| quote | 22 / 30 serif italic | 400 | quote card · `QuoteText` | ext `quote` |
| book-meta | 11 / 16 caps, +1.2 tracking | 500 | running head · folio | ext `bookMeta` |
- **map**: 10 · 11 ➡ 11 · 12 · 13 ➡ 12 (label) o 14 (sentence) · 14 · 15 ➡ 14 · 16 · 17 ➡ 16 · 18-21 ➡ 18 · 22-24 ➡ 22 · 26+ ➡ 28
- 🔑 **a full sentence is nvr < 14sp** — too long 4 14 ➡ cut th txt, don't shrink it (Kreis + ⚙️ legal lines now 10-11sp)
- weights 400 · 500 · 700 only · bold = titles · Primary btn · numbers (now bold = evywhere ➡ means nothing)
- 13 langs: `start` / `end` nvr `left` / `right` (ar · ur = RTL) · room 4 +40 % length (DE · ru) · no all-caps except book-meta · non-Latin = system fallback face · 🚫 Fraktur
- font scale: layouts must survive system 1.3× (btn txt wraps 2 2 lines, nvr clips)

## 4 · 📏 space · shape · depth · icons
- **grid 4**: `2 · 4 · 8 · 12 · 16 · 24 · 32 · 48` — kill 6 · 10 · 14 · 18 · 22 (➡ nearest)
  - screen gutter 16 · panel pad 16 · betw panels 12 · betw sections 24 · inside a group 8 · icon ⬄ label 4
  - reader: 24 start · 20 end (spine side wider)
- **tap target ≥ 48** even wh th visual is 32-36 (CloseStar · chips · bare actions)
- **radius**: `xs 6` verse frame · `m 12` callouts, bubbles, fields · `l 16` panels, hero, quote card · `xl 24` dialogs, sheet top · `full` btns, chips, tags — kill 8 · 10 · 18
- **depth = flat**: panels = paperHi + 1dp `line`, 0 shadow · quote card = th only lifted thing (8dp) · dialog 6dp + scrim · ❌ M3 tonal elevation
- **icons**: 1 mono family (as now), 20dp i btns / rows · 24dp i nav + top bar · tint = txt colour o th row · emoji nvr inside a btn label

## 5 · 🔘 buttons (qa-D7 · D-030)
_now 7 looks: `Button` · `OutlinedButton` · `TextButton` · `AssistChip` · `FilterChip(selected=false)` as btn · `CardAction` circles · bare `Action` ➡ "clunky-generic"_
### 5 kinds — nothing else
| # | kind | look | wh | max |
|---|------|------|----|-----|
| 1 | **Primary** | red fill · white label-l bold (L = 16sp) · pill · **L** h 52 full-width / **S** h 40 hug | THE action: Amen · day done · start plan · install · send | 1 per screen / dialog / panel |
| 2 | **Tonal** | blueTint fill · blue label-l · pill h 40 | 2nd actions: found / join · copy link · share targets · refresh | 2 side by side |
| 3 | **Text** | blue label-l, no fill · h 40 | defer / leave / more: "Later" · "Show me" | — |
| 4 | **ActionItem** | mono icon + label-m under it · 48 target · style **Circle** (blueTint disc 44 + blue icon · red disc = 2 loud next 2 Primary) o **Bare** | icon rows: quote card row · About row = Circle · verse menu i th book = Bare | 5 per row |
| 5 | **Chip** | pill h 32 · label-m · **Choice** (toggle: off = paperDeep, on = redTint + ✓ + red txt) o **Suggest** (tap = go: paperHi + `outline`) | persona · "also post 2 reading room" = Choice · Marxoscope topics · Hooked bench = Suggest | wraps |
- **singletons** (1 look, fixed spot): ★ CloseStar (36 visual, top-end o dialogs + sheets) · Switch (red track) · MoreChip "read more ▾" (redTint) · CountChip "💬 N" · DogEar 📐
- 🚫 gone: outlined btns · AssistChip · emoji i labels ("🤖 Los" ➡ "Start") · 2 Primaries i 1 view · full-width Tonal stacks (share panel ➡ list rows)
- **destructive** (leave circle · delete note): Text btn i `redDeep` ➡ confirm MarxDialog (🐛 now "Leave circle" leaves at once)
- **states**: pressed = 8 % ink overlay (+ press edge on Primary-L, see B) · disabled = ink 12 % fill + ink 38 % txt · loading = label ➡ 18dp spinner, width stays · focus ring 2dp blue
- **labels**: sentence case · verb 1st · Primary-S ≤ 12 chars (check DE + ru) · in-book links (footnote · read more) = red, not a btn
- **order i a row**: Text (defer) start · Primary end · dialogs = stacked (§ 7)

### 3 directions (pick 1 ➡ D-030)
```
 A · pill tonal               B · letterpress              C · rubric
╭────────────────────╮      ┌────────────────────┐
│  Amen — read today │      │  Amen — read today │        Amen — read today
╰────────────────────╯      ├────────────────────┤        ━━━━━━━━━━━━━━━━━
 red pill · flat             │▓▓▓ 4dp redDeep ▓▓▓▓│        serif ink + 2dp red rule
                             └────────────────────┘        no fill at all
( Copy )  ( In book )        [ Copy ]  [ In book ]         Copy · In book
 blueTint pills               paperHi + ink edge, r8        red underlined words
   Later                        Later                        Later
```
- **A** 👍 calm, modern, round = tappable holds · 👎 least "own"
- **B** 👍 sinks 3dp on press + 📳 = satisfying (Duolingo feel, qa-R1) + fits a printing press · 👎 loud if evy btn does it · r8 breaks round = tappable
- **C** 👍 most bookish · 👎 weak affordance, 48dp targets hard, bad 4 th daily Amen
- 🤖 **pick: A evywhere + B's press edge only on Primary-L** (pill keeps its shape, 4dp redDeep under it, sinks on press) ➡ th 1 daily btn feels physical, rest stays quiet
- ✔️ **A+ picked by eye** (🦠 @1114, directions render = `62be257`) ➡ built i `ui/ds/` (L0) · whole system i 1 img: `DsGalleryTest` ➡ `android/app/build/shots/ds_gallery.png` · run: `./gradlew testDebugUnitTest --tests "*DsGalleryTest*" -Pshots --rerun`

### now ➡ ds
| now | ➡ |
|-----|---|
| `Button` full-width (Amen · Tag geschafft · install · Weiter so) | Primary-L |
| `Button` small (Senden · OK · Found · Join · Los) | Primary-S |
| `OutlinedButton` (plan entry · share targets · leave) | Tonal · list row · destructive Text |
| `TextButton` (Später · Show me · Refresh) | Text |
| `AssistChip` ×9 (Hooked bench) · `FilterChip(selected=false)` (Marxoscope) | Chip Suggest |
| `FilterChip` (persona · also post) | Chip Choice |
| `CardAction` | ActionItem Circle |
| `RowScope.Action` (verse menu) | ActionItem Bare (label 10 ➡ 12sp) |

## 6 · 🧭 nav
- **keep**: 5 tabs (Today · Plans · Read · Circle · My Capital) + top bar (Marx = home · lang · ⚙️) — structure is fine, only th paint is off
- **bottom bar**: bg `paperDeep` + 1dp `line` on top · indicator `redTint` · selected icon + label = `red` · unselected = `inkMuted` · labels alw shown, label-m, 1 line (tst longest i 13 langs, else shorten th word)
- **top bar**: tab roots = red, white title-m · sub-screens (plan detail · circle detail · ⚙️ · Hooked) = `paper` bar, ink title, back arrow — red bar = "u r home"
- **reader** (D-031 pick b): slim paper head (back · chapter · 💬 N), no bottom nav, status bar = page colour ➡ th page is th screen · leave = back / swipe fr edge
- **back** alw returns 2 th tab root it came fr · deep links (`chapter/{kap}?v=`) land w th verse flashed (as now)
- **sheet ⬄ dialog**: sheet = lists + compose (comments · share targets · pickers) · dialog = decide / reward / info

## 7 · 🧩 components
### surfaces
| name | look | use |
|------|------|-----|
| **Panel** | paperHi · 1dp line · r16 · pad 16 · flat | plan card · settings group · circle card |
| **AccentPanel** | Panel + 4dp start stripe + 44 emoji disc (accent 14 %) | plans (title = ink) · active = 2dp gold ring + "active" tag |
| **Callout**(tone) | tint fill · r12 · pad 12 · lead emoji + title-s + body-m · no border | gold = reward / news (update banner · break) · blue = app talks / new txt (bridge · AI offer · nudge) · accent = Andacht · hook = Hooked post-it |
| **QuoteCard** | sunset gradient · r16 · 8dp shadow · quote style white | Today carousel + share bitmap (same ratio 1080×1200) |
| **Hero** | daytime gradient + scrim · r16 · pad 16 | Today top |
| **Tag** | pill h 20 · label-s · accent 14 % + ink | "Day 3" · "active" · "new" |
- nesting: Panel > Callout ok · Callout > Callout ❌ · Panel > Panel ❌ (now: white card ➡ Andacht box ➡ verse frame ➡ bridge box = 3 deep)
- lists inside a Panel = rows 56 high + `line` dividers, no cards i cards

### 🗣️ 5 voices (qa-D6: "mine ⬄ others ⬄ Marx at a glance")
| who | face | container | colour |
|-----|------|-----------|--------|
| **Marx** (Kapital) | serif | none — bare on paper / page | ink |
| **me** (notes · my msgs) | sans | start ruler 3dp + tint, r12 end corners | blue + blueTint |
| **others** (comments · circle chat) | sans | bubble r12, name + time label-s on top | paperDeep |
| **app** (Andacht · bridge · hints) | sans | Callout | by tone |
| **Hooked** (th app about its tricks) | sans | post-it r12 | hook yellow |
- 🔑 change: my bubbles i comments + chat = **blue** (now gold) ➡ same "me" as my margin notes · gold stays = earned
- Andacht + bridge r NOT Marx ➡ sans + Callout + label "new text" (D-027) — nvr serif

### dialogs + sheets (D-007 stays)
- `MarxDialog`: paperHi · r24 · gold hairline (55 %) · pad 24 · title-m centred · ★ top-end
- actions = **stacked**: Primary-L full-width · Text "Later" centred under it (only if deferring means sth, else ★ is enough) — kills th cramped end-aligned row
- reward dialogs (badge · unlock · plan done): 1 at a ti, queued · medal 96 · 1 line y u got it · Primary "Keep going"
- sheet: paperHi · r24 top · drag handle · title-m + ★ · content scrolls, composer sticks 2 th bottom above th keyboard
- copy / saved feedback = 📳 + inline ✓ on th control 1.2 s — ❌ Toast

### notes (D-017 · D-020 stay)
- `NoteBubble` = th "me" voice: ruler 3dp blue · blueTint · ✍ 16 · body-m · sits under its verse, indent 12
- editor: same bubble + cursor, tap outside = save · ★ = discard · Chip Choice "also post 2 reading room — as X"
- marked verse = goldTint bg + 1.5dp gold frame r6 (kept) · open verse = 1dp `outline` frame · flash = 2 pulses then fade
- oth ppl's notes on a page = CountChip "💬 N" i th running head ➡ comments sheet

### comments sheet (qa-D5)
- head: title-m "Reading room · ¶ ref" + ★ · 1 hint line body-m, only while empty
- empty = small ✍ + "Write a margin note" (ghost i th composer) — 🚫 "be the first" line gone
- bubbles per voices table · name label-s bold + time label-s muted · own = end-aligned blue
- composer: field r12 + Primary-S "Send" · "writes as X" = label-s above th field, tap ➡ rename · legal ➡ 1 ⓘ icon ➡ dialog (now 10sp wall)

### badges (D-014)
- earned = gold disc + ribbon (as now) · **locked = paperDeep disc + dashed `outline` ring + emoji silhouette 30 %** (now grey wall)
- nxt reachable badge = progress ring (red arc) + "2 more days" label-m ➡ goal gradient, 1 at a ti
- grid: 3 per row via `weight(1f)` (now fixed 100dp cells) · label-m 2 lines max · earned 1st, then nxt, then rest
- hidden badges = "?" disc, no label

### feed (D-025)
- items sit **bare on paper** (Marx voice) — ref label-s + book-body snippet + MoreChip · 24 betw passages, no dividers
- chapter boundary = book-title centred + small ornament (❦ o ☭ 30 % ink) + % bar 4dp red on paperDeep
- teaser "next: »…«" = body-m italic `inkMuted` (now `Gray`)
- break = Callout gold evy 10 · end o book = reward dialog
- loader = 3 ink dots · error = Callout blue "offline — th book is on ur fon, keep reading"

### hero + streak (qa-D1 · D8 · D9 · D-004)
- compact (D-036 a): row A = star 48 w number (display) + greeting / headline title-m · row B = 7 day dots 28 + Tag "☭ 3/10" (tap ➡ stamp card i My Capital)
- today's dot by time (qa-D8), no words, no "!":
  - morning = white 30 % + white ring · fr 14:00 = gold fill + ink letter · fr 19:00 = white disc + red inner ring + 2 pulses on enter, then still
  - done = gold ☭ stamp (any hour)
- nudge (qa-D9) = Callout blue + small Marx w halo: glowing = done · dim = open · tilted = missed yesterday · copy = invite, nvr blame ("Marx kept ur page")
- missed day = dot stays empty, 🕊️ joker shown as a dove dot — no red

### inputs + states
- field: paperHi · 1dp `outline` · r12 · label above (label-m) · focus = 2dp blue · error = redDeep border + ⚠ + words
- Switch: track red on / paperDeep off · row = body-l label + body-m hint under it, 56 high
- progress: linear 4dp, red on paperDeep, r full · ring 3dp
- empty state = 1 emoji 32 + 1 sentence body-m + (maybe) 1 Tonal btn · loading = skeleton lines i `paperDeep`, no spinner page

## 8 · 🎬 motion · 📳 haptics · 🔊 sound
| token | ms | use |
|-------|----|-----|
| instant | 100 | press, ripple, chip toggle |
| fast | 180 | snap-back, small fades, ✓ pop |
| base | 260 | page turn commit, sheet, expand "read more" |
| slow | 400 | dialog in, hero change, page peek |
| fx | 1200-2300 | CheckPop · swoosh · confetti · unlock (linear master clock, as now) |
- easing: move = FastOutSlowIn · enter = decelerate · exit = accelerate · physical (press edge, medal pop, dog-ear) = spring (damping 0.6-0.8)
- rules:
  - 1 celebration at a ti ➡ queue (now Orden dialog waits a fixed 2600 ms) · daily = small, rare = big (D-034)
  - nothing loops 4ever · attention cue ≤ 3 repeats o 6 s (page-turn hint ✔ alr)
  - reduced motion (animator scale 0) ➡ jump 2 end state, keep 📳 + txt
  - motion nvr blocks input: fx overlays pass touches thru aft 400 ms
  - page turn = th signature move ➡ don't add oth big transitions (tabs = plain fade 180)
- 📳 map: Primary = Confirm · toggle = ToggleOn / Off · page turn = light tick · long-press verse = LongPress (as now) · celebration = `buzz()` pattern · error = Reject
- 🔊 (D-035, later): page rustle · stamp thud · badge chime — ≤ 300 ms, dflt off, 1 ⚙️ switch, follows ringer mode

## 9 · ♿ a11y + 🌍 i18n (ck per lane)
- contrast per § 2 · gold nvr carries txt on light · state nvr by colour alone (✓ icon on selected chip, ring on today)
- tap ≥ 48 · `contentDescription` on evy icon-only control (★ · DogEar · CountChip · ActionItem icons)
- TalkBack order = visual order · verse = 1 node (ref + txt), its actions = custom actions
- strings: `tr("de","en")` inline (D-010) ➡ `bin/translate.py --only ui` at fan-in · no txt i images
- RTL: mirrored stripe / ruler / dog-ear / page-turn direction · numbers + refs stay LTR

## 10 · 📱 per-screen deltas (now ➡ spec)
### Today (`TodayScreen.kt` 🔥 · `Cards.kt` · `Feed.kt` · `Hooks.kt`)
- budget 412×915 ➡ ~730 usable: hero 120 + 12 + card ~395 + action row 72 = ~599 ➡ ~130 left = plan entry row 56 ✔ · 360×760 ➡ 592 usable: 120 + 12 + 338 + 72 = 542 ✔
- hero ➡ compact (§ 7) · stamp row + rank line move 2 My Capital (Tag stays)
- Amen btn leaves th card ➡ streak tap under th reading snippet (D-015, still unbuilt) = Primary-L w press edge
- 5 CardActions ➡ ActionItem Circle (labels 11 ➡ 12sp) · hint line 11sp ➡ gone (o body-m, 1×)
- plan block: white card + 3 nested boxes ➡ AccentPanel: head (disc · title ink · Tag "Day x / N") · Callout accent = Andacht · verses bare · Callout blue = "And today?" · Primary-L "Day done"
- no plan ➡ 1 row "📖 Pick a reading plan ➡" (Tonal, 56) — th qa-D1 read-plan btn
- Marxoscope: FilterChips ➡ Chip Suggest · AI offer = Callout blue + Text btn
- Hooked bench: #FFF1B8 card + 9 AssistChips ➡ hook post-it + Chip Suggest · only wh Hooked is on
- update banner ➡ Callout gold + Primary-S "Update" · crowd line 12sp red ➡ label-m `inkMuted` (red = act)
- feed per § 7

### Plans + plan detail (`PlansScreen.kt`)
- cards ➡ AccentPanel (6dp stripe ➡ 4 · title accent ➡ ink · white ➡ paperHi · shadow ➡ hairline)
- day tag white-on-accent ➡ Tag · "✔ active" gold txt ➡ Tag gold fill + ink · active ring 2dp gold stays
- detail: days = rows 56 w ✔ / ○ + ref · 1 Primary-L pinned at th bottom ("Start" / "Continue day N")

### Read list (`ReadScreen.kt`)
- title title-l · progress line body-m + bar 4dp · rows 56: ✔ (ok green icon) / ○ + chapter title body-l serif + "p. N" label-s end-aligned
- 7 Abschnitte = sticky title-s heads (red rubric) ➡ th 25 chapters stop being 1 flat wall

### Chapter (`ReadScreen.kt` · `VerseView.kt` · `Comments.kt`)
- chrome per D-031 · page colour, spine shadow, folio, DogEar = keep (qa-D2 "old, worthy" alr lives here)
- book-body 17 / 26 · section titles sans primary ➡ book-head serif red · ref label-s `inkMuted` (now bold blue) ➡ quieter margin
- verse menu ➡ ActionItem Bare ×4 (🔊 · 🔖 · ✍️ · 📋, D-018) on a paperHi strip r12 under th verse
- drop cap on a chapter's 1st verse (3 lines, serif bold red) = cheap "holy book" win · ornament betw sections
- footnote dialog: body-m serif, ref label-s · page-turn hint pill = paperHi + line, label-m

### Circle (`KreisScreen.kt`)
- 2 fields + 2 Buttons ➡ 2 Panels: "Found" (field + Primary-S) · "Join" (field + Tonal) ➡ only 1 Primary shows per Panel
- invite ➡ Callout gold + Primary-S "Join" + Text "Not now"
- detail: name title-l · code = mono body-l i a Tonal copy-pill (tap = copy + ✓) · members = Chips off-state · shared stamps row kept
- chat per voices table (mine gold ➡ blue) · composer = comments composer · "Leave" ➡ destructive Text + confirm
- legal 10-11sp ➡ ⓘ ➡ dialog

### My Capital (`MineScreen.kt` 🔥 · `OrdenView.kt`)
- order: stats row (streak · days · chapters = display numbers + label-m) ➡ stamp card (full, moved fr Today) ➡ badges ➡ bookmarks + notes
- badges per § 7 · bookmarks / notes = rows w ref + 1-line snippet (serif) + my note bubble
- About row = ActionItem Circle ×3 (as now, new tokens)

### ⚙️ Settings (`MineScreen.kt` 🔥 · `WheelPicker.kt` · `Changelog.kt` 🔥 = fan-in only)
- 1 scroll o small print ➡ 4 Panels w title-s: Reading (size · voice · persona chips) · Reminders (time · switch) · Community (name · Mitlesen switch) · App (lang · Hooked 🔒 row · what's new · about)
- evy row 56, hint = body-m under label, nvr 11sp · 🔒 Hooked row: progress ring + "3 more days" (D-026)
- time wheel: Primary-S "OK" · selected line = redTint band

### Hooked (`HookedScreen.kt` L4 · `Hooks.kt` L1 · `Unlock.kt` L5)
- phase cards (4 colours) ➡ Panel + 4dp stripe i th phase colour + title ink + Text "Show me"
- HookNote = hook post-it r12 body-m (now 12sp) · unlock dialog = reward dialog pattern, Primary-L "Switch on" + Text "Later"

### shell + dialogs (`MainActivity.kt` · `UpdateDialog.kt` · `ShareAppPanel.kt`)
- bars per § 6 · 🪝 TextButton i th top bar ➡ icon btn (emoji out o chrome)
- update dialog: 3 full-width Buttons ➡ Primary-L "Install" + Text "Later" · changelog = body-m list
- share panel = sheet w rows (icon · name) + Tonal "Copy link" — not a stack o outlined btns

### widget · notification · web
- widget + notif colours fr th same tokens (`res/values/colors.xml` mirrors `Tokens.kt`, + `values-night`) · notif accent red
- page `scagent.de/oh-my-marx` = same paper / ink / red + serif heads ➡ own step at fan-in (page gen = `bin/ship.sh`, VPS = ops)

## 11 · 🛡️ guard (how it stays 1 system)
- `ui/ds/` = th only place w raw `Color(0x` · `.sp` · `RoundedCornerShape(` · M3 `Button(` / `OutlinedButton(` / `AssistChip(` / `FilterChip(`
- `DesignGuardTest` (JVM): greps `src/main/java` outside `ui/ds/` ➡ counts per pattern ⬄ baseline file · count may only go **down** (ratchet) ➡ evy lane lowers it, 0 at th end
- exempt (listed i th tst): `Share.kt` cardBitmap · `Fx.kt` canvas · `notify/` RemoteViews · `Feast.kt` gradients
- `ShotsTest` + `ds_gallery` shot (all 5 btn kinds × states · surfaces · voices · type scale) ➡ 1 img 2 eyeball th whole system (D-019)

## 12 · 🏗️ build order — lanes (Opus-runnable · disjoint files)
_rules 4 evy lane: look only, ❌ behaviour change (except where a delta says so) · ❌ files outside ur list · strings via `tr()` · b4 + aft shots · all tsts green · guard baseline lowered · ❌ version bump / deploy (🦠 fon QA gates it)_
- **gate**: 🦠 answers § 0 (min D-029 · D-030 · D-036) ➡ then L0
- **L0 foundation** (serial, 1 agent, 1st — evything depends on it)
  - new `ui/ds/Tokens.kt` (colours light + dark · space · radius · motion) · `Type.kt` · `Buttons.kt` (5 kinds + singletons) · `Chips.kt` · `Surfaces.kt` (Panel · AccentPanel · Callout · Tag) · `Bubbles.kt` (voices)
  - `Theme.kt`: full light scheme + Typography + Shapes · dark scheme defined, not wired · `MarxDialog.kt`: tokens + stacked actions · `CardAction` / `Action` ➡ thin wrappers over ds (call sites untouched)
  - `DesignGuardTest` + baseline · `ds_gallery` shot
  - ✔ done wh: app looks ~same except nav bar + card colours fixed · gallery shot reviewed by 🦠
  - ✔️ **built @1114**: `ui/ds/` Tokens · Type · Buttons · Chips · Surfaces (+ `MarxShapes`) · Bubbles · StampCard · Theme.kt all roles + Typography + Shapes (dark defined, not wired) · `MarxDialog(primary, defer)` stacked + `MarxDialogPanel` · `CardAction` / verse `Action` / `CloseButton` = wrappers · guard baseline set · 80 tsts + assembleDebug green · 👀 visible: nav bar paperDeep (was lavender) · top bar title bold (titleLarge 700) · action labels 12sp · ⏳ gallery review by 🦠
  - call sites NOT moved (= lanes): `MarxDialog(actions = …)` row stays til each lane passes `primary` / `defer` · nav indicator red = L5 (MainActivity)
- **fan-out** (parallel, aft L0):
  | lane | files | does | done wh |
  |------|-------|------|---------|
  | **L1 Today** | `TodayScreen.kt` 🔥 · `Cards.kt` · `Feed.kt` · `Hooks.kt` | compact hero + time dot · D-015 Amen move · plan block · chips · feed | shots `today*` `feed*` fit 360×760 above th fold |
  | **L2 Book** | `ReadScreen.kt` · `VerseView.kt` · `Comments.kt` | type · verse menu · notes · comments sheet · drop cap · chrome (if D-031 ✔) | shots `read` `chapter` `verse_menu` `note_edit` + new `comments` · note tsts green |
  | **L3 Plans + Circle** | `PlansScreen.kt` · `KreisScreen.kt` | AccentPanel · Tags · circle panels · chat voices · leave confirm | shots `plans` `kreis` + new `plan_detail` `kreis_detail` |
  | **L4 Mine + ⚙️ + Hooked** | `MineScreen.kt` 🔥 · `OrdenView.kt` · `HookedScreen.kt` · `WheelPicker.kt` | stats row · stamp card in · badge states · settings Panels | shots `mine*` `settings*` + new `hooked` |
  | **L5 shell** | `MainActivity.kt` · `UpdateDialog.kt` · `Unlock.kt` · `ShareAppPanel.kt` · `Fx.kt` | bars · dialog pattern · celebration queue · haptics map · reduced motion | new shots `update` `unlock` `orden` · fx tsts |
  - ⚠️ L1 ⬄ L4 both touch th stamp card (leaves Today, lands i Mine) ➡ L0 extracts it 2 `ui/ds/StampCard.kt` 1st · L5 owns `Fx.kt` ➡ L1 only calls it
  - 🔥 `Changelog.kt` · `Share.kt` · `strings.xml` · `build.gradle.kts` = nobody i fan-out
- **fan-in** (serial): merge lanes ➡ `bin/translate.py --only ui` ➡ full shots montage ➡ guard = 0 ➡ D-029.. status ➡ `Changelog.kt` line ➡ DEV deploy on 🦠's word ➡ fon QA round
- **L6 later** (each gated by its D): dark wiring (D-032) · sounds (D-035) · bundled font 🧪 (D-033) · Marx halo asset (qa-D9) · web page restyle · Share.kt card (only if card look changes)

## 13 · 🚫 not i th spec
- new feats · new screens · IA changes (5 tabs stay) · copy rewrite beyond btn labels · Lesekreis unlock (qa-D10 rest) · Tamagotchi / avatar
- `pr/des/` reference shots (todo "design pass") = dir not found ➡ not used as a base

---

## 14 · 🎉 addendum @1119 — celebrations · holidays · holy book · note · close
_src: [qa_1117.md](qa_1117.md) 🎨 D6-D11 + r4 raw · 💬 Q1-Q4 · Q6 · Q7 · Q11 (all still blank ➡ folded into Ds below) · base = app @ `9751195` (L6-L8 + D-042/043 built) · docs only, ❌ code_
_🔑 1 idea: **th app is a print shop + a time clock** — u clock out (stamp), get promoted (seal), th calendar prints special editions (holidays), th book looks hand-set (holy book). Evy new piece uses th § 1-8 tokens, nothing new except named fx + assets_

### 14.0 · 💬 open choices (addendum) — "picks ok" = enough
_ordered by wh they unlock · D-042/043 (plan versioning · unlocks) + D-044 (dbl-tap) taken on main ➡ these start at D-045_
| D | Q | options | 🤖 pick | unlocks |
|---|---|---------|---------|---------|
| D-045 | **1 score** on th hero (Q1: "star 3 · 5 ☭ · 1 ✓ = confusing") | a) drop th 2 gift stamps · b) caption each counter (tried @1116, still confusing) · c) **hero = streak world only** (star + week + nxt-milestone bar) · stamps = career world (Day-done pane + My Capital) | **c** · ⚠️ amends D-036 A: hero size + look stay, only th stamp row leaves | C1 · C2 |
| D-046 | **milestones + freeze** (Q2 · Q3) | steps: 7 · 14 · 30 · 50 · 100 · 200 · 365 · then evy +100 · earn 1 freeze each, max 2 held · name: a) ❄️ Freeze · b) ✊ **Streiktag** · c) 🛟 Rettungsring · 🕊️ Sabbat: keep (weekly, auto) o fold in | **steps as listed + b ✊ Streiktag + keep Sabbat** (Sabbat = weekly grace · Streiktag = earned buffer, used only wh Sabbat's spent) | C1 · C2 |
| D-047 | **star tier aftr a break** | a) back 2 0 (Duolingo, loss aversion) · b) best ever, nvr lost · c) **"Abschreibung"** — 1 tier down per missed day (fixed capital depreciates, doesn't vanish) | **c** (kind but still stakes · D-004) | C1 · C2 |
| D-048 | **Day complete pane** (D10 · Q11) | a) stamp thumps onto th card + star counts N-1 ➡ N · b) 1 punchy line fr a pool (no quote nobody gets) · c) tomorrow = a teasing Q, drop "Verse arrives 07:30" · d) Marx head reacts | **a + b + c now · d = 🧪** aftr C3's head asset | C2 · C7 |
| D-049 | **1 celebration system** (§ 14.1 ladder + queue) | ✔️ as written · change X | ✔️ **as written** | C2 |
| D-050 | **rank-up = Beförderung** | a) **fullscreen RPG promotion** (badge flies in · Marx seals it "GENEHMIGT" · rays · perk line · share) · b) bigger dialog only · + perks: cosmetic table § 14.1.4 ✔️ / ❌ · rank as a D-043 unlock source y/n | **a + perks ✔️ + rank 3 opens 1 locked plan** | C2 |
| D-051 | **holiday Marx head** (D8 · Q6) | a) **1 base head + accessory overlays** (hat · beard tint · prop) · b) full redraw per feast · c) emoji next 2 th head (now = "half-baked") | **a**, b only 4 3 tentpoles (🎄 · 🎃 · 🎂) | C3 |
| D-052 | **feast calendar** (R4 gaps) | a) **all 18 new** (§ 14.2.3) · b) labour history only, no fun days · c) 🦠 picks fr th sheet | **a** (gaps ≤ ~4 w) · c stays possible on th sheet | C3 |
| D-053 | **holy-book devices** (D11) | a) ⚡ set only · b) **⚡ + 🟡** · c) all incl 🧪 (grain · margin glosses) | **b** · ❌ margin ¶ numbers (r3: "metadata, usr no need") | C4 |
| D-054 | **note look** (D7 · Q4) | a) yellow sticky + 📌 (⚠️ = Hooked's yellow ➡ voice clash) · b) **margin-comment card** (tail · initial disc · name + time · ✎) · c) keep ruler, only new bg | **b** | C5 ➡ C4 |
| D-055 | **close btn** (D9 · Q7) | V1-V8 sheet (§ 14.5) ➡ 🦠 picks by eye | 🤖 renders th sheet now (C6, no gate) · eye-guess **V4** (★ w ✕ cut-out = new usrs read "close") | C6 |

### 14.1 · 🏆 celebrations = 1 system
_🦠 r3 + r4: "anti-climax" · "bland, txt stuffed, no icon/anim" · "Moses?" · "morgen kommt = not teasing" · rank-up "shd feel like hero upgrade / MMORPG lvl-up"_
#### 14.1.1 the ladder — size ∝ rarity (D-034 · D-039 stay)
| tier | wh | how often | fx | surface | ms |
|------|----|-----------|----|---------|----|
| T0 | any Primary tap | evy tap | press edge sinks + 📳 Confirm | — | 100 |
| T1 | quote-of-th-day Amen | daily | ✓ CheckPop (D-039) | — | 1200 |
| T2 | plan day done | daily | ☭ swoosh ➡ **Stempeluhr** pane (§ 14.1.2) | dialog | 1200 + pane |
| T2b | chapter fully read | ~weekly | red wax seal ☭ presses onto th chapter's tailpiece (§ 14.3) | inline i th book | 600 |
| T3 | streak milestone (D-046) | 7 · 14 · 30 … | **Meilenstein** screen (§ 14.1.3) | fullscreen | 2300 |
| T4 | stamp card full = rank up | ~evy 10 reading days | **Beförderung** screen (§ 14.1.4) | fullscreen | 2300 |
| T5 | badge · plan unlock (D-043) · Hooked unlock · plan done · book done | rare | reward dialog (§ 7, as now) | dialog | — |
#### rules (= D-049)
- 🔑 **1 big screen per tap** · order: fx ➡ T2 pane ➡ T3 ➡ T4 ➡ T5 dialogs (`RewardSlot` queue, as L7)
- ≥ 2 due ➡ th pane's Primary changes 2 **"Moment … ▸"** w a gold glint = "there's more" (open loop, honest) · T3 + T4 same day ➡ T4 screen shows th milestone as a 2nd line, not a 2nd screen
- > 2 T5 dialogs i 1 go ➡ 1st shows, rest = "+ 2 more" chip ➡ My Capital (no dialog wall)
- quote Amen (T1) day hits a milestone ➡ ✓ pop ➡ T3 directly (no pane — pane = plan days only)
- reduced motion ➡ evy screen jumps 2 its end frame · 📳 + txt stay · fx overlays pass touches aftr 400 (§ 8)
- 🪝 evy new trick = 1 HookNote (Hooked on): Stempeluhr = "endowed progress" · milestone = "goal gradient" · Streiktag = "loss aversion, cushioned" · Beförderung = "variable reward o th self" · "Moment ▸" = "Zeigarnik / open loop"

#### 14.1.2 Stempeluhr — Day complete, redesigned (D-048 · D10)
_now: title + "N days i a row. Accumulate, accumulate! That is Moses and th prophets! (Capital 22,56)" + "Tomorrow: «…»" + "Verse arrives at 07:30" = 3 txt lines, 0 image, 0 motion_
```
╭────────────────────────────────────╮
│                                  ★ │  ★ = close (§ 5)
│    STEMPELKARTE 2                  │  book-meta, inkMuted
│  ┌──────────────────────────────┐  │
│  │ ☭  ☭  ☭  ☭  ☭  ☭  ◉  ○  ○  ○ │  │  ◉ = today: red ring ➡ ☭ THUMP
│  └──────────────────────────────┘  │     (spring 1.6 ➡ 1, -8° ➡ -3°, 4 ink specks)
│               ╱★╲                  │
│              ╱ 12 ╲                │  star 64, counts 11 ➡ 12 (digit roll 260)
│               ╲  ╱                 │  + 1 gold glow pulse
│          12 Tage am Stück          │  title-m
│                                    │
│   „Tagessoll erfüllt —             │  body-l, app voice (sans), ink
│        ohne Aufseher."             │  1 line fr th pool ⬇
│  ────────────────────────────────  │  line
│   Morgen: Warum heckt Geld Junge?  │  body-m italic inkMuted · Q fr C7
│                                    │
│  ╭────────────────────────────╮    │
│  │        Feierabend          │    │  Primary-L + press edge
│  ╰────────────────────────────╯    │  ≥ 2 moments due ➡ "Moment … ▸" + glint
╰────────────────────────────────────╯
```
- 🎬 timeline (starts aftr th ☭ swoosh lands): 0 pane in (slow 400) ➡ 400 stamp thump (📳 heavy click + 🔊 stamp thud, D-035) ➡ 700 count + glow ➡ 1000 line + teaser fade in · btn live fr 400
- card full (10/10) ➡ card flips (rotationY, base 260) ➡ back side = "Karte voll" + Primary "Beförderung ▸" ➡ T4
- 🗣️ **line pool** (~12, rotate by day = variable reward · DE/EN pairs, tr() inline · nvr a Kapital quote here — quotes live i th book):
  - "Schicht erledigt. Der Mehrwert gehört heute dir." / "Shift done. Today th surplus value is yours."
  - "Tagessoll erfüllt — ohne Aufseher." / "Quota met — no overseer."
  - "Lesekapital +1. Ausbeutungsfrei." / "Reading capital +1. Exploitation-free."
  - "Stempel drauf. Die Uhr gehört heute dir." / "Stamped. Today th clock is yours."
  - "Nicht mal Engels war so pünktlich." / "Not even Engels was this punctual."
  - "Ein Tag, den dir keiner abkaufen kann." / "A day nobody can buy off you."
  - ⚠️ hero alr says "Schicht erledigt" wh done ➡ pool line 1 only on days ≠ hero copy, o hero ➡ "Feierabend ✓"
- **teaser** = a Q th opens a loop, not a cut quote: new per-day field `qd` / `qe` i `plans.json` (C7) · fallback til then: 1st sentence o tomorrow's Andacht ending i "?" · else no teaser (nvr a cut verse)
- 🚫 gone: "Moses und die Propheten" · "Losung kommt um 07:30" (lives i ⚙️) · Hooked NEXT note stays (Hooked on only)

#### 14.1.3 Meilenstein — streak milestones (D-046 · D-047 · D6 · R3)
- **star tiers** = th hero star grows ornaments (same `ic_star` asset + layers, nvr a Path() copy):

| tier | streak | star gets | colour |
|------|--------|-----------|--------|
| 0 | 0-6 | plain (now) | red + gold rim |
| 1 | 7+ | soft glow behind | gold 30 % radial |
| 2 | 14+ | thin ring | gold 2dp |
| 3 | 30+ | laurel (2 branches, bottom) | goldDeep on light · gold on gradient |
| 4 | 50+ | 5 tiny stars on th ring | gold |
| 5 | 100+ | halo w rays (= Saint Marx's halo) | gold |
| 6 | 200+ | ribbon under it, "200" | red + gold txt |
| 7 | 365+ | full Orden: laurel + halo + ribbon w year | gold |
- 📉 break (D-047 c "Abschreibung"): shown tier = max(tier(streak), lastTier − missedDays) ➡ falls 1 tier / day, nvr a cliff · copy (1×, Callout blue): "Dein Lesekapital schreibt sich ab — langsam. Ein Tag reicht, um es zu stoppen."
- **hero** (D-045 c): star + week row + **nxt-milestone bar** (4dp gold on white 20 %, label-m "noch 3 bis 14") · stamp row + rank line leave th hero ➡ pane + My Capital
- **screen** (fullscreen, ink 85 % scrim over th hero gradient):
```
╭────────────────────────────────────╮
│                                    │
│            ✦  ╱★╲  ✦               │  star 160 · new ornament grows in
│           ❦  ╱ 14 ╲  ❦             │  (laurel unfolds, spring 600)
│               ╲  ╱                 │
│                                    │
│     14 Tage ununterbrochen         │  display, white
│     Rekord!  (if = best)           │  label-m gold
│                                    │
│  ■■■■■■■■■■■■■■  ← recap chain      │  N cells fill start ➡ end 800 ms
│  (🕊 + ✊ days = their own icon)    │  > 30 ➡ last 14 + "… +N"
│                                    │
│  ┌──────────────────────────────┐  │
│  │ ✊  +1 Streiktag erkämpft     │  │  token slides up · slots ✊ ✊ / ○
│  │     rettet 1 verpassten Tag  │  │  body-m
│  └──────────────────────────────┘  │
│  ( Weiter )            Teilen      │  Primary-L · Text
╰────────────────────────────────────╯
```
- 🎬 0 star ➡ 200 recap fills (📳 light tick evy 2nd cell) ➡ 1000 ornament grows ➡ 1600 headline ➡ 1900 Streiktag token · 2300 still
- ✊ **Streiktag** (D-046 b): auto-used aftr 🕊️ Sabbat's spent · week row shows it as a ✊ dot (red ring, white fist) · copy on use: "Streiktag eingelöst — die Kette hält." · max 2 held, shown as 2 tiny slots next 2 th star · 🚫 shop · 🚫 paid repair · 🚫 "streak at risk" red push (D-004)
- share card = same 1080×1200 as th quote card: star w tier + "14 Tage Kapital am Stück" + app name

#### 14.1.4 Beförderung — rank-up = lvl-up (D-050 · r4)
_now: same "Day complete" dialog w "Neuer Rang:" + 22sp red txt + confetti = "bland"_
```
╭────────────────────────────────────╮
│   ╲  │  ╱      gold rays, 12        │  rotate 6°/s, fade out at 2300
│ ──  ┌───┐  ──                       │  (nvr loop 4ever, § 8)
│ [📗]➜│ 📈 │                         │  old badge shrinks + slides start
│  ──  └───┘  ──                      │  new badge flies in fr top, 1.15 overshoot
│   ╱ ┏━━━━━━━━━━┓ ╲                  │
│     ┃GENEHMIGT ✓┃ ← red rubber      │  Marx head peeks up fr th bottom edge,
│     ┗━━━━━━━━━━┛    stamp -12°      │  slams th seal: 📳 heavy + 🔊 thud + 4dp shake
│                                    │
│    RANG 3 / 7                      │  book-meta gold
│    Erweiterte Reproduktion         │  title-l serif white
│    Neu: ein Leseplan ist frei 🔓   │  body-m · th perk (1 line)
│    28 Lesetage · Karte 3 voll      │  label-m white 70 %
│  ( Weiter )            Teilen      │
╰────────────────────────────────────╯
```
- 🎬 0 rays + old badge ➡ 400 swap fly ➡ 1100 Marx peeks ➡ 1400 SEAL ➡ 1700 confetti (existing `Confetti`) + title + perk · 2300 still
- **rank badges** = 7 vector emblems (not emoji): 📗 shovel + coin heap · 🔁 2-arrow circle · 📈 spiral up · 🏭 workbench + hammer · ⚙️ gear + chimney · 🧔 Marx bust i laurel · ☭ ☭ i full wreath
- **perks** (cosmetic, honest · 💬 D-050):

| rank | perk |
|------|------|
| 1 Ursprüngliche Akkumulation | hero frame gets a thin gold border |
| 2 Einfache Reproduktion | 2nd stamp card design (blue cloth) |
| 3 Erweiterte Reproduktion | 1 locked plan opens (new D-043 source `rank`) |
| 4 Manufaktur | rank title shows next 2 ur name i Circles |
| 5 Große Industrie | star gets a gear rim |
| 6 Klassiker-Status | ur name lands i th book's colophon (§ 14.3) |
| 7 Expropriation … | red star ➡ gold star 4ever + th ☭ swoosh turns gold |

### 14.2 · 🎄 holidays = special editions (D-051 · D-052 · D8 · R4)
_now: `Feast.kt` 12 feasts = gradient + emoji next 2 th greeting + verse · Marx head (top bar `ic_marx_fg` · share card `ic_launcher_fg`) nvr changes ➡ "half-baked"_
#### 14.2.1 4 slots per feast — evy feast fills ≥ 2
| slot | wh | rule |
|------|----|------|
| 🧔 **head** | top bar · share card · widget head | 1 base head + overlay layers (D-051 a) · same line weight + palette as `ic_launcher_fg` |
| ⭐ **emblem** | hero star slot (92) | stays ★ unless th feast owns a symbol (♀ · 🎄 · 🎃) · number alw inside |
| 🖼️ **frame** | hero border + corners | 1 ornament band (garland · bunting · cobweb · fuse) · ≤ 12dp, outside th txt |
| 🎨 **gradient + greet + verse** | hero | exists, keep |
- feast days may swap paint + emblem, nvr layout (§ 2) · star tiers (§ 14.1.3) still show: feast emblem gets th same ornaments
#### 14.2.2 th 12 existing — upgrade
| feast | head | emblem | frame / extra |
|-------|------|--------|---------------|
| 🎆 Neujahr 1.1. | party hat, beard full o confetti | ★ as a firework burst | **"Merry crisis & happy new fear"** = greet · 💥 aftermath o 31.12.'s fuse |
| 🥂 Silvester 31.12. | holds a dynamite stick, fuse lit | ★ w a burning fuse that shortens by hour | fuse runs along th hero top ➡ boom = Neujahr (2-day story) |
| 💘 Valentin 14.2. | heart-shaped glasses + cupid arrow thru a price tag | ★ w a heart cut-out | rose garland |
| 💐 Frauentag 8.3. | Clara Zetkin guest head (she founded th day) o Marx + purple ribbon | **big ♀, number inside th circle** (🦠 r4) | purple + violet band |
| 🕯️ Todestag 14.3. | greyscale head + candle | ★ outline only | quiet: no confetti that day |
| 🐣 Ostern | Marx hatching fr a cracked egg | ★ egg-shaped | "th dead seize th living" stays |
| ✊ 1. Mai | red carnation i th buttonhole + raised fist | ★ w ✊ | red bunting |
| 🎂 Geburtstag 5.5. | party hat + cake "1818" | ★ w 1 candle | confetti on open (1×) |
| 📕 Kapital 14.9. | reading glasses + Bd. 1 under th arm | ★ on a book spine | gilt edge on th hero |
| 🎃 Halloween 31.10. | **pumpkin w Marx beard + halo** (🦠 r3) · o vampire cape (ch. 10 "vampire-like") | jack-o'-lantern ★ (carved, glowing) | cobweb corners |
| 🛍️ Black Friday | buried under shopping bags, tag "−100 % Mehrwert" | ★ w crossed-out SALE tag | black, receipt-paper strip |
| 🎄 Weihnachten 24-26.12. | **Santa-Marx** (beard alr fits) · sack "Produktionsmittel" | ★ on top o a tiny tree | snow on th hero top · greet + "Ho ho ho — die Produktionsmittel für alle!" |
#### 14.2.3 18 new — fills R4 gaps (D-052)
```
       Jan  Feb  Mar  Apr  May  Jun  Jul  Aug  Sep  Oct  Nov  Dec
now    ●    ●    ●●   ●    ●●   ·    ·    ·    ●    ●    ●    ●●
new    ○    ○    ○    ○    ○    ○○○○ ○○   ○○   ○○   ○    ○    ○
was: 5.5 ➡ 14.9 = 4 months empty · now: longest gap ~4 w (5.5 ➡ 1.6 · 15.1 ➡ 14.2)
🦠 wants ~2 w ➡ th sheet shows th holes, fill w fun days by hand
```
| date | feast | head | why it fits |
|------|-------|------|-------------|
| 15.1. | Rosa + Karl (Luxemburg, Liebknecht † 1919) | red carnation | biggest left date i DE (LL-Demo) |
| 21.2. | Manifest 1848 | holds th pamphlet | th press, th first print |
| 18.3. | Pariser Kommune 1871 | Phrygian cap | Marx' "Bürgerkrieg i Frankreich" |
| 28.4. | Workers' Memorial | hard hat · quiet | ch. 13-15 accidents · no confetti |
| 8.5. | Tag der Befreiung 1945 | quiet · red carnation | anti-fascism (⬄ R5 fascism plan) |
| 1.6. | Kindertag | — (emblem only: ★ w a kite) | ch. 10 + 13 child labour |
| 4.6. | Weberaufstand 1844 | weaver's cap | Marx wrote on it |
| 21.6. | längster Tag (fun, unofficial) | sunglasses · sweating | ch. 8 "th capitalist loves it" |
| 28.6. | Stonewall / CSD | rainbow beard | solidarity, summer |
| 5.7. | Matchgirls' strike 1888 | holds a match | th first big women-led strike |
| 14.7. | Bastille 1789 | tricolour cockade | th bourgeoisie's own revolution |
| 5.8. | Engels † 1895 | **Engels guest head** | th co-author |
| 16.8. | Peterloo 1819 | quiet | cavalry vs. workers |
| 1.9. | Antikriegstag | dove on th shoulder | DGB day |
| 28.9. | 1. Internationale 1864 | globe | Marx wrote its address |
| 17.10. | Tag gegen Armut (UN) | empty pockets turned out | ch. 23 pauperism |
| 28.11. | Engels' birthday | Marx + Engels double head | bromance |
| 2.12. | Jenny Marx † 1881 | **Jenny guest head** | th one who kept it all running |
- unofficial / fun days = th "random ones" 🦠 asked 4 (variable reward) · quiet days (†) = no confetti, grey paint, 1 line
- 🖨️ **feast sheet** (test-only, like `DsChoicesTest`): all heads + emblems side by side ➡ `android/app/build/shots/feast_sheet.png` = th design sesh board 🦠 asked 4 (r3)
#### 14.2.4 ♀ emblem (Frauentag · 🦠 r4)
```
 ╭───────────────────────────────────────────╮
 │ 12 Tage Lesekapital akkumuliert    ╭────╮ │   ♀ ring: 6dp white stroke
 │ Weiter geht's!                     │ 12 │ │   + violet glow, number = display
 │ 💐 Frauentag — Kap. 13 kannte …    ╰─┬──╯ │   inside th circle (not below)
 │                                    ──┼──  │   cross = same stroke
 │  ✓  ✓  ✓  ◉  ·  ·  ·                      │
 ╰───────────────────────────────────────────╯
```

### 14.3 · 📜 holy book (D-053 · D11)
_alr: drop cap 📖 + 📜 (L6) · red rubric heads · folio "— 50 —" · page break hairline · ❦ betw sections (§ 7) · spine shadow_
| # | device | wh | where | cost |
|---|--------|----|-------|------|
| 1 | **illuminated initial** | chapter's 1st letter: 4 lines, red letter on a goldTint square, ink hairline frame, tiny vine | 📖 📜 | 🟡 (draw, no asset) |
| 2 | **section initial** | evy section's 1st letter: 2-line red drop cap, no box | 📖 📜 | ⚡ (DropCapText alr) |
| 3 | **red pilcrow ¶** | small red ¶ starts each paragraph instead o an indent | 📜 | ⚡ |
| 4 | **incipit** | above th chapter title, book-meta red: "HIER BEGINNT DAS ERSTE KAPITEL" · number i Roman (Kapitel I) | 📖 📜 | ⚡ |
| 5 | **headpiece** | printer's band ❦ ✥ ❦ ✥ ❦ over th chapter title | 📖 📜 | ⚡ |
| 6 | **tailpiece + seal** | ☙ vignette at chapter end · chapter fully read ➡ red wax ☭ seal presses onto it (T2b) | 📖 📜 | 🟡 |
| 7 | **running head** | small caps: verso "DAS KAPITAL" · recto chapter title (alternates by page) | 📖 | ⚡ |
| 8 | **catchword** (Kustode) | 1st word o th nxt page, bottom end ➡ old-book device + a free page-turn teaser | 📖 | ⚡ |
| 9 | **ribbon bookmark** | 🔖 page = red silk ribbon hangs fr th top edge, swallowtail end (replaces th icon on th page) | 📖 | 🟡 |
| 10 | **gilt edge** | 2dp gold gradient on th page's outer edge | 📖 | 🟡 |
| 11 | **explicit + colophon** | book end: "HIER ENDET DER ERSTE BAND" + colophon (typeface · "gesetzt auf der Pseudopod" · rank-6 names, § 14.1.4) | 📖 📜 | ⚡ |
| 12 | paper grain | 2-3 % noise on `page` | 📖 | 🧪 perf |
| 13 | margin glosses | notes as tiny ✍ marks i th margin, tap = open | 📖 | 🧪 (⬄ D-054) |
- ❌ margin ¶ numbers: r3 took th ¶ ref line out ("metadata, usr no need") ➡ don't bring it back via th margin
```
┌──────────────────────────────────────┐▒  ← gilt edge (10)
│ DAS KAPITAL                          │▒  running head (7), book-meta
│                                    ┃ │▒  ribbon (9) if bookmarked
│           ❦ ✥ ❦ ✥ ❦                ┃ │▒  headpiece (5)
│   HIER BEGINNT DAS ERSTE KAPITEL   ╿ │▒  incipit (4), red
│          Kapitel I · Die Ware        │▒  book-title
│ ╔═════╗                              │▒
│ ║  D  ║er Reichtum der Gesellschaf-  │▒  illuminated initial (1)
│ ║     ║ten, in welchen kapitalis-    │▒
│ ╚═════╝tische Produktionsweise herr- │▒
│ scht, erscheint als eine »ungeheure  │▒
│ Warensammlung« …                     │▒
│ ¶ Die Ware ist zunächst ein äußerer  │▒  pilcrow (3) — 📜 only
│ Gegenstand, ein Ding, das durch …    │▒
│                              Eigen-  │▒  catchword (8)
│               — 49 —                 │▒  folio (alr)
└──────────────────────────────────────┘▒
```
- 13 langs: Roman numerals + incipit = DE/EN/… via tr() · ar · ur = mirrored (ribbon + gilt edge on th oth side) · zh · hi · bn = no drop cap (script has no caps) ➡ initial box w 1st glyph, 2 lines

### 14.4 · ✍️ note = comment card (D-054 · D7 · r2)
_r2: "change bg + icon + small ruler so we see it's a note · look how other apps do comments" · "drop th star there" · tap away = save only, verse menu stays_
```
  Die Ware ist zunächst ein äußerer Gegenstand, ein
  Ding, das durch seine Eigenschaften …                 ← Marx, bare
     ▲
  ╭──┴──────────────────────────────────────────╮
  ┃ (S)  Du · Notiz · 19. Nov              ✎    │   initial disc 24 blue · label-s
  ┃      Ware = alles, was verkauft wird?        │   body-m ink
  ┃      ↳ auch im Lesesaal                      │   label-s blue (if posted)
  ╰──────────────────────────────────────────────╯
  ┃ = 3dp blue ruler (start)  ·  card = paperHi + 1dp blue 30 % + r12 · tail 8dp → th verse
```
- = th "me" voice (§ 7) i comment shape: blue ruler stays, **bg leaves blueTint** (was "a blue quote") ➡ paperHi card + hairline + tail · ✎ = edit · ❌ ★ i th editor
- editor: same card, field inside, 1 ghost line "Notiz …" · tap outside = save (o drop if unchanged) · verse menu stays open (r2) · Chip Choice "also post 2 reading room" stays
- comments sheet: oth ppl = same card shape i paperDeep, no ruler · mine = this card ➡ note ⬄ comment = 1 component (`NoteCard`)

### 14.5 · ★ close btn — variants sheet (D-055 · D9 · r1)
_now (V1): ★ red, 64 % o a 36 circle (red 10 % + 30 % hairline), optically lifted · 48 target · 🦠 r1 "gv many versions, see it i diff ctx"_
| V | look | 👍 | 👎 |
|---|------|----|----|
| V1 | now | brand, calm | new usrs don't read ★ = close |
| V2 | bare ★ 28, no circle | lightest | weak target cue |
| V3 | mono ✕ i circle | evyone gets it | loses th brand mark (§ 1) |
| V4 | ★ w a small ✕ knocked out o its centre | brand + "close" | ✕ tiny at 36 |
| V5 | red disc, white ★ (inverted) | strong, seal-like | loud next 2 Primary (§ 1 "1 loud thing") |
| V6 | outline ★, gold rim only | bookish, quiet | 3:1 on paperHi? (ck) |
| V7 | red wax-seal blob w embossed ★ | ties 2 stamps + seals | busy at 36 |
| V8 | V1 + ★ spins 72° on press (spring) | playful, 0 cost at rest | motion only, same static look |
- sheet = 8 variants × 3 ctx (MarxDialog on paperHi · sheet · hero gradient) + 1 dark row ➡ `close_sheet.png` · 🦠 picks ➡ `CloseStar` dflt changes, 1 line

### 14.6 · 🏗️ lanes 4 Opus — disjoint files (§ 12 rules apply: look only unless th lane says · tr() · b4 + aft shots · tsts green · guard ratchet down · ❌ bump / deploy)
_lane ids = **C1-C7** (§ 12 uses L0-L6, qa uses L1-L8 ➡ no clash)_
| lane | gate | files (only these) | does | done wh |
|------|------|--------------------|------|---------|
| **C1 🧮 streak logic** | D-045 · 045 · 046 | `logic/Daily.kt` · `data/Prefs.kt` (strikes held, last tier) · new `test/…/StreakGameTest.kt` | milestones · tiers · Abschreibung · Streiktag earn + auto-use aftr Sabbat · API: `milestoneHit(days)` · `starTier(days, now)` · `strikes` | tsts: earn at 7/14 · max 2 · used only aftr Sabbat · tier falls 1/day · old streaks unchanged |
| **C2 🏆 moments** | C1 merged · D-048 · 048 · 049 | new `ui/Moments.kt` (Stempeluhr · Meilenstein · Beförderung) · `ui/Fx.kt` (stamp thump · rays · count roll · seal) · `ui/TodayScreen.kt` 🔥 (pane wiring · hero: stamp row out, milestone bar in) · `ui/MineScreen.kt` 🔥 (stamp card + rank line in) · `ui/Hooks.kt` (5 notes) · new `ui/ds/Emblem.kt` (StreakStar + tiers) · new `res/drawable/rank_*.xml` ×7 · new `test/…/MomentsShotsTest.kt` | § 14.1 all · queue order + "Moment ▸" · reduced motion end frames | shots `moment_shift` · `moment_shift_full` · `moment_milestone_14` · `moment_rank_3` · `hero_tiers` (0-7) · FxQueueTest: 1 big screen per tap |
| **C3 🎄 feasts** | D-051 · 051 | `logic/Feast.kt` (+ 18 feasts, head + emblem + frame ids) · new `ui/ds/MarxHead.kt` · new `res/drawable/head_*.xml` + `frame_*.xml` · `MainActivity.kt` · `ui/Share.kt` · `notify/Widget.kt` (1 call each ➡ MarxHead) · new `test/…/FeastSheetTest.kt` | § 14.2 · base head + overlays | `feast_sheet.png` (30 feasts × head + emblem + frame) · Feast.on() tst evy date i th table · longest gap ≤ 4 w tst |
| **C3b ♀ emblem** | C2 + C3 merged | `ui/ds/Emblem.kt` | feast emblems (♀ · 🎄 · 🎃 · ★ variants) | `hero_feasts` shot |
| **C4 📜 holy book** | D-053 | `ui/VerseView.kt` · `ui/ReadScreen.kt` · `ui/Feed.kt` · new `ui/ds/Ornaments.kt` · new `res/drawable/orn_*.xml` | § 14.3 ⚡ + 🟡 · tail: NoteBubble ➡ NoteCard swap (aftr C5) | shots `chapter` · `chapter_scroll` · `chapter_end_seal` · `bookmark_ribbon` · rtl + zh shot |
| **C5 ✍️ note card** | D-054 | `ui/ds/Bubbles.kt` (NoteCard) · `ui/Comments.kt` · new `test/…/NoteCardShotTest.kt` | § 14.4 · comments sheet uses it | shots `note_card` · `note_edit` · `comments` · note tsts green |
| **C6 ★ close sheet** | none — start now | `ui/ds/Buttons.kt` (`CloseStar(variant)`, dflt = V1) · new `test/…/CloseSheetTest.kt` | § 14.5 V1-V8 | `close_sheet.png` ➡ 🦠 picks |
| **C7 ❓ teasers** | D-048 c | `assets/content/plans.json` (`qd` / `qe` per day) · `bin/` plan build step | 1 teasing Q per plan day (LLM draft ➡ 🦠 skims) · no cut change ➡ no ver bump (D-042) | all 10 plans hv `qd`+`qe` · test_bin green |
- order: **C6 now** · C1 ∥ C3 ∥ C4 ∥ C5 ∥ C7 ➡ C2 (needs C1) ➡ C3b (needs C2 + C3) · C4's NoteCard swap = last commit, aftr C5
- 🔥 `Changelog.kt` · `strings.xml` · `build.gradle.kts` · `i18n_*` = fan-in only (as § 12)
- fan-in: `bin/translate.py --only ui` (pool lines · feast greets · incipits) ➡ shots montage ➡ D status ➡ DEV deploy on 🦠's word ➡ fon QA

### 14.7 · 🚫 not i th addendum
- streak shop · gems · paid repair · friend streaks · "at risk" red push · leaderboards
- new tabs / screens beyond th 3 moments · Lesekreis unlock · Tamagotchi / avatar (§ 13 stays)
- holiday art = vector, flat, same 2-3 colours per head · 🚫 photos · 🚫 AI raster (APK size + drift)

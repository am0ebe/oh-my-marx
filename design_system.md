# design system — omm ☭ 🎨
_spec @1113 · docs only, ❌ code yet · SSOT 4 look + feel: rules ➡ tokens ➡ components ➡ per-screen deltas ➡ build lanes_
_y lives i [arch.md](arch.md) D-029..036 (💬 = open, 🤖 pick marked) · base = app @ `eb033ae` (`ui/Theme.kt` + 20 `ui/*.kt` + headless shots) · [qa_1112.md](qa_1112.md) § 🎨 qa-D1..D10 + r4 sweep #7_
_contrast = WCAG 2.x, computed (text ≥ 4.5 · large txt / icon / border ≥ 3) · sizes = dp / sp_

## 0 · 💬 open choices (🦠 decides · "picks ok" = enough)
| D | Q | options | 🤖 pick |
|---|---|---------|---------|
| D-029 | base rules o th spec (§ 1) | ✔️ as written · change X | as written |
| D-030 | btn look (qa-D7, mockups § 5) | A pill tonal · B letterpress · C rubric | **A + B's press edge on th 1 big btn** |
| D-031 | reader chrome | a) as now · b) slim paper head, no bottom nav · c) immersive, tap = toggle | **b** |
| D-032 | dark theme | a) light only · b) follow system · c) + ⚙️ toggle | **tokens now, wire later = b** |
| D-033 | book font | a) system serif · b) bundle 1 (Literata / EB Garamond, +0.3-1 MB) | **a** now · b = 🧪 later |
| D-034 | daily Amen fx | a) sickle swoosh evy day (now) · b) CheckPop daily, swoosh = rank-up / full card | **b** |
| D-035 | sounds (qa-R1) | a) none · b) 3 tiny (page · stamp · badge) + ⚙️ switch, dflt off | **b, later lane** |
| D-036 | Today above th fold (qa-D1) | a) compact hero + stamp tag · b) keep hero, shrink card · c) hero collapses on scroll | **a** |

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
| 1 | **Primary** | red fill · white label-l bold · pill · **L** h 52 full-width / **S** h 40 hug | THE action: Amen · day done · start plan · install · send | 1 per screen / dialog / panel |
| 2 | **Tonal** | blueTint fill · blue label-l · pill h 40 | 2nd actions: found / join · copy link · share targets · refresh | 2 side by side |
| 3 | **Text** | blue label-l, no fill · h 40 | defer / leave / more: "Later" · "Show me" | — |
| 4 | **ActionItem** | mono icon + label-m under it · 48 target · style **Circle** (redTint disc 44) o **Bare** | icon rows: quote card row · About row = Circle · verse menu i th book = Bare | 5 per row |
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

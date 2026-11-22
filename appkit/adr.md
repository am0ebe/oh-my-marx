# adr — appkit 🧩
_Code says **what**, this file says **why**. If code and a decision disagree, that is a bug in one of them: flag it, don't silently fix either side. Template v2.2 @1120 (vibe++ D-014, D-015, D-023). `mkpr` copies this file into new projects. Converting old records: [adr_convert.md](../vibe++/adr_convert.md)._

## 🎯 Intent
- 🦠 @1122: "It's all about having different reusable components we can use more often. E.g. chat. Or updater." The kit holds everything general from building our apps (omm, cali, the next one), so app 3 starts from the kit, not from a copy of app 1.
- Four shelves: **tool** (`apptool`: ship + deploy) · **templates** (README, web page) · **lessons** (what we learn while developing apps, like a refl / distill) · **core code** shared between the apps (updater, chat, …).
- Good = a new app gets ship, deploy, page, README, updater by config, and a fix in a component lands in every app.

## 📐 Requirements and limits
- Must, must not, limits (money, privacy, hardware, tokens, time).

## 🧩 Architecture (current state)
- Components, data flow, interfaces, where state lives. Mermaid is fine.
- 🔥 Hot files: one owner at a time; parallel lanes never split these.

## 🧾 Decisions
_Newest on top. Append only: never delete, supersede instead. Rules below the examples._

### D-003 @1122 · apptool has its own test and never dies without a word · Accepted (cali crew @1122, M8 #335)
- **Y:** apptool ships every app, yet it was only tested by hand. Its first deploy from a clean tree exited 1 with no output, because a page hook ended non-zero under `set -e`. A test with a fake build catches that class in 8 seconds, and an `ERR` trap names the function, line and command of any other silent death.
- **Scope:** `tool/test_apptool.sh` runs the real apptool against a throw-away app: fake `gradlew`, local folders (`PUBLISH_HOST=""`, `WEB=file://…`), no network, no phone. Not covered: the real Gradle build, `ssh vps`, the GitHub release step, `ship -k` (needs a JDK).
- **Cost:** A hook that relied on a failing command being ignored now prints a 🐛 line; the exit code is unchanged.
- **Alt:** Wrap `page` in `|| { … }` (rejected: that switches `set -e` off inside the hook, so a half-rendered page would upload) · test through each app's wrapper (rejected: needs a 1-minute build per case).
- **Ck:** `tool/test_apptool.sh` green (118 checks) · case "a hook th dies says where" is red without the trap.

### D-002 @1122 · 🏛️ The kit lives in this repo and every app vendors it at <app>/appkit/ with git subtree · Accepted (omm crew @1122, 🦠 order via M8 #327 + #329)
- **Y:** Same path in every app, so app code never changes when the kit moves; a vendored copy lets public clones and CI build from one checkout. Full record (alternatives, no-behavior-change proof): [omm D-079](../omm/adr.md#d-079).
- **Ck:** `git -C ../omm log --oneline -- appkit` shows the subtree add · omm 149 unit + lint + py green on the vendored copy.

### D-001 @1122 · 🏛️ appkit is four shelves — tool, templates, lessons, shared core code · Accepted (🦠 @1122 via M8)
- **Y:** 🦠's words: not only the ship / deploy tool, also templates for README and web page, notes on anything we learn when developing apps ("like a refl / distill"), and core code that can and should be shared (chat, updater, …). Reusable components we use more often.
- **Scope:** Android apps of ours (omm · cali · next). In = general, proven in one app, wanted by a second. An app stays config (package · name · page url · key path); no app names inside the kit.
- **Cost:** Two or more apps depend on one kit: a kit bug breaks all of them, and a kit change needs every app's tests green.
- **Alt:** Keep copying per app (rejected: copies drift, a fix lands in one app only) · tool-only kit (rejected by 🦠 @1122: too narrow).
- **Ck:** omm consumes the kit with its tests green (m8 order 327) · cali adopts (m8 order 328) · `ls pr/appkit` shows the four shelves.

### D-00X @MMDD · <Outcome: what is now true> · Proposed
- **Y:** One or two sentences.
- **Scope:** _(optional)_ Where it applies, and where it does not.
- **Cost:** _(optional)_ What we give up or pay (size, speed, money, a lost option).
- **Alt:** _(optional)_ Rejected options, each with a short "why not".
- **Prev:** _(optional)_ The decision this one supersedes: D-000.
- **Ck:** _(optional)_ Test, file or command that proves the code still fits.

### Rules
- **Title = outcome**, not topic. ✔️ "Tap turns the page; double-tap opens the menu" · ❌ "Gestures swapped".
  - No ` · ` inside the title: `bin/adr` splits the heading on it.
- **Status is written out, never inferred:**
  - `Proposed` — waiting for 🦠. Put the choices in an **Options:** field, one sub-bullet each, recommendation marked `(rec)`; **Y:** explains only the recommendation.
  - `Accepted (🦠 @MMDD)` — who accepted, and when. Add `via M8 #NNN` if relayed. A crew's own call: `Accepted (crew @MMDD)`; another crew's: `Accepted (kb crew @MMDD)`.
  - `Superseded by D-NNN` — keep the entry, change only the status.
  - Split or partial: join with `;`, e.g. `Accepted (🦠 @1120); hold part superseded by D-065`. 🚫 ` · ` inside a status.
- **Y** is required. Everything else only when it helps a reader who comes back cold.
- **Field order:** Y · Options (Proposed only) · Ctx · Scope · Cost · Alt · Effects · Lock-in · Prev · Ck (skip the ones you don't need).
- **Link to a decision as `adr.md#d-002`** (kb D-046: the KB site gives every decision heading that id). It survives title and status changes; a long `#d-002-old-title` anchor breaks on every rename.
- **Date** `@MMDD` is required; unknown → the day the line first appeared in git (recipe step 2 in [adr_convert.md](../vibe++/adr_convert.md)).
- Fields with no slot above (privacy, go-live …) keep their own label, just before Prev/Ck.
- **Link, don't copy.** Name the test, file or command. Implementation details live in code and git.
- **🏛️ tier** = foundational and hard to reverse (data format, security boundary, public interface, main stack). Only those require Ctx, Alt, Effects and Lock-in. Expect a few per project, not dozens.
- To list a decision in the KB pattern index, add `- **pattern**: <name>`.

## ❓ Open questions
- Unknowns worth naming. The next thing to decide.

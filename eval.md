# ☭ omm eval card · @1121
**state:** v1 milestone (Matrix chat + audio + quality bar), QA phase on DEV `0.13.0-dev.1121.0014`. Next: two night jobs at 03:00 (topic chips, 3 translations), then one DEV deploy.

**today**
- Chat tested end to end against the live server, also through the real app screens (12 of 12 passed).
- "Delete account" built in ⚙️ (not deployed yet). Topic-chip re-tagging built, its data runs tonight.
- 13 open questions gathered into one list with recommendations. Old research and design notes closed with proof.

**try**
1. Holiday icons: `o ~/gopro/go/pr/omm/android/app/build/shots/egg_arch_de.png`. Good = the title fits on the arch, the small line under the egg reads fine. ~1 min.
2. Sunday morning: `tail -40 ~/gopro/go/pr/omm/data/retag_night_1122.log`. Good = at least 2 of 3 quotes per chip are really about that topic (boss, rent, …). ~1 min.
3. Phone: updater test, steps in todo ⏳ (install the DEV apk from the browser, then update in one tap). ~5 min.

**tools:** no `<x>tool`. Worth knowing: `bin/matrix_e2e.py` (live chat test · `--app` = through the app · `--cleanup`) · `bin/retag.py --stats` · `bin/check_langs.py` (ship guard). Lesson: notes.md § 🏗️ technical: Matrix (10 min).

**needs you** (recommendation first)
1. ✔️ 12 questions settled by your "ok all" (D-073, D-074). One left: mark D-015 as replaced by D-039, no code change. Rec yes.
2. Delete-account wording (quoted in todo 👀): rec ok as is.
3. Send scagent.de/omm to 2-3 friends: rec yes after the next DEV deploy.

# eos omm — resume log · newest on TOP
_1 block per /eos · written by /eos § 3 · todo keeps 1 line: `▶ resume ➡ eos.md`_

## 261119 15:30 · qa_1117 r1-r4 built · v0.12.0 stable · plan versioning + 6 🔒 plans
- **at** · stable = v0.12.0 (@1118) · DEV = `0.13.0-dev.1119.1507` (= all up 2 `01b7e27`) · lint + unit + 39 py + check_langs green · pushed
  - ⚠️ oth crew lanes landed aftr th deploy: `6f48c4d` L9 double-tap + `55fa867` design § 14 addendum ➡ NOT on DEV yet
- **▶ NEXT** · `bin/deploy.sh` (picks up L9) ➡ 🦠 fon pass over qa_1117 `👀 ck:` lines (r1-r3 L1-L5 · r4 L6-L8 · 🔒 plans via ⚙️ DEV unlock) ➡ Fable design spec (D6 · D8-D11 · design § 14)
- **👤 🦠** · Q1-Q9 i qa_1117 (Q1 one score + Q2 milestones unblock streak work) · skim bridge facts i 6 new plans · updater fon tst (todo ⏳) · fon cold-start ms
- **⚠️** · any plan cut change ➡ build.py auto-bumps `ver` ➡ commit `data/plan_versions.json` w it (test_bin fails otherwise) · translate `--only x a` on new plan days = ~1 h ➡ run i bg · 🔵 13,319/13,320 DE⬄EN split (align.py) · wertform v2 Andachten = local M tic

## 261117 · ship v0.11.0 (moved fr todo § ⏳ ▶ RESUME @1117)
- ▶ **RESUME @1117 ship** — v0.11.0 = stable (shipped @1117, GH release ✔️) · DEV = `0.11.0-dev.1117.1108` (`a859d58`, = all commits) · lint + unit + 31 py green · all pushed
  - 👤 🦠: fon tst in-app update msg on BOTH: DEV 2227 ➡ 1108 · normal 0.10.0 ➡ 0.11.0 (Today banner on app start)
  - ▶ nxt: fon QA ➡ Fable design spec (🦠: AFTER this round) · nxt `bin/deploy.sh` = `0.12.0-dev…`
  - 👤 🦠: fon tst updater (todo ⏳ 🦠📱 item) · fon cold start `adb shell am start -W` (L6 before ⬄ after) · baseline profile = device run
  - ⚠️ traps: new tr() string ➡ `bin/translate.py --only ui` or ship refuses (check_langs) · guard baseline strict both ways · new plan days ➡ `translate.py --only a x` · EN gaps = 0 (ContentTest fails on any blank ➡ fix i align.py: PIN / labels)

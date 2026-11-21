# 🌙 night 1122 — omm (#308 · Rosa 🔥)
- **did** · fresh-eyes review: Matrix path (D-073) · app secu · docs drift · ⏳ F1 fix · ⏳ 03:00 GPU logs
- **top 3** · 🔴 F1 Synapse-era fon accounts = dead token ➡ comments + Kreise fail 4ever (fix branch nvr merged) · 🔴 F2 live HS = **continuwuity** since @1113, omm repo + D-072 still say Synapse · 🔴 F4 comment name = sender-set per msg ➡ anyone posts as "strix"
- **🦠** · F3 delete-account promise ("new readers no longer see ur comments") unverified on continuwuity ❓ · F7 `allowBackup` ships Matrix pw 2 Google backup: keep (= account survives a new fon) o exclude ❓

## findings
| # | sev | wh | where | rec | fixed? |
|--|--|--|--|--|--|
| F1 | 🔴 | accounts made b4 th 11/13 cutover hold a token th new server nvr K ➡ evy comment / Kreis call 401 ➡ "failed", no way back (only ⚙️ Delete account, not deployed) · fix = branch `matrix-reregister` @befc620 (11/12) **nvr merged** | `ui/AppVm.kt:114` `session()` | merge w whoami ck, re-register only aftr a dead token | ⏳ |
| F2 | 🔴 | live = continuwuity v0.5.10 (container `matrix`) since @1113 · omm `server/matrix/` (README · overrides.yaml · compose.yml · setup.py) = Synapse template, dead · notes/adr/todo/eos say "live Synapse" · todo 🏔️ v1 "Synapse `user_ips_max_age`" = Synapse knob, stale (kb triage 1117 #59 flagged it) | `server/matrix/*` · `todo_omm.md` 🏔️ v1 · `adr.md` D-072 | ops D-003 = config SSOT ➡ ops: omm keeps `SERVICE.md` contract + pointer ➡ rm `server/matrix/` (🦠 ok, rm) | ❌ finding |
| F3 | 🔴 | D-072 dialog promises "new readers no longer see your comments" = Synapse `erase` · continuwuity erase support unverified · `--app` step 12 only cks token dead + left circle | `adr.md` D-072 · `Matrix.kt` `deactivate` | ops: 1 throwaway acct on stg ➡ comment ➡ deactivate erase ➡ 2nd acct reads room · ❌ ➡ redact own msgs b4 deactivate (app-side) | ❌ finding |
| F4 | 🔴 | shown name = `content["de.ohmymarx.name"]`, set by th sender per msg · no user id shown ➡ any token holder (o Element) posts as "strix" / any friend | `Matrix.kt:76` · `ui/Comments.kt:133` · `ui/KreisScreen.kt:290` | show th localpart muted under th name (`rosa-ab12`) · o name fr room member state (still free 2 set, but 1 per acct) | ❌ finding (UX = 🦠) |

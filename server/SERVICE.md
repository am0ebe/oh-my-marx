# 📜 SERVICE.md — what Oh My Marx needs from whoever runs it (the contract)

## 🧭 who owns what (separation of concerns, @1103)
| | **omm** (this repo = the product) | **ops** (`pr/ops` = our infra) |
|--|--|--|
| owns | app · server **code** · images/config **templates** · this contract | **running** it: VPS dirs, Caddy routes, TLS, pins + updates, backups, monitoring, box secrets |
| changes prod? | ❌ no — asks ops (todo / msg) · only exception: writes the download pages (below) | ✔️ yes |
| changes code? | ✔️ yes | ❌ no — asks omm |
| docs | generic: "this service needs X" (works on any box) | concrete: "on our VPS it's at /srv/…, routed by …" ➡ `pr/ops/omm.md` (ops-owned runbook + rip-out list) |
- 🔌 **rip-out test**: drop omm ➡ ops' doc lists every container/dir/route/backup to delete · drop our VPS ➡ this file is enough for any operator to run it elsewhere
- the shared seam is **only this file**. If it changes, tell the other side.

## 1 · `omm-api` — anonymous read counter (`server/omm_api.py`)
| | |
|--|--|
| build | `server/Dockerfile` (python:3.12-alpine, stdlib only, pip removed from the image) · context `server/` w `.dockerignore` |
| listens | :8000 HTTP inside the container · no TLS of its own |
| route needed | `https://<host>/omm/api/*` ➡ strip prefix ➡ `:8000` · app hard-codes `https://scagent.de/omm/api/v1` |
| proxy must | **replace** any client-sent `X-Forwarded-For` (Caddy ≥ 2.5 default) · the rate limit keys on its 1st hop ➡ `:8000` never reachable without the proxy |
| state | `/data/omm.db` (SQLite) — totals + today's tokens · loss = counters restart at 0 (not critical) |
| health | `GET /v1/health` ➡ `{"ok":true}` |
| privacy promise | no access log, no IPs on disk — **don't add proxy access logs for this route** |
| resources | ~20 MB RAM · 64 MB cap is plenty |
| updates | rebuild when the base image moves · no migrations |

## 2 · Matrix homeserver — comments (any client-server v3 homeserver · ours = continuwuity `matrix` since @1113, ops-owned ➡ `pr/ops/matrix.md` · old Synapse template `server/matrix/` removed @1122, in git history)
| | |
|--|--|
| image | any client-server v3 homeserver · ours = continuwuity (config + compose = ops) |
| listens | client API only (no federation needed) |
| routes needed | `https://<host>/_matrix/*` ➡ the homeserver · `/.well-known/matrix/client` ➡ `{"m.homeserver":{"base_url":"https://<host>"}}` · **no** admin API on the public route |
| server_name | `scagent.de` — baked into every user id + room alias, **can't change later** |
| signup | registration **token** required (no open signup) · UIA flow: `m.login.registration_token` ➡ `m.login.dummy` (the app does exactly this) |
| state | the homeserver's DB (all comments + accounts) · signing key · reg token · admin secret ➡ **must be backed up** · loss = all comments + accounts gone |
| one-time setup | 31 public chapter rooms `#omm-kap-<V1..V6 \| 1..25>:scagent.de`: `public_chat`, history `world_readable`, power levels `events_default 0 · state_default 100 · users_default 0 · redact 50 · kick 50 · ban 50 · invite 0` (everyone posts + redacts own, only the admin touches state) · ours = ops `bin/matrix_setup.py` (safe to rerun) |
| user rooms (@1104) | 👥 **Lesekreise** = rooms users create themselves: alias `#omm-kreis-<6-char code>`, join rule public but not in the directory, history `shared`, custom timeline event `de.ohmymarx.amen` (`{"day":"YYYY-MM-DD"}`) · ⇒ room count + db grow with users (small text only) · needs: normal users may create rooms (default) |
| health | `GET /_matrix/client/versions` |
| resources | ~105 MB RAM idle · 512 MB cap |
| updates | homeserver security releases are frequent ➡ bump often · DB backup before each bump (continuwuity = RocksDB dir copy, `pr/ops/matrix.md`) |

## 3 · secrets crossing the seam
- **registration token**: set on the homeserver (ours: ops, `pr/ops/matrix.md`) ➡ the app build needs it in git-ignored `android/omm.properties` (`omm.regToken=…`) ➡ ships inside the APK (by design)
- `admin.secret` (@ommadmin pw): stays on the server, never in this repo

## 4 · the 1 thing omm does on the box
- `bin/ship.sh` / `bin/deploy.sh` rsync APKs + `index.html` into `/srv/www/oh-my-marx/` (+ `dev/`), owned by am0
- ops' part: Caddy serves `/srv/www` as static files at `https://scagent.de/`

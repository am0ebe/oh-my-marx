# 📜 SERVICE.md — what Oh My Marx needs from whoever runs it (the contract)

## 🧭 who owns what (separation of concerns, @1103)
| | **bible** (this repo = the product) | **ops** (`pr/ops` = our infra) |
|--|--|--|
| owns | app · server **code** · images/config **templates** · this contract | **running** it: VPS dirs, Caddy routes, TLS, pins + updates, backups, monitoring, box secrets |
| changes prod? | ❌ no — asks ops (todo / msg) · only exception: writes the download pages (below) | ✔️ yes |
| changes code? | ✔️ yes | ❌ no — asks bible |
| docs | generic: "this service needs X" (works on any box) | concrete: "on our VPS it's at /srv/…, routed by …" ➡ `pr/ops/omm.md` (ops-owned runbook + rip-out list) |
- 🔌 **rip-out test**: drop bible ➡ ops' doc lists every container/dir/route/backup to delete · drop our VPS ➡ this file is enough for any operator to run it elsewhere
- the shared seam is **only this file**. If it changes, tell the other side.

## 1 · `omm-api` — anonymous read counter (`server/omm_api.py`)
| | |
|--|--|
| build | `server/Dockerfile` (python:3.12-alpine, stdlib only) · context `server/` w `.dockerignore` |
| listens | :8000 HTTP inside the container · no TLS of its own |
| route needed | `https://<host>/omm/api/*` ➡ strip prefix ➡ `:8000` · app hard-codes `https://scagent.de/omm/api/v1` |
| state | `/data/omm.db` (SQLite) — totals + today's tokens · loss = counters restart at 0 (not critical) |
| health | `GET /v1/health` ➡ `{"ok":true}` |
| privacy promise | no access log, no IPs on disk — **don't add proxy access logs for this route** |
| resources | ~20 MB RAM · 64 MB cap is plenty |
| updates | rebuild when the base image moves · no migrations |

## 2 · `omm-matrix` — comments (Synapse, `server/matrix/`)
| | |
|--|--|
| image | `matrixdotorg/synapse` (any current 1.x) · config = generated `homeserver.yaml` + `server/matrix/overrides.yaml` appended |
| listens | :8008 client API only (no federation listener) |
| routes needed | `https://<host>/_matrix/*` ➡ `:8008` · `/.well-known/matrix/client` ➡ `{"m.homeserver":{"base_url":"https://<host>"}}` · **not** `/_synapse/admin` |
| server_name | `scagent.de` — baked into every user id + room alias, **can't change later** |
| state | `/data/` = `homeserver.db` (all comments + accounts) · `*.signing.key` · `homeserver.yaml` · `omm_reg_token` · + `admin.secret` next to it ➡ **must be backed up** · loss = all comments + accounts gone |
| one-time setup | `server/matrix/setup.py` (admin login ➡ registration token ➡ 31 chapter rooms) · safe to rerun |
| health | `GET /_matrix/client/versions` |
| resources | ~105 MB RAM idle · 512 MB cap |
| updates | Synapse security releases are frequent ➡ bump often · SQLite backup before each bump |

## 3 · secrets crossing the seam
- **registration token**: made on the server by `setup.py` (`/data/omm_reg_token`) ➡ the app build needs it in git-ignored `android/omm.properties` (`omm.regToken=…`) ➡ ships inside the APK (by design)
- `admin.secret` (@ommadmin pw): stays on the server, never in this repo

## 4 · the 1 thing bible does on the box
- `bin/ship.sh` / `bin/deploy.sh` rsync APKs + `index.html` into `/srv/www/oh-my-marx/` (+ `dev/`), owned by am0
- ops' part: Caddy serves `/srv/www` as static files at `https://scagent.de/`

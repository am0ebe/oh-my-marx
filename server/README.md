# 👥 omm-api — Oh My Marx community counter

Step ① of the community plan (`../notes.md` § community): **"👥 34× gelesen"** on every book page and
**"Heute 12 × Amen ☭"** on Home. Social proof only, no accounts, no chat (that's step ②, Matrix).

## Privacy (why it's built this way)
- the app sends a **random token that changes every day** + the page keys it just read (`p:247` = MEW page)
- the server keeps a token **only for today** (to count each reader once per page per day)
- the first write of a new day **deletes all of yesterday's tokens** → only totals are left
- IPs are never written to disk (RAM only, 1 min, for the rate limit) · no access log
- the app only talks to it while **⚙️ Mitlesen** is on (on by default since @1103, one switch to turn it off)

## Run + test locally
```
python3 server/omm_api.py --db /tmp/omm.db --port 8000
python3 -m unittest server/test_omm_api.py
curl -s localhost:8000/v1/stats?keys=p:247
```

## On the VPS
Container (as `am0`, docker group, no sudo — already done by Claude @1103):
```
rsync -a --exclude __pycache__ --exclude test_omm_api.py --exclude matrix server/ vps:/srv/omm-api/ && ssh vps 'cd /srv/omm-api && docker compose up -d --build'
# (stack moved ~am0/omm-api ➡ /srv/omm-api by ops @1103: fleet.py runs as `claude` + manages updates/bup)
# ⚠️ nvr `rsync --delete` over data/: vps_bup.sh keeps data/*.bak (600) there · .dockerignore keeps data/ out o th image
ssh vps 'docker exec caddy wget -qO- http://omm-api:8000/v1/health'     # {"ok": true}
```
**Public route — 1 manual step (Caddyfile is owned by `claude`, needs sudo):**
```
ssh -t vps sudo -e /srv/proxy/Caddyfile
```
in the `scagent.de, www.scagent.de { … }` block, replace `file_server` with:
```
	handle_path /omm/api/* {
		reverse_proxy omm-api:8000
	}
	handle {
		file_server
	}
```
then `ssh vps 'docker exec caddy caddy reload --config /etc/caddy/Caddyfile'` and check
`curl -s https://scagent.de/omm/api/v1/health` → `{"ok": true}`.

## API
| | |
|--|--|
| `POST /v1/read` `{"token","keys":[…]}` | count this reader once per key today (max 20 keys) |
| `POST /v1/amen` `{"token"}` | 1 Amen per reader per day |
| `GET /v1/stats?keys=p:247\|p:248` | `{"reads":{…},"amen_today":n,"readers_today":n}` |
| `GET /v1/health` | `{"ok":true}` |

Rate limit 120 writes / IP / min · body ≤ 4 KB · keys `^[a-z]:[A-Za-z0-9.,_-]{1,40}$`.

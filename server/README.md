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

## Run it anywhere
`docker compose up -d --build` in this dir (compose.yml = template: joins an external `web` network, no host port) ·
then route `https://<host>/omm/api/*` (prefix stripped) to `omm-api:8000`. Full needs: [`SERVICE.md`](SERVICE.md).

## Where it runs for real
**ops runs it** on our VPS (dirs, Caddy, updates, backups, monitoring) — see `pr/ops/omm.md`.
This repo doesn't touch prod: code changes here ➡ ask ops to roll them out.

## API
| | |
|--|--|
| `POST /v1/read` `{"token","keys":[…]}` | count this reader once per key today (max 20 keys) |
| `POST /v1/amen` `{"token"}` | 1 Amen per reader per day |
| `GET /v1/stats?keys=p:247\|p:248` | `{"reads":{…},"amen_today":n,"readers_today":n}` |
| `GET /v1/health` | `{"ok":true}` |

Rate limit 120 writes / IP / min · body ≤ 4 KB · keys `^[a-z]:[A-Za-z0-9.,_-]{1,40}$`.

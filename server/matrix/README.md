# 💬 omm-matrix — comments for Oh My Marx (step ② of `../../notes.md` § community)

## 🧩 Matrix in 1 minute (why we use it)
- an **open chat standard** — like e-mail, but chat. No company in the middle.
- **homeserver** = a server that stores accounts + rooms (ours: Synapse on the VPS, `scagent.de`)
- **client** = the app people use: Element, FluffyChat, gomuks … — and **Oh My Marx**, which speaks a tiny part of the protocol
- **federation** = servers talk to each other (`@rosa:scagent.de` ⬄ `@max:matrix.org`). **Ours: off.**
- 🔐 cryptography, not coins: servers sign their events; DMs / private rooms can be **end-to-end encrypted**
  (only the people in the chat can read). Our public chapter rooms are **not** encrypted (the app has no E2E code).
- why Matrix and not our own comment DB: accounts, rooms, moderation, history, rate limits come for free — and every
  Matrix app can join the same rooms (desktop deep dives, own friends chat)

## 👥 fast: your own chat with friends (~5 min)
1. **your account** (as server admin), asks for a password:
   `ssh -t vps docker exec -it omm-matrix register_new_matrix_user -c /data/homeserver.yaml -u strix -a http://localhost:8008`
2. **app**: Element (Android/iOS, "Element Classic") · desktop: Element Desktop or https://app.element.io
   ➡ *Sign in* ➡ *Edit* homeserver ➡ `scagent.de` ➡ `strix` + password
3. try it: *Join room* ➡ `#omm-kap-8:scagent.de` = the app's chapter-8 comments, live
4. **friends**: Element ➡ *Create account* ➡ server `scagent.de` ➡ it asks for the **registration token**
   (`cat android/omm.properties` on tow, or `ssh vps 'docker exec omm-matrix cat /data/omm_reg_token'`)
5. you: *Start chat* ➡ `@friend:scagent.de` ➡ DM (encrypted by default in Element) · or a room ➡ invite
- these are **normal Matrix accounts** — usable for anything, not just Oh My Marx
- 📱 **Element X** (the new, faster Element on mobile) can *log in* to an existing account (Synapse ≥ 1.114 has the
  sliding sync it needs), but it can't *sign up* with a registration token → create the account first (step 1 / classic Element)

## 🌍 later: talk to the rest of the Matrix world (federation on)
1. `overrides.yaml`: listener `names: [client, federation]` · drop `federation_domain_whitelist: []` · `trusted_key_servers: [{server_name: matrix.org}]`
2. Caddy: `/.well-known/matrix/server` ➡ `{"m.server":"scagent.de:443"}` (federation then runs over 443, `/_matrix/*` is already routed)
3. ⚠️ more traffic + spam · joining big public rooms costs a lot of RAM on Synapse · signing key becomes the server's identity (backup!)

A small **Synapse** homeserver on the VPS. The app uses it for **comments under every book page**.
Room per chapter (`#omm-kap-8:scagent.de`), each comment carries the MEW page (`de.ohmymarx.page`).

## Shape (why)
- **client API only, federation off** (`federation_domain_whitelist: []`): nothing talks to other servers, small attack surface
- **registration only with a token** (`registration_requires_token`): the token ships in the APK (via git-ignored
  `android/omm.properties` → `BuildConfig.OMM_REG_TOKEN`). It only keeps drive-by bots out; rate limits do the rest
- **no media, no presence, no URL previews** → less storage, less traffic, nothing to leak
- rooms: public, `world_readable`, everyone may post + delete own messages, only `@ommadmin` changes room state
- Caddy: `scagent.de/_matrix/*` → `omm-matrix:8008` + `/.well-known/matrix/client`. Admin API (`/_synapse/admin`) is **not** routed (404 outside)
- any Matrix app (Element …) can log in to `scagent.de` and join the same rooms → long discussions on a desktop

## Files on the VPS (`/srv/omm-matrix` — moved from `~am0/omm-matrix` by ops @1103; owner am0, group claude, 2775)
**ops runs this stack now**: updates (fleet.py auto-bumps the Synapse pin, sqlite backup first), backup (vps_bup.sh), health (mon.sh) → `pr/ops/handoff_omm.md`
| | |
|--|--|
| `compose.yml` | ⚠️ the **box's** copy is the truth (fleet.py edits the tag there) — re-read before you rsync this repo's copy over it |
| `data/homeserver.yaml` | generated + `overrides.yaml` appended (later keys win) |
| `data/*.signing.key`, `data/homeserver.db` | ⚠️ **back these up** (db = all comments) |
| `admin.secret` | `@ommadmin` password (600, never printed) |
| `data/omm_reg_token` | the app's registration token |

## Setup (done @1103 · kept as th record / 4 a rebuild — paths now /srv/omm-matrix)
```
ssh vps 'mkdir -p /srv/omm-matrix/data'
ssh vps 'cd /srv/omm-matrix && docker run --rm -v /srv/omm-matrix/data:/data -e SYNAPSE_SERVER_NAME=scagent.de -e SYNAPSE_REPORT_STATS=no -e UID=1000 -e GID=1000 matrixdotorg/synapse:v1.164.2 generate'
ssh vps 'cat >> /srv/omm-matrix/data/homeserver.yaml' < server/matrix/overrides.yaml
rsync server/matrix/compose.yml server/matrix/setup.py vps:/srv/omm-matrix/ && ssh vps 'cd /srv/omm-matrix && docker compose up -d'   # ⚠️ fresh box only (see compose.yml note)
ssh vps 'cd /srv/omm-matrix && umask 077 && openssl rand -hex 24 > admin.secret && docker exec omm-matrix register_new_matrix_user -c /data/homeserver.yaml -u ommadmin -p "$(cat admin.secret)" -a http://localhost:8008'
rsync server/matrix/setup.py vps:/srv/omm-matrix/ && ssh vps 'cd /srv/omm-matrix && docker exec -i -e OMM_ADMIN_PW="$(cat admin.secret)" omm-matrix python3 - < setup.py'   # token + 31 chapter rooms (safe 2 rerun)
ssh vps 'docker exec omm-matrix cat /data/omm_reg_token' | sed 's/^/omm.regToken=/' > android/omm.properties
```

## Moderation (as admin, on the VPS)
```
# delete a message:      redact via any Matrix client logged in as @ommadmin (Element: long-press ➡ Remove)
# lock out a user:       docker exec omm-matrix curl … /_synapse/admin/v1/deactivate/@user:scagent.de   (needs admin token)
# rotate the app token:  re-run setup.py after deleting data/omm_reg_token ➡ rebuild app (old installs can't sign up)
```

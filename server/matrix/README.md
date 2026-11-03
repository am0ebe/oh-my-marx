# 💬 omm-matrix — comments for Oh My Marx (step ② of `../../notes.md` § community)

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

## Files on the VPS (`~am0/omm-matrix`)
| | |
|--|--|
| `compose.yml` | from here · `docker compose up -d` |
| `data/homeserver.yaml` | generated + `overrides.yaml` appended (later keys win) |
| `data/*.signing.key`, `data/homeserver.db` | ⚠️ **back these up** (db = all comments) |
| `admin.secret` | `@ommadmin` password (600, never printed) |
| `data/omm_reg_token` | the app's registration token |

## Setup (done @1103, re-runnable)
```
ssh vps 'mkdir -p ~/omm-matrix/data'
ssh vps 'cd ~/omm-matrix && docker run --rm -v ~/omm-matrix/data:/data -e SYNAPSE_SERVER_NAME=scagent.de -e SYNAPSE_REPORT_STATS=no -e UID=1000 -e GID=1000 matrixdotorg/synapse:v1.164.2 generate'
ssh vps 'cat >> ~/omm-matrix/data/homeserver.yaml' < server/matrix/overrides.yaml
rsync server/matrix/compose.yml server/matrix/setup.py vps:omm-matrix/ && ssh vps 'cd ~/omm-matrix && docker compose up -d'
ssh vps 'cd ~/omm-matrix && umask 077 && openssl rand -hex 24 > admin.secret && docker exec omm-matrix register_new_matrix_user -c /data/homeserver.yaml -u ommadmin -p "$(cat admin.secret)" -a http://localhost:8008'
ssh vps 'cd ~/omm-matrix && docker exec -i -e OMM_ADMIN_PW="$(cat admin.secret)" omm-matrix python3 - < setup.py'   # token + 31 chapter rooms
ssh vps 'docker exec omm-matrix cat /data/omm_reg_token' | sed 's/^/omm.regToken=/' > android/omm.properties
```

## Moderation (as admin, on the VPS)
```
# delete a message:      redact via any Matrix client logged in as @ommadmin (Element: long-press ➡ Remove)
# lock out a user:       docker exec omm-matrix curl … /_synapse/admin/v1/deactivate/@user:scagent.de   (needs admin token)
# rotate the app token:  re-run setup.py after deleting data/omm_reg_token ➡ rebuild app (old installs can't sign up)
```

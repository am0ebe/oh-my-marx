"""💬 one-time / idempotent setup o th Oh My Marx homeserver — run INSIDE th container:

    docker exec -i -e OMM_ADMIN_PW="$(cat /srv/omm-matrix/admin.secret)" omm-matrix python3 - < setup.py

- logs in as @ommadmin
- app registration token: reuses /data/omm_reg_token o creates one (printed: it ships i th APK anyway —
  it only keeps random bots fr signing up, rate limits do th rest)
- 1 public room per Kapital chapter: #omm-kap-<kap>:scagent.de · evyone can post, only admin changes state
"""
import json
import os
import urllib.error
import urllib.parse
import urllib.request

HS = "http://localhost:8008"
KAPS = "V1 V2 V3 V4 V5 V6 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25".split()


def call(method, path, body=None, token=None):
    req = urllib.request.Request(HS + path, data=json.dumps(body).encode() if body is not None else None, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req) as r:
            return r.status, json.loads(r.read() or b"{}")
    except urllib.error.HTTPError as e:
        return e.code, json.loads(e.read() or b"{}")


code, r = call("POST", "/_matrix/client/v3/login",
               {"type": "m.login.password", "identifier": {"type": "m.id.user", "user": "ommadmin"}, "password": os.environ["OMM_ADMIN_PW"]})
assert code == 200, r
tok = r["access_token"]

# admin creates 31 rooms in a row ➡ lift its own rate limit (users keep th normal one)
call("POST", "/_synapse/admin/v1/users/@ommadmin:scagent.de/override_ratelimit", {"messages_per_second": 0, "burst_count": 0}, tok)

# 🎟️ registration token 4 th app
path = "/data/omm_reg_token"
if os.path.exists(path):
    reg = open(path).read().strip()
else:
    code, r = call("POST", "/_synapse/admin/v1/registration_tokens/new", {}, tok)
    assert code == 200, r
    reg = r["token"]
    open(path, "w").write(reg)
print("REG_TOKEN", reg)

# 📖 chapter rooms
for k in KAPS:
    alias = "omm-kap-" + k.lower()
    code, _ = call("GET", "/_matrix/client/v3/directory/room/" + urllib.parse.quote(f"#{alias}:scagent.de"))
    if code == 200:
        continue
    code, r = call("POST", "/_matrix/client/v3/createRoom", {
        "preset": "public_chat", "visibility": "public", "room_alias_name": alias,
        "name": f"Kapital · Kap. {k}", "topic": "💬 Oh My Marx — Kommentare zu diesem Kapitel (pro MEW-Seite)",
        "initial_state": [{"type": "m.room.history_visibility", "state_key": "", "content": {"history_visibility": "world_readable"}}],
        # evyone may post + redact own msgs · only admin (100) touches room state
        "power_level_content_override": {"events_default": 0, "state_default": 100, "users_default": 0, "redact": 50, "invite": 0, "kick": 50, "ban": 50},
    }, tok)
    print("room", alias, code, r.get("room_id", r))
print("done")

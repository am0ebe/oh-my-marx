#!/usr/bin/env python3
"""matrix_e2e — smoke tst vs th LIVE Synapse (scagent.de): th same calls th app makes (data/Matrix.kt), end 2 end.

y: MatrixTest only knows a fake server ➡ this proves th real one (token signup · chapter comments · Lesekreis) works.
wh: makes 2 REAL test accounts `e2e-<MMDD-hhmm>-a/-b` + 1 Lesekreis room "e2e Kreis …" (🦠 ok @1121)
  ① chapter room #omm-kap-<kap>: A comments (w MEW page) ➡ B reads it ➡ A deletes it ➡ B sees it gone
  ② Lesekreis: A creates (code) ➡ B joins by code ➡ members · name · chat both ways · B sends an Amen ➡ A sees it
  ③ guards: signup wo token / w a wrong token = refused · unknown room = soft fail
  ➡ table (step · ✔️/❌) · exit 1 if any ❌ · th Kreis + accounts STAY so 🦠 can look i Element ➡ then --cleanup
  state (ids + test pws) ➡ priv/matrix_e2e.json (git-ignored) · token fr android/omm.properties, nvr printed

examples:
  bin/matrix_e2e.py                # run all steps ➡ table + how 2 see it i Element
  bin/matrix_e2e.py --login        # print th e2e-a login (user + pw) 2 sign in i Element
  bin/matrix_e2e.py --cleanup      # aftr looking: leave + drop alias + deactivate both test accounts
"""
import argparse, datetime, json, pathlib, secrets, sys, time, urllib.error, urllib.parse, urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent
PROPS = ROOT / "android" / "omm.properties"
STATE = ROOT / "priv" / "matrix_e2e.json"
SERVER = "scagent.de"
HS = f"https://{SERVER}/_matrix/client/v3"
CODE_ABC = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"   # = Matrix.newCode (no look-alikes)


def call(method, path, body=None, token=None, base=HS):
    """1 HTTP call ➡ (status, json) · never raises on 4xx/5xx (like Matrix.kt's call)"""
    req = urllib.request.Request(base + path, method=method, data=None if body is None else json.dumps(body).encode(),
                                 headers={"Content-Type": "application/json", "User-Agent": "omm-matrix-e2e"})
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    try:
        with urllib.request.urlopen(req, timeout=15) as r:
            return r.status, json.loads(r.read() or b"{}")
    except urllib.error.HTTPError as e:
        try:
            return e.code, json.loads(e.read() or b"{}")
        except ValueError:
            return e.code, {}


def enc(s): return urllib.parse.quote(s, safe="")


def reg_token():
    if not PROPS.exists():
        sys.exit(f"❌ {PROPS} missing (git-ignored, lives on tow) ➡ no registration token")
    for line in PROPS.read_text().splitlines():
        if line.startswith("omm.regToken="):
            return line.split("=", 1)[1].strip()
    sys.exit("❌ no omm.regToken= i android/omm.properties")


def register(user, pw, token, name):
    """= Matrix.register: UIA 401 ➡ registration_token ➡ dummy · returns (status, json)"""
    body = {"username": user, "password": pw, "initial_device_display_name": "omm e2e"}
    code, r = call("POST", "/register", body)
    for _ in range(3):
        if code != 401:
            break
        done = r.get("completed", [])
        auth = ({"type": "m.login.registration_token", "token": token, "session": r.get("session")}
                if "m.login.registration_token" not in done else {"type": "m.login.dummy", "session": r.get("session")})
        code, r = call("POST", "/register", dict(body, auth=auth))
    if code == 200:
        call("PUT", f"/profile/{enc(r['user_id'])}/displayname", {"displayname": name}, r["access_token"])
    return code, r


def send(tok, room, content, typ="m.room.message"):
    code, r = call("PUT", f"/rooms/{enc(room)}/send/{enc(typ)}/omm{time.time_ns()}", content, tok)
    return r.get("event_id") if code == 200 else None


def messages(tok, room):
    """= Matrix.comments / timeline: last 300, redacted (no body) dropped"""
    code, r = call("GET", f"/rooms/{enc(room)}/messages?dir=b&limit=300", None, tok)
    return [e for e in r.get("chunk", []) if (e.get("type") == "m.room.message" and "body" in e.get("content", {}))
            or e.get("type") == "de.ohmymarx.amen"] if code == 200 else []


class Run:
    def __init__(self):
        self.rows = []

    def step(self, name, ok, note=""):
        self.rows.append((name, bool(ok), note))
        print(f"{'✔️' if ok else '❌'} {name}" + (f" · {note}" if note else ""), flush=True)
        return ok

    def table(self):
        print("\n| # | step | ok | note |\n|---|---|---|---|")
        for i, (n, ok, note) in enumerate(self.rows, 1):
            print(f"| {i} | {n} | {'✔️' if ok else '❌'} | {note} |")
        bad = sum(not ok for _, ok, _ in self.rows)
        print(f"\n{len(self.rows) - bad}/{len(self.rows)} ✔️" + (f" · {bad} ❌" if bad else ""))
        return bad


def run(kap):
    token = reg_token()
    t = Run()
    stamp = datetime.datetime.now().strftime("%m%d-%H%M")
    code, v = call("GET", "/versions", base=f"https://{SERVER}/_matrix/client")
    if not t.step("server answers /versions", code == 200 and v.get("versions"), f"HTTP {code}"):
        return t.table()

    # ③ guards 1st (cheap, mk nothing)
    c0, _ = call("POST", "/register", {"username": f"e2e-{stamp}-x", "password": secrets.token_hex(16)})
    t.step("signup wo token refused", c0 == 401, f"HTTP {c0} (401 = asks 4 auth)")
    cw, _ = register(f"e2e-{stamp}-x", secrets.token_hex(16), "wrong-" + secrets.token_hex(8), "x")
    t.step("signup w wrong token refused", cw in (401, 403), f"HTTP {cw}")

    users = {}
    for who, name in (("a", "e2e Rosa"), ("b", "e2e Juno")):
        uid, pw = f"e2e-{stamp}-{who}", secrets.token_hex(16)
        c, r = register(uid, pw, token, name)
        if t.step(f"register {who} w app token", c == 200, r.get("user_id", f"HTTP {c} {r.get('errcode', '')}")):
            users[who] = {"user_id": r["user_id"], "pw": pw, "token": r["access_token"], "name": name}
    st = {"stamp": stamp, "users": users, "rooms": {}}
    save(st)
    if len(users) < 2:
        return t.table()
    A, B = users["a"]["token"], users["b"]["token"]

    c, r = call("GET", f"/profile/{enc(users['a']['user_id'])}/displayname", None, A)
    t.step("display name set", r.get("displayname") == "e2e Rosa", r.get("displayname", f"HTTP {c}"))

    # ① chapter comments
    alias = f"#omm-kap-{kap}:{SERVER}"
    c, r = call("POST", f"/join/{enc(alias)}", {}, A)
    room = r.get("room_id")
    t.step(f"A joins {alias}", c == 200, room or f"HTTP {c}")
    if room:
        st["rooms"]["kap"] = room
        txt = f"e2e test comment {stamp} (deleted by th test)"
        ev = send(A, room, {"msgtype": "m.text", "body": txt, "de.ohmymarx.name": "e2e Rosa", "de.ohmymarx.page": 49})
        t.step("A comments w MEW page 49", ev, ev or "")
        cb, _ = call("POST", f"/join/{enc(alias)}", {}, B)
        seen = [e for e in messages(B, room) if e["content"].get("body") == txt]
        t.step("B reads it (txt + page + name)", cb == 200 and seen and seen[0]["content"].get("de.ohmymarx.page") == 49
               and seen[0]["content"].get("de.ohmymarx.name") == "e2e Rosa")
        if ev:
            cr, _ = call("PUT", f"/rooms/{enc(room)}/redact/{enc(ev)}/omm{time.time_ns()}", {}, A)
            gone = not any(e["content"].get("body") == txt for e in messages(B, room))
            t.step("A deletes it ➡ gone 4 B", cr == 200 and gone, f"redact HTTP {cr}")
        save(st)

    # ② Lesekreis
    code_ = "".join(secrets.choice(CODE_ABC) for _ in range(6))
    kname = f"e2e Kreis {stamp}"
    c, r = call("POST", "/createRoom", {
        "name": kname, "room_alias_name": f"omm-kreis-{code_.lower()}", "visibility": "private", "preset": "public_chat",
        "topic": f"👥 Oh My Marx Lesekreis · Code {code_} · e2e tst", "initial_state": [
            {"type": "m.room.history_visibility", "state_key": "", "content": {"history_visibility": "shared"}}]}, A)
    kroom = r.get("room_id")
    kalias = f"#omm-kreis-{code_.lower()}:{SERVER}"
    if t.step("A creates Lesekreis", c == 200, f"{kalias}" if kroom else f"HTTP {c} {r.get('errcode', '')}"):
        st["rooms"]["kreis"], st["kreis_alias"], st["kreis_code"] = kroom, kalias, code_
        save(st)
        c, r = call("POST", f"/join/{enc(kalias)}", {}, B)
        t.step("B joins by code", c == 200 and r.get("room_id") == kroom, f"code {code_}")
        c, r = call("GET", f"/rooms/{enc(kroom)}/joined_members", None, A)
        names = {v.get("display_name") for v in r.get("joined", {}).values()}
        t.step("members = both, w names", {"e2e Rosa", "e2e Juno"} <= names, " · ".join(sorted(n for n in names if n)))
        c, r = call("GET", f"/rooms/{enc(kroom)}/state/m.room.name", None, B)
        t.step("B sees Kreis name", r.get("name") == kname, r.get("name", f"HTTP {c}"))
        ma, mb = f"Hallo Juno, hier Rosa ({stamp})", f"Hallo Rosa, Juno liest mit ({stamp})"
        send(A, kroom, {"msgtype": "m.text", "body": ma, "de.ohmymarx.name": "e2e Rosa"})
        t.step("A ➡ B chat", any(e["content"].get("body") == ma for e in messages(B, kroom)))
        send(B, kroom, {"msgtype": "m.text", "body": mb, "de.ohmymarx.name": "e2e Juno"})
        t.step("B ➡ A chat", any(e["content"].get("body") == mb for e in messages(A, kroom)))
        day = datetime.date.today().isoformat()
        send(B, kroom, {"day": day, "de.ohmymarx.name": "e2e Juno"}, "de.ohmymarx.amen")
        t.step("B Amen ➡ A sees it", any(e["type"] == "de.ohmymarx.amen" and e["content"].get("day") == day
                                          for e in messages(A, kroom)), day)

    c, _ = call("POST", f"/join/{enc('#omm-kreis-nope00:' + SERVER)}", {}, A)
    t.step("unknown Kreis code = soft fail", c == 404, f"HTTP {c}")
    save(st)
    bad = t.table()
    if st.get("kreis_alias"):
        print(f"\n👀 see it i Element (app.element.io · homeserver {SERVER}):\n"
              f"  join {st['kreis_alias']}  (own account, o th test user: bin/matrix_e2e.py --login)\n"
              f"  {alias} shows 1 deleted msg by {users['a']['user_id']}\n"
              f"  done looking ➡ bin/matrix_e2e.py --cleanup")
    return bad


def save(st):
    STATE.parent.mkdir(exist_ok=True)
    STATE.write_text(json.dumps(st, indent=1))
    STATE.chmod(0o600)


def load_state():
    if not STATE.exists():
        sys.exit(f"no {STATE} ➡ nothing 2 do (run bin/matrix_e2e.py 1st)")
    return json.loads(STATE.read_text())


def cleanup():
    st = load_state()
    t = Run()
    a = st["users"].get("a")
    if st.get("kreis_alias") and a:
        c, _ = call("DELETE", f"/directory/room/{enc(st['kreis_alias'])}", None, a["token"])
        t.step(f"drop alias {st['kreis_alias']}", c == 200, f"HTTP {c}")
    for who, u in st["users"].items():
        for room in st["rooms"].values():
            c, _ = call("POST", f"/rooms/{enc(room)}/leave", {}, u["token"])
            t.step(f"{who} leaves {room}", c == 200, f"HTTP {c}")
        c, _ = call("POST", "/account/deactivate", {"erase": False, "auth": {
            "type": "m.login.password", "identifier": {"type": "m.id.user", "user": u["user_id"]}, "password": u["pw"]}},
            u["token"])
        t.step(f"deactivate {u['user_id']}", c == 200, f"HTTP {c}")
    bad = t.table()
    if not bad:
        STATE.unlink()
    return bad


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--kap", default="8", help="chapter room 4 th comment step (dflt 8 = #omm-kap-8)")
    ap.add_argument("--login", action="store_true", help="print th e2e-a login 4 Element")
    ap.add_argument("--cleanup", action="store_true", help="leave · drop alias · deactivate th test accounts")
    a = ap.parse_args()
    if a.login:
        u = load_state()["users"]["a"]
        print(f"homeserver {SERVER} · user {u['user_id']} · pw {u['pw']}")
        return 0
    return 1 if (cleanup() if a.cleanup else run(a.kap.lower())) else 0


if __name__ == "__main__":
    sys.exit(main())

#!/usr/bin/env python3
"""omm_api — Oh My Marx community counter (step ① o notes.md § community).

"👥 34× gelesen" on a book page · "Heute 12 × Amen ☭" on Home. Social proof, nothing else.

Privacy by construction (no accounts, no ids, no history):
  - th app sends a random token that changes evy day (+ th page keys it just read)
  - th token is only kept 4 TODAY, 2 count each reader once per page per day
  - at th 1st write o a new day all o yesterday's tokens r deleted ➡ only totals remain
  - IPs r never stored (only held i RAM 4 th rate limit, 1 min)

stdlib only (http.server + sqlite3) ➡ a 50 MB alpine image, nothing 2 patch.

    python3 omm_api.py --db omm.db --port 8000
    POST /v1/read  {"token": "...", "keys": ["p:247", "p:248"]}   ➡ {"ok": true}
    POST /v1/amen  {"token": "..."}                               ➡ {"ok": true}
    GET  /v1/stats?keys=p:247|p:248                               ➡ {"reads": {"p:247": 34}, "amen_today": 12, "readers_today": 30}
    GET  /v1/health                                               ➡ {"ok": true}
"""
from __future__ import annotations

import argparse
import json
import re
import sqlite3
import threading
import time
from collections import defaultdict, deque
from datetime import datetime
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import parse_qs, urlparse
from zoneinfo import ZoneInfo

TZ = ZoneInfo("Europe/Berlin")          # "today" = th server's day, th client can't fake it
TOKEN = re.compile(r"^[A-Za-z0-9_-]{16,64}$")
KEY = re.compile(r"^[a-z]:[A-Za-z0-9.,_-]{1,40}$")   # p:247 = MEW page · v:8,9 = verse · d:… = day card
MAX_KEYS = 20
MAX_BODY = 4096
RATE = 120                               # writes per IP per minute

SCHEMA = """
CREATE TABLE IF NOT EXISTS seen  (day TEXT, token TEXT, key TEXT, PRIMARY KEY (day, token, key));
CREATE TABLE IF NOT EXISTS reads (key TEXT PRIMARY KEY, n INTEGER NOT NULL DEFAULT 0);
CREATE TABLE IF NOT EXISTS amen  (day TEXT PRIMARY KEY, n INTEGER NOT NULL DEFAULT 0);
CREATE TABLE IF NOT EXISTS meta  (k TEXT PRIMARY KEY, v TEXT);
"""


def today() -> str:
    return datetime.now(TZ).date().isoformat()


class Store:
    """1 sqlite file, 1 lock (tiny traffic) · seen = today's dedupe rows only"""

    def __init__(self, path: str):
        self.db = sqlite3.connect(path, check_same_thread=False, isolation_level=None)
        self.db.execute("PRAGMA journal_mode=WAL")
        self.db.executescript(SCHEMA)
        self.lock = threading.Lock()

    def _roll(self, day: str) -> None:
        # new day ➡ forget all tokens (privacy: no token outlives its day)
        row = self.db.execute("SELECT v FROM meta WHERE k='day'").fetchone()
        if row is None or row[0] != day:
            self.db.execute("DELETE FROM seen WHERE day < ?", (day,))
            self.db.execute("INSERT OR REPLACE INTO meta VALUES ('day', ?)", (day,))

    def read(self, token: str, keys: list[str]) -> None:
        day = today()
        with self.lock:
            self.db.execute("BEGIN")
            self._roll(day)
            for k in keys:
                cur = self.db.execute("INSERT OR IGNORE INTO seen VALUES (?, ?, ?)", (day, token, k))
                if cur.rowcount:  # 1st time this token read this key today
                    self.db.execute("INSERT INTO reads VALUES (?, 1) ON CONFLICT(key) DO UPDATE SET n = n + 1", (k,))
            self.db.execute("INSERT OR IGNORE INTO seen VALUES (?, ?, '_any')", (day, token))  # readers_today
            self.db.execute("COMMIT")

    def amen(self, token: str) -> None:
        day = today()
        with self.lock:
            self.db.execute("BEGIN")
            self._roll(day)
            cur = self.db.execute("INSERT OR IGNORE INTO seen VALUES (?, ?, '_amen')", (day, token))
            if cur.rowcount:
                self.db.execute("INSERT INTO amen VALUES (?, 1) ON CONFLICT(day) DO UPDATE SET n = n + 1", (day,))
            self.db.execute("INSERT OR IGNORE INTO seen VALUES (?, ?, '_any')", (day, token))
            self.db.execute("COMMIT")

    def stats(self, keys: list[str]) -> dict:
        day = today()
        with self.lock:
            q = ",".join("?" * len(keys))  # f-string below = only "?" placeholders, values bound ➡ B608 false alarm
            reads = dict(self.db.execute(f"SELECT key, n FROM reads WHERE key IN ({q})", keys).fetchall()) if keys else {}  # nosec B608
            amen = (self.db.execute("SELECT n FROM amen WHERE day = ?", (day,)).fetchone() or (0,))[0]
            readers = self.db.execute("SELECT COUNT(*) FROM seen WHERE day = ? AND key = '_any'", (day,)).fetchone()[0]
        return {"reads": reads, "amen_today": amen, "readers_today": readers}


class Limiter:
    """sliding 60 s window per IP, RAM only"""

    def __init__(self, per_min: int):
        self.per_min = per_min
        self.hits: dict[str, deque] = defaultdict(deque)
        self.lock = threading.Lock()

    def ok(self, ip: str) -> bool:
        now = time.monotonic()
        with self.lock:
            q = self.hits[ip]
            while q and now - q[0] > 60:
                q.popleft()
            if len(q) >= self.per_min:
                return False
            q.append(now)
            if len(self.hits) > 10_000:  # don't grow forever
                self.hits = defaultdict(deque, {k: v for k, v in self.hits.items() if v})
            return True


def make_handler(store: Store, limiter: Limiter):
    class H(BaseHTTPRequestHandler):
        server_version = "omm/1"

        def log_message(self, *a):  # no access log = no IPs on disk
            pass

        def _send(self, code: int, obj: dict) -> None:
            body = json.dumps(obj).encode()
            self.send_response(code)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(body)))
            self.send_header("Cache-Control", "no-store")
            self.end_headers()
            self.wfile.write(body)

        def _ip(self) -> str:
            # behind Caddy: th real client = X-Forwarded-For's 1st hop
            return (self.headers.get("X-Forwarded-For") or self.client_address[0]).split(",")[0].strip()

        def do_GET(self):
            u = urlparse(self.path)
            if u.path == "/v1/health":
                return self._send(200, {"ok": True})
            if u.path == "/v1/stats":
                # | separated (keys may contain commas: v:8,9)
                keys = [k for k in parse_qs(u.query).get("keys", [""])[0].split("|") if k][:MAX_KEYS]
                if not all(KEY.match(k) for k in keys):
                    return self._send(400, {"error": "bad key"})
                return self._send(200, store.stats(keys))
            self._send(404, {"error": "not found"})

        def do_POST(self):
            if not limiter.ok(self._ip()):
                return self._send(429, {"error": "slow down"})
            n = int(self.headers.get("Content-Length") or 0)
            if n <= 0 or n > MAX_BODY:
                return self._send(413 if n > MAX_BODY else 400, {"error": "body"})
            try:
                data = json.loads(self.rfile.read(n))
            except (ValueError, UnicodeDecodeError):
                return self._send(400, {"error": "json"})
            token = data.get("token") if isinstance(data, dict) else None
            if not isinstance(token, str) or not TOKEN.match(token):
                return self._send(400, {"error": "token"})
            path = urlparse(self.path).path
            if path == "/v1/read":
                keys = data.get("keys")
                if not isinstance(keys, list) or not 0 < len(keys) <= MAX_KEYS or not all(isinstance(k, str) and KEY.match(k) for k in keys):
                    return self._send(400, {"error": "keys"})
                store.read(token, keys)
                return self._send(200, {"ok": True})
            if path == "/v1/amen":
                store.amen(token)
                return self._send(200, {"ok": True})
            self._send(404, {"error": "not found"})

    return H


def serve(db: str, host: str, port: int) -> ThreadingHTTPServer:
    srv = ThreadingHTTPServer((host, port), make_handler(Store(db), Limiter(RATE)))
    srv.daemon_threads = True
    return srv


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description="Oh My Marx community counter")
    ap.add_argument("--db", default="omm.db")
    # 0.0.0.0 = container port, Caddy fronts it (B104)
    ap.add_argument("--host", default="0.0.0.0")  # nosec B104
    ap.add_argument("--port", type=int, default=8000)
    a = ap.parse_args()
    print(f"omm_api on {a.host}:{a.port} · db {a.db}", flush=True)
    serve(a.db, a.host, a.port).serve_forever()

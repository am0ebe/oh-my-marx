"""python3 -m unittest server/test_omm_api.py · real HTTP against a throwaway server + temp db"""
import json
import os
import sys
import tempfile
import threading
import unittest
import urllib.error
import urllib.request

sys.path.insert(0, os.path.dirname(__file__))
import omm_api


class Api(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        self.srv = omm_api.serve(os.path.join(self.tmp.name, "t.db"), "127.0.0.1", 0)
        self.base = f"http://127.0.0.1:{self.srv.server_address[1]}"
        threading.Thread(target=self.srv.serve_forever, daemon=True).start()

    def tearDown(self):
        self.srv.shutdown(); self.srv.server_close(); self.tmp.cleanup()

    def post(self, path, obj):
        req = urllib.request.Request(self.base + path, json.dumps(obj).encode(), {"Content-Type": "application/json"})
        try:
            with urllib.request.urlopen(req) as r:
                return r.status, json.loads(r.read())
        except urllib.error.HTTPError as e:
            return e.code, json.loads(e.read())

    def get(self, path):
        with urllib.request.urlopen(self.base + path) as r:
            return json.loads(r.read())

    A = "a" * 20
    B = "b" * 20

    def test_counts_each_reader_once_per_day(self):
        self.post("/v1/read", {"token": self.A, "keys": ["p:247", "v:8,9"]})
        self.post("/v1/read", {"token": self.A, "keys": ["p:247"]})      # same reader again: no +1
        self.post("/v1/read", {"token": self.B, "keys": ["p:247"]})
        s = self.get("/v1/stats?keys=p:247|v:8,9|p:1")
        self.assertEqual(s["reads"], {"p:247": 2, "v:8,9": 1})
        self.assertEqual(s["readers_today"], 2)

    def test_amen_once(self):
        self.post("/v1/amen", {"token": self.A})
        self.post("/v1/amen", {"token": self.A})
        self.post("/v1/amen", {"token": self.B})
        self.assertEqual(self.get("/v1/stats")["amen_today"], 2)

    def test_new_day_forgets_tokens_keeps_totals(self):
        self.post("/v1/read", {"token": self.A, "keys": ["p:247"]})
        real = omm_api.today
        try:
            omm_api.today = lambda: "2999-01-01"
            self.post("/v1/read", {"token": self.B, "keys": ["p:1"]})  # 1st write o th new day ➡ roll
            s = self.get("/v1/stats?keys=p:247")
            self.assertEqual(s["reads"]["p:247"], 1)          # total kept
            self.assertEqual(s["readers_today"], 1)           # only B today
        finally:
            omm_api.today = real

    def test_rejects_junk(self):
        self.assertEqual(self.post("/v1/read", {"token": "short", "keys": ["p:1"]})[0], 400)
        self.assertEqual(self.post("/v1/read", {"token": self.A, "keys": ["DROP TABLE"]})[0], 400)
        self.assertEqual(self.post("/v1/read", {"token": self.A, "keys": [f"p:{i}" for i in range(30)]})[0], 400)
        self.assertEqual(self.post("/v1/nope", {"token": self.A})[0], 404)

    def test_rate_limit(self):
        codes = [self.post("/v1/amen", {"token": self.A})[0] for _ in range(omm_api.RATE + 5)]
        self.assertIn(429, codes)


if __name__ == "__main__":
    unittest.main()

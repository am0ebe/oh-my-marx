#!/usr/bin/env python3
"""andacht — 1 short tongue-in-cheek "devotional" per plan day, DE + EN ➡ data/andachten.jsonl (resumable)

reads data/plans.json (fr build.py) · skips th 1-year plan (too many days, pure reading)
a cached Andacht is reused only while th day's verse ids stay th same ➡ re-run build.py aftwards

usage: andacht.py [-m MODEL] [--plans 30tage vampir] [--redo]
"""
import argparse, json, pathlib, sys, time, urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "data" / "andachten.jsonl"
SKIP = {"jahr"}

SCHEMA = {"type": "object", "required": ["de", "en"],
          "properties": {"de": {"type": "string"}, "en": {"type": "string"}}}

PROMPT = """You write the daily "Andacht" (devotional) for a playful app that presents Marx' "Das Kapital" like a Bible app.
Reading plan: "{plan}" — day {day} of {ndays}.

Today's reading (German original, Kapital {ids}):
{de}

Write a short devotional, 60-100 words, in German (de) and the same in English (en):
- 1 sentence of context: what Marx is getting at here, in plain words.
- connect it to modern everyday life (jobs, overtime, rent, delivery apps, streaming, smartphones, gig work, AI).
- wink: parody the tone of a gentle Bible-app devotional ("Heute lädt uns Marx ein …"), but materialist — dry, a bit cheeky, at least one small joke; no pathos, no party jargon, not preachy.
- end with one short reflection question, just the question itself (no "Du:" / "You:" label).
- do not invent quotes; do not cite verse numbers.
Example of the tone (different topic): "Heute lädt uns Marx ein, die Ware mal genau anzuschauen. Ein Tisch, sagt er, ist ein ganz gewöhnliches Ding — bis er zur Ware wird und anfängt, Grillen zu entwickeln. Kennst du das? Dein Sneaker-Drop um 9:00 hat mehr Sozialleben als du. Was würde dein Warenkorb über dich erzählen, wenn er reden könnte?" """

def ask(model, prompt, url):
    body = {"model": model, "prompt": prompt, "format": SCHEMA, "stream": False,
            "options": {"temperature": 0.7, "num_ctx": 8192}}
    req = urllib.request.Request(url, data=json.dumps(body).encode(), headers={"Content-Type": "application/json"})
    return json.loads(json.load(urllib.request.urlopen(req, timeout=600))["response"])

def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("-m", "--model", default="qwen3:30b-a3b-instruct-2507-q4_K_M")
    ap.add_argument("--plans", nargs="*")
    ap.add_argument("--redo", action="store_true")
    ap.add_argument("--url", default="http://localhost:11434/api/generate")
    a = ap.parse_args()
    verses = {json.loads(l)["id"]: json.loads(l) for l in open(ROOT / "data" / "verses.jsonl", encoding="utf-8")}
    plans = json.load(open(ROOT / "data" / "plans.json", encoding="utf-8"))
    done = {}
    if OUT.exists() and not a.redo:
        for l in open(OUT, encoding="utf-8"):
            x = json.loads(l); done[(x["plan"], x["day"])] = x
    todo = [(p, i, d) for p in plans if p["id"] not in SKIP and (not a.plans or p["id"] in a.plans)
            for i, d in enumerate(p["days"]) if done.get((p["id"], i), {}).get("v") != d["v"]]
    print(f"{len(todo)} Andachten to write", file=sys.stderr)
    with open(OUT, "w" if a.redo else "a", encoding="utf-8") as out:
        for n, (p, i, d) in enumerate(todo, 1):
            t = time.time()
            de = "\n\n".join(verses[v]["de"] for v in d["v"])[:5000]
            r = ask(a.model, PROMPT.format(plan=p["tde"], day=i + 1, ndays=len(p["days"]), ids=", ".join(d["v"]), de=de), a.url)
            out.write(json.dumps({"plan": p["id"], "day": i, "v": d["v"], "de": r["de"].strip(), "en": r["en"].strip(),
                                  "model": a.model}, ensure_ascii=False) + "\n"); out.flush()
            print(f"[{n}/{len(todo)}] {p['id']} d{i+1} · {time.time()-t:.1f}s · {r['de'][:80]}", file=sys.stderr)

if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""tease — 1 teasing question per plan day, DE + EN ➡ data/teasers.jsonl (resumable)

why: the Stempeluhr pane (Day complete) ends with "Tomorrow: …?" — a question that opens a loop
     (Hooked: load the next trigger), never a cut quote (design § 14.1.2 · D-048 c · lane C7)
what: for evy plan day asks local ollama for a short curious question about THAT day's reading;
      the app shows day N+1's question on day N · build.py merges qd/qe into plans.json by verse ids
      (no cut change ➡ no plan version bump, D-042) · skips th 1-year plan (pure reading)

examples:
  bin/tease.py                       # all missing days, then: bin/build.py
  bin/tease.py --plans vampir bibel  # only these plans
  bin/tease.py --redo --plans gig    # rewrite gig's questions
"""
import argparse, json, pathlib, sys, time, urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "data" / "teasers.jsonl"
SKIP = {"jahr"}

SCHEMA = {"type": "object", "required": ["de", "en"],
          "properties": {"de": {"type": "string"}, "en": {"type": "string"}}}

PROMPT = """A playful app presents Karl Marx' "Das Kapital" like a Bible app. At the end of each day it teases TOMORROW's reading
with ONE short question that makes the reader curious — a cliffhanger, not a summary.
Reading plan: "{plan}" — this is the reading of day {day}.

The reading (German original):
{de}

Write the teaser question in German (de) and English (en):
- max 10 words, ends with "?"
- concrete and a bit cheeky (money, bosses, machines, rent, time, vampires … whatever THIS text is about)
- a question the text answers, but do not give the answer away
- no Marx quote, no verse numbers, no "Marx fragt" / "Morgen:" prefix
Examples (different texts): "Warum heckt Geld Junge?" / "Why does money breed young?" ·
"Wer hat dem Arbeitstag seine Grenze gestohlen?" / "Who stole the working day's limit?" """


def ask(model, prompt, url):
    body = {"model": model, "prompt": prompt, "format": SCHEMA, "stream": False,
            "options": {"temperature": 0.8, "num_ctx": 8192}}
    req = urllib.request.Request(url, data=json.dumps(body).encode(), headers={"Content-Type": "application/json"})
    return json.loads(json.load(urllib.request.urlopen(req, timeout=600))["response"])


def clean(q):
    q = q.strip().strip('"„“”«»').strip()
    for p in ("Morgen:", "Tomorrow:"):
        q = q.removeprefix(p).strip()
    return q if q.endswith("?") else q.rstrip(".!") + "?"


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("-m", "--model", default="qwen3:30b-a3b-instruct-2507-q4_K_M")
    ap.add_argument("-p", "--plans", nargs="*")
    ap.add_argument("-r", "--redo", action="store_true")
    ap.add_argument("-u", "--url", default="http://localhost:11434/api/generate")
    a = ap.parse_args()
    verses = {json.loads(l)["id"]: json.loads(l) for l in open(ROOT / "data" / "verses.jsonl", encoding="utf-8")}
    plans = json.load(open(ROOT / "data" / "plans.json", encoding="utf-8"))
    done = set()
    if OUT.exists():
        for l in open(OUT, encoding="utf-8"):
            x = json.loads(l)
            if not (a.redo and (not a.plans or x["plan"] in a.plans)):
                done.add((x["plan"], tuple(x["v"])))
    todo = [(p, i, d) for p in plans if p["id"] not in SKIP and (not a.plans or p["id"] in a.plans)
            for i, d in enumerate(p["days"]) if (p["id"], tuple(d["v"])) not in done]
    print(f"{len(todo)} teasers to write", file=sys.stderr)
    with open(OUT, "a", encoding="utf-8") as out:
        for n, (p, i, d) in enumerate(todo, 1):
            t = time.time()
            de = "\n\n".join(verses[v]["de"] for v in d["v"] if v in verses)[:5000]
            r = ask(a.model, PROMPT.format(plan=p["tde"], day=i + 1, de=de), a.url)
            out.write(json.dumps({"plan": p["id"], "day": i, "v": d["v"], "de": clean(r["de"]), "en": clean(r["en"]),
                                  "model": a.model}, ensure_ascii=False) + "\n"); out.flush()
            print(f"[{n}/{len(todo)}] {p['id']} d{i+1} · {time.time()-t:.1f}s · {clean(r['de'])}", file=sys.stderr)


if __name__ == "__main__":
    main()

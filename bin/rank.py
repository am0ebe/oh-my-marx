#!/usr/bin/env python3
"""rank — relative 2nd pass over score.py's quotes ➡ data/rank.jsonl (resumable per section)

why: absolute 1-5 ratings came out inflated (73% juice=5, 92% "polemic") ➡ useless 4 picking.
here th LLM sees all quotes o 1 section at once and picks th best ~1/6 (⭐ = Vers-des-Tages pool)
+ th ones that really mock a *named* person/group (= "Marx teilt aus" plan).

usage: rank.py [-m MODEL] [--redo]
"""
import argparse, json, pathlib, sys, time, urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "data" / "rank.jsonl"

SCHEMA = {"type": "object", "required": ["best", "roasts"],
          "properties": {"best": {"type": "array", "items": {"type": "string"}},
                         "roasts": {"type": "array", "items": {"type": "string"}}}}

PROMPT = """Below are candidate quotes from one section of Marx, "Das Kapital" vol. 1 ({where}), each with its id.
They are for a playful daily-quote app ("Bible app for Kapital"), read by modern people with no background.

{items}

Return JSON:
- best: the ids of the {k} BEST quotes — aphoristic, vivid, surprising, funny, angry or poetic, AND understandable on their own. Be strict: dry arithmetic, statistics, report excerpts and abstract value-form logic do not qualify.
- roasts: ids where Marx himself openly mocks or sneers at a NAMED person or group (an economist, a factory owner, a parson, "Herr X"...). Quoting a report or describing misery is NOT a roast. Usually 0-2 per section; may be empty."""

def ask(model, prompt, url):
    body = {"model": model, "prompt": prompt, "format": SCHEMA, "stream": False,
            "options": {"temperature": 0.1, "num_ctx": 16384}}
    req = urllib.request.Request(url, data=json.dumps(body).encode(), headers={"Content-Type": "application/json"})
    return json.loads(json.load(urllib.request.urlopen(req, timeout=900))["response"])

def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("-m", "--model", default="qwen3:30b-a3b-instruct-2507-q4_K_M")
    ap.add_argument("--redo", action="store_true")
    ap.add_argument("--url", default="http://localhost:11434/api/generate")
    a = ap.parse_args()
    verses = [json.loads(l) for l in open(ROOT / "data" / "verses.jsonl", encoding="utf-8")]
    old = {json.loads(l)["id"]: json.loads(l)["de"] for l in open(ROOT / "data" / "verses_scored_v0.jsonl", encoding="utf-8")}
    by_de = {old[s["id"]]: s for s in map(json.loads, open(ROOT / "data" / "scores.jsonl", encoding="utf-8")) if s["id"] in old}
    groups = {}
    for v in verses:
        s = by_de.get(v["de"])
        if v["kind"] == "text" and s and s["quote_de"]:
            groups.setdefault(v["sec"] or v["kap"], []).append((v["id"], s["quote_de"]))
    done = set() if a.redo or not OUT.exists() else {json.loads(l)["group"] for l in open(OUT, encoding="utf-8")}
    todo = [(g, items) for g, items in groups.items() if g not in done]
    print(f"{len(todo)} sections to rank", file=sys.stderr)
    with open(OUT, "w" if a.redo else "a", encoding="utf-8") as out:
        for n, (g, items) in enumerate(todo, 1):
            t = time.time()
            stars, roasts = set(), set()
            for i in range(0, len(items), 40):  # big sections (13.9, 23.5.e) i chunks o 40
                chunk = items[i:i + 40]
                k = max(1, round(len(chunk) / 6))
                r = ask(a.model, PROMPT.format(where=f"section {g}", k=k,
                                               items="\n".join(f"[{vid}] {q}" for vid, q in chunk)), a.url)
                ids = {vid for vid, _ in chunk}
                norm = lambda xs: {str(x).strip("[] ").replace(" ", "") for x in xs}  # M echoes "[8,9]"
                stars |= norm(r["best"][:k + 1]) & ids
                roasts |= norm(r["roasts"]) & ids
            out.write(json.dumps({"group": g, "n": len(items), "star": sorted(stars), "roast": sorted(roasts)}, ensure_ascii=False) + "\n")
            out.flush()
            print(f"[{n}/{len(todo)}] {g} · {len(items)} ➡ ⭐{len(stars)} 🔥{len(roasts)} · {time.time()-t:.1f}s", file=sys.stderr)

if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""retag — local LLM re-tags evy text Vers: only 0-2 topics it is ABOUT, each w a weight ➡ data/tags.jsonl (resumable).

y: score.py let th LLM pad evy quote 2 4 tags ➡ "kapitalist" on 83 % ➡ th "Was nervt?" chip "Chef" = 1233/1511 quotes = random.
   a 2nd, narrow pass gives each chip a real pool. score.py's `t` stays (plans are cut by it, D-042) ➡ build.py adds `tw`.
wh: per Vers 1 ollama call (tags only, short) ➡ {"id", "de_sha", "tw": {tag: 1-3}, "model"}
    weight 3 = th paragraph is mainly about it · 2 = clearly a main topic · 1 = mentioned · {} = none fits
    keyed by a hash o th German text (ids moved before, D-028) ➡ build.py matches by it · --stats = pool size per chip

examples (ollama on tow = GPU · night: echo 'bin/gpu_night.sh bin/retag.py' | at 03:00):
  bin/retag.py -n 20          # try th prompt on 20 Verse 1st
  bin/retag.py                # all not yet re-tagged ➡ data/tags.jsonl (~1-2 h on th GPU)
  bin/retag.py --stats        # quotes per "Was nervt?" chip at weight ≥ 2 (guard: ≥ 30 each)
"""
import argparse, hashlib, json, pathlib, sys, time, urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "data" / "tags.jsonl"
sys.path.insert(0, str(ROOT / "bin"))
from score import TAGS  # noqa: E402  (same 25 topics)

# = logic/Daily.kt NERVT (chip ➡ tags) · test_bin ck they match
NERVT = {"chef": ["kapitalist", "ausbeutung"], "ueberstunden": ["arbeitszeit"], "lohn": ["lohn", "geld"],
         "miete": ["wohnen", "gesundheit"], "konsum": ["ware", "konsum"], "ki": ["maschine", "fabrik"],
         "krise": ["krise", "arbeitslosigkeit"]}
MIN_POOL = 30

SCHEMA = {"type": "object", "required": ["topics"], "properties": {"topics": {"type": "array", "maxItems": 2, "items": {
    "type": "object", "required": ["tag", "weight"],
    "properties": {"tag": {"type": "string", "enum": TAGS}, "weight": {"type": "integer", "minimum": 1, "maximum": 3}}}}}}

PROMPT = """One paragraph of Karl Marx, "Das Kapital" vol. 1 (chapter {kap}: {title}):

{de}

Which topics is this paragraph ABOUT? Pick 0, 1 or 2 from this list: {tags}
- Only a topic the paragraph is really about, not one it merely touches. 0 topics is fine and common.
- "kapitalist" only if it is about the capitalist as a person or class (his motives, behaviour), not just because capital is mentioned.
- weight: 3 = the paragraph is mainly about it · 2 = clearly one of its main topics · 1 = a side topic.
Return JSON: {{"topics": [{{"tag": ..., "weight": ...}}]}}"""


def de_sha(de): return hashlib.sha1(de.encode()).hexdigest()[:12]


def ask(model, prompt, url):
    body = {"model": model, "prompt": prompt, "format": SCHEMA, "stream": False, "options": {"temperature": 0.1, "num_ctx": 4096}}
    req = urllib.request.Request(url, data=json.dumps(body).encode(), headers={"Content-Type": "application/json"})
    return json.loads(json.load(urllib.request.urlopen(req, timeout=600))["response"])


def clean(r):
    """LLM answer ➡ {tag: weight} · unknown tags dropped · max 2 · a tag twice = its higher weight"""
    tw = {}
    for t in (r.get("topics") or [])[:2]:
        tag, w = t.get("tag"), t.get("weight")
        if tag in TAGS and isinstance(w, int) and 1 <= w <= 3:
            tw[tag] = max(w, tw.get(tag, 0))
    return tw


def pools(rows, quoted=None):
    """chip ➡ nr o Verse w weight ≥ 2 on any o its tags (only quoted ones count if `quoted` given = what th app can show)"""
    return {chip: sum(1 for r in rows if (quoted is None or r["de_sha"] in quoted) and any(r["tw"].get(t, 0) >= 2 for t in tags))
            for chip, tags in NERVT.items()}


def load_rows():
    return [json.loads(l) for l in open(OUT, encoding="utf-8")] if OUT.exists() else []


def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("-m", "--model", default="qwen3:30b-a3b-instruct-2507-q4_K_M")
    ap.add_argument("-n", "--limit", type=int, default=0)
    ap.add_argument("--url", default="http://localhost:11434/api/generate")
    ap.add_argument("--stats", action="store_true", help="pool size per chip (weight ≥ 2) · exit 1 if one < 30")
    a = ap.parse_args()
    if a.stats:
        p = pools(load_rows())
        for chip, n in p.items():
            print(f"{'✔️' if n >= MIN_POOL else '❌'} {chip:13} {n}")
        return 0 if p and min(p.values()) >= MIN_POOL else 1
    verses = [json.loads(l) for l in open(ROOT / "data" / "verses.jsonl", encoding="utf-8")]
    done = {r["de_sha"] for r in load_rows()}
    todo = [v for v in verses if v["kind"] == "text" and de_sha(v["de"]) not in done]
    if a.limit: todo = todo[:a.limit]
    print(f"{len(todo)} to re-tag ({len(done)} done)", file=sys.stderr, flush=True)
    with open(OUT, "a", encoding="utf-8") as out:
        for i, v in enumerate(todo, 1):
            t = time.time()
            p = PROMPT.format(kap=v["kap"], title=v["sec_title"] or "-", de=v["de"][:4000], tags=", ".join(TAGS))
            try:
                tw = clean(ask(a.model, p, a.url))
            except Exception as e:
                print(f"{v['id']} ❌ {e}", file=sys.stderr, flush=True); continue
            out.write(json.dumps({"id": v["id"], "de_sha": de_sha(v["de"]), "tw": tw, "model": a.model}, ensure_ascii=False) + "\n"); out.flush()
            print(f"[{i}/{len(todo)}] {v['id']} {tw} · {time.time() - t:.1f}s", file=sys.stderr, flush=True)
    for chip, n in pools(load_rows()).items():
        print(f"{'✔️' if n >= MIN_POOL else '⚠️'} {chip:13} {n} at weight ≥ 2", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main())

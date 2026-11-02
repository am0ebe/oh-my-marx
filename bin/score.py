#!/usr/bin/env python3
"""score — local LLM rates evy text Vers ➡ data/scores.jsonl (resumable: skips ids alr scored).

per Vers: quote_de/quote_en (best short verbatim line = "Vers des Tages" candidate) · juice · difficulty ·
standalone · tags · polemic (Marx roasting sb) · bible (biblical/religious ref)

usage: score.py [-m MODEL] [-n LIMIT] [--ids 8,9 8,10]
"""
import argparse, json, pathlib, sys, time, urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent
OUT = ROOT / "data" / "scores.jsonl"
TAGS = ["arbeitszeit", "lohn", "ausbeutung", "kinderarbeit", "gesundheit", "wohnen", "konsum", "geld",
        "ware", "wert", "maschine", "fabrik", "krise", "arbeitslosigkeit", "kolonien", "gewalt", "natur",
        "staat", "gesetz", "kapitalist", "akkumulation", "geschichte", "religion", "philosophie", "polemik"]

SCHEMA = {"type": "object", "required": ["quote_de", "quote_en", "juice", "difficulty", "standalone", "tags", "polemic", "bible"],
          "properties": {
              "quote_de": {"type": "string"}, "quote_en": {"type": "string"},
              "juice": {"type": "integer", "minimum": 1, "maximum": 5},
              "difficulty": {"type": "integer", "minimum": 1, "maximum": 5},
              "standalone": {"type": "integer", "minimum": 1, "maximum": 5},
              "tags": {"type": "array", "items": {"type": "string", "enum": TAGS}, "maxItems": 4},
              "polemic": {"type": "boolean"}, "bible": {"type": "boolean"}}}

PROMPT = """You rate one paragraph of Karl Marx, "Das Kapital" vol. 1 (chapter {kap}: {title}) for a playful daily-reading app ("Bible app for Kapital").

German original:
{de}

English (Moore/Aveling 1887, may be missing or partial):
{en}

Return JSON:
- quote_de: the single most quotable sentence (or two short ones) COPIED VERBATIM from the German, max ~200 chars, no footnote markers like [52]. Prefer vivid, aphoristic, surprising, funny or angry lines.
- quote_en: the matching sentence COPIED VERBATIM from the English text; "" if the English lacks it.
- juice 1-5: how gripping / quotable / vivid / funny for a modern reader (5 = poster-worthy aphorism, 1 = dry tables or arithmetic).
- difficulty 1-5: how hard to understand without context (5 = abstract value-form dialectics).
- standalone 1-5: does it make sense read alone, without the previous paragraphs?
- tags: up to 4 from the allowed list.
- polemic: true if Marx mocks, roasts or sneers at someone (economists, factory owners, clergy ...).
- bible: true if it quotes or alludes to the Bible, religion, God, saints, Moses etc."""

def ask(model, prompt, url):
    body = {"model": model, "prompt": prompt, "format": SCHEMA, "stream": False,
            "options": {"temperature": 0.2, "num_ctx": 8192}}
    req = urllib.request.Request(url, data=json.dumps(body).encode(), headers={"Content-Type": "application/json"})
    return json.loads(json.load(urllib.request.urlopen(req, timeout=600))["response"])

def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("-m", "--model", default="qwen3:30b-a3b-instruct-2507-q4_K_M")
    ap.add_argument("-n", "--limit", type=int, default=0)
    ap.add_argument("--ids", nargs="*")
    ap.add_argument("--url", default="http://localhost:11434/api/generate")
    a = ap.parse_args()
    verses = [json.loads(l) for l in open(ROOT / "data" / "verses.jsonl", encoding="utf-8")]
    done = {json.loads(l)["id"] for l in open(OUT, encoding="utf-8")} if OUT.exists() else set()
    todo = [v for v in verses if v["kind"] == "text" and v["id"] not in done and (not a.ids or v["id"] in a.ids)]
    if a.limit: todo = todo[:a.limit]
    print(f"{len(todo)} to score ({len(done)} done)", file=sys.stderr)
    with open(OUT, "a", encoding="utf-8") as out:
        for i, v in enumerate(todo, 1):
            t = time.time()
            p = PROMPT.format(kap=v["kap"], title=v["sec_title"] or "-", de=v["de"][:6000], en=(v["en"] or "(missing)")[:6000])
            try:
                r = ask(a.model, p, a.url)
            except Exception as e:
                print(f"{v['id']} ❌ {e}", file=sys.stderr); continue
            r["tags"] = [x for x in r.get("tags", []) if x in TAGS]
            r["quote_ok"] = r["quote_de"].strip(" .\"„“") in v["de"]  # verbatim check
            out.write(json.dumps({"id": v["id"], **r, "model": a.model}, ensure_ascii=False) + "\n"); out.flush()
            print(f"[{i}/{len(todo)}] {v['id']} j{r['juice']} d{r['difficulty']} · {time.time()-t:.1f}s · {r['quote_de'][:70]}", file=sys.stderr)

if __name__ == "__main__":
    main()

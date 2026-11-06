#!/usr/bin/env python3
"""translate — app content + UI strings ➡ it/es/pt/zh/ar (local LLM, resumable) ➡ assets/content/i18n_<lang>.json

what gets translated (fr th built assets, so run AFT build.py):
  q     quotes (Vers des Tages, Marxoskop, Mein Kapital)  · fr DE original, EN as reference
  a     Andachten (plan days)                             · keep th wink
  plan  plan title + description
  chap  chapter + Abschnitt titles
  ui    UI strings: evy T("de", "en") / tr("de", "en") pair i th Kotlin source (key = EN string)
NOT translated: full verse text + footnotes (app falls back 2 EN) — 1.3 M chars/lang, maybe later

cache: data/i18n/<lang>.jsonl · entry reused while its source txt is unchanged

usage: translate.py [--langs it es] [--only q a plan chap ui] [--export-only] [-m MODEL]
"""
import argparse, json, pathlib, re, sys, time, urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "android" / "app" / "src" / "main" / "assets" / "content"
SRC = ROOT / "android" / "app" / "src" / "main" / "java"
CACHE = ROOT / "data" / "i18n"

LANGS = {
    "it": "Italian",
    "es": "Spanish (neutral, understandable in Spain and Latin America)",
    "pt": "Brazilian Portuguese",
    "zh": "Simplified Chinese (Mandarin, mainland usage)",
    "ar": "Modern Standard Arabic",
    # @1106: + the big ones (≈ top 12 by speakers together w EN/ZH/ES/AR/PT/DE)
    "fr": "French",
    "ru": "Russian",
    "hi": "Hindi (Devanagari script)",
    "bn": "Bengali (Bangla script)",
    "ur": "Urdu (Perso-Arabic script)",
    "id": "Indonesian",
}

STYLE = {
    "q": "These are quotations from Karl Marx, 'Das Kapital' vol. 1 (19th-century prose). Translate faithfully from the "
         "German original (English given only as reference). Keep it a dignified, quotable Marx sentence — no modernising, "
         "no explanations, no added quotation marks. Keep footnote markers (a number in square brackets) if present, never add new ones.",
    "a": "These are short tongue-in-cheek 'devotionals' from a playful app that presents Marx' Kapital like a Bible app. "
         "Keep the dry, cheeky wink and the gentle Bible-app parody tone; keep the closing question. Adapt jokes so they "
         "work for native readers; keep the length.",
    "plan": "These are reading-plan titles and descriptions from a playful Marx-as-Bible app. Keep puns and the wink; "
            "titles stay short.",
    "chap": "These are chapter titles of Marx' 'Das Kapital' vol. 1. Use the established title in the target language if one exists.",
    "ui": "These are UI strings of a playful Android app that presents Marx' Kapital like a Bible app (with a wink: Marxist "
          "jokes, Bible-app parody, honest about its own Hooked/habit tricks). Translate from the English, the German is "
          "the original for reference. Keep emojis, keep placeholders like %d %s %02d exactly, keep it short for buttons. "
          "Keep the humour; adapt puns so they work natively.",
}

# ── sources ──────────────────────────────────────────────────────────────────

def src_items(only):
    """yield (kind, key, de, en)"""
    load = lambda n: json.load(open(ASSETS / n, encoding="utf-8"))
    if "q" in only:
        for v in load("verses.json"):
            if v.get("qd"):
                yield "q", v["id"], v["qd"], v.get("qe", "")
    if "a" in only or "plan" in only:
        for p in load("plans.json"):
            if "plan" in only:
                yield "plan", p["id"] + ":t", p["tde"], p["ten"]
                yield "plan", p["id"] + ":d", p["dde"], p["den"]
            if "a" in only:
                for i, d in enumerate(p["days"]):
                    if d.get("ad"):
                        yield "a", f"{p['id']}:{i}", d["ad"], d.get("ae", "")
    if "chap" in only:
        for c in load("chapters.json"):
            yield "chap", c["kap"], c["de"], c["en"]
    if "ui" in only:
        yield from ui_strings()

PAIR = re.compile(r'\b(?:T|tr)\(\s*"((?:[^"\\]|\\.)*)"\s*,\s*"((?:[^"\\]|\\.)*)"\s*,?\s*\)', re.S)
COMMENT = re.compile(r'^\s*(//|\*|/\*\*).*$', re.M)

def ui_strings():
    seen = set()
    for f in sorted(SRC.rglob("*.kt")):
        for m in PAIR.finditer(COMMENT.sub("", f.read_text(encoding="utf-8"))):
            de, en = (bytes(s, "utf-8").decode("unicode_escape").encode("latin-1").decode("utf-8") for s in m.groups())
            if "$" in en:
                print(f"⚠️  {f.name}: string template i T()/tr() can't be looked up ➡ use %d: {en[:60]}", file=sys.stderr)
                continue
            if en not in seen:
                seen.add(en)
                yield "ui", en, de, en

# ── LLM ──────────────────────────────────────────────────────────────────────

def ask(model, url, kind, lang, batch):
    """batch: {key: (de, en)} ➡ {key: translation}"""
    keys = list(batch)
    ids = {f"k{i}": k for i, k in enumerate(keys)}  # short ids: keys can be long UI strings
    items = {f"k{i}": {"de": batch[k][0], "en": batch[k][1]} for i, k in enumerate(keys)}
    schema = {"type": "object", "required": list(ids), "properties": {i: {"type": "string"} for i in ids}}
    prompt = (f"{STYLE[kind]}\n\nTranslate each item into {LANGS[lang]}. Return a JSON object mapping each id to its "
              f"translation only.\n\n{json.dumps(items, ensure_ascii=False, indent=1)}")
    body = {"model": model, "prompt": prompt, "format": schema, "stream": False,
            "options": {"temperature": 0.3, "num_ctx": 16384}}
    req = urllib.request.Request(url, data=json.dumps(body).encode(), headers={"Content-Type": "application/json"})
    out = json.loads(json.load(urllib.request.urlopen(req, timeout=900))["response"])
    return {ids[i]: out[i].strip() for i in ids if out.get(i, "").strip()}

def batches(items, max_chars=3500, max_n=20):
    b, n = {}, 0
    for k, de, en in items:
        if b and (n + len(de) + len(en) > max_chars or len(b) >= max_n):
            yield b; b, n = {}, 0
        b[k] = (de, en); n += len(de) + len(en)
    if b:
        yield b

# ── main ─────────────────────────────────────────────────────────────────────

def load_cache(lang):
    f = CACHE / f"{lang}.jsonl"
    c = {}
    if f.exists():
        for l in open(f, encoding="utf-8"):
            x = json.loads(l); c[(x["kind"], x["k"])] = x
    return c

def export(lang, cache):
    out = {"q": {}, "a": {}, "plan": {}, "chap": {}, "ui": {}}
    for (kind, k), x in cache.items():
        out[kind][k] = x["t"]
    plans = {}
    for k, t in out["plan"].items():
        pid, part = k.rsplit(":", 1)
        plans.setdefault(pid, ["", ""])[0 if part == "t" else 1] = t
    out["plan"] = plans
    f = ASSETS / f"i18n_{lang}.json"
    f.write_text(json.dumps(out, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    print(f"✔️ {f.name} {f.stat().st_size // 1024}k · " + " · ".join(f"{k} {len(v)}" for k, v in out.items()), file=sys.stderr)

def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--langs", nargs="*", default=list(LANGS))
    ap.add_argument("--only", nargs="*", default=["ui", "chap", "plan", "a", "q"])
    ap.add_argument("--export-only", action="store_true")
    ap.add_argument("-m", "--model", default="qwen3:30b-a3b-instruct-2507-q4_K_M")
    ap.add_argument("--url", default="http://localhost:11434/api/generate")
    a = ap.parse_args()
    CACHE.mkdir(parents=True, exist_ok=True)
    items = list(src_items(set(a.only)))
    for lang in a.langs:
        cache = load_cache(lang)
        if not a.export_only:
            for kind in a.only:  # cheap kinds 1st ➡ a half-done run is already useful
                todo = [(k, de, en) for kd, k, de, en in items if kd == kind
                        and cache.get((kind, k), {}).get("src") != de + "\x00" + en]
                if not todo:
                    continue
                print(f"── {lang} {kind}: {len(todo)} to translate", file=sys.stderr)
                with open(CACHE / f"{lang}.jsonl", "a", encoding="utf-8") as fo:
                    for n, b in enumerate(batches(todo), 1):
                        t = time.time()
                        try:
                            r = ask(a.model, a.url, kind, lang, b)
                        except Exception as e:  # 1 bad batch shouldn't kill an hour-long run ➡ retry 1 by 1
                            print(f"  ⚠️ batch {n}: {e} ➡ 1 by 1", file=sys.stderr)
                            r = {}
                            for k in b:
                                try:
                                    r |= ask(a.model, a.url, kind, lang, {k: b[k]})
                                except Exception as e1:
                                    print(f"  ❌ {k}: {e1}", file=sys.stderr)
                        for k, tr in r.items():
                            x = {"kind": kind, "k": k, "src": b[k][0] + "\x00" + b[k][1], "t": tr}
                            cache[(kind, k)] = x
                            fo.write(json.dumps(x, ensure_ascii=False) + "\n")
                        fo.flush()
                        print(f"  [{lang} {kind} {n}] {len(r)}/{len(b)} · {time.time() - t:.1f}s · "
                              f"{next(iter(r.values()), '')[:60]}", file=sys.stderr)
        export(lang, cache)

if __name__ == "__main__":
    main()

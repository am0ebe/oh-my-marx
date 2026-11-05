#!/usr/bin/env python3
"""translate_full — the FULL Kapital text (paragraphs + Marx's footnotes) ➡ it/es/pt/zh/ar, own LLM translation.

Experiment (@1104): can a local model give a readable full text, so IT/ES/PT/ZH/AR readers stop falling back to EN?
Source = the German original (data/verses.jsonl + data/notes.jsonl), Moore/Aveling EN given as a reference only.
A per-language glossary pins the standard Marxist terms (Mehrwert = plusvalore / plusvalía / mais-valor / 剩余价值 …).

Output (not wired into the app yet): data/i18n_full/<lang>.jsonl, one line per item
  {"kind": "v"|"fn", "k": id, "src": sha1(de)[:10], "t": translation, "warn": "..."?}
Resumable: rerun = skips items whose German source is unchanged. Ctrl-C anytime.

  bin/translate_full.py                               # all 5 langs, book then footnotes per lang (~6-9 h total)
  bin/translate_full.py --langs it es --only v        # just the paragraphs for it + es
  bin/translate_full.py --limit 20 --langs it         # quick test run
  bin/translate_full.py --show 5 --langs it           # 🌅 morning check: 5 random DE | EN | IT side by side
  bin/translate_full.py --stats                       # coverage + warnings per lang
  bin/translate_full.py --bad --langs zh              # list items w a length warning (truncated / bloated)
  bin/translate_full.py --redo-bad                    # retranslate those 1 by 1 (fixes below), keep whichever is better

Length-warning causes (@1106): ⇧ bloated = DE⬄EN misaligned (EN para covers several DE paras) ➡ redo w/o EN ·
⇩ truncated = model ended its JSON string at an ASCII quote i th source ➡ redo w „typographic“ quotes.

Overnight:  nohup bin/translate_full.py > data/i18n_full/run.log 2>&1 &     then  tail -f data/i18n_full/run.log
"""
import argparse, hashlib, json, pathlib, random, re, sys, time, urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent
DATA = ROOT / "data"
OUT = DATA / "i18n_full"

LANGS = {
    "it": "Italian",
    "es": "Spanish (neutral, understandable in Spain and Latin America)",
    "pt": "Brazilian Portuguese",
    "zh": "Simplified Chinese (mainland usage)",
    "ar": "Modern Standard Arabic",
}

# standard terms of the established Kapital translations · German ➡ target (keep consistent across 2 M chars)
TERMS_DE = ["Ware", "Gebrauchswert", "Tauschwert", "Wertform", "Äquivalentform", "Geld", "Kapital", "Mehrwert",
            "Mehrarbeit", "notwendige Arbeit", "Arbeitskraft", "Arbeitstag", "konstantes / variables Kapital",
            "Rate des Mehrwerts", "Akkumulation", "ursprüngliche Akkumulation", "Produktionsmittel",
            "Fetischcharakter der Ware", "industrielle Reservearmee", "Lohnarbeit", "Zirkulation", "Kooperation",
            "Manufaktur", "große Industrie", "abstrakte Arbeit", "gesellschaftlich notwendige Arbeitszeit",
            "absoluter / relativer Mehrwert", "organische Zusammensetzung"]
TERMS = {
    "it": ["merce", "valore d'uso", "valore di scambio", "forma di valore", "forma di equivalente", "denaro", "capitale",
           "plusvalore", "pluslavoro", "lavoro necessario", "forza-lavoro", "giornata lavorativa",
           "capitale costante / variabile", "saggio del plusvalore", "accumulazione", "accumulazione originaria",
           "mezzi di produzione", "carattere di feticcio della merce", "esercito industriale di riserva",
           "lavoro salariato", "circolazione", "cooperazione", "manifattura", "grande industria", "lavoro astratto",
           "tempo di lavoro socialmente necessario", "plusvalore assoluto / relativo", "composizione organica"],
    "es": ["mercancía", "valor de uso", "valor de cambio", "forma de valor", "forma de equivalente", "dinero", "capital",
           "plusvalor", "plustrabajo", "trabajo necesario", "fuerza de trabajo", "jornada laboral",
           "capital constante / variable", "tasa de plusvalor", "acumulación", "acumulación originaria",
           "medios de producción", "carácter fetichista de la mercancía", "ejército industrial de reserva",
           "trabajo asalariado", "circulación", "cooperación", "manufactura", "gran industria", "trabajo abstracto",
           "tiempo de trabajo socialmente necesario", "plusvalor absoluto / relativo", "composición orgánica"],
    "pt": ["mercadoria", "valor de uso", "valor de troca", "forma de valor", "forma de equivalente", "dinheiro", "capital",
           "mais-valor", "mais-trabalho", "trabalho necessário", "força de trabalho", "jornada de trabalho",
           "capital constante / variável", "taxa de mais-valor", "acumulação", "acumulação primitiva",
           "meios de produção", "caráter fetichista da mercadoria", "exército industrial de reserva",
           "trabalho assalariado", "circulação", "cooperação", "manufatura", "grande indústria", "trabalho abstrato",
           "tempo de trabalho socialmente necessário", "mais-valor absoluto / relativo", "composição orgânica"],
    "zh": ["商品", "使用价值", "交换价值", "价值形式", "等价形式", "货币", "资本", "剩余价值", "剩余劳动", "必要劳动",
           "劳动力", "工作日", "不变资本 / 可变资本", "剩余价值率", "积累", "原始积累", "生产资料", "商品的拜物教性质",
           "产业后备军", "雇佣劳动", "流通", "协作", "工场手工业", "大工业", "抽象劳动", "社会必要劳动时间",
           "绝对 / 相对剩余价值", "有机构成"],
    "ar": ["السلعة", "القيمة الاستعمالية", "القيمة التبادلية", "شكل القيمة", "الشكل المعادل", "النقد", "رأس المال",
           "فائض القيمة", "العمل الفائض", "العمل الضروري", "قوة العمل", "يوم العمل", "رأس المال الثابت / المتغير",
           "معدل فائض القيمة", "التراكم", "التراكم البدائي", "وسائل الإنتاج", "الطابع الصنمي للسلعة",
           "جيش العمل الصناعي الاحتياطي", "العمل المأجور", "التداول", "التعاون", "المانيفاكتورة", "الصناعة الكبرى",
           "العمل المجرد", "وقت العمل الضروري اجتماعيًا", "فائض القيمة المطلق / النسبي", "التركيب العضوي"],
}
# plausible length of a translation relative to the German (chars) ➡ outside = "warn" (truncated / runaway)
RATIO = {"it": (0.6, 1.8), "es": (0.6, 1.8), "pt": (0.6, 1.8), "zh": (0.15, 0.8), "ar": (0.45, 1.6)}

STYLE = ("You are translating Karl Marx, 'Das Kapital' vol. 1 (4th German edition, 1890) into {lang}, paragraph by "
         "paragraph, for a reading app. Translate faithfully from the GERMAN original; the English (Moore/Aveling 1887) "
         "is only a reference and may be misaligned or missing. Keep Marx's register: precise, ironic, 19th-century but "
         "readable; no modernising, no summarising, no explanations, nothing left out. Rules:\n"
         "- keep footnote markers like [52] exactly where they are\n"
         "- keep Latin/French/English quotations in the original language; text in ⟨…⟩ is an editor's German "
         "translation of such a quote ➡ translate what is inside the ⟨…⟩ into {lang}, keep the brackets\n"
         "- keep numbers, formulas (W - G - W, c + v + m), currency (£, sh., d.) and names of persons as they are\n"
         "- use this glossary of established terms consistently:\n{glossary}")

def sha(s): return hashlib.sha1(s.encode()).hexdigest()[:10]

def items(only):
    """yield (kind, key, de, en) · v = book paragraph · fn = Marx's footnote"""
    if "v" in only:
        for l in open(DATA / "verses.jsonl", encoding="utf-8"):
            v = json.loads(l)
            if v.get("de", "").strip():
                yield "v", v["id"], v["de"], v.get("en", "")
    if "fn" in only:
        for l in open(DATA / "notes.jsonl", encoding="utf-8"):
            n = json.loads(l)
            if n.get("de", "").strip():
                yield "fn", f"{n['kap']}:{n['n']}", n["de"], n.get("en", "")

def ask(model, url, lang, batch):
    """batch: {key: (de, en)} ➡ {key: translation}"""
    ids = {f"k{i}": k for i, k in enumerate(batch)}
    payload = {i: {"de": batch[k][0], "en": batch[k][1]} for i, k in ids.items()}
    glossary = "\n".join(f"  {d} = {t}" for d, t in zip(TERMS_DE, TERMS[lang]))
    schema = {"type": "object", "required": list(ids), "properties": {i: {"type": "string"} for i in ids}}
    prompt = (STYLE.format(lang=LANGS[lang], glossary=glossary) +
              f"\n\nReturn a JSON object mapping each id to its {LANGS[lang]} translation only.\n\n" +
              json.dumps(payload, ensure_ascii=False, indent=1))
    body = {"model": model, "prompt": prompt, "format": schema, "stream": False, "keep_alive": "30m",
            "options": {"temperature": 0.2, "num_ctx": 16384}}
    req = urllib.request.Request(url, data=json.dumps(body).encode(), headers={"Content-Type": "application/json"})
    r = json.load(urllib.request.urlopen(req, timeout=1800))
    out = json.loads(r["response"])
    return {ids[i]: out[i].strip() for i in ids if isinstance(out.get(i), str) and out[i].strip()}, r.get("eval_count", 0)

def check(lang, de, t):
    """➡ warning string or None"""
    lo, hi = RATIO[lang]
    r = len(t) / max(len(de), 1)
    if len(de) > 80 and not lo <= r <= hi:
        return f"length ratio {r:.2f}"
    if sorted(re.findall(r"\[\d+\]", de)) != sorted(re.findall(r"\[\d+\]", t)):
        return "footnote markers differ"
    return None

def batches(todo, max_chars=2500, max_n=8):
    """long paragraphs go alone, short ones share a call (less overhead, same quality)"""
    b, n = {}, 0
    for k, de, en in todo:
        if b and (n + len(de) > max_chars or len(b) >= max_n):
            yield b; b, n = {}, 0
        b[k] = (de, en); n += len(de)
    if b:
        yield b

def load(lang):
    f = OUT / f"{lang}.jsonl"
    c = {}
    if f.exists():
        for l in open(f, encoding="utf-8"):
            x = json.loads(l); c[(x["kind"], x["k"])] = x
    return c

def run(lang, only, model, url, limit):
    cache = load(lang)
    todo = [(kind, k, de, en) for kind, k, de, en in items(only)
            if cache.get((kind, k), {}).get("src") != sha(de)]
    if limit:
        todo = todo[:limit]
    total = sum(len(de) for _, _, de, _ in todo)
    print(f"── {lang}: {len(todo)} items · {total // 1000}k chars to do", flush=True)
    done_chars, t0, toks = 0, time.time(), 0
    f = open(OUT / f"{lang}.jsonl", "a", encoding="utf-8")
    for kind in ("v", "fn"):
        part = [(k, de, en) for kd, k, de, en in todo if kd == kind]
        for b in batches(part):
            for attempt in (1, 2):
                try:
                    res, n = ask(model, url, lang, b); toks += n
                    break
                except Exception as e:  # timeout / bad JSON ➡ retry once, then split into singles
                    print(f"  ⚠️ {lang} {kind} {next(iter(b))}: {e} (try {attempt})", flush=True)
                    res = {}
            if len(res) < len(b) and len(b) > 1:  # missing ids ➡ redo those one by one
                for k in [k for k in b if k not in res]:
                    try:
                        r1, n = ask(model, url, lang, {k: b[k]}); res.update(r1); toks += n
                    except Exception as e:
                        print(f"  ⚠️ {lang} {kind} {k}: {e}", flush=True)
            for k, (de, en) in b.items():
                if k not in res:
                    continue
                x = {"kind": kind, "k": k, "src": sha(de), "t": res[k]}
                if w := check(lang, de, res[k]):
                    x["warn"] = w
                f.write(json.dumps(x, ensure_ascii=False) + "\n"); f.flush()
            done_chars += sum(len(de) for de, _ in b.values())
            el = time.time() - t0
            eta = el / max(done_chars, 1) * (total - done_chars)
            print(f"  [{lang} {kind}] {done_chars * 100 // max(total, 1)}% · {done_chars // 1000}k/{total // 1000}k chars"
                  f" · {toks / max(el, 1):.0f} tok/s · eta {eta / 60:.0f} min", flush=True)
    f.close()
    print(f"✔️ {lang} done in {(time.time() - t0) / 60:.0f} min", flush=True)

def ratio_bad(x):
    return (x.get("warn") or "").startswith("length")

def bad(lang):
    src, c = source(), load(lang)
    xs = [x for x in c.values() if ratio_bad(x)]
    print(f"── {lang}: {len(xs)} length warnings")
    for x in xs:
        de, en = src.get((x["kind"], x["k"]), ("", ""))
        print(f"  {x['kind']} {x['k']:>8} · {x['warn']} · DE {len(de)} / EN {len(en)} · {x['t'][:70]!r}")

def requote(s):
    """ASCII "…" ➡ „…“ (German typographic) so th model doesn't end its JSON string at th 1st quote"""
    out, open_ = [], True
    for ch in s:
        if ch == '"':
            out.append("„" if open_ else "“"); open_ = not open_
        else:
            out.append(ch)
    return "".join(out)

def redo_bad(lang, model, url):
    src, c = source(), load(lang)
    xs = [x for x in c.values() if ratio_bad(x)]
    print(f"── {lang}: redo {len(xs)} items singly", flush=True)
    fixed = 0
    for x in xs:
        key = (x["kind"], x["k"])
        de, en = src[key]
        if len(en) > 1.6 * len(de):        # EN para spans more than this DE one ➡ it misleads, drop it
            en = ""
        try:
            res, _ = ask(model, url, lang, {x["k"]: (requote(de), en)})
        except Exception as e:
            print(f"  ⚠️ {x['k']}: {e}", flush=True); continue
        t = res.get(x["k"])
        if not t:
            continue
        w = check(lang, de, t)
        old_r = float(x["warn"].split()[-1]); lo, hi = RATIO[lang]
        new_r = len(t) / max(len(de), 1)
        # keep th new one if it passes, o at least sits closer 2 th allowed window than th old one
        dist = lambda r: max(lo - r, r - hi, 0)
        if w is None or dist(new_r) < dist(old_r):
            c[key] = {"kind": x["kind"], "k": x["k"], "src": x["src"], "t": t, **({"warn": w} if w else {})}
            fixed += w is None
        print(f"  {x['k']:>8} {old_r:.2f} ➡ {new_r:.2f} {'✔️' if w is None else '⚠️'}", flush=True)
    # rewrite th file compact (1 line per item, no superseded duplicates)
    tmp = OUT / f"{lang}.jsonl.tmp"        # tmp + rename ➡ a crash nvr leaves a half file (= hours o GPU)
    with open(tmp, "w", encoding="utf-8") as f:
        for v in c.values():
            f.write(json.dumps(v, ensure_ascii=False) + "\n")
    tmp.replace(OUT / f"{lang}.jsonl")
    print(f"✔️ {lang}: {fixed}/{len(xs)} now pass", flush=True)

def source():
    s = {}
    for kind, k, de, en in items(["v", "fn"]):
        s[(kind, k)] = (de, en)
    return s

def show(lang, n):
    src, c = source(), load(lang)
    for key in random.sample(sorted(c), min(n, len(c))):
        de, en = src.get(key, ("?", "?"))
        x = c[key]
        print(f"\n━━ {key[0]} {key[1]}" + (f"  ⚠️ {x['warn']}" if x.get("warn") else ""))
        print(f"🇩🇪 {de[:600]}\n🇬🇧 {en[:600]}\n➡ {lang}: {x['t'][:600]}")

def stats(langs):
    src = source()
    nv = sum(1 for k in src if k[0] == "v"); nf = len(src) - nv
    for lang in langs:
        c = load(lang)
        v = sum(1 for k in c if k[0] == "v"); fn = len(c) - v
        w = [x for x in c.values() if x.get("warn")]
        print(f"{lang}: book {v}/{nv} · footnotes {fn}/{nf} · ⚠️ {len(w)}"
              + ("" if not w else " (e.g. " + ", ".join(f"{x['k']}: {x['warn']}" for x in w[:3]) + ")"))

def main():
    ap = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("--langs", nargs="*", default=list(LANGS))
    ap.add_argument("--only", nargs="*", default=["v", "fn"], help="v = book paragraphs · fn = footnotes")
    ap.add_argument("--limit", type=int, default=0, help="max items per lang (test runs)")
    ap.add_argument("--show", type=int, default=0, help="print N random samples side by side, translate nothing")
    ap.add_argument("--stats", action="store_true")
    ap.add_argument("--bad", action="store_true", help="list length-warned items")
    ap.add_argument("--redo-bad", action="store_true", help="retranslate length-warned items 1 by 1")
    ap.add_argument("-m", "--model", default="qwen3:30b-a3b-instruct-2507-q4_K_M")
    ap.add_argument("--url", default="http://localhost:11434/api/generate")
    a = ap.parse_args()
    OUT.mkdir(parents=True, exist_ok=True)
    if a.stats:
        return stats(a.langs)
    if a.bad:
        for lang in a.langs:
            bad(lang)
        return
    if a.redo_bad:
        for lang in a.langs:
            redo_bad(lang, a.model, a.url)
        return
    if a.show:
        for lang in a.langs:
            show(lang, a.show)
        return
    for lang in a.langs:
        run(lang, a.only, a.model, a.url, a.limit)

if __name__ == "__main__":
    main()

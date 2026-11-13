#!/usr/bin/env python3
"""build — data/{verses,notes,scores,andachten}.jsonl ➡ android/app/src/main/assets/content/*.json (P1 step 7)

also generates th reading plans (deterministic) ➡ data/plans.json (input 4 andacht.py)
scores r joined by DE text (ids shifted once aft a split.py fix ➡ data/verses_scored_v0.jsonl maps old ids)

usage: build.py
"""
import difflib, json, pathlib, re

ROOT = pathlib.Path(__file__).resolve().parent.parent
DATA = ROOT / "data"
OUT = ROOT / "android" / "app" / "src" / "main" / "assets" / "content"

# kap ➡ (DE title, EN title, Abschnitt) · Abschnitt 0 = Vorworte
CHAPTERS = {
    "V1": ("Vorwort zur ersten Auflage", "Preface to the First German Edition", 0),
    "V2": ("Nachwort zur zweiten Auflage", "Afterword to the Second German Edition", 0),
    "V3": ("Vorwort und Nachwort zur französischen Ausgabe", "Preface and Afterword to the French Edition", 0),
    "V4": ("Zur dritten Auflage (Engels)", "Preface to the Third German Edition (Engels)", 0),
    "V5": ("Vorwort zur englischen Ausgabe (Engels)", "Preface to the English Edition (Engels)", 0),
    "V6": ("Zur vierten Auflage (Engels)", "Preface to the Fourth German Edition (Engels)", 0),
    "1": ("1. Die Ware", "1. Commodities", 1),
    "2": ("2. Der Austauschprozeß", "2. Exchange", 1),
    "3": ("3. Das Geld oder die Warenzirkulation", "3. Money, or the Circulation of Commodities", 1),
    "4": ("4. Verwandlung von Geld in Kapital", "4. The Transformation of Money into Capital", 2),
    "5": ("5. Arbeitsprozeß und Verwertungsprozeß", "5. The Labour-Process and the Process of Producing Surplus-Value", 3),
    "6": ("6. Konstantes Kapital und variables Kapital", "6. Constant Capital and Variable Capital", 3),
    "7": ("7. Die Rate des Mehrwerts", "7. The Rate of Surplus-Value", 3),
    "8": ("8. Der Arbeitstag", "8. The Working-Day", 3),
    "9": ("9. Rate und Masse des Mehrwerts", "9. Rate and Mass of Surplus-Value", 3),
    "10": ("10. Begriff des relativen Mehrwerts", "10. The Concept of Relative Surplus-Value", 4),
    "11": ("11. Kooperation", "11. Co-operation", 4),
    "12": ("12. Teilung der Arbeit und Manufaktur", "12. Division of Labour and Manufacture", 4),
    "13": ("13. Maschinerie und große Industrie", "13. Machinery and Modern Industry", 4),
    "14": ("14. Absoluter und relativer Mehrwert", "14. Absolute and Relative Surplus-Value", 5),
    "15": ("15. Größenwechsel von Preis der Arbeitskraft und Mehrwert", "15. Changes of Magnitude in the Price of Labour-Power and in Surplus-Value", 5),
    "16": ("16. Verschiedene Formeln für die Rate des Mehrwerts", "16. Various Formulae for the Rate of Surplus-Value", 5),
    "17": ("17. Verwandlung von Wert resp. Preis der Arbeitskraft in Arbeitslohn", "17. The Transformation of the Value of Labour-Power into Wages", 6),
    "18": ("18. Der Zeitlohn", "18. Time-Wages", 6),
    "19": ("19. Der Stücklohn", "19. Piece-Wages", 6),
    "20": ("20. Nationale Verschiedenheit der Arbeitslöhne", "20. National Differences of Wages", 6),
    "21": ("21. Einfache Reproduktion", "21. Simple Reproduction", 7),
    "22": ("22. Verwandlung von Mehrwert in Kapital", "22. Conversion of Surplus-Value into Capital", 7),
    "23": ("23. Das allgemeine Gesetz der kapitalistischen Akkumulation", "23. The General Law of Capitalist Accumulation", 7),
    "24": ("24. Die sogenannte ursprüngliche Akkumulation", "24. The So-Called Primitive Accumulation", 7),
    "25": ("25. Die moderne Kolonisationstheorie", "25. The Modern Theory of Colonisation", 7),
}

LABEL = re.compile(r"^([IVX]+|\d+|[A-Za-z])[.)]\s+")
ROMAN = {"I": 1, "II": 2, "III": 3, "IV": 4, "V": 5, "VI": 6, "VII": 7, "VIII": 8, "IX": 9, "X": 10}

def label(t):
    """'a) Gehalt' ➡ ('abc', 1) · 'IV. X' / '4. X' ➡ ('num', 4) · no label ➡ None"""
    m = LABEL.match(t)
    if not m: return None
    x = m.group(1)
    if x in ROMAN: return ("num", ROMAN[x])   # Kapital's letter sections stop at F ➡ I/V/X = roman
    return ("num", int(x)) if x.isdigit() else ("abc", ord(x.lower()) - 96)

def en_sec_titles(V):
    """DE sec ➡ EN title fr th marxists.org headings align.py hung on each verse (en_head = heading stack, outer ➡ inner)
    1st: a stack entry whose label matches th DE one ('a)' ⬄ 'a.' · 'IV.' ⬄ '4.') · else th most common innermost
    (DE Kap 4 + 24 = several EN chapters ➡ EN chapter title) · label written th DE way, EN words · none ➡ "" (app shows DE)"""
    from collections import Counter
    secs = {}
    for v in V:
        if v["sec_title"]:
            secs.setdefault((v["kap"], v["sec"]), (v["sec_title"], []))[1].append(v.get("en_head") or [])
    out = {}
    for key, (de_t, stacks) in secs.items():
        want = label(de_t)
        # innermost match per verse ('3. The Equivalent form' beats th outer '3. The Form of Value')
        hits = Counter(next((h for h in reversed(s) if label(h) == want), None) for s in stacks) if want else Counter()
        hits.pop(None, None)
        inner = Counter(s[-1] for s in stacks if s)
        en = (hits or inner).most_common(1)[0][0] if (hits or inner) else ""
        if en:
            words = LABEL.sub("", en) if label(en) else en
            out[key] = (LABEL.match(de_t).group(1) + ". " if want else "") + words[:1].upper() + words[1:]
    return out

def jl(name):
    p = DATA / name
    return [json.loads(l) for l in open(p, encoding="utf-8")] if p.exists() else []

def norm(t):
    t = re.sub(r"\[\d+[a-z]?\]", "", t)
    t = re.sub(r"[„“”\"'‚‘’«»⟨⟩]", "", t)
    return re.sub(r"\s+", " ", t).strip(" .")

def sentences(t):
    t = re.sub(r"\s*\[\d+[a-z]?\]", "", t)
    return [s.strip() for s in re.split(r"(?<=[.!?])\s+(?=[A-ZÄÖÜ„\"“(])", t) if s.strip()]

def verbatim(quote, text, min_ratio=0.6):
    """LLM quotes r often slightly off (elided '…', paraphrased) ➡ return th best-matching 1-2 *source* sentences"""
    if not quote or not text:
        return ""
    sents = sentences(text)
    cands = sents + [a + " " + b for a, b in zip(sents, sents[1:])]
    nq = norm(quote)
    best = max(cands, key=lambda c: difflib.SequenceMatcher(None, nq, norm(c)).ratio(), default="")
    ok = difflib.SequenceMatcher(None, nq, norm(best)).ratio() >= min_ratio and len(best) <= 400
    return best if ok else ""

def score(v):
    """how good as a plan reading: ⭐ (rank.py) + juicy + standalone, not 2 hard, not a wall o txt"""
    return v.get("star", 0) * 6 + v.get("j", 0) * 2 + v.get("s", 0) - v.get("d", 3) * 0.5 - (len(v["de"]) > 1800) * 2

def in_secs(v, kap, secs):
    return v["kap"] == kap and (not secs or any(v["sec"] == s or v["sec"].startswith(s + ".") for s in secs))

def best(vs, n, used, maxlen=2500):
    pick = sorted((v for v in vs if v["id"] not in used and len(v["de"]) <= maxlen), key=score, reverse=True)[:n]
    used.update(v["id"] for v in pick)
    return sorted(pick, key=lambda v: v["_i"])  # book order within a day

def plans(V):
    T = [v for v in V if v["kind"] == "text"]
    P = []

    # 1 ── Kapital i 30 Tagen: front-loaded (Arbeitstag ➡ Akkumulation ➡ Fetisch …), Wertform last
    spec = [("8", ["8.1"]), ("8", ["8.2"]), ("8", ["8.3", "8.4"]), ("8", ["8.5", "8.6", "8.7"]),
            ("24", ["24.1", "24.2"]), ("24", ["24.3", "24.4", "24.5"]), ("24", ["24.6", "24.7"]), ("1", ["1.4"]),
            ("13", ["13.1", "13.2", "13.3"]), ("13", ["13.4", "13.5"]), ("13", ["13.6", "13.7", "13.8", "13.9", "13.10"]),
            ("10", []), ("11", []), ("12", []), ("23", ["23.1", "23.2", "23.3"]), ("23", ["23.4", "23.5"]), ("22", []), ("25", []),
            ("4", ["4.1", "4.2"]), ("4", ["4.3"]), ("5", []), ("6", []), ("7", []), ("9", []), ("14", []),
            ("17", []), ("18", []), ("19", []), ("21", []), ("1", ["1.1", "1.2", "1.3"])]
    used = set()
    P.append(dict(id="30tage", emoji="🚩", tde="Kapital in 30 Tagen", ten="Capital in 30 Days",
                  dde="Das Beste aus allen Kapiteln, das Spannende zuerst: Arbeitstag, Blutgesetze, Fetisch, Maschinen. Die Wertform kommt am Schluss, wenn du schon süchtig bist.",
                  den="The best of every chapter, exciting bits first: the working day, bloody legislation, fetishism, machines. The value-form comes last, once you're hooked.",
                  days=[[v["id"] for v in best([v for v in T if in_secs(v, k, s)], 2, used)] for k, s in spec]))

    # 2 ── Marx für Erschöpfte: 1 short, easy, juicy verse/day
    easy = sorted((v for v in T if v.get("j", 0) >= 5 and v.get("d", 5) <= 2 and len(v["de"]) < 700 and v.get("s", 0) >= 4),
                  key=lambda v: (-score(v), v["_i"]))
    pick, kaps = [], set()
    for v in easy:  # spread over chapters
        if v["kap"] not in kaps: pick.append(v); kaps.add(v["kap"])
        if len(pick) == 7: break
    P.append(dict(id="erschoepft", emoji="😴", tde="Marx für Erschöpfte", ten="Marx for the Exhausted",
                  dde="7 Tage, 1 kurzer Vers. Für alle, deren Arbeitskraft heute schon vollständig konsumiert wurde.",
                  den="7 days, 1 short verse. For everyone whose labour-power has already been fully consumed today.",
                  days=[[v["id"]] for v in pick]))

    def themed(pid, emoji, tde, ten, dde, den, pred, ndays, per_day=1, boost=lambda v: 0):
        cand = sorted((v for v in T if pred(v) and len(v["de"]) < 2000), key=lambda v: -(score(v) + boost(v)))[:ndays * per_day]
        cand.sort(key=lambda v: v["_i"])
        days = [[v["id"] for v in cand[i:i + per_day]] for i in range(0, len(cand), per_day)]
        P.append(dict(id=pid, emoji=emoji, tde=tde, ten=ten, dde=dde, den=den, days=days))

    themed("montag", "☕", "Montagmorgen-Marx", "Monday-Morning Marx",
           "14 Tage über Arbeitszeit, Lohn und wer eigentlich wem wie viele Minuten klaut. Ideal um 8:55.",
           "14 days on working hours, wages and who steals how many minutes from whom. Best at 8:55 am.",
           lambda v: any(t in v.get("t", []) for t in ("arbeitszeit", "lohn", "ausbeutung")), 14)
    themed("vampir", "🧛", "Vampir-Plan", "The Vampire Plan",
           "7 Tage Gothic-Ökonomie: Werwolfsheißhunger, Blutgesetze und Kapital, das lebendige Arbeit einsaugt. Für Halloween.",
           "7 days of gothic economics: werewolf hunger, bloody laws and capital sucking living labour. For Halloween.",
           lambda v: re.search(r"[Vv]amp[yi]r|Werwolf|[Bb]lut|saug|Menschenfleisch|Leiche|Gespenst|Teufel", v["de"]), 7,
           boost=lambda v: 20 * bool(re.search(r"[Vv]amp[yi]r|Werwolf|Menschenfleisch", v["de"]))
           + 8 * bool(re.search(r"saug|[Bb]lut", v["de"])))
    themed("bibel", "✝️", "Bibelstellen im Kapital", "Bible Verses in Capital",
           "Marx zitiert Moses, die Offenbarung und den heiligen Geist. 10 Tage Theologie mit Mehrwert.",
           "Marx quotes Moses, Revelation and the Holy Ghost. 10 days of theology with surplus value.",
           lambda v: v.get("bib") or re.search(r"Moses|Propheten|Bibel|Gott|heilig|Sünde|Offenbarung|Evangel|Christ|Apostel|Mammon|Kirche|Pfaff", v["de"]), 10)
    themed("burns", "🔥", "Marx teilt aus", "Marx Roasts",
           "10 Tage Polemik: Ökonomen, Fabrikanten und Pfaffen kriegen ihr Fett weg. Marx in Bestform.",
           "10 days of polemics: economists, factory lords and parsons get roasted. Marx at his sharpest.",
           lambda v: v.get("pol"), 10)

    fet = [v for v in T if in_secs(v, "1", ["1.4"])]
    P.append(dict(id="fetisch", emoji="🛍️", tde="Fetisch-Detox (Black Friday)", ten="Fetish Detox (Black Friday)",
                  dde="Der Fetischcharakter der Ware, komplett, in 7 Portionen. Danach siehst du jeden Warenkorb mit anderen Augen.",
                  den="The fetishism of commodities, complete, in 7 portions. Afterwards you'll see every shopping cart differently.",
                  days=[[v["id"] for v in fet[i * len(fet) // 7:(i + 1) * len(fet) // 7]] for i in range(7)]))
    wf = [v for v in T if in_secs(v, "1", ["1.3"])]
    P.append(dict(id="wertform", emoji="🧠", tde="Wertform – für Masochisten", ten="The Value-Form – for Masochists",
                  dde="20 Ellen Leinwand = 1 Rock. Kapitel 1.3 komplett. Marx selbst: »Aller Anfang ist schwer.« Wir haben dich gewarnt.",
                  den="20 yards of linen = 1 coat. Section 1.3 in full. Marx himself: “Every beginning is difficult.” You've been warned.",
                  days=[[v["id"] for v in wf[i * len(wf) // 7:(i + 1) * len(wf) // 7]] for i in range(7)]))

    # ── das ganze Kapital i 1 Jahr, Althusser-Reihenfolge: Vorworte, Abschnitt 2-7, dann Abschnitt 1 ──
    order = [v for v in T if v["kap"].startswith("V")] + [v for v in T if v["kap"].isdigit() and int(v["kap"]) >= 4] \
        + [v for v in T if v["kap"] in ("1", "2", "3")]
    budget = sum(len(v["de"]) for v in order) / 365
    days, cur, size = [], [], 0
    for v in order:
        cur.append(v["id"]); size += len(v["de"])
        if size >= budget: days.append(cur); cur, size = [], 0
    if cur: days.append(cur)
    P.append(dict(id="jahr", emoji="📅", tde="Das ganze Kapital in einem Jahr", ten="All of Capital in One Year",
                  dde=f"Alles, {len(days)} Tage à ~10 Minuten. Reihenfolge nach Althusser: erst Arbeitstag & Akkumulation, die Wertform zum Schluss.",
                  den=f"Everything, {len(days)} days of ~10 minutes. Althusser's order: working day & accumulation first, value-form at the end.",
                  days=days))
    return P

def extra_plans(ids):
    """data/plans_extra.json = hand-curated plans (🤖 Ludditen & KI): verses by id (Kapital txt untouched) + per day a NEW
    bridge txt xd/xe ('💡 Und heute?') · missing id ➡ fail loud (a split.py change must not silently empty a day)"""
    p = DATA / "plans_extra.json"
    out = []
    for e in (json.loads(p.read_text(encoding="utf-8")) if p.exists() else []):
        bad = [i for d in e["days"] for i in d["v"] if i not in ids]
        if bad:
            raise SystemExit(f"plans_extra {e['id']}: unknown verse ids {bad}")
        out.append({k: e[k] for k in ("id", "emoji", "tde", "ten", "dde", "den")}
                   | {"days": [d["v"] for d in e["days"]], "x": [{k: d[k] for k in ("xd", "xe") if d.get(k)} for d in e["days"]]})
    return out

def main():
    V = jl("verses.jsonl")
    old = {json.loads(l)["id"]: json.loads(l)["de"] for l in open(DATA / "verses_scored_v0.jsonl", encoding="utf-8")} \
        if (DATA / "verses_scored_v0.jsonl").exists() else {}
    by_de = {}
    for s in jl("scores.jsonl"):
        de = old.get(s["id"])
        if de: by_de[de] = s
    # rank.py: relative picks per section (absolute juice/polemic were inflated)
    stars = {i for r in jl("rank.jsonl") for i in r["star"]}
    roasts = {i for r in jl("rank.jsonl") for i in r["roast"]}
    out, n_scored = [], 0
    ste = en_sec_titles(V)
    for i, v in enumerate(V):
        s = by_de.get(v["de"], {})
        n_scored += bool(s)
        q = verbatim(s.get("quote_de", ""), v["de"])
        qe = verbatim(s.get("quote_en", ""), v["en"].replace(" ¶ ", " ")) if q else ""
        o = {"id": v["id"], "kap": v["kap"], "sec": v["sec"], "st": v["sec_title"], "ste": ste.get((v["kap"], v["sec"]), ""), "p": v["page"],
             "de": v["de"], "en": v["en"], "kind": v["kind"], "_i": i}
        if s:
            o |= {"qd": q, "qe": qe, "j": s["juice"], "d": s["difficulty"],
                  "s": s["standalone"], "t": s["tags"], "pol": v["id"] in roasts, "bib": s["bible"],
                  "star": v["id"] in stars and bool(q)}
        out.append(o)

    P = plans(out)
    P[1:1] = extra_plans({o["id"] for o in out if o["kind"] == "text"})   # curated (🤖 Ludditen) right aft th flagship
    andachten = {(a["plan"], a["day"]): a for a in jl("andachten.jsonl")}
    for p in P:
        p["days"] = [{"v": ids} | ({"ad": andachten[(p["id"], i)]["de"], "ae": andachten[(p["id"], i)]["en"]}
                                   if (p["id"], i) in andachten and andachten[(p["id"], i)]["v"] == ids else {})
                     | (p["x"][i] if p.get("x") else {})
                     for i, ids in enumerate(p["days"]) if ids]
        p.pop("x", None)
    (DATA / "plans.json").write_text(json.dumps(P, ensure_ascii=False, indent=1), encoding="utf-8")

    first_page = {}
    for v in out:
        first_page.setdefault(v["kap"], v["p"])
    chapters = [{"kap": k, "de": de, "en": en, "ab": ab, "p": first_page.get(k)} for k, (de, en, ab) in CHAPTERS.items()]

    OUT.mkdir(parents=True, exist_ok=True)
    verses = [{k: x for k, x in o.items() if k not in ("_i", "kind") and x not in ("", None, [], False)}
              for o in out if o["kind"] == "text"]
    dump = lambda name, obj: (OUT / name).write_text(json.dumps(obj, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    dump("verses.json", verses)
    dump("notes.json", [{k: x for k, x in n.items() if x} for n in jl("notes.jsonl")])
    dump("chapters.json", chapters)
    dump("plans.json", P)
    sizes = " · ".join(f"{f.name} {f.stat().st_size // 1024}k" for f in sorted(OUT.glob("*.json")))
    print(f"{len(verses)} verses ({n_scored} scored, ⭐{sum(1 for v in verses if v.get('star'))} 🔥{sum(1 for v in verses if v.get('pol'))}) · {len(P)} plans ({sum(len(p['days']) for p in P)} days, "
          f"{sum(1 for p in P for d in p['days'] if 'ad' in d)} w Andacht) · {sizes}")

if __name__ == "__main__":
    main()

#!/usr/bin/env python3
"""align — DE Absätze (data/de.jsonl) ⬄ EN paragraphs (Moore/Aveling, data/raw/en) ➡ data/verses.jsonl + data/notes.jsonl

per DE Kap: DP alignment (Gale-Church style) over paragraph sequences
  cost = length-ratio mismatch + bead penalty − anchor bonus (shared numbers + footnote refs)
  beads: 1-1 · 1-2 · 2-1 · 2-2 · 1-3 · 3-1 · 1-0 · 0-1  ➡ 1 DE Absatz gets 0..n EN paragraphs
footnotes: DE fn ⬄ EN fn derived fr aligned verses (zip o their fn refs i order)
k-1 beads (marxists.org merged paragraphs) ➡ EN sentences split over th DE Absätze by length (no "no English paragraph" gaps)
EN headings (h3-h6 + subhead spans) tracked per paragraph ➡ verse["en_head"] ➡ build.py derives EN section titles
chapter TOC (<p class="toc"> + index lines) dropped b4 aligning (was ch.1 page 1 body txt)

usage: align.py [--show KAP]   # --show prints th alignment o 1 Kap 4 eyeballing
"""
import html, json, math, pathlib, re, sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
EN = ROOT / "data" / "raw" / "en"
sys.path.insert(0, str(ROOT / "bin"))
from fetch import DE2EN

EN_PREFACES = {"V1": ["p1"], "V2": ["p3"], "V3": ["p2", "p4"], "V4": ["p5"], "V5": ["p6"], "V6": ["p7"]}
BEADS = {(1, 1): 0.0, (1, 2): 0.6, (2, 1): 0.6, (2, 2): 1.2, (1, 0): 2.5, (0, 1): 2.5} \
        | {(k, 1): 0.5 + 0.4 * k for k in range(3, 7)} | {(1, k): 0.5 + 0.4 * k for k in range(3, 7)}
# ⬆ k-1 beads: marxists.org merges paragraphs i places (ch33: 12 EN ⬄ 34 DE)

def clean(frag):
    frag = re.sub(r'<sup class="enote">\s*<a [^>]*>\s*\[?(\d+[a-z]?)\]?\s*</a>\s*</sup>', r" [\1] ", frag, flags=re.S | re.I)
    frag = re.sub(r"<br\s*/?>", " ", frag, flags=re.I)
    frag = re.sub(r"<[^>]+>", "", frag)
    return re.sub(r"\s+", " ", html.unescape(frag)).replace("\xa0", " ").strip()

def parse_en(stem):
    """➡ (paragraphs [{en, kind}], footnotes {n: txt})"""
    src = (EN / f"{stem}.htm").read_text(encoding="utf-8")
    body, _, notes_src = re.split(r"(<h4>\s*Footnotes\s*</h4>)", src, maxsplit=1, flags=re.I) + ["", ""][:0] \
        if re.search(r"<h4>\s*Footnotes\s*</h4>", src, re.I) else (src, "", "")
    if m := re.search(r"<h3>[^<]*(Chapter|SECTION)[^<]*</h3>|<h[13][^>]*>.*?(Preface|Afterword)", body, re.I | re.S):
        body = body[m.start():]
    body = re.split(r'<p class="footer"|<hr class="end"\s*/?>\s*(?:<p class="footer"|$)', body, flags=re.I)[0]
    body = drop_toc(body)
    paras, heads = [], Heads()
    for m in re.finditer(r"<h([1-6])[^>]*>(.*?)</h\1>|<table\b.*?</table>|<p\b[^>]*>.*?(?=<p\b|<table\b|<hr\b|<h[1-6]\b|</body>|$)",
                         body, re.S | re.I):
        if m.group(1):
            heads.push(int(m.group(1)), clean(m.group(2)))
            continue
        frag = m.group(0)
        if re.match(r'<p class="(footer|information|title|toc)', frag, re.I):
            continue
        if sub := re.search(r'<span class="subhead">(.*?)</span>', frag, re.S | re.I):
            heads.push(7, clean(sub.group(1)))
        t = clean(re.sub(r"</t[dh]>", " | ", frag, flags=re.I)) if frag.lower().startswith("<table") else clean(frag)
        t = re.sub(r"(\s*\|\s*)+", " | ", t).strip(" |")
        if len(t) < 3 or re.search(r"Section 1 - .*Section 2 - ", t):  # skip in-page section index
            continue
        paras.append({"en": t, "kind": "table" if frag.lower().startswith("<table") else "text", "head": heads.cur()})
    notes = {}
    for m in re.finditer(r'<p class="information">(.*?)(?=<p class="information">|<hr|</body>|$)', notes_src, re.S | re.I):
        t = clean(m.group(1))
        if fm := re.match(r"\[?(\d+[a-z]?)\]?[.)]?\s+", t):
            notes[fm.group(1)] = t[fm.end():]
    return paras, notes

def drop_toc(body):
    """chapter TOC = <p class="toc"> + index/indentb lines up 2 th 1st heading aft it ➡ gone (was ch.1 page 1 body txt)"""
    # 🐛 @1113 v1 cut "toc ➡ nxt heading" = also ate ch17's intro (3 Absätze before SECTION 1) ➡ only th contiguous TOC lines
    m = re.search(r'<p class="toc"', body, re.I)
    if not m:
        return body
    line = re.compile(r'\s*<p class="(?:toc|index\w*|indentb)"[^>]*>.*?(?=<p\b|<h[1-6]\b|<hr\b|$)', re.S | re.I)
    j = m.start()
    while mm := line.match(body, j):
        j = mm.end()
    return body[:m.start()] + body[j:]

SMALL = {"a", "an", "and", "as", "at", "by", "for", "from", "in", "into", "of", "on", "or", "the", "to", "with", "upon"}

def title_case(t):
    """'THE TWO FACTORS OF A COMMODITY' ➡ 'The Two Factors of a Commodity' · mixed case left alone"""
    if t == t.upper():
        t = " ".join(w if i and w in SMALL else "-".join(p[:1].upper() + p[1:] for p in w.split("-"))
                     for i, w in enumerate(t.lower().split(" ")))
    return re.sub(r"([.?!] )([a-z])", lambda m: m.group(1) + m.group(2).upper(), t)   # '… Work. the Relay' ➡ 'The'

class Heads:
    """EN heading stack per page, outer ➡ inner: 2 'Chapter Four: X' ➡ 'X' (DE Kap 4 + 24 = several EN chapters)
    · 3 'SECTION 3' + h4 title ➡ '3. The Form of Value' · 5 'A. …' · 6 '1. …' · 7 subhead span '(a.) …' ➡ 'a. …'
    cur() = th stack above a paragraph · build.py picks th entry whose label matches th DE sec ('a)' ⬄ 'a.' · 'IV.' ⬄ '4.')"""
    def __init__(self): self.stack, self.sec_no = {}, None
    def push(self, lvl, txt):
        txt = re.sub(r"\s*\[\d+[a-z]?\]\s*", " ", re.sub(r"\s+", " ", txt)).strip(" :.")
        if not txt or re.match(r"(Part [IVX]+|Footnotes)\b", txt, re.I):
            return
        if ch := re.match(r"Chapter [\w-]+\s*[:.]\s*(.+)", txt, re.I):
            self.stack, self.sec_no = {2: title_case(ch.group(1))}, None
            return
        if lvl <= 3 and (sec := re.match(r"SECTION\s+([IVX\d]+)$", txt, re.I)):
            self.sec_no = sec.group(1)
            self.stack = {k: v for k, v in self.stack.items() if k < 3} | {3: f"{self.sec_no}."}   # title follows i h4
            return
        if lvl == 4 and self.sec_no and self.stack.get(3, "").endswith("."):
            self.stack[3] = f"{self.sec_no}. {title_case(txt)}"
            return
        txt = title_case(re.sub(r"^\((\w+)\.?\)\s*", r"\1. ", txt))   # '(a.) The nature' ➡ 'a. The nature'
        lvl = max(lvl, 4)
        self.stack = {k: v for k, v in self.stack.items() if k < lvl} | {lvl: txt}
    def cur(self):
        return [self.stack[k] for k in sorted(self.stack)]

SENT = r"(?<=[.!?:;])\s+(?=[A-Z“\"‘(\[0-9])"

def split_en(en_txt, de_lens):
    """1 EN chunk ⬄ k DE Absätze ➡ k EN pieces, cut at th sentence boundary closest 2 each DE cumulative length share
    (monotonic · evy piece ≥1 sentence while there r enough) · was: all EN on th 1st Absatz, rest "no English paragraph here\""""
    k = len(de_lens)
    sents = [x.strip() for x in re.split(r"\s*¶\s*|" + SENT, en_txt) if x and x.strip()]
    if k == 1 or len(sents) < 2:
        return [en_txt] + [""] * (k - 1)
    total_de, total_en = sum(de_lens) or 1, sum(len(x) for x in sents) or 1
    cum, acc = [], 0
    for x in sents:
        acc += len(x); cum.append(acc / total_en)
    cuts, prev, share = [], 0, 0.0
    for i in range(k - 1):
        share += de_lens[i] / total_de
        lo, hi = prev + 1, len(sents) - (k - 1 - i)   # leave ≥1 sentence 4 evy later piece
        prev = prev if lo > hi else min(range(lo, hi + 1), key=lambda c: abs(cum[c - 1] - share))
        cuts.append(prev)
    bounds = [0] + cuts + [len(sents)]
    return [" ".join(sents[a:b]) for a, b in zip(bounds, bounds[1:])]

def anchors(t):
    return set(re.findall(r"\b\d[\d,.]*\d\b|\b\d{2,}\b", re.sub(r"\[\d+[a-z]?\]", "", t))) \
        | {"fn" + x for x in re.findall(r"\[(\d+[a-z]?)\]", t)}

NAME_W = 1.0
FN_PEN = 0.4
NOT_NAMES = {"English", "French", "German", "The", "This", "That", "But", "And", "For", "Mr", "Messrs", "Sir", "Lord", "Act", "Acts",
             "Factory", "Inspector", "Inspectors", "Report", "Reports", "Commission", "Parliament", "House", "Commons", "Political", "Economy"}

def names_en(t):
    """mid-sentence capitalised EN words (≥ 4 letters) = proper names, mostly spelled th same i th DE (Fourier · Leyden · Ure)
    · @1113: k-1 ⬄ 1-k slips (13,120-125: Moore/Aveling inline Marx' fn quotes) needed a content anchor beyond numbers"""
    t = re.sub(r"\[\d+[a-z]?\]", "", t)
    return {w for w in re.findall(r"(?<=[a-z,;] )([A-Z][a-zäöüé]{3,})\b", t) if w not in NOT_NAMES}

def words_de(t):
    """DE tokens + genitive-s stripped ('Londons' ➡ 'London')"""
    ws = set(re.findall(r"\w+", t))
    return ws | {w[:-1] for w in ws if w.endswith("s")}

def align(de, en, de_fn=None):
    """DP ➡ list o (de_idx[], en_idx[]) beads
    de_fn[i] = txt o th footnotes DE Absatz i cites · Moore/Aveling sometimes inline a long fn as paragraphs (13,125 ⬄
    fn 194: Danzig · Leyden · Boxhorn) ➡ length may match txt OR txt + fns (+ FN_PEN) · fn names count as DE words"""
    F = [len(x) for x in de_fn] if de_fn else [0] * len(de)
    LD = sum(len(x) for x in de) or 1
    LE = sum(len(x) for x in en) or 1
    r = LE / LD
    A_de, A_en = [anchors(x) for x in de], [anchors(x) for x in en]
    N_en = [names_en(x) for x in en]
    W_de = [words_de(x + " " + (de_fn[i] if de_fn else "")) for i, x in enumerate(de)]
    n, m = len(de), len(en)
    INF = float("inf")
    D = [[INF] * (m + 1) for _ in range(n + 1)]
    B = [[None] * (m + 1) for _ in range(n + 1)]
    D[0][0] = 0.0
    for i in range(n + 1):
        for j in range(m + 1):
            if D[i][j] == INF:
                continue
            for (di, dj), pen in BEADS.items():
                a, b = i + di, j + dj
                if a > n or b > m:
                    continue
                ld = sum(len(x) for x in de[i:a])
                le = sum(len(x) for x in en[j:b])
                c = pen + 2.0 * abs(math.log((le + 50) / (r * ld + 50)))
                lf = sum(F[i:a])
                if lf and dj > 1:   # EN inlined th cited fn(s) as extra paragraphs
                    c = min(c, pen + FN_PEN + 2.0 * abs(math.log((le + 50) / (r * (ld + lf) + 50))))
                if di and dj:
                    ad = set().union(*A_de[i:a]); ae = set().union(*A_en[j:b])
                    if ad or ae:
                        c -= 1.5 * len(ad & ae) / max(len(ad | ae), 1)
                    ne = set().union(*N_en[j:b])
                    if ne:   # names (Fourier · Leyden · Sidmouth) the EN chunk shares w th DE chunk
                        c -= NAME_W * len(ne & set().union(*W_de[i:a])) / len(ne)
                if D[i][j] + c < D[a][b]:
                    D[a][b], B[a][b] = D[i][j] + c, (i, j)
    beads, (i, j) = [], (n, m)
    while (i, j) != (0, 0):
        pi, pj = B[i][j]
        beads.append((list(range(pi, i)), list(range(pj, j))))
        i, j = pi, pj
    return beads[::-1]

def main():
    show = sys.argv[2] if len(sys.argv) > 2 and sys.argv[1] == "--show" else None
    verses = [json.loads(l) for l in open(ROOT / "data" / "de.jsonl", encoding="utf-8")]
    notes_de = [json.loads(l) for l in open(ROOT / "data" / "notes_de.jsonl", encoding="utf-8")]
    kaps = list(dict.fromkeys(v["kap"] for v in verses))
    fnmap, en_notes_all, stats = {}, {}, []
    for k in kaps:
        stems = EN_PREFACES.get(k) or [f"ch{c:02d}" for c in DE2EN[int(k)]]
        en_paras, en_notes = [], {}
        for s in stems:
            p, nt = parse_en(s)
            en_paras += [dict(x, ch=s) for x in p]
            en_notes |= {(s, n): t for n, t in nt.items()}
        en_notes_all |= en_notes
        vs = [v for v in verses if v["kap"] == k]
        fn_txt = {x["n"]: x["de"] for x in notes_de if x["kap"] == k}
        beads = align([v["de"] for v in vs], [p["en"] for p in en_paras], [" ".join(fn_txt.get(f, "") for f in v["fn"]) for v in vs])
        for di, ej in beads:
            en_txt = " ¶ ".join(en_paras[j]["en"] for j in ej)
            en_ch = en_paras[ej[0]]["ch"] if ej else None
            text_di = [i for i in di if vs[i]["kind"] != "meta"] or di   # headings don't eat EN sentences
            pieces = dict(zip(text_di, split_en(en_txt, [len(vs[i]["de"]) for i in text_di]))) if en_txt else {}
            for i in di:
                vs[i]["en"] = pieces.get(i, "")  # k-1 bead ➡ EN split by sentences (was: all on th 1st DE Absatz)
                vs[i]["en_ch"] = en_ch
                vs[i]["en_head"] = en_paras[ej[0]]["head"] if ej else []
                vs[i]["bead"] = f"{len(di)}-{len(ej)}"
            de_fn = [f for i in di for f in vs[i]["fn"]]
            en_fn = re.findall(r"\[(\d+[a-z]?)\]", en_txt)
            for a, b in zip(de_fn, en_fn):
                fnmap[(k, a)] = (en_ch, b)
        ones = sum(1 for d, e in beads if len(d) == 1 and len(e) == 1)
        stats.append(f"{k}: {len(vs)}⬄{len(en_paras)} 1-1 {ones}/{len(beads)}")
        if show == k:
            for di, ej in beads:
                print(f"--- {[vs[i]['id'] for i in di]} ⬄ {ej}")
                for i in di: print("  DE", vs[i]["de"][:110])
                for j in ej: print("  EN", en_paras[j]["en"][:110])
    for v in verses:
        v.setdefault("en", ""); v.setdefault("en_ch", None); v.setdefault("bead", "1-0"); v.setdefault("en_head", [])
        v["en_fn"] = re.findall(r"\[(\d+[a-z]?)\]", v["en"])
    for x in notes_de:
        ch_n = fnmap.get((x["kap"], x["n"]))
        x["en"] = en_notes_all.get(ch_n, "") if ch_n else ""
    out = ROOT / "data"
    (out / "verses.jsonl").write_text("".join(json.dumps(v, ensure_ascii=False) + "\n" for v in verses), encoding="utf-8")
    (out / "notes.jsonl").write_text("".join(json.dumps(x, ensure_ascii=False) + "\n" for x in notes_de), encoding="utf-8")
    if not show:
        print("\n".join(stats))
    print(f"verses w EN: {sum(1 for v in verses if v['en'])}/{len(verses)} · notes w EN: {sum(1 for x in notes_de if x['en'])}/{len(notes_de)}")

if __name__ == "__main__":
    main()

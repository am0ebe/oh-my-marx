#!/usr/bin/env python3
"""align — DE Absätze (data/de.jsonl) ⬄ EN paragraphs (Moore/Aveling, data/raw/en) ➡ data/verses.jsonl + data/notes.jsonl

per DE Kap: DP alignment (Gale-Church style) over paragraph sequences
  cost = length-ratio mismatch + bead penalty − anchor bonus (shared numbers + footnote refs)
  beads: 1-1 · 1-2 · 2-1 · 2-2 · 1-3 · 3-1 · 1-0 · 0-1  ➡ 1 DE Absatz gets 0..n EN paragraphs
footnotes: DE fn ⬄ EN fn derived fr aligned verses (zip o their fn refs i order)

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
    body = re.sub(r"<h[1-6][^>]*>.*?</h[1-6]>", "", body, flags=re.S | re.I)
    paras = []
    for m in re.finditer(r"<table\b.*?</table>|<p\b[^>]*>.*?(?=<p\b|<table\b|<hr\b|</body>|$)", body, re.S | re.I):
        frag = m.group(0)
        if re.match(r'<p class="(footer|information|title|toc)', frag, re.I):
            continue
        t = clean(re.sub(r"</t[dh]>", " | ", frag, flags=re.I)) if frag.lower().startswith("<table") else clean(frag)
        t = re.sub(r"(\s*\|\s*)+", " | ", t).strip(" |")
        if len(t) < 3 or re.search(r"Section 1 - .*Section 2 - ", t):  # skip in-page section index
            continue
        paras.append({"en": t, "kind": "table" if frag.lower().startswith("<table") else "text"})
    notes = {}
    for m in re.finditer(r'<p class="information">(.*?)(?=<p class="information">|<hr|</body>|$)', notes_src, re.S | re.I):
        t = clean(m.group(1))
        if fm := re.match(r"\[?(\d+[a-z]?)\]?[.)]?\s+", t):
            notes[fm.group(1)] = t[fm.end():]
    return paras, notes

def anchors(t):
    return set(re.findall(r"\b\d[\d,.]*\d\b|\b\d{2,}\b", re.sub(r"\[\d+[a-z]?\]", "", t))) \
        | {"fn" + x for x in re.findall(r"\[(\d+[a-z]?)\]", t)}

def align(de, en):
    """DP ➡ list o (de_idx[], en_idx[]) beads"""
    LD = sum(len(x) for x in de) or 1
    LE = sum(len(x) for x in en) or 1
    r = LE / LD
    A_de, A_en = [anchors(x) for x in de], [anchors(x) for x in en]
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
                if di and dj:
                    ad = set().union(*A_de[i:a]); ae = set().union(*A_en[j:b])
                    if ad or ae:
                        c -= 1.5 * len(ad & ae) / max(len(ad | ae), 1)
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
        beads = align([v["de"] for v in vs], [p["en"] for p in en_paras])
        for di, ej in beads:
            en_txt = " ¶ ".join(en_paras[j]["en"] for j in ej)
            en_ch = en_paras[ej[0]]["ch"] if ej else None
            for x, i in enumerate(di):
                vs[i]["en"] = en_txt if x == 0 else ""  # DE 2-1 bead ➡ EN on th 1st DE Absatz
                vs[i]["en_ch"] = en_ch
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
        v.setdefault("en", ""); v.setdefault("en_ch", None); v.setdefault("bead", "1-0")
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

#!/usr/bin/env python3
"""split — data/raw/de/*.htm (MEW 23) ➡ data/de.jsonl (1 line = 1 Absatz = 1 "Vers") + data/notes_de.jsonl (Fußnoten).

verse id = bible-style "Kap,Absatz" · prefaces = V1..V6 (V1 = Vorwort 1. Aufl … V6 = Zur 4. Aufl)
fields: id kap n sec sec_title page kind de fn[]   (fn = footnote ids cited, per-Kap like MEW, eg "158a")
kind: text · table (stat tables, cells ' | ') · meta (headings, signatures, formula lines)
lead-ins ending w ':' ("Mill sagt:") merged into th following Absatz
footnote refs i txt ➡ [52] · page markers <245> dropped (page = MEW page at Absatz start)

usage: split.py   (re-runs fully, overwrites outputs)

🔁 ids moved vs th previous data/de.jsonl (same DE txt, new id) ➡ data/id_moves.json (cumulative old ➡ new, chains
composed) + th id-keyed data files remapped once, here (rank · andachten · i18n q · i18n_full v) · th app migrates
th usr's marks/progress fr th exported copy (assets id_moves.json, data/IdMoves.kt) · @1113: "FÜFNZEHNTES" typo
put Kap 15's opening i Kap 14 + Kap 15 ids continued at 15,40
"""
import html, json, pathlib, re

ROOT = pathlib.Path(__file__).resolve().parent.parent
RAW = ROOT / "data" / "raw" / "de"

PREFACES = {"011": "V1", "018": "V2", "031": "V3", "033": "V4", "036": "V5", "041": "V6"}
ORDINALS = ["ERSTES", "ZWEITES", "DRITTES", "VIERTES", "FÜNFTES", "SECHSTES", "SIEBENTES", "ACHTES",
            "NEUNTES", "ZEHNTES", "ELFTES", "ZWÖLFTES", "DREIZEHNTES", "VIERZEHNTES", "FÜNFZEHNTES",
            "SECHZEHNTES", "SIEBZEHNTES", "ACHTZEHNTES", "NEUNZEHNTES", "ZWANZIGSTES", "EINUNDZWANZIGSTES",
            "ZWEIUNDZWANZIGSTES", "DREIUNDZWANZIGSTES", "VIERUNDZWANZIGSTES", "FÜNFUNDZWANZIGSTES"]
TYPOS = {"FÜFNZEHNTES": "FÜNFZEHNTES"}   # mlwerke source typos i chapter heads (me23 Kap 15)
KAP_HEAD = re.compile(r"\b(" + "|".join(ORDINALS + list(TYPOS)) + r")\s+KAPITEL\b")
SEC_ANCHOR = re.compile(r'<A NAME="Kap_(\d+)((?:_[A-Za-z0-9]+)*)">(.*?)(?:</A>|$)', re.S | re.I)  # </A> sometimes missing
PAGE = re.compile(r"<(\d{1,3})>")

def clean(frag):
    frag = re.sub(r'<A [^>]*HREF="#[MZ](\d+[a-z]?)"[^>]*>(?:(?!&lt;=).)*?</A>', r" [\1] ", frag, flags=re.S | re.I)  # fn refs
    frag = re.sub(r"<BR\s*/?>", " ", frag, flags=re.I)
    frag = re.sub(r"<[^>]+>", "", frag)
    txt = html.unescape(frag)
    return re.sub(r"\s+", " ", txt).strip()

def paragraphs(src):
    """yield raw <P>…</P> chunks (mlwerke closes Ps sloppily ➡ split on opening tag)."""
    body = src.split("</SMALL>", 1)[-1]  # drop nav + "Seitenzahlen verweisen auf" header
    # statistic tables (Kap 13, 23) ➡ 1 Absatz each, cells joined, flagged ⟦T⟧
    body = re.sub(r"<TABLE\b.*?</TABLE>",
                  lambda m: "<P>⟦T⟧ " + re.sub(r"<P\b[^>]*>|</?T[DR]\b[^>]*>", " | ", m.group(0), flags=re.I) + "</P>",
                  body, flags=re.S | re.I)
    for chunk in re.split(r"<P\b[^>]*>", body, flags=re.I)[1:]:
        yield chunk

def main():
    verses, notes = [], []
    kap, n, sec, sec_title, page = None, 0, "", "", None
    for f in sorted(RAW.glob("me23_*.htm")):
        num = f.stem.split("_")[1]
        if num == "000":
            continue
        src = f.read_text(encoding="utf-8")
        if num in PREFACES:
            kap, n, sec, sec_title = PREFACES[num], 0, "", ""
        in_notes = False  # footnotes sit at th end o each file
        for chunk in paragraphs(src):
            raw_txt = html.unescape(re.sub(r"<[^>]+>", "", chunk))
            stripped = raw_txt.strip()
            # fn anchor = NAME="M8">(8) · ⚠️ some files use NAME="M341" as *page* anchor ➡ needs th "(n)"
            # back-link = HREF="#Z8">&lt;= · ⚠️ 441 has a typo'd text ref HREF="#Z203">(203) ➡ match th "&lt;="
            if stripped == "Fußnoten" or re.search(r'NAME="M(\d+[a-z]?)">\s*\(\1\)|HREF="#Z\w+">&lt;=', chunk, re.I):
                in_notes = True
            if in_notes:
                if KAP_HEAD.search(raw_txt) or SEC_ANCHOR.search(chunk):
                    in_notes = False  # 2 chapters i 1 file (me23_589) ➡ back 2 text
                else:
                    t = clean(chunk).removesuffix("<=").strip()
                    if not t or t == "Fußnoten" or re.fullmatch(r"_+", t):
                        continue
                    if fm := re.match(r"\((\d+[a-z]?)\)\s*", t):  # new footnote
                        notes.append({"kap": kap, "n": fm.group(1), "de": t[fm.end():]})
                    elif notes and notes[-1]["kap"] == kap:  # multi-Absatz footnote
                        notes[-1]["de"] += "\n" + t
                    continue
            if m := KAP_HEAD.search(raw_txt):
                new = str(ORDINALS.index(TYPOS.get(m.group(1), m.group(1))) + 1)
                if new != kap:  # "DREIZEHNTES KAPITEL. TEIL II" = continuation file, keep counting
                    kap, n, sec, sec_title = new, 0, "", ""
                continue
            if m := SEC_ANCHOR.search(chunk):
                if kap is not None and m.group(1) != kap and not str(kap).startswith("V"):
                    n = 0   # guard: a missed chapter head must not carry th Absatz count into th nxt Kap (was 15,40…)
                kap = m.group(1)
                sec = kap + m.group(2).replace("_", ".")
                sec_title = clean(m.group(3))
                continue
            pages = PAGE.findall(html.unescape(chunk))
            txt = PAGE.sub("", clean(chunk))
            txt = re.sub(r"\s+", " ", txt).strip().replace("<", "⟨").replace(">", "⟩")  # editor's ⟨Übersetzung⟩
            if pages and re.match(r'\s*(<[^>]+>\s*)*<A NAME="[SM]\d+">\s*(<[^>]+>\s*)*&lt;\d', chunk, re.I):
                page = int(pages[0])  # marker at Absatz start ➡ Absatz begins on that page
            if not txt or kap is None or len(txt) < 3 or "Seitenzahlen verweisen" in txt:
                if pages: page = int(pages[-1])
                continue
            if txt in ("Inhalt", "|") or re.fullmatch(r"[\d. ]*Kapitel.*\| Inhalt \|.*", txt):
                continue
            kind = "table" if txt.startswith("⟦T⟧") else "text"
            if txt == "Fußnoten" or re.fullmatch(r"_+", txt):
                continue
            if kind == "text" and len(txt) < 60 and not re.search(r"[.!?;:\"“”)\]]$", txt):
                kind = "meta"  # headings · signatures · dates · formula lines
            txt = re.sub(r"(\s*\|\s*)+", " | ", txt.removeprefix("⟦T⟧").strip()).strip(" |")
            prev = verses[-1] if verses else None
            if prev and prev["kap"] == kap and prev["kind"] == "text" and kind == "text" \
                    and prev["de"].endswith(":") and len(prev["de"]) < 120:
                prev["de"] += " " + txt  # "Mill sagt:" + th quote = 1 Vers
                prev["fn"] = re.findall(r"\[(\d+[a-z]?)\]", prev["de"])
            else:
                n += 1
                verses.append({"id": f"{kap},{n}", "kap": kap, "n": n, "sec": sec, "sec_title": sec_title,
                               "page": page, "kind": kind, "de": txt,
                               "fn": re.findall(r"\[(\d+[a-z]?)\]", txt)})
            if pages: page = int(pages[-1])  # nxt Absatz continues on th last page seen
    out = ROOT / "data"
    old = [json.loads(l) for l in open(out / "de.jsonl", encoding="utf-8")] if (out / "de.jsonl").exists() else []
    moves = id_moves(old, verses)
    if moves:
        save_moves(moves)
        remap_data(moves)
        print(f"🔁 {len(moves)} verse ids moved ➡ data/id_moves.json + id-keyed data remapped (eg {next(iter(moves.items()))})")
    (out / "de.jsonl").write_text("".join(json.dumps(v, ensure_ascii=False) + "\n" for v in verses), encoding="utf-8")
    (out / "notes_de.jsonl").write_text("".join(json.dumps(x, ensure_ascii=False) + "\n" for x in notes), encoding="utf-8")
    per = {}
    for v in verses: per[v["kap"]] = per.get(v["kap"], 0) + 1
    print(f"{len(verses)} Absätze · {len(notes)} Fußnoten · per Kap: {per}")

MOVES = ROOT / "data" / "id_moves.json"

def id_moves(old, new):
    """same DE txt, dif id ➡ {old: new} · txt that's not unique i th new split ➡ skipped (can't tell which)"""
    by_txt = {}
    for v in new:
        by_txt.setdefault(v["de"], []).append(v["id"])
    moves = {}
    for v in old:
        c = by_txt.get(v["de"], [])
        if v["id"] not in c and len(c) == 1:
            moves[v["id"]] = c[0]
    return moves

def compose(prev, moves):
    """cumulative map: earlier a ➡ b, now b ➡ c ➡ a ➡ c · + th new moves · identity rows dropped"""
    out = {a: moves.get(b, b) for a, b in prev.items()} | moves
    return {a: b for a, b in out.items() if a != b}

def save_moves(moves):
    prev = json.loads(MOVES.read_text(encoding="utf-8")) if MOVES.exists() else {}
    MOVES.write_text(json.dumps(compose(prev, moves), ensure_ascii=False, indent=0), encoding="utf-8")

def remap_data(moves, data=ROOT / "data"):
    """rename verse ids i evy id-keyed pipeline file (1×, right when th split moved them) ➡ no LLM re-run needed"""
    m = lambda i: moves.get(i, i)
    def rewrite(p, f):
        if p.exists():
            rows = [f(json.loads(l)) for l in open(p, encoding="utf-8")]
            p.write_text("".join(json.dumps(r, ensure_ascii=False) + "\n" for r in rows), encoding="utf-8")
    rewrite(data / "rank.jsonl", lambda r: r | {"star": [m(i) for i in r["star"]], "roast": [m(i) for i in r["roast"]]})
    rewrite(data / "andachten.jsonl", lambda r: r | {"v": [m(i) for i in r["v"]]})
    for p in sorted((data / "i18n").glob("*.jsonl")):
        rewrite(p, lambda r: r | {"k": m(r["k"])} if r.get("kind") == "q" else r)
    for p in sorted((data / "i18n_full").glob("*.jsonl")):
        rewrite(p, lambda r: r | {"k": m(r["k"])} if r.get("kind") == "v" else r)

if __name__ == "__main__":
    main()

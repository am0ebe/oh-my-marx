#!/usr/bin/env python3
"""fetch — raw Kapital Bd.1 html ➡ data/raw/{de,en}/ (P1 step 1, idempotent: skips existing).

DE: MEW 23 (Dietz) fr archived mlwerke.de (wayback) — has MEW page markers <NNN> + footnote nos
EN: Moore/Aveling 1887 fr marxists.org — 33 ch, DE Kap 4 ➡ EN 4-6 · DE 24.1-7 ➡ EN 26-32 (see DE2EN)

usage: fetch.py            # both
       fetch.py de|en
"""
import pathlib, re, sys, time, urllib.request

ROOT = pathlib.Path(__file__).resolve().parent.parent / "data" / "raw"
DE_BASE = "https://web.archive.org/web/2016id_/http://www.mlwerke.de/me/me23/"
EN_BASE = "https://www.marxists.org/archive/marx/works/1867-c1/"

# DE Kapitel ➡ EN chapter(s) — Moore/Aveling split Kap 4 + 24 into several ch
DE2EN = {k: [k] for k in (1, 2, 3)} | {4: [4, 5, 6]} | {k: [k + 2] for k in range(5, 24)} \
        | {24: list(range(26, 33)), 25: [33]}

def get(url):
    req = urllib.request.Request(url, headers={"User-Agent": "marxversion-fetch/0.1 (personal study)"})
    for attempt in range(3):
        try:
            return urllib.request.urlopen(req, timeout=60).read()
        except Exception as e:
            print(f"  retry {attempt+1}: {url} ({e})", file=sys.stderr)
            time.sleep(5)
    raise SystemExit(f"failed: {url}")

def save(dst, url, enc):
    if dst.exists():
        return
    dst.parent.mkdir(parents=True, exist_ok=True)
    raw = get(url)
    try:
        txt = raw.decode(enc)
    except UnicodeDecodeError:  # some marxists.org pages r cp1252 ("Règlement")
        txt = raw.decode("cp1252", errors="replace")
    dst.write_text(txt, encoding="utf-8")
    print(f"{dst.relative_to(ROOT)} ⬅ {url}", file=sys.stderr)
    time.sleep(1)  # be nice 2 wayback / marxists.org

def fetch_de():
    idx = get(DE_BASE + "me23_000.htm").decode("latin-1")
    save(ROOT / "de" / "me23_000.htm", DE_BASE + "me23_000.htm", "latin-1")
    for f in sorted(set(re.findall(r"me23_\d{3}\.htm", idx)) - {"me23_000.htm"}):
        save(ROOT / "de" / f, DE_BASE + f, "latin-1")

def fetch_en():
    for p in ["part0"] + [f"p{i}" for i in range(1, 8)]:  # prefaces ➡ see EN_PREFACES i align.py
        save(ROOT / "en" / f"{p}.htm", EN_BASE + f"{p}.htm", "utf-8")
    for n in range(1, 34):
        save(ROOT / "en" / f"ch{n:02d}.htm", EN_BASE + f"ch{n:02d}.htm", "utf-8")

if __name__ == "__main__":
    which = sys.argv[1:] or ["de", "en"]
    if "de" in which: fetch_de()
    if "en" in which: fetch_en()

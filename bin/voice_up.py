#!/usr/bin/env python3
"""voice_up — ck audio's voice packs vs th app, then (-u) upload them 2 scagent.de/oh-my-marx/voice/ (D-085 · D-090).

y: th app fetches manifest.json + 1 zip per lang wo an update (D-085) ➡ a broken pack = broken 🔊 4 evy reader.
   This ck = th app's own rules b4 anything goes public: sha256 + size match th manifest · zip = index.json + <hex>.opus
   only · ids = q:<verse> / a:<plan>:<day> · + how many clips still match today's txt (stale = phone voice).
   Upload order = zips 1st, manifest last ➡ an app nvr sees a manifest th points at a missing zip.

  bin/voice_up.py [PACKS]        PACKS = audio's out dir w manifest.json (dflt ../audio/voice/out/packs) · ck only
examples:
  bin/voice_up.py                          # ck audio's packs: per lang persona · MB · items · fresh % (read-only)
  bin/voice_up.py -u                       # ck, then rsync 2 vps:/srv/www/oh-my-marx/voice/ (dir must exist)
  bin/voice_up.py -u -y                    # 1st upload: dir missing = newly public URL ➡ only aftr 🦠's ok
"""
import argparse
import hashlib
import json
import os
import pathlib
import re
import subprocess
import sys
import zipfile

ROOT = pathlib.Path(__file__).resolve().parents[1]
CONTENT = ROOT / "android/app/src/main/assets/content"
PACKS = ROOT.parent / "audio/voice/out/packs"
BASE = "https://scagent.de/oh-my-marx/voice/"     # = VoicePacks.BASE i th app
REMOTE = "/srv/www/oh-my-marx/voice"
LANGS = ["en", "de", "zh", "hi", "es", "fr", "ar", "bn", "pt", "ru", "ur", "id", "it"]   # Lang.menu
ZIP_NAME = re.compile(r"[A-Za-z0-9._-]{1,80}\.zip")    # = VoicePacks.ZIP_NAME
CLIP = re.compile(r"[0-9a-f]{8,40}\.opus")              # = VoicePacks.CLIP
ID = re.compile(r"q:V?\d+,\d+|a:[a-z0-9_-]+:\d+")        # verse ids "8,9" + "V1,2" (Vorworte) · plan ids lowercase
FRESH_MIN = 0.9                                         # < 90 % clips match today's txt ➡ warn (re-render th rest)


def sha12(s):
    return hashlib.sha1(s.encode()).hexdigest()[:12]


def texts(lang, content=CONTENT):
    """{id: raw txt} th app hashes 4 th stale ck (same strings as audio's omm_pack.items)"""
    if lang in ("de", "en"):
        qf, af = ("qd", "ad") if lang == "de" else ("qe", "ae")
        out = {f"q:{v['id']}": v[qf] for v in json.loads((content / "verses.json").read_text()) if v.get(qf)}
        for p in json.loads((content / "plans.json").read_text()):
            out |= {f"a:{p['id']}:{i}": d[af] for i, d in enumerate(p["days"]) if d.get(af)}
        return out
    tr = json.loads((content / f"i18n_{lang}.json").read_text())
    return {f"{k}:{i}": t for k in ("q", "a") for i, t in tr.get(k, {}).items() if t}


def check(packs, content=CONTENT):
    """➡ (errors, warnings, rows) · errors = th app wd refuse o break · rows = 1 line per pack"""
    err, warn, rows = [], [], []
    mf = packs / "manifest.json"
    if not mf.is_file():
        return [f"no {mf} (audio: omm_pack.py manifest)"], warn, rows
    m = json.loads(mf.read_text())
    if m.get("v") != 1:
        err.append(f"manifest v={m.get('v')} ≠ 1 (app ignores it)")
    if m.get("base", BASE).rstrip("/") != BASE.rstrip("/"):
        err.append(f"manifest base {m.get('base')} ≠ {BASE}")
    seen = {}
    for p in m.get("packs", []):
        lang, f = p.get("lang", ""), p.get("file", "")
        tag = f"{lang}/{p.get('persona', '?')}"
        if lang not in LANGS:
            err.append(f"{tag}: lang not i th app's menu"); continue
        if lang in seen:
            warn.append(f"{tag}: 2nd pack 4 {lang} ➡ app plays only th 1st listed ({seen[lang]}) · 2 voices = 1 zip per lang")
            continue
        seen[lang] = p.get("persona")
        z = packs / f
        if not ZIP_NAME.fullmatch(f) or not z.is_file():
            err.append(f"{tag}: zip {f!r} missing o bad name"); continue
        if z.stat().st_size != p.get("bytes"):
            err.append(f"{tag}: {z.stat().st_size} B ≠ manifest {p.get('bytes')} (dialog shows th wrong MB)")
        if hashlib.sha256(z.read_bytes()).hexdigest() != p.get("sha256"):
            err.append(f"{tag}: sha256 ≠ manifest (app deletes it aftr download)"); continue
        with zipfile.ZipFile(z) as zf:
            names = set(zf.namelist())
            odd = sorted(n for n in names if n != "index.json" and not CLIP.fullmatch(n))
            if odd:
                err.append(f"{tag}: zip hs {len(odd)} odd entries ({odd[0]}) ➡ app refuses th pack")
            if "index.json" not in names:
                err.append(f"{tag}: no index.json"); continue
            ix = json.loads(zf.read("index.json"))
        items = ix.get("items", {})
        bad_id = [i for i in items if not ID.fullmatch(i)]
        no_file = [i for i, v in items.items() if v.get("f") not in names]
        if bad_id:
            err.append(f"{tag}: {len(bad_id)} ids not q:/a: ({bad_id[0]})")
        if no_file:
            err.append(f"{tag}: {len(no_file)} items point at a clip not i th zip ({no_file[0]})")
        now = texts(lang, content)
        fresh = sum(1 for i, v in items.items() if i in now and sha12(now[i]) == v.get("sha"))
        share = fresh / len(items) if items else 0
        if share < FRESH_MIN:
            warn.append(f"{tag}: only {fresh}/{len(items)} clips match today's txt ➡ rest = phone voice (re-render)")
        rows.append(f"{lang:2} {p.get('persona', '?'):16} {p['bytes'] / 1e6:5.1f} MB  {len(items):5} items  "
                    f"{share:4.0%} fresh  {len(now) - fresh:5} txts = phone voice")
    missing = [l for l in LANGS if l not in seen]
    if missing:
        warn.append(f"no pack 4 {' '.join(missing)} ➡ phone voice there (fine, app hides th offer)")
    return err, warn, rows


def far(host, cmd):
    return subprocess.run(["ssh", "-o", "BatchMode=yes", "-o", "ConnectTimeout=10", host, cmd] if host else ["sh", "-c", cmd]).returncode


def upload(packs, host, remote, yes):
    """zips 1st, manifest last · dir missing = newly public ➡ needs yes · old zips stay (rm = data, ask)"""
    if far(host, f"test -d {remote}") != 0:
        if not yes:
            print(f"🛑 {remote} does not exist yet: 1st upload = newly public URL {BASE} ➡ 🦠's ok, then -y", file=sys.stderr)
            return 2
        if far(host, f"mkdir -p {remote}") != 0:
            print(f"🚧 can't create {remote} (parent owned by root? once: sudo install -d -o $USER {remote})", file=sys.stderr)
            return 1
    dst = f"{host}:{remote}/" if host else f"{remote}/"
    m = json.loads((packs / "manifest.json").read_text())
    zips = [str(packs / p["file"]) for p in m["packs"]]
    for files in (zips, [str(packs / "manifest.json")]):
        if subprocess.run(["rsync", "-t", "--chmod=F644", *files, dst]).returncode != 0:
            print("❌ rsync failed", file=sys.stderr); return 1
    print(f"✔️ {len(zips)} pack(s) + manifest ➡ {BASE} · DEV app sees it ≤ 10 min · stable ≤ 1 d aftr a 404, else ≤ 7 d (D-090)")
    return 0


def main(argv=None):
    ap = argparse.ArgumentParser(prog="voice_up", description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("packs", nargs="?", default=str(PACKS), help="audio's packs dir w manifest.json")
    ap.add_argument("-u", "--upload", action="store_true", help="ck, then rsync 2 th web dir (zips 1st, manifest last)")
    ap.add_argument("-y", "--yes-public", action="store_true", help="1st upload: create th dir = newly public URL (🦠's ok)")
    ap.add_argument("-V", "--version", action="version", version="voice_up 1")
    a = ap.parse_args(argv)
    packs = pathlib.Path(a.packs)
    err, warn, rows = check(packs)
    for r in rows:
        print(r)
    for w in warn:
        print(f"⚠️ {w}")
    for e in err:
        print(f"❌ {e}")
    if err:
        return 1
    print(f"✔️ {len(rows)} pack(s) ok 4 th app")
    if not a.upload:
        return 0
    return upload(packs, os.environ.get("PUBLISH_HOST", "vps"), os.environ.get("VOICE_DIR", REMOTE), a.yes_public)


if __name__ == "__main__":
    sys.exit(main())
